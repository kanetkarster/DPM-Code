import java.util.ArrayList;

import lejos.nxt.ColorSensor;
import lejos.nxt.LCD;
import lejos.nxt.Motor;
import lejos.nxt.NXTRegulatedMotor;
import lejos.nxt.Sound;
import lejos.util.Delay;
/**
 * 
 * @author jit kanetkar
 * preforms localization to an intersection of gridlines
 * 
 * @requirement	US localization must have been called first, 
 * 	or the robot is placed near the intersection of 2 gridlines
 */
public class LightLocalizer {
	private Odometer odo;
	private Driver robot;
	private ColorSensor cs;
	private static final int LINE_VALUE = 420;
	private final double d_Light_To_Sensor = 12;
	
	NXTRegulatedMotor leftMotor = Motor.A;
	NXTRegulatedMotor rightMotor = Motor.B;
	
	public static int counter = 0;

	ArrayList<Double> angles = new ArrayList<Double>();
	
	public static double lightValue;
	public static double theta;
/**
 * 	
 * @param odo		Global odometer, allows location to be set
 * @param driver	allows control of the drive train
 * @param cs		Gets color of lines / surface
 */
	public LightLocalizer(Odometer odo, Driver driver, ColorSensor cs) {
		this.odo = odo;
		this.robot = driver;
		this.cs = cs;
		LCD.clear();
		// turn on the light
	}
/**
 * Has the robot face  0 degrees
 * 
 * Assumes that the robot is on the intersection of 2 gridlines, and knowing that each
 * is 90 degrees apart and having an estimated direction it is able to precisely navigate
 * so that the center of the robot is over the intersection of two gridlines
 */
	public void doLocalization() {
		// drive to location listed in tutorial
		// start rotating and clock all 4 gridlines
		// do trig to compute (0,0) and 0 degrees
		// when done travel to (0,0) and turn to 0 degrees
	double lastLineTime = System.currentTimeMillis();
	cs.setFloodlight(lejos.robotics.Color.RED);
	cs.calibrateHigh();
	double lv = cs.getNormalizedLightValue() - 125;
	robot.rotate(true);
	//rotates in approximately a circle (odo resets at 360, so 358 is used
	while (odo.getTheta() * 180 / Math.PI <= 358){
		//polls every 50 ms
		try {Thread.sleep(50);} catch (InterruptedException e) {}
		lightValue = cs.getNormalizedLightValue();
		if(cs.getNormalizedLightValue() < lv && ((System.currentTimeMillis() - lastLineTime) > 60)){
				counter++;
				angles.add(odo.getTheta());
				lastLineTime = System.currentTimeMillis();
			}
	}

	robot.stop();

	odo.setY(-d_Light_To_Sensor * Math.cos((angles.get(2)-angles.get(0))/2));
	odo.setX(-d_Light_To_Sensor * Math.cos((angles.get(3)-angles.get(1))/2));
	
	} 
 }