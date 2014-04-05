import bluetooth.BluetoothConnection;
import lejos.nxt.*;
import lejos.util.Delay;
/**
 * 
 * @author jit kanetkar
 *
 */
public class Main {
	public static final double WHEEL_BASE = 15.8;
	public static final double WHEEL_RADIUS = 2.15;
	public static double xDest = 30, yDest = 30;
	public static double[] starting;
	public static double lowerLeftX, lowerLeftY, upperRightX, upperRightY, dropZoneX, dropZoneY;
	public static int blockID = 1;
	public static Driver driver;
	public static double lightValue = -1;
	public static BlockDetection blockDetector;
	public static USLocalizer usl;
	public static Odometer odo;
	public static boolean hasBlock = false;
	public static void main(String[] args) {
		//sets xDest, yDest and block ID
		//getBluetooth();
		//after Bluetooth input received:
		ColorSensor cs = new ColorSensor(SensorPort.S1);
		UltrasonicSensor us1 = new UltrasonicSensor(SensorPort.S2);
		UltrasonicSensor us2 = new UltrasonicSensor(SensorPort.S4);
		ColorSensor blockSensor = new ColorSensor(SensorPort.S3);

		UltrasonicPoller usPoller = new UltrasonicPoller(us1);
		UltrasonicPoller usPoller2 = new UltrasonicPoller(us2);
		odo = new Odometer();
		driver = new Driver(odo);
		blockDetector = new BlockDetection(usPoller, usPoller2, blockSensor, blockID);
		//OdometryDisplay lcd = new OdometryDisplay(odo, blockDetector, usPoller);
		OdometryDisplay lcd = new OdometryDisplay(odo, blockDetector, usPoller2);

		lcd.start();
		odo.start();
		while(Button.waitForAnyPress() == 0);
		//light localize
		usl = new USLocalizer(odo, driver, usPoller);
		usl.doLocalization();
		//goes over grid intersection
		driver.turnTo(45, 200);
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
		//travel(xDest, 0);
		driver.travel(xDest, yDest, false);
		driver.turnToAbsolute(0, 150);
		Sound.beep();
		searchBlock(usPoller, 90);
		//return to home zone
		//travel(0,0);
		System.exit(1);
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
		double x, y;
		double sign = dir ? 1 : -1; 
		driver.stop();
		driver.goBackward(10);
		driver.turnTo(sign * 90);
		
		x = odo.getX(); y = odo.getY();
		driver.goForward(35, true);
		while(35 - Math.sqrt(Math.pow((odo.getX() - x), 2) + Math.pow((odo.getY() - y), 2)) > 0){
			if(blockDetector.seesObject()){
				return;
			}
		}
/*		driver.turnTo(-1 * sign * 90);
		driver.goForward(35, true);
		x = odo.getX(); y = odo.getY();
		while(25 - Math.sqrt(Math.pow((odo.getX() - x), 2) + Math.pow((odo.getY() - y), 2)) > 0){
			if(blockDetector.seesObject()){
				return;
			}
		}
		driver.turnTo(-1 * sign * 30);*/
	}
	/**
	 * Has the robot physically pick up the block
	 * @return the robot is holding the block
	 */
	public static void getBlock(){
		driver.grab();
		hasBlock = true;
	}
/**
 * Has the robot travel to a specified x and y coordinate with block avoidance running
 * 
 * This calls driver.travel(x, y). which returns immiediately. Then, while 
 * the robot is sufficiently far away from the end location, it travels
 * to the specified location while implementing block avoidance.
 * 
 * The robot will avoid Left or Right based off the position of the robot 
 * relative to the walls and which sensor sees the block
 * 
 * @param x	coordinate to travel to
 * @param y	coordinate to travel to
 * @return the robot at approximately the passed in x and y coordinate
 */
	public static void travel(double x, double y){
		//Travel doesn't block, so Immiediate Return occurs
		driver.travel(x, y);
		boolean avoiding = true;
		boolean dirToTurn = true;
		//avoidance
		while(avoiding){
			//avoids if object
			if(blockDetector.seesObject()){
				driver.stop();
				//beeps that it sees an object
				Sound.beep();
				Delay.msDelay(100);

					//obstacle avoidance
					if (blockDetector.seesObjectLeft() && blockDetector.seesObjectRight()){
					}
					else if (blockDetector.seesObjectLeft()){
						//avoids to right side
						dirToTurn = true;
					} else if (blockDetector.seesObjectRight()) {
						//avoids to left side
						dirToTurn = false;
					}
					avoidBlock(dirToTurn);
				if(!blockDetector.seesObject() || blockDetector.seesBlock()){
					driver.travel(x, y);
				}
			}
			if((x-2 < odo.getX() && odo.getX() < x+2) && (y-2 < odo.getY() && odo.getY() < y+2)){
				avoiding = false;
			}
		}
	}
	/**
	 * Algorithm to search for a block
	 * 
	 * The robot keeps rotating while it hasn't grabbed a block
	 * If it sees an object, it approaches, constantly checking the RGB values
	 * If the BlockDetection Timer ever senses the proper ratios, the robot grabs the block and returns to the previous method
	 * 
	 * @param usPoller	gives the distance, if any, an object is seen
	 * @return The robot should be carrying the block
	 */
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
				while(System.currentTimeMillis() - time < 4000){
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
			Delay.msDelay(500);
		}
	}
	public static void searchBlock(UltrasonicPoller usPoller, double maxAngle){
		double dist, time;
		boolean seesBlock = false;
		while((Math.toDegrees(odo.getTheta()) < maxAngle) && !hasBlock){
			//Approaches object if it sees one within 40 cm
			if(usPoller.getDistance() < 40){
				dist = usPoller.getDistance() - 8;
				driver.goForward(dist, true);
				//goes until 10cm away from a block
				//pauses to ensure LS has the correct reading
				time = System.currentTimeMillis();
				while(System.currentTimeMillis() - time < 4000){
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
	/**
	 * Updates global variables to be pertinant to important locations traveled to by the robot
	 */
	public static void getBluetooth(){
		BluetoothConnection conn = new BluetoothConnection();
		int[] player = conn.getPlayerInfo();

/*		xDest = player[1] * 30.4 + 15;
		yDest = player[2] * 30.4 + 15;
		blockID = player[5];*/
		starting = getStartingPosition(player[0]);
		
		lowerLeftX = player[1];
		lowerLeftY= player[2];
		
		upperRightX = player[3];
		upperRightY = player[4];
		
		blockID = player[5];
		
		dropZoneX = player[6];
		dropZoneY = player[7];
	}
	public static double[] getStartingPosition(int startingCorner){
		switch (startingCorner){
			case 1:
				return new double[]{0,0,0};
			case 2:
				return new double[]{304, 0, 270};
			case 3:
				return new double[]{304, 304, 180};
			case 4:
				return new double[]{0, 304, 90};
			default: return null;
		}
	}
}
