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
	public static Odometer odo;
	public static boolean hasBlock = false;
	public static void main(String[] args) {

		//ColorSensor cs = new ColorSensor(SensorPort.S1);
		UltrasonicSensor us = new UltrasonicSensor(SensorPort.S2);
		ColorSensor blockSensor = new ColorSensor(SensorPort.S3);
		
		UltrasonicPoller usPoller = new UltrasonicPoller(us);
		odo = new Odometer();
		driver = new Driver(odo);

		//sensors
		blockDetector = new BlockDetection(usPoller, blockSensor, getColorValues(5));
		OdometryDisplay lcd = new OdometryDisplay(odo, blockDetector, usPoller);
		
		odo.start();
		lcd.start();

/*		while(Button.waitForAnyPress() == 0){}
		travel(xDest, yDest);*/
		
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
		System.exit(0);
		//driver.grab();
		//driver.travel(70 ,190);
	}
	/**
	 * Takes a specified block color and returns the RGB values of that block
	 * 
	 * BlockIDs:
	 * 
	 * 1	Red
	 * 2	Yellow
	 * 3	White
	 * 4	Light Blue
	 * 5	Dark Blue
	 * 
	 * @param block	which block we have to search for
	 * @return	the RGB values of the block we have to search for from ~10 cm
	 */
	public static int[] getColorValues(int blockID){
		switch (blockID){
								//10 cm
			case 1:
				return new int[]{60, 6, 6};		//100, 13, 13
			case 2:
				return new int[]{70, 45, 12};	//80, 55, 12
			case 3:
				return new int[]{70, 60, 60};	//70, 60, 60
			case 4:
				return new int[]{60, 70, 80};	//65, 70, 80
			case 5:
				return new int[]{6, 12, 30};	//8, 16, 34
			default:
				return null;
		}
	}
	public static void travel(double x, double y){
		//Travel doesn't block anymore, so Immiediate Return occurs
		driver.travel(x, y);
		boolean avoiding = true;
		//avoidance
		while(avoiding){
			//avoids if object
			if(blockDetector.seesObject()){
				driver.stop();
				//beeps that it sees an object
				Sound.beep();
				Delay.msDelay(100);
				//goes forward to improve accuracy of light sensor
				driver.goForward(2, false);
				//beeps and gets block if it sees one
				if(blockDetector.seesBlock()){
					Sound.beep();
					Delay.msDelay(100);
					getBlock();
				} else {
					//obstacle avoidance
					avoidBlock(true);
				}
				if(!blockDetector.seesObject() || blockDetector.seesBlock()){
					driver.travel(x, y);
				}
			}
			if((x-2 < odo.getX() && odo.getX() < x+2) && (y-2 < odo.getY() && odo.getY() < y+2)){
				avoiding = false;
			}
		}
	}
	public static void searchBlock(UltrasonicPoller usPoller){
		while(!hasBlock){
			//Approaches object if it sees one within 40 cm
			if(usPoller.getDistance() < 40){
				driver.goForward(27);
				//pauses to ensure LS has the correct reading
				driver.stop();
				Delay.msDelay(500);
				//grabs block if the Blue value is high enough
				if(blockDetector.seesBlock()){
					getBlock();
				} else {
				//otherwise it moves backwards and keeps rotating
					driver.goBackward(22);
					driver.rotate(true);
				}
			} else {
				//keeps rotating
				driver.rotate(true);
			}
			//pauses to make sure it turns enough
			Delay.msDelay(500);
		}
	}
}
