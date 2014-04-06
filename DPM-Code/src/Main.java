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
	public static double[] starting = new double[]{0, 0, 0};
	
	public static double X1 = 30, X2 = 60, X3 = 90, dropX;
	public static double Y1 = 150, Y2 = 120, Y3 = 150, dropY;
	public static double SA1 = 0, SA2 = -45, SA3 = 180;
	public static double EA1 = 180, EA2 = 45, EA3 = 0;
	
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

		UltrasonicPoller usPoller = new UltrasonicPoller(us1, 50);
		Delay.msDelay(23);
		UltrasonicPoller usPoller2 = new UltrasonicPoller(us2, 60);
		odo = new Odometer();
		driver = new Driver(odo);
		blockDetector = new BlockDetection(usPoller, usPoller2, blockSensor, blockID);
		//OdometryDisplay lcd = new OdometryDisplay(odo, blockDetector, usPoller);
		OdometryDisplay lcd = new OdometryDisplay(odo, blockDetector, usPoller2);

		lcd.start();
		odo.start();
		while(Button.waitForAnyPress() == 0);
		//us localize
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
		
		//odo.setX(0.00);	odo.setY(0.00); odo.setTheta(0.00);	
		odo.setPosition(starting, new boolean[]{true, true, true});

		Sound.buzz();
		travel(X1, Y1);
		driver.turnToAbsolute(SA1, 150);
		Sound.beep();
		searchBlock(usPoller, usPoller2, EA1);
		Sound.buzz();
		
		if(!hasBlock){
			//travels to second search location
			travel(X2, Y2);
			driver.turnToAbsolute(SA2, 150);
			Sound.beep();
			searchBlock(usPoller, usPoller2, EA2);
			Sound.buzz();
		}
		if(!hasBlock){
			//travels to third search location
			travel(X3, Y3);
			driver.turnToAbsolute(SA3, 150);
			Sound.beep();
			searchBlock(usPoller, usPoller2, EA3);
			Sound.buzz();
		}
		//return to home zone
		travel(dropX, dropY);
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
						dirToTurn = false;
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
	public static void searchBlock(UltrasonicPoller usPoller1, UltrasonicPoller usPoller2, double maxAngle){
		double dist, time;
		boolean seesBlock = false;
		driver.turnToAbsolute(maxAngle, 150, true);
		while(!hasBlock){
			//Approaches object if it sees one within 40 cm
			dist = Math.min(usPoller1.getDistance(), usPoller2.getDistance());
			if(dist < 30){
				dist -= 5;
				driver.goForward(dist, true);
				//goes until 10cm away from a block
				//pauses to ensure LS has the correct reading
				time = System.currentTimeMillis();
				while(System.currentTimeMillis() - time < 4000){
					if(blockDetector.seesBlock()){
						seesBlock = true;
						Sound.beep();
					}
				}
				if(seesBlock || blockDetector.seesBlock()){
					getBlock();
				}
				//otherwise it moves backwards and keeps rotating
				driver.goBackward(dist);
				driver.turnToAbsolute(maxAngle, 150, true);
				//pauses to make sure it turns enough
				Delay.msDelay(350);
			} else if(Math.abs(maxAngle - Math.toDegrees(odo.getTheta())) < 5){
				//keeps rotating
				break;
			}		
			}
	}
	/**
	 * Updates global variables to be pertinant to important locations traveled to by the robot
	 */
	public static void getBluetooth(){
		BluetoothConnection conn = new BluetoothConnection();
		double[] player = conn.getPlayerInfo();

/*		xDest = player[1] * 30.4 + 15;
		yDest = player[2] * 30.4 + 15;
		blockID = player[5];*/
		starting = getStartingPosition((int) player[0]);
		blockID = (int) player[1];

		X1 = player[2];
		Y1 = player[3];
		SA1 = player[4];
		EA1 = player[5];
		
		X2 = player[6];
		Y2 = player[7];
		SA2 = player[8];
		EA1 = player[9];
		
		X3 = player[10];
		Y3 = player[3];
		SA3 = player[11];
		EA3 = player[12];
		
		dropX = player[13];
		dropY = player[14];
	}
	public static double[] getStartingPosition(int startingCorner){
		switch (startingCorner){
			case 1:
				return new double[]{0,0,0};
			case 2:
				return new double[]{304, 0, Math.toRadians(270)};
			case 3:
				return new double[]{304, 304, Math.toRadians(180)};
			case 4:
				return new double[]{0, 304, Math.toRadians(90)};
			default: return null;
		}
	}
}
