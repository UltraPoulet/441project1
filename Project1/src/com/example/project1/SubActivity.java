package com.example.project1;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Dictionary;
import java.util.Hashtable;

import com.example.project1.EventProtocols.EventAdd;
import com.example.project1.EventProtocols.EventDel;
import com.example.project1.EventProtocols.EventJoin;
import com.example.project1.EventProtocols.EventLeave;
import com.example.project1.EventProtocols.EventMove;
import com.example.project1.History.Type;

import edu.umich.imlc.collabrify.client.CollabrifyAdapter;
import edu.umich.imlc.collabrify.client.CollabrifyClient;
import edu.umich.imlc.collabrify.client.CollabrifyListener;
import edu.umich.imlc.collabrify.client.exceptions.CollabrifyException;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

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
	private String Tag = "SubActivity";
	private long participantID = -1;
	private Dictionary<Long, Long> cursorLocs = new Hashtable<Long, Long>();
	private ArrayDeque<Tuple<byte[], String, Integer>> globals;
	EditTextModified etm;
	
	boolean broadcastJoin = false;
	
	@Override
	public void onWindowFocusChanged(boolean hasFocus) {
		if(this.getCurrentFocus() instanceof EditTextModified)
		{
			etm = (EditTextModified)this.getCurrentFocus();
			Log.i(Tag, "hey, we worked!");
			etm.myclient = myClient;
			etm.broadcastJoin = broadcastJoin;
			etm.subActivity = true;
			broadcastJoin = true;
		}
		super.onWindowFocusChanged(hasFocus);
	}
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		globals = new ArrayDeque<Tuple<byte[],String,Integer>>();
		setContentView(R.layout.activity_main);	
		setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_NOSENSOR);
		
		collabrify = new CollabrifyAdapter() {
			@Override
			public void onDisconnect()
			{
				Log.i(Tag, "Disconnected from session.");
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
				broadcast("Leave", "");
				Log.i(Tag, "Broadcasted Leave");
			}
			
			@Override
			public void onReceiveEvent(final long orderId, int subId, final String eventType, final byte[] data)
			{
				//myClient.pauseEvents();
				Log.d(Tag, "Received event " + eventType);
				boolean isOwner = false;
				if(!etm.locals.isEmpty())
				{
					//if we're at event
					Log.i(Tag, "subid: " + etm.locals.getFirst().first + " actual: " + subId);
					if(subId == etm.locals.getFirst().first)
					{
						isOwner = true;
						final Tuple<Integer, String, Integer> localelem = etm.locals.pop();
						//don't delete locals on an undo, it effectively undoes your undo.
						if(!etm.longActivity.isEmpty() && etm.longActivity.getFirst() == subId){
							Log.d(Tag, localelem.second + " entered thingy " + etm.subActivity);
							if(!localelem.second.contains("Move")){
								etm.longActivity.pop();
								Log.d(Tag, localelem.second);
							}
						}
							
						else if(localelem.second.contains("Add"))
						{
							try {
								Log.d(Tag, "Deleting local created");
								runOnUiThread(new Runnable()
								{
									@Override
									public void run()
									{
										etm.isAction = true;
										etm.getText().delete(localelem.third, localelem.third + 1);
										etm.isAction = false;
									}
								});
							}
							catch (Exception e) {e.printStackTrace();}
						}
						else if(localelem.second.contains("Delete"))
						{
							try {
								Log.d(Tag, "Adding local deleted " + localelem.second.split(",")[1] + " to " + localelem.third);
								runOnUiThread(new Runnable()
								{
									@Override
									public void run()
									{
										etm.isAction = true;
										etm.getText().insert(localelem.third, localelem.second.split(",")[1]);
										etm.isAction = false;
									}
								});
							}
							catch (Exception e) {e.printStackTrace();}
						}
						
						//run through globals up to event
						while(!globals.isEmpty())
						{
							Tuple<byte[], String, Integer> elem = globals.pop();
							Log.i(Tag, "Globals not empty yet! " + elem.second);
							//the only events that modify elements are adds and deletes
							//there are no owned adds/deletes in this array, it's all
							//other people. Finding your first add/delete is the trigger to
							//stop adding.
							helper(elem.second, elem.first, false);
						}
					}
					else
					{
						Log.i(Tag, eventType + " sub id: " + subId);
						globals.add(new Tuple<byte[], String, Integer>(data, eventType, subId));
						//myClient.resumeEvents();
						return;
					}
				}
				helper(eventType, data, isOwner);
				//myClient.resumeEvents();
			}
	
			@Override
			public void onError(CollabrifyException e)
			{
			 	Log.e(Tag, "error", e);
			}
			
			@Override
			public void onSessionJoined(long maxOrderId, long baseFileSize) {
				try {
					sessionID = myClient.currentSessionId();
					participantID = myClient.currentSessionParticipantId();
					broadcast("Join", "");
					Log.i(Tag, "Broadcasted Join");
					broadcastJoin = true;
					String sessionName = myClient.currentSessionName();
					runOnUiThread(new Runnable(){
						@Override
						public void run() {
							try {
								String sessionName = myClient.currentSessionName();
								setTitle("WeWrite " + sessionName);
							} catch (CollabrifyException e) {
								e.printStackTrace();
							}
						}
						
					});
					Log.d(Tag,"It should have changed title");

				}
				catch(Exception e) { 
					e.printStackTrace();
				}
				super.onSessionJoined(maxOrderId, baseFileSize);
			}
			@Override
			public void onSessionCreated(long id) {
				super.onSessionCreated(id);
				Log.d(Tag, "Session created: " + id);
				try {
					participantID = myClient.currentSessionParticipantId();
					broadcast("Join", "");
					Log.i(Tag, "Broadcasted Join");
					sessionID = id;
				}
				catch(Exception e) {
					e.printStackTrace();
				}
			}
			
			@Override
			public void onSessionEnd(long id){
				super.onSessionEnd(id);
				Log.i(Tag, "Disconnected from session.");
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
		//System.out.println(vars);
		if(vars != null)
		{
			sessionName = vars;
			try {
				myClient.createSession(sessionName, tags, null, 0);
				this.setTitle("WeWrite " + sessionName);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		else {
			//get list
			long id = getIntent().getLongExtra("id", -1);
			if(id != -1) {
				sessionID = id;
				Log.i(Tag, "Session Joined " + sessionID);
				try {
					myClient.joinSession(sessionID, null);
					endSessionVis = false;
					invalidateOptionsMenu();
				} 
				catch (Exception e1) {
					e1.printStackTrace();
				}
			}
			else {
				Log.e(Tag, "Invalid sessionID");
				Intent i = new Intent(getBaseContext(), MainActivity.class);
				i.putExtra("text", text);
				startActivity(i);
			}
		}
			
		
	}
	
	public void helper(final String eventType, final byte[] data, final boolean owner)
	{
		runOnUiThread(new Runnable()
		{
			@Override
			public void run()
			{	
				//myClient.pauseEvents();
				//handle the incoming event
				try {
					if (eventType.contains("Move"))
					{
						EventMove eventMove = EventMove.parseFrom(data);
						System.out.println(eventMove.getPartID() + " " + eventMove.getNewLoc());
						cursorLocs.put(eventMove.getPartID(), eventMove.getNewLoc());
						//Toast.makeText(getBaseContext(), eventType + " " + eventMove.getNewLoc(), //Toast.LENGTH_SHORT).show();
					}
					else if(eventType.contains("Add"))
					{
						try {
							EventAdd eventAdd = EventAdd.parseFrom(data);
							Log.d(Tag, eventAdd.getPartID() + " " + cursorLocs.get(eventAdd.getPartID()).toString() + " " + eventAdd.getChar());
							int appendPos = cursorLocs.get(eventAdd.getPartID()).intValue();
							etm.isAction = true;
							etm.getText().insert(appendPos, eventAdd.getChar());
							if(!owner)
								etm.history.adjustIndexes(appendPos, true);
							etm.isAction = false;
							//Toast.makeText(getBaseContext(), "Add " + eventAdd.getChar() + " " + appendPos, //Toast.LENGTH_SHORT).show();
							Log.i(Tag, etm.history.strMerge());
						}
						catch (Exception e) {e.printStackTrace();}
					}
					else if(eventType.contains("Delete"))
					{
						try {
							EventDel eventDel = EventDel.parseFrom(data);
							int appendPos = cursorLocs.get(eventDel.getPartID()).intValue();
							etm.isAction = true;
							etm.getText().delete(appendPos-1, appendPos);
							if(!owner)
								etm.history.adjustIndexes(appendPos, false);
							etm.isAction = false;
							//Toast.makeText(getBaseContext(), "Delete " + appendPos, //Toast.LENGTH_SHORT).show();
							Log.i(Tag, etm.history.strMerge());
						}
						catch (Exception e) {e.printStackTrace();}
					}
					else if(eventType.contains("Join"))
					{
						EventJoin eventJoin = EventJoin.parseFrom(data);
						//start everyone at 0
						cursorLocs.put(eventJoin.getPartID(), (long) 0);
						Log.e(Tag, "Cursor Loc: " + eventJoin.getPartID() + " " + cursorLocs.get(eventJoin.getPartID()));
						//Toast.makeText(getBaseContext(), eventType, //Toast.LENGTH_SHORT).show();
					}
					else if(eventType.contains("Leave"))
					{
						EventLeave eventLeave = EventLeave.parseFrom(data);
						cursorLocs.remove(eventLeave.getPartID());
						//Toast.makeText(getBaseContext(), eventType, //Toast.LENGTH_SHORT).show();
					}
					else
						Log.d(Tag, "Invalid event Type: " + eventType);
				}
				catch(Exception e) {
					e.printStackTrace();
				}
				//myClient.resumeEvents();
			}
		});
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
	
	//if not an add, just put an empty string
	public void broadcast(String type, String added)
	{
		Log.d(Tag, "Entered broadcast");
		if(myClient != null && myClient.inSession())
		{
			Log.d(Tag, "Client in session");
			try
			{
				if (type == "Join") {
					EventJoin eventJoin = EventJoin.newBuilder().setPartID(participantID).build();
					myClient.broadcast(eventJoin.toByteArray(), type);
				}
				else if (type == "Leave") {
					EventLeave eventLeave = EventLeave.newBuilder().setPartID(participantID).build();
					myClient.broadcast(eventLeave.toByteArray(), type);
				}
			}
			catch( CollabrifyException e ){Log.e(Tag, "error", e);}    	
		}
	}
}