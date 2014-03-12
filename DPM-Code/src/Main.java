import lejos.nxt.*;
import lejos.util.Delay;
/**
 * 
 * @author jit kanetkar
 *
 */
public class Main {
	public static double xDest = 25;
	public static double yDest = 150;
	public static Driver driver;
	public static BlockDetection blockDetector;
	public static boolean hasBlock = false;
	public static void main(String[] args) {
		// setup the pollers
		UltrasonicPoller usPoller = new UltrasonicPoller(new UltrasonicSensor(SensorPort.S2));
		ColorSensor cs = new ColorSensor(SensorPort.S1);
		ColorSensor ls = new ColorSensor(SensorPort.S2);

		//cs.setFloodlight(lejos.robotics.Color.RED);
		Odometer odo = new Odometer();
		driver = new Driver(odo);
		blockDetector =  new BlockDetection(usPoller, cs, driver);
		OdometryDisplay lcd = new OdometryDisplay(odo, blockDetector, usPoller);
		odo.start();

		int buttonChoice;
		do {
			// clear the display
			LCD.clear();

			// ask the user whether the motors should Avoid Block or Go to locations
			LCD.drawString("< Left | Right >", 0, 0);
			LCD.drawString("       |        ", 0, 1);
			LCD.drawString("  Grab | Drive  ", 0, 2);
			LCD.drawString(" Block |        ", 0, 3);
			LCD.drawString("       |        ", 0, 4);

			buttonChoice = Button.waitForAnyPress();
		} while (buttonChoice != Button.ID_LEFT
				&& buttonChoice != Button.ID_RIGHT);
		
		if(buttonChoice == Button.ID_RIGHT){
				driver.grab();
				Delay.msDelay(2000);
				driver.release();
		}
		while (Button.waitForAnyPress() != Button.ID_ESCAPE);
		System.exit(0);
	}
	/**
	 * Block avoidance method:
	 * 
	 * Turns 90 degrees to the right/left depending on which US detects a block
	 * Goes forward 30 cm, while making sure there are no blocks ahead
	 * Turns back to original heading
	 * Goes forward 15 cm
	 * Turns backwards 30 degrees to ensure it won't hit obstacle
	 * 		recursive call to keep avoiding block
	 * otherwise contiues on original path
	 * 
	 * @param dir	direction to rotate in
	 */
	public static void avoidBlock(boolean dir){
		driver.stop();
		Sound.buzz();
		Delay.msDelay(1000);
		driver.turnTo(90);
		if(blockDetector.seesBlock() || !blockDetector.seesObject()){
			driver.goForward(30, false);
			driver.turnTo(-90);
			driver.goForward(15, false);
			driver.turnTo(-30);
			if(blockDetector.seesObject()){
				avoidBlock(true);
			}
		}
	}
	/**
	 * Has the robot physically pick up the block
	 */
	public static void getBlock(){
		hasBlock = true;
		driver.grab();
		//driver.travel(70 ,190);
	}
}
