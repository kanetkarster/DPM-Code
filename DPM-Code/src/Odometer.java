import lejos.nxt.*;
import lejos.util.*;

/**
 * Odometer.java
 * @author jit kanetkar
 * 
 * Odometer used throughout this project
 */

public class Odometer extends Thread {
	// robot position
	/*constants*/
	
	NXTRegulatedMotor leftMotor = Motor.A;
	NXTRegulatedMotor rightMotor = Motor.B;
	// odometer update period, in ms
	private static final int ODOMETER_PERIOD = 25;
	//LCD update period
	
	private static double WHEEL_BASE = Main.WHEEL_BASE;
	private static double WHEEL_RADIUS = Main.WHEEL_RADIUS;	
	/*variables*/ 
	private static int previousTachoL;          /* Tacho L at last sample */
	private static int previousTachoR;          /* Tacho R at last sample */
	private static int currentTachoL;           /* Current tacho L */
	private static int currentTachoR;           /* Current tacho R */
	private double x, y, theta;

	

	// lock object for mutual exclusion
	public Object lock;

	/**
	 * Odometer constructor
	 */
	public Odometer() {
		x = 0.0;
		y = 0.0;
		theta = 0.0;
		lock = new Object();
		
		leftMotor.resetTachoCount();
		rightMotor.resetTachoCount();
		
		previousTachoL = 0;
		previousTachoR = 0;
		currentTachoL = 0;
		currentTachoR = 0;
	}

	// run method (required for Thread)
	public void run() {
		long updateStart, updateEnd;

		while (true) {
			updateStart = System.currentTimeMillis();
			// put (some of) your odometer code here
			double leftDistance, rightDistance, deltaDistance, deltaTheta, dX, dY;
			currentTachoL = leftMotor.getTachoCount();
			currentTachoR = rightMotor.getTachoCount();
			
			leftDistance = 3.14159 * WHEEL_RADIUS * (currentTachoL - previousTachoL) / 180;
			rightDistance = 3.14159 * WHEEL_RADIUS * (currentTachoR - previousTachoR) / 180;
			
			previousTachoL = currentTachoL;
			previousTachoR = currentTachoR;
			
			deltaDistance = .5 * (leftDistance + rightDistance);
			deltaTheta = (leftDistance - rightDistance) / WHEEL_BASE;

			synchronized (lock) {
				// don't use the variables x, y, or theta anywhere but here!
				theta = (theta + deltaTheta) % (2 * Math.PI);
				
				dX = deltaDistance * Math.sin(theta);
				dY = deltaDistance * Math.cos(theta);
				
				x += dX;
				y += dY;
			}

			// this ensures that the odometer only runs once every period
			updateEnd = System.currentTimeMillis();
			if (updateEnd - updateStart < ODOMETER_PERIOD) {
				try {
					Thread.sleep(ODOMETER_PERIOD - (updateEnd - updateStart));
				} catch (InterruptedException e) {
					// there is nothing to be done here because it is not
					// expected that the odometer will be interrupted by
					// another thread
				}
			}
		}
	}

	// accessors
	/**
	 * 
	 * @param position	array containing coordiantes and angle, to be updated
	 */
	public void getPosition(double[] position) {
		// ensure that the values don't change while the odometer is running
		synchronized (lock) {
				position[0] = x;
				position[1] = y;
				position[2] = theta / (2 * Math.PI) * 360;
		}
	}
	/**
	 * @return	x coordinate of robot
	 */
	public double getX() {
		double result;

		synchronized (lock) {
			result = x;
		}

		return result;
	}
	/**
	 * @return	y coordinate of robot
	 */
	public double getY() {
		double result;

		synchronized (lock) {
			result = y;
		}

		return result;
	}
	/**
	 * @return angle of robot
	 */
	public double getTheta() {
		double result;

		synchronized (lock) {
			result = theta;
		}

		return result;
	}

	// mutators
	/**
	 * Changes the odometer's location
	 * 
	 * @param position	array containing coordinates values
	 * @param update	array containing which coordinates to set
	 */
	public void setPosition(double[] position, boolean[] update) {
		// ensure that the values don't change while the odometer is running
		synchronized (lock) {
			if (update[0])
				x = position[0];
			if (update[1])
				y = position[1];
			if (update[2])
				theta = position[2] % (2 * Math.PI);
		}
	}
	/**
	 * @param x	new x coordinate of robot
	 */
	public void setX(double x) {
		synchronized (lock) {
			this.x = x;
		}
	}
	/**
	 * @param y	new y coordinate of robot
	 */
	public void setY(double y) {
		synchronized (lock) {
			this.y = y;
		}
	}
	/**
	 * @param theta	new angle of robot
	 */
	public void setTheta(double theta) {
		synchronized (lock) {
			this.theta = theta % (2 * Math.PI);
		}
	}
}