/*
* @author Sean Lawlor
* @date November 3, 2011
* @class ECSE 211 - Design Principle and Methods
* 
* Modified by F.P. Ferrie
* February 28, 2014
* Changed parameters for W2014 competition
*/
import lejos.nxt.Button;
import lejos.nxt.LCD;
import bluetooth.*;

public class BTTest {
	// example call of the transmission protocol
	// The print function is just for debugging to make sure data is received correctly
	// make sure to import the bluetooth.BluetoothConnection library
	public static void main(String [] args) {
		int[] player = getPlayerInfo();
		
		LCD.clear();
		
		LCD.drawString("Starting:  " + player[0], 0, 0);
		LCD.drawString("Zone LL X: " + player[1], 0, 1);
		LCD.drawString("Zone LL Y: " + player[2], 0, 2);
		LCD.drawString("Zone UR X: " + player[3], 0, 3);
		LCD.drawString("Zone UR Y: " + player[4], 0, 4);
		LCD.drawString("Flag Col:  " + player[5], 0, 5);
		LCD.drawString("Drop X:    " + player[6], 0, 6);
		LCD.drawString("Drop Y:    " + player[7], 0, 7);
		
		Button.ESCAPE.waitForPress();
	}
	/**
	 * Returns an array containing relavent information
	 * 
	 * 0	Starting Corner
	 * 
	 * 1	Zone Lower Left
	 * 2	Zone Lower Right
	 * 
	 * 3	Zone Top Left
	 * 4	Zone Top Right
	 * 
	 * 5	Flag Color
	 * 
	 * 6	Drop Zone X
	 * 7	Drop Zone Y
	 * @return array containing information for player
	 */
	public static int[] getPlayerInfo() {
		BluetoothConnection conn = new BluetoothConnection();
		
		// as of this point the bluetooth connection is closed again, and you can pair to another NXT (or PC) if you wish
		
		// example usage of Tranmission class
		Transmission t = conn.getTransmission();
		if (t == null) {
			LCD.drawString("Failed to read transmission", 0, 5);
		} else {
/*			PlayerRole role = t.role;
			StartCorner corner = t.startingCorner;
			int greenZoneLL_X = t.greenZoneLL_X;
			int greenZoneLL_Y = t.greenZoneLL_Y;
			int redZoneLL_X = t.redZoneLL_X;
			int redZoneLL_Y = t.redZoneLL_Y;
			int greenDZone_X = t.greenDZone_X;
			int greenDZone_Y = t.greenDZone_Y;
			int redDZone_X = t.redDZone_X;
			int redDZone_Y = t.redDZone_Y;
			int greenFlag = t.greenFlag;
			int	redFlag = t.redFlag;
			// print out the transmission information
			conn.printTransmission();*/
			
			int[] player = new int[8];
			if(t.role.getId() == 1){
				player[0] = t.startingCorner.getId();
				
				player[1] = t.greenZoneLL_X;
				player[2] = t.greenZoneLL_Y;
				
				player[3] = t.greenZoneUR_X;
				player[4] = t.greenZoneUR_Y;
				
				player[5] = t.greenFlag;
				
				player[6] = t.greenDZone_X;
				player[7] = t.greenDZone_Y;
			}
			else if (t.role.getId() == 2){
				player[0] = t.startingCorner.getId();
				
				player[1] = t.redZoneLL_X;
				player[2] = t.redZoneLL_Y;
				
				player[3] = t.redZoneUR_X;
				player[4] = t.redZoneUR_Y;
				
				player[5] = t.redFlag;
				
				player[6] = t.redDZone_X;
				player[7] = t.redDZone_Y;
			}
			return player;
		}
		// stall until user decides to end program
		return null;
	}
}
