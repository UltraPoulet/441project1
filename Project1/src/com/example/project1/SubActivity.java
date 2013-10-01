package com.example.project1;

import java.util.ArrayList;
import java.util.Dictionary;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.ListIterator;

import com.example.project1.EventProtocols.EventAdd;
import com.example.project1.EventProtocols.EventDel;
import com.example.project1.EventProtocols.EventJoin;
import com.example.project1.EventProtocols.EventLeave;
import com.example.project1.EventProtocols.EventMove;

import edu.umich.imlc.collabrify.client.CollabrifyAdapter;
import edu.umich.imlc.collabrify.client.CollabrifyClient;
import edu.umich.imlc.collabrify.client.CollabrifyListener;
import edu.umich.imlc.collabrify.client.CollabrifyParticipant;
import edu.umich.imlc.collabrify.client.exceptions.CollabrifyException;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

public class SubActivity extends Activity
{
	public CollabrifyClient myClient;
	private long sessionID;
	private String sessionName;
	private CollabrifyListener collabrify;
	private ArrayList<String> tags = new ArrayList<String>();
	private String text = "";
	private String Tag = "WeWrite SubActivity";
	private long participantID = -1;
	private Dictionary<Long, Long> cursorLocs = new Hashtable<Long, Long>();
	EditTextModified etm;
	private boolean onBoot = true;
	private boolean isCreate = false;
	
	boolean broadcastJoin = false;
	
