import bluetooth.BluetoothConnection;
import lejos.nxt.*;
import lejos.util.Delay;
/**
 * 
 * @author jit kanetkar
 *
 */
public class Main {
	public static double xDest, yDest;
	public static int blockID;
	public static Driver driver;
	public static double lightValue = -1;
	public static BlockDetection blockDetector;
	public static Odometer odo;
	public static boolean hasBlock = false;
	public static void main(String[] args) {
		//sets xDest, yDest and block ID
		getBluetooth();
		
		ColorSensor cs = new ColorSensor(SensorPort.S1);
		UltrasonicSensor us = new UltrasonicSensor(SensorPort.S2);
		ColorSensor blockSensor = new ColorSensor(SensorPort.S3);

		UltrasonicPoller usPoller = new UltrasonicPoller(us);
		odo = new Odometer();
		driver = new Driver(odo);
		blockDetector = new BlockDetection(usPoller, blockSensor, blockID);
		OdometryDisplay lcd = new OdometryDisplay(odo, blockDetector, usPoller);
		
		lcd.start();
		//wait for button press to start
		while(Button.waitForAnyPress() == 0);
		odo.start();
		//light localize
		USLocalizer usl = new USLocalizer(odo, driver, usPoller);
		usl.doLocalization();
		//goes over grid intersection
		driver.turnTo(45);
		driver.goForward(12, false);
		//light localizes
		LightLocalizer lsl = new LightLocalizer(odo, driver, cs);
		lsl.doLocalization();
		
		driver.travel(1, -.7 , false);
		Delay.msDelay(100);
		driver.turnTo(Math.toDegrees(-odo.getTheta() - LightLocalizer.a));
		
		odo.setX(0.00);	odo.setY(0.00); odo.setTheta(0.00);	
		
		Sound.buzz();
		//travels to passed in coordinates
		travel(xDest, yDest);
		//searches for block
		searchBlock(usPoller);
		//return to home zone
		travel(0,0);
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
		if(!blockDetector.seesObject()){
			driver.goForward(30, false);
			driver.turnTo(-90);
			driver.goForward(20, false);
			driver.turnTo(-30);
			if(blockDetector.seesObject()){
				avoidBlock(true);
			}
		} else {
			
		}
		
	}
	/**
	 * Has the robot physically pick up the block
	 */
	public static void getBlock(){
		hasBlock = true;
		driver.grab();
		driver.travel(70 ,190);
		System.exit(0);
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
	public static void travel(double x, double y){
		//Travel doesn't block, so Immiediate Return occurs
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
		double dist, time;
		boolean seesBlock = false;
		while(!hasBlock){
			//Approaches object if it sees one within 40 cm
			if(usPoller.getDistance() < 40){
				dist = usPoller.getDistance() - 8;
				driver.goForward(dist, true);
				//goes until 10cm away from a block
				//pauses to ensure LS has the correct reading
				time = System.currentTimeMillis();
				while(System.currentTimeMillis() - time < 5000){
					if(blockDetector.seesBlock()){
						seesBlock = true;
					}
				}
				if(seesBlock || blockDetector.seesBlock()){
					getBlock();
				}
				//otherwise it moves backwards and keeps rotating
				driver.goBackward(dist);
				driver.rotate(true);
				
			} else {
				//keeps rotating
				driver.rotate(true);
			}
			//pauses to make sure it turns enough
			Delay.msDelay(750);
		}
	}
	public static void getBluetooth(){
		BluetoothConnection conn = new BluetoothConnection();
		int[] player = conn.getPlayerInfo();

		xDest = player[1] * 30.4;
		yDest = player[2] * 30.4;
		blockID = player[5];
	}
}
