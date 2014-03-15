/**
 * OdometryDisplay.java
 * @author jit kanetkar
 * 
 * Controls what is displayed on the NXT screen
 */
import lejos.nxt.ColorSensor.Color;
import lejos.nxt.LCD;
import lejos.nxt.Sound;

public class OdometryDisplay extends Thread {
	private static final long DISPLAY_PERIOD = 250;
	private Odometer odometer;
	BlockDetection detector;
	UltrasonicPoller usPoller;
	
	public static double thetaD;
	public static double thetaR;
	public static double theta;
	/**
	 * Constructs odometery display thread
	 * 
	 * @param odometer	where to get coordinates
	 * @param detector	where to get block statuses
	 * @param usPoller	where to get distance
	 */
	public OdometryDisplay(Odometer odometer, BlockDetection detector, UltrasonicPoller usPoller) {
		this.odometer = odometer;
		this.detector = detector;
		this.usPoller = usPoller;
	}
	
	/**
	 * Main thread method
	 * 
	 * keeps running, updating the NXT distplay every  50 ms
	 */
	public void run() {
		long displayStart, displayEnd;
		double[] position = new double[3];

		// clear the display once
		LCD.clearDisplay();

		while (true) {
			displayStart = System.currentTimeMillis();
			// clear the lines for displaying odometry information
/*			LCD.drawString("X:              ", 0, 0);
			LCD.drawString("Y:              ", 0, 1);
			LCD.drawString("T:              ", 0, 2);
			
			// get the odometry information
			odometer.getPosition(position);

			// display odometry information
			for (int i = 0; i < 3; i++) {
				LCD.drawString(formattedDoubleToString(position[i], 2), 3, i);
			}*/
			
			LCD.drawString(detector.seesObject() ? "Sees Object" : "No Object", 0, 4);
			LCD.drawString(detector.seesBlock() ? "Sees Block" : "No Block", 0, 5);
			
			Color color = detector.getColor();
			LCD.drawString("Blue: " + color.getBlue() + "    ", 0, 0);
			LCD.drawString("Green: " + color.getGreen() + "     ", 0, 1);
			LCD.drawString("Red: " + color.getRed() + "     ", 0, 2);
			
			LCD.drawString("Distance: " + usPoller.getDistance() + "     ", 0, 7);
			// throttle the OdometryDisplay
			displayEnd = System.currentTimeMillis();
			if (displayEnd - displayStart < DISPLAY_PERIOD) {
				try {
					Thread.sleep(DISPLAY_PERIOD - (displayEnd - displayStart));
				} catch (InterruptedException e) {
					// there is nothing to be done here because it is not
					// expected that OdometryDisplay will be interrupted
					// by another thread
				}
			}
		}
	}
	/**
	 * @param x	double to write as String
	 * @param places	handles decimal places
	 * @return	string which will fit well on NXT display
	 */
	private static String formattedDoubleToString(double x, int places) {
		String result = "";
		String stack = "";
		long t;
		
		// put in a minus sign as needed
		if (x < 0.0)
			result += "-";
		
		// put in a leading 0
		if (-1.0 < x && x < 1.0)
			result += "0";
		else {
			t = (long)x;
			if (t < 0)
				t = -t;
			
			while (t > 0) {
				stack = Long.toString(t % 10) + stack;
				t /= 10;
			}
			
			result += stack;
		}
		
		// put the decimal, if needed
		if (places > 0) {
			result += ".";
		
			// put the appropriate number of decimals
			for (int i = 0; i < places; i++) {
				x = Math.abs(x);
				x = x - Math.floor(x);
				x *= 10.0;
				result += Long.toString((long)x);
			}
		}
		
		return result;
	}

}