	@Override
	public void onWindowFocusChanged(boolean hasFocus) {
		if(onBoot)
		{
			if(this.getCurrentFocus() instanceof EditTextModified)
			{
				etm = (EditTextModified)this.getCurrentFocus();
				Log.i(Tag, "hey, we worked!");
				etm.myclient = myClient;
				etm.subActivity = true;
				etm.first = isCreate;
				broadcastJoin = true;
			}
		}
		super.onWindowFocusChanged(hasFocus);
	}
	@Override
	public void onBackPressed() {
		// TODO Auto-generated method stub
		super.onBackPressed();
	}
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
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
				Log.d(Tag, "Received event: " + eventType);
				Enumeration<Long> cursors = cursorLocs.keys();
				while(cursors.hasMoreElements())
				{
					Long curr = cursors.nextElement();
					Log.d(Tag, curr + " at: " + cursorLocs.get(curr));
				}
				if(!etm.allEvents.isEmpty())
				{
					//start just after the last event
					ListIterator<Event> mostRecent = etm.allEvents.listIterator(etm.allEvents.size());
					//if we're at event
					Log.i(Tag, "subid: " + etm.allEvents.get(etm.allEvents.size() - 1).subId + " actual: " + subId);
					if(etm.leastRecent != null && subId == etm.leastRecent.subId)
					{
						Event ev = null;
						if(eventType.contains("Join"))
						{
							cursorLocs.put(participantID, (long)0);
							Log.e(Tag, "Cursor Loc: " + participantID + " " + cursorLocs.get(participantID));
						}
						
						if(!mostRecent.hasPrevious()){
							Log.d(Tag, "mostRecent has no previous");
						}
						else{
							Log.d(Tag, "it has previous?");
						}
						//undo all events down to locals
						while(mostRecent.hasPrevious())
						{
							//don't delete locals on an undo, it effectively undoes your undo.
							//if(!etm.longActivity.isEmpty() && etm.longActivity.getFirst() == subId){
							//	Log.i(Tag, "Undo/redo in locals");
							//	etm.longActivity.pop();
							//	etm.locals.pop();
							//}
							
							ev = mostRecent.previous();
							
							if(ev.eventType.contains("Add"))
							{
								try {
									Log.d(Tag, "Brute force: Deleting created " + ev.deleted);
									uiHelper(ev, false);
								}
								catch (Exception e) {e.printStackTrace();}
							}
							else if(ev.eventType.contains("Delete"))
							{
								try {
									Log.d(Tag, "Brute force: Adding deleted " + ev.deleted);
									uiHelper(ev, true);
								}
								catch (Exception e) {e.printStackTrace();}
							}
							else{
								Log.d(Tag, "This is something else " + ev.eventType);
							}
							//get out if we're now looking at the event
							if(ev.skip)
							{
								Log.d(Tag, "Get out of jail free card");
								break;
							}
						}
						//mostRecent now equals the last local
						if(mostRecent.hasNext() || mostRecent.hasPrevious())
						{
							ev.skip = true;
							if(!ev.eventType.contains("Join") && !ev.eventType.contains("Move") && !ev.eventType.contains("Leave"))
								appendPos(ev.location, (ev.eventType.contains("Add")));
						}
						
						boolean first = true;
						
						if(!mostRecent.hasNext()){
							Log.d(Tag, "mostRecent has no next");
						}
						
						while(mostRecent.hasNext())
						{
							//ev=mostRecent.
							ev = mostRecent.next();
							
							Log.d(Tag, "Params: " + ev.isLocal + " " + first + " " + !ev.skip);
							if(ev.isLocal && first && !ev.skip)
							{
								first = false;
								ev.skip = true;
								Log.d(Tag, "Changed lastLocal: " + ev.eventType + " char: " + ev.deleted);
								etm.leastRecent = ev;
							}
							
							if(ev.eventType.contains("Add"))
							{
								try {
									Log.d(Tag, "Brute force: Re-Adding created " + ev.deleted);
									uiHelper(ev, true);
								}
								catch (Exception e) {e.printStackTrace();}
							}
							else if(ev.eventType.contains("Delete"))
							{
								try {
									Log.d(Tag, "Brute force: Re-Deleting deleted " + ev.deleted);
									uiHelper(ev, false);
								}
								catch (Exception e) {e.printStackTrace();}
							}
							
						}
						
						if(first)
						{
							Log.d(Tag, "Setting leastRecent to null");
							etm.leastRecent = null;
						}
						//if we've caught up to our first join and this is the first run
						if(onBoot)
						{
							onBoot = false;
							runOnUiThread(new Runnable()
							{
								@Override
								public void run()
								{
									etm.setFocusableInTouchMode(true);
									etm.setSelection(0);
								}
							});
						}
					}
					else
					{
						helper(eventType, data, subId);
					}
				}
				else{
					Log.d(Tag, "allEvents empty");
				}
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
					runOnUiThread(new Runnable(){
						@Override
						public void run() {
							try {
								String sessionName = myClient.currentSessionName();
								setTitle("WeWrite " + sessionName);
								etm.setFocusable(false);
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
					sessionID = id;
					isCreate = true;
				}
				catch(Exception e) {
					e.printStackTrace();
				}
			}
			@Override
			public void onParticipantJoined(CollabrifyParticipant p) {
				if(isCreate && cursorLocs.get(participantID) == null)
				{
					broadcast("Join", "");
					Log.i(Tag, "Broadcasted Join");
				}
				super.onParticipantJoined(p);
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
		isCreate = getIntent().getBooleanExtra("isCreate", false);
		
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
	
	public void appendPos(int pos, boolean isAdd)
	{
		Enumeration<Long> cursors = cursorLocs.keys();
		while(cursors.hasMoreElements())
		{
			Long curr = cursors.nextElement();
			if(cursorLocs.get(curr) >= pos)
			{
				if(isAdd)
				{
					Log.d(Tag, "Cursor for " + curr + " incremented, " + cursorLocs.get(curr) + " >= " + pos);
					cursorLocs.put(curr, cursorLocs.get(curr) + 1);
				}
				else
				{
					Log.d(Tag, "Cursor for " + curr + " decremented, " + cursorLocs.get(curr) + " >= " + pos);
					cursorLocs.put(curr, cursorLocs.get(curr) - 1);
				}
			}
		}
	}
	
	public void uiHelper(final Event ev, final boolean isAdd)
	{
		runOnUiThread(new Runnable()
		{
			@Override
			public void run()
			{
				etm.isAction = true;
				if(isAdd)
				{
					etm.getText().insert(ev.location, ev.deleted);
				}
				else
				{
					etm.getText().delete(ev.location, ev.location+1);
				}
				etm.isAction = false;
			}
		});
	}
	
	public void helper(final String eventType, final byte[] data, final int subId)
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
						etm.allEvents.add(new Event(subId, eventType, "", (int)eventMove.getNewLoc(), false, false));
					}
					else if(eventType.contains("Add"))
					{
						try {
							EventAdd eventAdd = EventAdd.parseFrom(data);
							Log.d(Tag, "id: " + eventAdd.getPartID());
							Log.d(Tag, "loc: " + cursorLocs.get(eventAdd.getPartID()).toString());
							Log.d(Tag, "char: " + eventAdd.getChar());
							int appendPos = cursorLocs.get(eventAdd.getPartID()).intValue();
							etm.isAction = true;
							etm.getText().insert(appendPos, eventAdd.getChar());
							if(!(eventAdd.getPartID() == participantID))
								etm.history.adjustIndexes(appendPos, true);
							etm.isAction = false;
							//Toast.makeText(getBaseContext(), "Add " + eventAdd.getChar() + " " + appendPos, //Toast.LENGTH_SHORT).show();
							Log.i(Tag, etm.history.strMerge());
							//fix cursor locs
							appendPos(appendPos, true);
							etm.allEvents.add(new Event(subId, eventType, eventAdd.getChar(), appendPos, false, false));
						}
						catch (Exception e) {e.printStackTrace();}
					}
					else if(eventType.contains("Delete"))
					{
						try {
							EventDel eventDel = EventDel.parseFrom(data);
							int appendPos = cursorLocs.get(eventDel.getPartID()).intValue();
							etm.isAction = true;
							String deleted = String.valueOf(etm.getText().charAt(appendPos-1));
							Log.d(Tag, "Pos: " + appendPos);
							etm.getText().delete(appendPos-1, appendPos);
							if(!(eventDel.getPartID() == participantID))
								etm.history.adjustIndexes(appendPos, false);
							etm.isAction = false;
							Log.i(Tag, etm.history.strMerge());
							//fix cursor locs
							appendPos(appendPos, false);
							etm.allEvents.add(new Event(subId, eventType, deleted, appendPos, false, false));
						}
						catch (Exception e) {e.printStackTrace();}
					}
					else if(eventType.contains("Join"))
					{
						EventJoin eventJoin = EventJoin.parseFrom(data);
						//start everyone at 0
						cursorLocs.put(eventJoin.getPartID(), (long) 0);
						Log.e(Tag, "Cursor Loc: " + eventJoin.getPartID() + " " + cursorLocs.get(eventJoin.getPartID()));
						etm.allEvents.add(new Event(subId, eventType, "", -1, false, false));
						Log.i(Tag, "added to allEvents");
					}
					else if(eventType.contains("Leave"))
					{
						EventLeave eventLeave = EventLeave.parseFrom(data);
						cursorLocs.remove(eventLeave.getPartID());
						etm.allEvents.add(new Event(subId, eventType, "", -1, false, false));
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
		menu.getItem(3).setVisible(false);
		menu.getItem(4).setVisible(isCreate);
		menu.getItem(5).setVisible(false);
		menu.getItem(6).setVisible(true);
		
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
					int sub = myClient.broadcast(eventJoin.toByteArray(), type);
					//helper(type, eventJoin.toByteArray(), sub);
					if(etm.leastRecent == null)
					{
						Log.d(Tag, "etm.leastRecent set!");
						etm.leastRecent = new Event(sub, type, added, 0, true, false);
						etm.allEvents.add(etm.leastRecent);
					}
				}
				else if (type == "Leave") {
					EventLeave eventLeave = EventLeave.newBuilder().setPartID(participantID).build();
					int sub = myClient.broadcast(eventLeave.toByteArray(), type);
					//helper(type, eventLeave.toByteArray(), sub);
				}
			}
			catch( CollabrifyException e ){Log.e(Tag, "error", e);}    	
		}
	}
}