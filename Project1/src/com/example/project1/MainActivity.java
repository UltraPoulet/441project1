package com.example.project1;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import edu.umich.imlc.collabrify.client.CollabrifyAdapter;
import edu.umich.imlc.collabrify.client.CollabrifyClient;
import edu.umich.imlc.collabrify.client.CollabrifyListener;
import edu.umich.imlc.collabrify.client.CollabrifySession;
import edu.umich.imlc.collabrify.client.exceptions.CollabrifyException;
import android.os.Bundle;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ActivityInfo;
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
	private long sessionID;
	private String sessionName;
	private CollabrifyListener collabrify;
	private ArrayList<String> tags = new ArrayList<String>();
	private String Tag = "WeWrite MainActivity";
	private String text = null;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_NOSENSOR);
		text = this.getIntent().getStringExtra("text");
		collabrify = new CollabrifyAdapter() {
			@Override
			public void onDisconnect()
			{
				Log.i(Tag, "Disconnected from session.");
			}

			@Override
		      public void onReceiveSessionList(final List<CollabrifySession> sessionList)
		      {
				if( sessionList.isEmpty() )
				{
				  Log.i(Tag, "No session available");
				  return;
				}
				List<String> sessionNames = new ArrayList<String>();
				for(CollabrifySession session : sessionList )
				  sessionNames.add(session.name());
				final AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
				builder.setTitle("Choose Session").setItems(
					sessionNames.toArray(new String[sessionList.size()]), new DialogInterface.OnClickListener()
					{
				      @Override
				      public void onClick(DialogInterface dialog, int which)
				      {
						try
						{
						  sessionID = sessionList.get(which).id();
						  sessionName = sessionList.get(which).name();
						  Intent i = new Intent(getBaseContext(), SubActivity.class);
						  i.putExtra("id", sessionID);
						  i.putExtra("text", text);
						  startActivity(i);
						}
						catch( Exception e ){Log.e(Tag, "error", e);}
				      }
				    }
				);
		
				runOnUiThread(new Runnable()
				{
		
				  @Override
				  public void run()
				  {
				    builder.show();
				  }
				});
		      }

		      @Override
		      public void onError(CollabrifyException e)
		      {
		    	  Log.e(Tag, "error", e);
		      }

		};
		
		
		try{
			myClient = new CollabrifyClient(this, "johnrabi@umich.edu", "user display name", "441fall2013@umich.edu", "XY3721425NoScOpE", false, collabrify );
		}
		catch (CollabrifyException e){
			Log.e("Error", "Error creating client", e);
		}
		
		
		tags.add("Default");
		Log.i(Tag, "Added Default to tags");
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		menu.getItem(2).setVisible(createSessionVis);
		menu.getItem(3).setVisible(endSessionVis);
		menu.getItem(4).setVisible(joinSessionVis);
		menu.getItem(5).setVisible(leaveSessionVis);

		if(text != null)
		{
			System.out.println(text);
			((EditTextModified)this.getCurrentFocus()).setText(text);
		}
		
		return true;
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item){
		EditTextModified etm = (EditTextModified)this.getCurrentFocus();
		text = etm.getText().toString();
		switch(item.getItemId()){
		case R.id.action_undo:
			etm.UndoRedoHandler(true);
			return true;
		case R.id.action_redo:
			etm.UndoRedoHandler(false);
			return true;
		case R.id.action_createSession:
			Log.d("Create", "Create Session");
			
			//This is trying to create a session
			Random rand = new Random();
			sessionName = "Rabideau" + rand.nextInt(Integer.MAX_VALUE);
			Log.d(Tag, "Session name is " + sessionName);
			
			runOnUiThread(new Runnable()
			{
				@Override
				public void run()
				{
					//need this to be null, .class file
					Intent i = new Intent(getBaseContext(), SubActivity.class);
					i.putExtra("name", sessionName);
					i.putExtra("text", text);
					i.putExtra("isCreate", true);
					startActivity(i);
				}
			});
			
			return true;
		case R.id.action_joinSession:
			Log.d("Join", "Join session");
			//snag and display available sessions
			try { myClient.requestSessionList(tags); }
			catch (Exception e) { Log.e(Tag, "error", e); }
			return true;
		default:
			return super.onOptionsItemSelected(item);
		}
	}

}
