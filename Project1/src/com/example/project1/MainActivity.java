package com.example.project1;

import edu.umich.imlc.collabrify.client.CollabrifyClient;
import edu.umich.imlc.collabrify.client.exceptions.CollabrifyException;
import android.os.Bundle;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

@SuppressLint("NewApi")
public class MainActivity extends Activity {

	
	public CollabrifyClient myClient;
	boolean createSessionVis = true;
	boolean endSessionVis = false;
	boolean joinSessionVis = true;
	boolean leaveSessionVis = false;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		try{
			CollabrifyAdapterExtended adapter = new CollabrifyAdapterExtended();
			myClient = new CollabrifyClient(this, "johnrabi@umich.edu", "user display name", "441fall2013@umich.edu", "XY3721425NoScOpE", false, adapter );
		}
		catch (CollabrifyException e){
			Log.e("Error", "Error creating client", e);
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		menu.getItem(3).setVisible(createSessionVis);
		menu.getItem(4).setVisible(endSessionVis);
		menu.getItem(5).setVisible(joinSessionVis);
		menu.getItem(6).setVisible(leaveSessionVis);
		
		return true;
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item){
		EditTextModified etm = (EditTextModified)this.getCurrentFocus();
		switch(item.getItemId()){
		case R.id.action_settings:
			return true;
		case R.id.action_undo:
			etm.UndoRedoHandler(true);
			return true;
		case R.id.action_redo:
			etm.UndoRedoHandler(false);
			return true;
		case R.id.action_createSession:
			Log.d("Create", "Create Session");
			createSessionVis = false;
			endSessionVis = true;
			joinSessionVis = false;
			leaveSessionVis = false;
			this.invalidateOptionsMenu();
			
			//This was a test to see if it can even find sessions. Nope
			try{
				myClient.requestSessionList(null);
				Log.d("Test", "Can it find a sessionlist?");
			}
			catch( CollabrifyException e){
				e.printStackTrace();
			}
			
			//This is trying to create a session
			try{
				String sessionName = "Rabideau";
				myClient.createSession(sessionName, null, null, 0);
				System.out.println("Session name is " + sessionName);
				Log.d("Tag", "Session name is " + sessionName);
			}
			catch( CollabrifyException e ){
				Log.e("Tag", "error", e);
			}
			
			//And failing.
			
			
			return true;
		case R.id.action_endSession:
			Log.d("End", "Ended session");
			createSessionVis = true;
			endSessionVis = false;
			joinSessionVis = true;
			leaveSessionVis = false;
			this.invalidateOptionsMenu();
			return true;
		case R.id.action_joinSession:
			Log.d("Join", "Join session");
			createSessionVis = false;
			endSessionVis = false;
			joinSessionVis = false;
			leaveSessionVis = true;
			this.invalidateOptionsMenu();
			return true;
		case R.id.action_leaveSession:
			Log.d("Leave", "Leave session");
			createSessionVis = true;
			endSessionVis = false;
			joinSessionVis = true;
			leaveSessionVis = false;
			this.invalidateOptionsMenu();
			try{
				if(myClient.inSession()){
					myClient.leaveSession(true);
				}
				else{
					Log.d("Test", "Why am I even here?");
				}
			}
			catch ( CollabrifyException e ){
				Log.e("Error", "Error ending session");
			}
			return true;
		default:
			return super.onOptionsItemSelected(item);
		}
	}

}
