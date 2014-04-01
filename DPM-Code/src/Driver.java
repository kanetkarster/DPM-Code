import lejos.nxt.Motor;
import lejos.nxt.NXTRegulatedMotor;
import lejos.nxt.Sound;
import lejos.util.Delay;

/**
 * Driver.java
 * 
 * @author Jit Kanetkar
 * 
 * The driver class used in our design
 * Controls all of the robot's movement
 */
public class Driver extends Thread  {
	/* minimum speed the robot will travel at */
	private final int MIN_SPEED = 150;
	/* max speed the robot will travel at */
	private final int MAX_SPEED = 350;
	/*factor the error is multiplied by to calculate the speed*/
	private final int SCALING_FACTOR = 10;
	
	private static final int FORWARD_SPEED = 250;
	private static final int ROTATE_SPEED = 150;
	private static final int LOCALIZE_SPEED = 75;
	
	private static int speed;
	public static double xDest, yDest;
	
	NXTRegulatedMotor leftMotor = Motor.A;
	NXTRegulatedMotor rightMotor = Motor.B;
	NXTRegulatedMotor armMotor = Motor.C;
	
	private static double WHEEL_BASE = Main.WHEEL_BASE;
	private static double WHEEL_RADIUS = Main.WHEEL_RADIUS;
	
