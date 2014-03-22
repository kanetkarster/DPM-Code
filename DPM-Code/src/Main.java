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
		
		//ColorSensor cs = new ColorSensor(SensorPort.S1);
		UltrasonicSensor us = new UltrasonicSensor(SensorPort.S2);
		ColorSensor blockSensor = new ColorSensor(SensorPort.S3);
		
		UltrasonicPoller usPoller = new UltrasonicPoller(us);
		Odometer odo = new Odometer();
		//Driver driver = new Driver(odo);
		
		//sensors
		blockDetector = new BlockDetection(usPoller, blockSensor,  getColorValues(2));
		OdometryDisplay lcd = new OdometryDisplay(odo, blockDetector, usPoller);
		
		odo.start();
		lcd.start();
		 
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
	/**
	 * Takes a specified block color and returns the RGB values of that block
	 * Accurate at distance of 10 cm
	 * BlockIDs:
	 * 	Light Blue	{60, 70, 80}	1
	 * 	Red			{60, 6, 6}		2
	 * 	Yellow		{70, 45, 12}	3
	 * 	White		{70, 60, 60}	4
	 *	Dark Blue	{6, 12, 30}		5
	 * 
	 * @param block	which block we have to search for
	 * @return	the RGB values of the block we have to search for from ~10 cm
	 */
	public static int[] getColorValues(int blockID){
		switch (blockID){
			case 1:
				//light blue
				return new int[]{60, 70, 80};
			case 2:
				//red
				return new int[]{60, 6, 6};
			case 3:
				//yellow
				return new int[]{70, 45, 12};
			case 4:
				//white
				return new int[]{70, 60, 60};
			case 5:
				//dark blue
				return new int[]{6, 12, 30};
			default:
				//pls don't pls
				return null;
		}
	}
}
