package com.example.project1;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import edu.umich.imlc.collabrify.client.CollabrifyAdapter;
import edu.umich.imlc.collabrify.client.CollabrifyClient;
import edu.umich.imlc.collabrify.client.CollabrifyListener;
import edu.umich.imlc.collabrify.client.CollabrifySession;
import edu.umich.imlc.collabrify.client.exceptions.CollabrifyException;
import edu.umich.imlc.collabrify.client.exceptions.ConnectException;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

public class SubActivity extends Activity
{
	public CollabrifyClient myClient;
	boolean createSessionVis = false;
	boolean endSessionVis = true;
	boolean joinSessionVis = false;
	boolean leaveSessionVis = true;
	private long sessionID;
	private String sessionName;
	private CollabrifyListener collabrify;
	private ArrayList<String> tags = new ArrayList<String>();
	private String text = "";
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);		
		
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
						Intent i = new Intent(getBaseContext(), MainActivity.class);
						i.putExtra("text", text);
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
		
		text = getIntent().getStringExtra("text");
		
		String vars = getIntent().getStringExtra("name");
		if(vars != null)
		{
			sessionName = vars;
			try {
				myClient.createSession(sessionName, tags, null, 0);
				sessionID = myClient.currentSessionId();
				Log.i("Tag", "In session: " + sessionID);
			} catch (Exception e) {
				
			}
		}
		else {
			//get list
			long id = getIntent().getLongExtra("id", -1);
			if(id != -1) {
				sessionID = id;
				Log.i("Tag", "Session Joined " + sessionID);
				try {
					myClient.joinSession(sessionID, null);
				} 
				catch (Exception e1) {
					e1.printStackTrace();
				}
			}
			else {
				Log.e("Tag", "Invalid sessionID");
				Intent i = new Intent(getBaseContext(), MainActivity.class);
				i.putExtra("text", text);
				startActivity(i);
			}
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
		case R.id.action_endSession: 
			Log.d("End", "Ended session");
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
		case R.id.action_leaveSession:
			Log.d("Leave", "Leave session");
			createSessionVis = true;
			endSessionVis = false;
			joinSessionVis = true;
			leaveSessionVis = false;
			this.invalidateOptionsMenu();
			try{
				if(myClient.inSession())
					myClient.leaveSession(false);
				else
					Log.d("Test", "Why am I even here?");
			}
			catch ( CollabrifyException e ){
				Log.e("Error", "Error ending session " + e);
			}
			return true;
		default:
			return super.onOptionsItemSelected(item);
		}
	}
}