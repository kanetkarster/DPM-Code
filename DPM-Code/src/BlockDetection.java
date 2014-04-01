import lejos.nxt.ColorSensor;
import lejos.nxt.ColorSensor.Color;
import lejos.nxt.LCD;
import lejos.nxt.Sound;
import lejos.util.Timer;
import lejos.util.TimerListener;

/**
 * BlockDetection.java
 * -figures out if a block is in front of the UltraSonic sensor
 * -if something is, it checks the color
 * @author Jit Kanetkar
 */
public class BlockDetection implements TimerListener{
	private final int MIN_DISTANCE = 20;
	private double bluePerGreen, bluePerRed, greenPerRed, error;
	private static final int TIMER_PERIOD = 40;
	//private final int BLOCK_GREEN = 5;
	private UltrasonicPoller usPoller1;
	private UltrasonicPoller usPoller2;
	private ColorSensor coSensor;
	private Color color = new Color(0, 0, 0, 0, 0);
	private Object lock;
	private Timer timer;
	private boolean seesBlock = false;
	private boolean seesObjectLeft = false;
	private boolean seesObjectRight = false;


	/**
	 * Starts timer and sets local variables
	 * @param usPoller	gets the distance robot sees an object
	 */
	public BlockDetection(UltrasonicPoller usPoller1, UltrasonicPoller usPoller2, ColorSensor coSensor, int blockID){
		this.coSensor = coSensor;
		this.usPoller1 = usPoller1;
		this.usPoller2 = usPoller2;
		double[] RGBratios = getColorValues(blockID);
		this.bluePerGreen = RGBratios[0]; this.bluePerRed = RGBratios[1]; this.greenPerRed = RGBratios[2];
		error = RGBratios[3];
		
		this.lock = new Object();
		this.timer = new Timer(TIMER_PERIOD, this);
		
		timer.start();
	}
	@Override
	/**
	 * A timer run every 50ms which updates what the object detection Color and Ultrasonic sensors see.
	 * It constantly updates these values, which are called in the main method
	 */
	public void timedOut() {
		synchronized(lock){color = coSensor.getColor();}
		if(usPoller1.getDistance() < MIN_DISTANCE){
			seesObjectLeft = true;
			detectBlock();
		} else {
			seesObjectLeft = false;
		}
		if(usPoller2.getDistance() < MIN_DISTANCE){
			seesObjectRight = true;
			detectBlock();
		} else {
			seesObjectRight = false;
		}
	}
	/**
	 * updates seesBlock boolean based on the UltraSonic sensors' distance
	 */
	private void detectBlock(){
		//beeps if block is blue enough
		if( (Math.abs(bluePerRed - 	 ((double) color.getBlue()) / color.getRed())) < error
			&& (Math.abs(bluePerGreen -	((double) color.getBlue()) / color.getGreen())) < error
			&& (Math.abs(greenPerRed - ((double) color.getGreen()) / color.getRed())) < error){			
			seesBlock = true;
			Sound.beep();
		}
		else seesBlock = false;
	}
	/**
	 * @return whether a block is close to the UltraSonic sensors && has satisfactory colors
	 */
	public boolean seesBlock(){
		boolean boo;
		synchronized(lock){ boo = seesBlock;}
		return boo;
	}
	/**
	 * @return the Color object last seen by the ColorSensor
	 */
	public Color getColor(){
		Color col;
		synchronized(lock){ col = color;}
		return col;
	}
	/**
	 * @return the Blue seen by the ColorSensor
	 */
	public int getBlue() {
		int blue;
		synchronized(lock){ blue = color.getBlue();}
		return blue;
	}
	/**
	 * @return the Green seen by the ColorSensor
	 */
	public int getGreen(){
		int green;
		synchronized(lock){ green = color.getGreen();}
		return green;
	}
	/**
	 * @return the Red seen by the ColorSensor
	 */
	public int getRed(){
		int red;
		synchronized(lock){ red = color.getRed();}
		return red;
	}
	/**
	 * @return if the robot sees an object
	 */
	public boolean seesObject() {
		boolean boo;
		synchronized(lock){ boo = seesObjectLeft || seesObjectRight;}
		return boo;
	}
	public boolean seesObjectLeft() {
		boolean boo;
		synchronized(lock){ boo = seesObjectLeft;}
		return boo;
	}
	public boolean seesObjectRight() {
		boolean boo;
		synchronized(lock){ boo = seesObjectRight;}
		return boo;
	}
	/**
	 * Returns the Constants for each block required to use block recognition
	 * 
	 * @param blockID  Gives the ID of the block respective of the prespecified values
	 * 1	Light Blue
	 * 2	Red
	 * 3	Yellow
	 * 4	White
	 * 5	Dark Blue
	 * 
	 * @return 	double[] RGBRatios
	 * an array containing the following for each block:
	 * Blue/Green 	Ratio
	 * Blue/Red 	Ratio
	 * Green/Red 	Ratio
	 * error		difference to allow for maximum recognition without false positives
	 */
	public static double[] getColorValues(int blockID){
		switch (blockID){
			case 1:
				//light blue
				return new double[]{1.15, 1.26, 1.1, .15};
			case 2:
				//red
				return new double[]{1, 0.1, 0.1, .3};
			case 3:
				//yellow
				return new double[]{0.28, 0.19, 0.66, .3};
			case 4:
				//white
				return new double[]{1, 0.83, 0.84, .15};
			case 5:
				//dark blue
				return new double[]{2.44, 4.3, 1.75, .6};
			default:
				//pls don't pls
				return null;
		}
	}
}
