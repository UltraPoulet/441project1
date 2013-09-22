package com.example.project1;

import edu.umich.imlc.collabrify.client.CollabrifyClient;
import edu.umich.imlc.collabrify.client.exceptions.CollabrifyException;
import android.os.Bundle;
import android.app.Activity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

public class MainActivity extends Activity {

	public CollabrifyClient newClient;
	
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }
    
    public boolean onOptionsItemSelected(MenuItem item) {
    	EditTextModified etm = (EditTextModified)this.getCurrentFocus();
    	switch (item.getItemId()) {
	        case R.id.action_settings:
	        	return true;
	        case R.id.action_undo:
	        	etm.UndoRedoHandler(true);
	        	return true;
	        case R.id.action_redo:
	        	etm.UndoRedoHandler(false);
	        	return true;
	        case R.id.action_createSession:
	        	//create session
	        	try{
	        		newClient = new CollabrifyClient(this, "user email", "display name", "441fall2013@umich.edu", "XY3721425NoScOpE", false, new CollabrifyAdapterExtended());
	        	}
	        	catch( CollabrifyException e ){
	        		e.printStackTrace();
	        	}
	        	try{
	        		String sessionName = "Rabideau";
	        		newClient.createSession(sessionName,null,null,2);
	        		Log.i("Tag", "Session name is " + sessionName);
	        	}
	        	catch( CollabrifyException e ){
	        		Log.e("Tag", "error", e);
	        	}
	        	item.setEnabled(false);
	        	return true;
	        default:
	        	return super.onOptionsItemSelected(item);
    	}
    }
}
