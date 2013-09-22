package com.example.project1;

import android.util.Log;
import edu.umich.imlc.collabrify.client.CollabrifyAdapter;

class CollabrifyAdapterExtended extends CollabrifyAdapter{
	public void onSessionCreated(long id){
		Log.println(1, "I dunno", "Session Created: " + id);
	}
}