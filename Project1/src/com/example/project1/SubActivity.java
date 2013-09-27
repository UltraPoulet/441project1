package com.example.project1;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import edu.umich.imlc.collabrify.client.CollabrifyAdapter;
import edu.umich.imlc.collabrify.client.CollabrifyClient;
import edu.umich.imlc.collabrify.client.CollabrifyListener;
import edu.umich.imlc.collabrify.client.CollabrifySession;
import edu.umich.imlc.collabrify.client.exceptions.CollabrifyException;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

public class SubActivity extends Activity
{
	public CollabrifyClient myClient;
	boolean createSessionVis = true;
	boolean endSessionVis = false;
	boolean joinSessionVis = true;
	boolean leaveSessionVis = false;
	private long sessionID;
	private String sessionName;
	private CollabrifyListener collabrify;
	private ArrayList<String> tags = new ArrayList<String>();
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		//huh?
		/*
		try{
			CollabrifyAdapterExtended adapter = new CollabrifyAdapterExtended();
			myClient = new CollabrifyClient(this, "johnrabi@umich.edu", "user display name", "441fall2013@umich.edu", "XY3721425NoScOpE", false, adapter );
		}
		catch (CollabrifyException e){
			Log.e("Error", "Error creating client", e);
		}
		*/
		collabrify = new CollabrifyAdapter() {
			@Override
			public void onDisconnect()
			{
				Log.i("Tag", "Disconnected from session.");
				runOnUiThread(new Runnable()
				{
					@Override
					public void run()
					{
						Intent i = new Intent(null, MainActivity.class);
						startActivity(i);
					}
				});
			}
			
			@Override
			public void onReceiveEvent(final long orderId, int subId, String eventType, final byte[] data)
			{
				//handle the incoming event
			}
			
			@Override
			public void onReceiveSessionList(final List<CollabrifySession> sessionList)
			{
					if( sessionList.isEmpty() )
					{
					Log.i("Tag", "No session available");
					return;
						}
			}
	
			@Override
			public void onSessionCreated(long id)
			{
			 	//switch and start the intent
			 	Log.i("Tag", "Session created: " + id);
			 	sessionID = id;
			 	runOnUiThread(new Runnable()
			 	{
			 		@Override
			 		public void run()
			 		{
			 			//need this to be null, .class file
			 			Intent i = new Intent(null, SubActivity.class);
			 			startActivity(i);
			 		}
			 	});
			}
	
			@Override
			public void onError(CollabrifyException e)
			{
			 	Log.e("Tag", "error", e);
			}
	
			@Override
			public void onSessionJoined(long maxOrderId, long baseFileSize)
			{
			 		//no idea what we need here
			}
		};
		
		
		try{
			myClient = new CollabrifyClient(this, "johnrabi@umich.edu", "user display name", "441fall2013@umich.edu", "XY3721425NoScOpE", false, collabrify );
		}
		catch (CollabrifyException e){
			Log.e("Error", "Error creating client", e);
		}
		
		
		tags.add("Default");
		Log.i("Tag", "Added to tags");
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
						
			//This is trying to create a session
			try{
				Random rand = new Random();
				String sessionName = "Rabideau" + rand.nextInt(Integer.MAX_VALUE);
				//should probably handle with base session...
				myClient.createSession(sessionName, tags, null, 0);
				Log.d("Tag", "Session name is " + sessionName);
			}
			catch( CollabrifyException e ){
				Log.e("Tag", "error", e);
			}
			
			try{
				myClient.requestSessionList(tags);
				Log.d("Test", "Can it find a sessionlist?");
			}
			catch( CollabrifyException e){
				e.printStackTrace();
			}
			
			return true;
		case R.id.action_endSession: //what is this? Isn't ending a session just owner leave?
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
			//snag and display available sessions
			try { myClient.requestSessionList(tags); }
			catch (Exception e) { Log.e("Tag", "error", e); }
			return true;
		case R.id.action_leaveSession:
			Log.d("Leave", "Leave session");
			createSessionVis = true;
			endSessionVis = false;
			joinSessionVis = true;
			leaveSessionVis = false;
			this.invalidateOptionsMenu();
			try{
				if(myClient.inSession())
					myClient.leaveSession(true);
				else
					Log.d("Test", "Why am I even here?");
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