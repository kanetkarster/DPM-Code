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
	public static double lightValue = -1;
	public static BlockDetection blockDetector;
	public static boolean hasBlock = false;
	public static void main(String[] args) {
		Odometer odo = new Odometer();
		Driver driver = new Driver(odo);
		UltrasonicSensor us = new UltrasonicSensor(SensorPort.S2);
		UltrasonicPoller usPoller = new UltrasonicPoller(us);
		ColorSensor cs = new ColorSensor(SensorPort.S1);
		OdometryDisplay lcd = new OdometryDisplay(odo, blockDetector, usPoller);

		odo.start();
		lcd.start();
		
		Button.waitForAnyPress();
		// perform the ultrasonic localization
		USLocalizer usl = new USLocalizer(odo, driver, us, USLocalizer.LocalizationType.FALLING_EDGE);
		usl.doLocalization();
				
		// perform the light sensor localization
		LightLocalizer lsl = new LightLocalizer(odo, driver, cs);
		lsl.doLocalization();

		driver.travel(0, 0, false);
		driver.turnTo(Math.toDegrees(-odo.getTheta()));

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
