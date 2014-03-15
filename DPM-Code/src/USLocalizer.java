import lejos.nxt.LCD;
import lejos.nxt.Motor;
import lejos.nxt.NXTRegulatedMotor;
import lejos.nxt.Sound;
import lejos.nxt.UltrasonicSensor;
import lejos.util.Delay;

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
	private UltrasonicSensor us;
	
	public USLocalizer(Odometer odo, Driver driver, UltrasonicSensor us) {
		this.odo = odo;
		this.robot = driver;
		this.us = us;
		
		// switch off the ultrasonic sensor
		us.off();
	}
	
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
			// update the odometer position (example to follow:)
			robot.turnTo(errorAngle + 45);
			odo.setPosition(new double [] {0.0, 0.0, Math.toRadians(45)}, new boolean [] {true, true, true});
			robot.goForward(12, false);
		
	}
	 private void rotateFromWall(boolean direction)
	 {
		robot.rotate(direction);
		while(distance < (WALL_DISTANCE + NOISE)){
			distance = getFilteredData();	//debugging, don't care about collissions
		}
		robot.stop();
	}
	 /**
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
	private double getAngle(double alpha, double beta){
/*		 return (alpha > beta) ? (225 - (alpha + beta)/2) : (45 - (alpha + beta)/2);
*/	
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
	private int getFilteredData() {
		int dist;
		
		// do a ping
		us.ping();
		// wait for the ping to complete
		try { Thread.sleep(50); } catch (InterruptedException e) {}
		
		// there will be a delay here
		dist = us.getDistance();
		if(dist > 50)
			dist = 50;
		return dist;
	}

}
