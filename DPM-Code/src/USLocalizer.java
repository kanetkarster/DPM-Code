import lejos.nxt.LCD;
import lejos.nxt.Motor;
import lejos.nxt.NXTRegulatedMotor;
import lejos.nxt.Sound;
import lejos.nxt.UltrasonicSensor;
import lejos.util.Delay;

/**
 * @author Jit Kanetkar
 * Localizes the robot to a relatively accurate heading
 * uses the Ultrasonic Sensor and an approximation of the grid to estimate heading
 */
public class USLocalizer {	
	public static final double WALL_DISTANCE = 30;
	public static final double NOISE = 5;
	private static final int FORWARD_SPEED = 250;
	private static final int ROTATE_SPEED = 150;

	NXTRegulatedMotor leftMotor = Motor.A;
	NXTRegulatedMotor rightMotor = Motor.B;
	
	public static double distance, angleA, angleB, errorAngle; 
	public static String doing = "";
	private Odometer odo;
	private Driver robot;
	private UltrasonicPoller poller;

	public USLocalizer(Odometer odo, Driver driver, UltrasonicPoller usPoller) {
		this.odo = odo;
		this.robot = driver;
		this.poller = usPoller;
	}
	/**
	 * rotates the robot so it is facing approximately 0 degrees
	 */
	public void doLocalization() {
		double [] pos = new double [3];
		// rotate the robot until it sees no wall
		rotateFromWall(true);
		//to avoid seeing one wall twice
		Sound.beep();
		robot.turnTo(25);
		Sound.beep();
		// keep rotating until the robot sees a wall, then latch the angle
		rotateToWall(true);
		angleA = odo.getTheta();
		Sound.beep();
		robot.turnTo(-25);
		Sound.beep();
		// switch direction and wait until it sees no wall
		rotateFromWall(false);
		// keep rotating until the robot sees a wall, then latch the angle
		rotateToWall(false);
		angleB = odo.getTheta();
		// angleA is clockwise from angleB, so assume the average of the
		// angles to the right of angleB is 45 degrees past 'north'
		errorAngle = getAngle(angleA, angleB);
		//dirty fix
		robot.turnTo(errorAngle + 20);
		// update the odometer position (example to follow:)
		odo.setPosition(new double [] {0.0, 0.0, 0.0}, new boolean [] {true, true, true});
	}
	/**
	 * Has the robot rotates until it sees no wall (an object is far)
	 * 
	 * @param direction true is clockwise, false is counterclockwise rotation
	 */
	 private void rotateFromWall(boolean direction)
	 {
		robot.rotate(direction);
		while(distance < (WALL_DISTANCE + NOISE)){
			distance = getFilteredData();	//debugging, don't care about collissions
		}
		robot.stop();
	}
	 /**
	  * Has the robot rotate until it sees a wall (an object is close)
	  * 
	  * @param direction true is clockwise, false is counterclockwise rotation
	  */
	private void rotateToWall(boolean direction){
		robot.rotate(direction);
		distance = getFilteredData();
		while(distance > (WALL_DISTANCE - NOISE)){
			distance = getFilteredData();
		}
		robot.stop();
	}
	/**
	 * Figures out how far off the robot's heading currently is
	 * 
	 * @param alpha	the angle turned in the first sweep
	 * @param beta	the angle turned in the second sweep
	 * @return	the correction to the current heading
	 */
	private double getAngle(double alpha, double beta){

		 double deltaTheta;
		 
		 if(alpha > beta)
			{
			  deltaTheta = 45 - (alpha + beta)/2;
			  
			}
			else
			{
				deltaTheta = 225 - (alpha + beta)/2;
			}
			 
		 return deltaTheta;
		}
	/**
	 * Removes sufficiently far objects
	 * @return the distance seen by the ultrasonic sensor
	 */
	private int getFilteredData() {
		int dist;
		try { Thread.sleep(50); } catch (InterruptedException e) {}
		dist = (int) poller.getDistance();
		if(dist > 50)
			dist = 50;
		return dist;
	}

}
