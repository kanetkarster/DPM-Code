import lejos.nxt.UltrasonicSensor;
import lejos.util.Timer;
import lejos.util.TimerListener;

/**
 * Group 21 - ECSE 211
 * Satyajit Kanetkar 	-- 260504913
 * Sean Wolfe			-- 260584644
 * 
 * @requirement: Sensor must be positioned at a 45 degree angle
 * @requirement: Robot must be on the right of the wall
 * 
 * No changes
 */

public class UltrasonicPoller implements TimerListener {
	private int TIMER_PERIOD;
	private UltrasonicSensor us;	
	private double distance;
	private static Object lock;
	private Timer timer;
	//initializes Ultrasonic poller
	public UltrasonicPoller(UltrasonicSensor us, int period) {
		this.us = us;
		this.us.off();
		this.TIMER_PERIOD = period; 
		lock = new Object();
		this.timer = new Timer(TIMER_PERIOD, this);
		us.ping();
		timer.start();
	}
	/**
	 * Returns the distance that the UltrasonicSensor's last reading
	 * 
	 * @return distance the ultrasonic polled at last poll
	 */
	public double getDistance(){
		double d;
		synchronized(lock){d = distance;}
		return d;
	}
	/**
	 * updates distance variable
	 */
	@Override
	public void timedOut() {
		synchronized(lock){
			this.distance = us.getDistance();
			}
		us.ping();
	}
	/**
	 * Stops timer
	 */
	public void stop(){
		timer.stop();
	}
}