	public double thetar, xr, yr;
	private boolean navigating;
	private Odometer odo;
	public Driver(Odometer odometer){
		this.odo =  odometer;
		navigating = false;
	}
/**
 * Has the robot move to a position, relative to starting coordinates
 * 
 * Calculates angle and distance to move to using basic trig and then calls
 * the turnTo and goForward method to move to that point
 * 
 * @param X Coordinate of destination
 * @param Y Coordinate of destination
 */
	public void travel (double x, double y){
			xDest = x;
			yDest = y;
		//gets position. Synchronized to avoid collision
			synchronized (odo.lock) {
				thetar = odo.getTheta() * 180 / Math.PI;
				xr = odo.getX();
				yr = odo.getY();
			}
			//calculates degrees to turn from 0 degrees
			double thetad =  Math.atan2(x - xr, y - yr) * 180 / Math.PI;
			//calculates actual angle to turn
			double theta =  thetad - thetar;
			//calculates magnitude to travel
			double distance  = Math.sqrt(Math.pow((y-yr), 2) + Math.pow((x-xr),2));
			//finds minimum angle to turn (ie: it's easier to turn +90 deg instead of -270)
			if(theta < -180){
				turnTo(theta + 360);
			}
			else if(theta > 180){
				turnTo(theta - 360);
			}
			else turnTo(theta);
			//updates values to display
			goForward(distance);
	}
	/**
	 * Has the robot move to a position, relative to starting coordinates
	 * 
	 * Calculates angle and distance to move to using basic trig and then calls
	 * the turnTo and goForward method to move to that point
	 * 
	 * @param X Coordinate of destination
	 * @param Y Coordinate of destination
	 */
	public void travel (double x, double y, boolean returnImmiediately){
			xDest = x;
			yDest = y;
		//gets position. Synchronized to avoid collision
			synchronized (odo.lock) {
				thetar = odo.getTheta() * 180 / Math.PI;
				xr = odo.getX();
				yr = odo.getY();
			}
			//calculates degrees to turn from 0 degrees
			double thetad =  Math.atan2(x - xr, y - yr) * 180 / Math.PI;
			//calculates actual angle to turn
			double theta =  thetad - thetar;
			//calculates magnitude to travel
			double distance  = Math.sqrt(Math.pow((y-yr), 2) + Math.pow((x-xr),2));
			//finds minimum angle to turn (ie: it's easier to turn +90 deg instead of -270)
			if(theta < -180){
				turnTo(theta + 360);
			}
			else if(theta > 180){
				turnTo(theta - 360);
			}
			else turnTo(theta);
			//updates values to display
			goForward(distance, returnImmiediately);
	}
/**
 * has both wheels turn a computed number of degrees to go forward a passed in distance
 * a non-blocking version, meaning the surrounding code is returned to immiediately
 * 
 * @param distance  the distance the robot should go forward in cm
 */
	public void goForward(double distance){
		
		// drive forward
		speed = FORWARD_SPEED;
		leftMotor.setSpeed(speed);
		rightMotor.setSpeed(speed);
		
		//for isNavigatingMethod
		navigating = true;
		
		leftMotor.rotate(convertDistance(WHEEL_RADIUS, distance), true);
		rightMotor.rotate(convertDistance(WHEEL_RADIUS, distance), true);
		
		navigating = false;
	}
	
/**
 * has both wheels turn a computed number of degrees to go forward a passed in distance
 * a optionally blocking version, meaning the surrounding code may or may not be retured
 * to immiediately
 * 
 * @param distance  the distance the robot should go forward in cm
 * @param returnImmediately  waits until robot has moved before continuing exectution
 */
	public void goForward(double distance, boolean returnImmediately){
		
		// drive forward
		speed = FORWARD_SPEED;
		leftMotor.setSpeed(speed);
		rightMotor.setSpeed(speed);
		
		//for isNavigatingMethod
		navigating = true;
		
		leftMotor.rotate(convertDistance(WHEEL_RADIUS, distance), true);
		rightMotor.rotate(convertDistance(WHEEL_RADIUS, distance), returnImmediately);
		
		navigating = false;
	}
/**
 * Has both wheels turn a computed number of degrees to go backward a passed in distance.
 * A blocking version, meaning the surrounding code is returned to when the robot is 
 * done moving
 * 
 * @param distance  the distance the robot should go forfalseward in cm
 */
	public void goBackward(double distance){
		// drive forward
		speed = FORWARD_SPEED;
		leftMotor.setSpeed(speed);
		rightMotor.setSpeed(speed);
		Sound.beep();
		//for isNavigatingMethod
		navigating = true;
		
		leftMotor.rotate(-convertDistance(WHEEL_RADIUS, distance), true);
		rightMotor.rotate(-convertDistance(WHEEL_RADIUS, distance), false);
		
		navigating = false;
	}
/**
 * Has both wheels turn a computed number of degrees to go backward a passed in distance.
 * An optionally-blocking version, meaning the surrounding code is returned to when the robot is 
 * done moving or not based on the passed in boolean
 * 
 * @param distance  the distance the robot should go forfalseward in cm
 * @param returnImmiediately  	whether the robot should continue executing or wait until movement
 * 								is done to continue with the code
 */
	public void goBackward(double distance, boolean returnImmiediately){
		// drive forward
		speed = FORWARD_SPEED;
		leftMotor.setSpeed(speed);
		rightMotor.setSpeed(speed);
		//for isNavigatingMethod
		navigating = true;
		
		leftMotor.rotate(-convertDistance(WHEEL_RADIUS, distance), true);
		rightMotor.rotate(-convertDistance(WHEEL_RADIUS, distance), returnImmiediately);
		
		navigating = false;
	}
/**
 * Has the robot turned relative to its current position
 * 
 * @param theta	degrees to turn
 */
	public void turnTo (double theta){
	
		// turn degrees clockwise
		leftMotor.setSpeed(LOCALIZE_SPEED);
		rightMotor.setSpeed(LOCALIZE_SPEED);
		
		navigating = true;
		//calculates angel to turn to and rotates
		leftMotor.rotate(convertAngle(WHEEL_RADIUS, WHEEL_BASE, theta), true);
		rightMotor.rotate(-convertAngle(WHEEL_RADIUS, WHEEL_BASE, theta), false);
		
		navigating = false;
	}
	public void turnTo (double theta, int speed){
		
		// turn degrees clockwise
		leftMotor.setSpeed(speed);
		rightMotor.setSpeed(speed);
		
		navigating = true;
		//calculates angel to turn to and rotates
		leftMotor.rotate(convertAngle(WHEEL_RADIUS, WHEEL_BASE, theta), true);
		rightMotor.rotate(-convertAngle(WHEEL_RADIUS, WHEEL_BASE, theta), false);
		
		navigating = false;
	}
/**
 * Has the robot continuosly rotate
 * !important! the robot will continue to rotate until a new movement is passed
 * @param forward	whether the robot will rotate forwards or backwards
 */
	public void rotate (boolean forward){
		leftMotor.setSpeed(LOCALIZE_SPEED);
		rightMotor.setSpeed(LOCALIZE_SPEED);
		if (forward){
			leftMotor.forward();
			rightMotor.backward();
		} else { 
			leftMotor.backward();
			rightMotor.forward();
		}
	}
/**
 * Has the robot continuosly rotate
 * !important! the robot will continue to rotate until a new movement is passed
 * @param forward	whether the robot will rotate forwards or backwards
 * @param speed		the speed at which to rotate at
 */
	public void rotate (boolean forward, int speed){
		leftMotor.setSpeed(speed);
		rightMotor.setSpeed(speed);
		if (forward){
			leftMotor.forward();
			rightMotor.backward();
		} else { 
			leftMotor.backward();
			rightMotor.forward();
		}
	}
/**
 * Rotates the 3rd motor to allow a block to be grabbed
 */
	public void grab(){
		//this.goForward(8, false);
		armMotor.forward();
		armMotor.setSpeed(250);
		armMotor.rotate(160, false);
		Delay.msDelay(2000);
		armMotor.setSpeed(50);
		armMotor.rotate(-100, false);
	}
	public void release(){
		armMotor.forward();
		armMotor.setSpeed(75);
		armMotor.rotate(-45, false);
	}
/**
 * stops all movement of drivetrain
 */
	public void stop(){
		leftMotor.setSpeed(0);
		rightMotor.setSpeed(0);
	}

/**
 * Returns true if the robot is navigating
 * 
 * @return boolean indicating if the robot is traveling
 */
	public boolean isNavigating(){
		return this.navigating;
	}
/**
 * Returns degrees to turn servos in order to rotate robot by that amount
 * 
 * Uses basic math to convert and absolute angle to degrees to turn.
 * 
 * @param Radius of lego wheel
 * @param Width of wheel base
 * @param Absolute angle to turn to
 * 
 * @return Degrees the servo should turn
 */
	public static int convertAngle(double radius, double width, double angle) {
		//(width * angle / radius ) / (2)
		return convertDistance(radius, Math.PI * width * angle / 360.0);
	}
/**
 * Moves robot linerly a certain distance
 * 
 * @param Radius of lego wheel
 * @param Distance to travel
 * 
 * @return degrees to turn servos in order to move forward by that amount
 */
	public static int convertDistance(double radius, double distance) {
		// ( D / R) * (360 / 2PI)
		return (int) ((180.0 * distance) / (Math.PI * radius));
	}
}
