package com.example.project1;

import java.util.List;

import android.util.Log;
import edu.umich.imlc.collabrify.client.CollabrifyAdapter;
import edu.umich.imlc.collabrify.client.CollabrifySession;

class CollabrifyAdapterExtended extends CollabrifyAdapter{
	
	public void onSessionCreated(long id){
		Log.d("I dunno", "Session Created: " + id);
	}
	
	public void onSessionJoined(long id){
		Log.d("Test", "Session joined: " + id);
	}
	
	
	public void onReceiveSessionList(final List<CollabrifySession> sessionList){
		Log.d("Test", "Did I get here at least?");
	}
}