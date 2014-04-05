/*
* @author Sean Lawlor
* @date November 3, 2011
* @class ECSE 211 - Design Principle and Methods
*
* Modified by F.P. Ferrie
* February 28, 2014
* Changed parameters for W2014 competition
*/
package bluetooth;

import java.io.DataInputStream;
import java.io.IOException;

import lejos.nxt.LCD;
import lejos.nxt.comm.*;
/*
 * This class inits a bluetooth connection, waits for the data
 * and then allows access to the data after closing the BT channel.
 * 
 * It should be used by calling the constructor which will automatically wait for
 * data without any further user command
 * 
 * Then, once completed, it will allow access to an instance of the Transmission
 * class which has access to all of the data needed
 */
public class BluetoothConnection {
	private Transmission trans;
	
	public BluetoothConnection() {
		LCD.clear();
		LCD.drawString("Starting BT connection", 0, 0);
		
		NXTConnection conn = Bluetooth.waitForConnection();
		DataInputStream dis = conn.openDataInputStream();
		LCD.drawString("Opened DIS", 0, 1);
		this.trans = ParseTransmission.parse(dis);
		LCD.drawString("Finished Parsing", 0, 2);
		try {
			dis.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		conn.close();
	}
	/**
	 * @return an array containing the important information for where the robot has to travel
	 * 
	 * array index values:
	 * 0	starting corner
	 * 
	 * 1	Zone LowerLeft X
	 * 2	Zone LowerLeft Y
	 * 
	 * 3	Zone UpperRight X
	 * 4	Zone UpperRight Y
	 * 
	 * 5	Flag color
	 * 
	 * 6	DropZone X
	 * 7	DropZone Y
	 * 
	 */
	public int[] getPlayerInfo(){
		int[] player = new int[8];
		if(this.trans.role.getId() == 1){
			player[0] = this.trans.startingCorner.getId();
			
			player[1] = this.trans.greenZoneLL_X;
			player[2] = this.trans.greenZoneLL_Y;
			
			player[3] = this.trans.greenZoneUR_X;
			player[4] = this.trans.greenZoneUR_Y;
			
			player[5] = this.trans.greenFlag;
			
			player[6] = this.trans.greenDZone_X;
			player[7] = this.trans.greenDZone_Y;
		}
		else if (this.trans.role.getId() == 2){
			player[0] = this.trans.startingCorner.getId();
			
			player[1] = this.trans.redZoneLL_X;
			player[2] = this.trans.redZoneLL_Y;
			
			player[3] = this.trans.redZoneUR_X;
			player[4] = this.trans.redZoneUR_Y;
			
			player[5] = this.trans.redFlag;
			
			player[6] = this.trans.redDZone_X;
			player[7] = this.trans.redDZone_Y;
		}
		return player;
	}
	public Transmission getTransmission() {
		return this.trans;
	}
	
	public void printTransmission() {
		try {
			LCD.clear();
			LCD.drawString(("Trans. Values"), 0, 0);
			LCD.drawString("Start: " + trans.startingCorner.toString(), 0, 1);
			LCD.drawString("Role: " + trans.role.getId(), 0, 2);
			LCD.drawString("GZ: " + trans.greenZoneLL_X + " " + trans.greenZoneLL_Y + " " + trans.greenZoneUR_X + " " + trans.greenZoneUR_Y,0, 3);
			LCD.drawString("RZ: " + trans.redZoneLL_X + " " + trans.redZoneLL_Y + " " + trans.redZoneUR_X + " " + trans.redZoneUR_Y, 0, 4);
			LCD.drawString("Gdz: " + trans.greenDZone_X + " " + trans.greenDZone_Y, 0, 5);
			LCD.drawString("Rdz: " + trans.redDZone_X + " " + trans.redDZone_Y, 0, 6);
			LCD.drawString("Flg: " + trans.greenFlag + " " + trans.redFlag, 0, 7);
		} catch (NullPointerException e) {
			LCD.drawString("Bad Trans", 0, 8);
		}
	}
	
}
