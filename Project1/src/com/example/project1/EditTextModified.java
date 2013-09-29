package com.example.project1;

import com.example.project1.EventProtocols.EventAdd;
import com.example.project1.EventProtocols.EventDel;
import com.example.project1.EventProtocols.EventJoin;
import com.example.project1.EventProtocols.EventLeave;
import com.example.project1.EventProtocols.EventMove;
import com.example.project1.History.Type;

import edu.umich.imlc.collabrify.client.CollabrifyClient;
import edu.umich.imlc.collabrify.client.exceptions.CollabrifyException;

import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.widget.EditText;
import android.widget.Toast;

public class EditTextModified extends EditText{
	
	public boolean changedText = false;
	public History history = new History();
	private String prev;
	private int lastPos;
	private boolean notBoot;
	public boolean isAction;
	public CollabrifyClient myclient = null;
	
	public boolean broadcastJoin;
	
	public EditTextModified(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		setLongClickable(false);
	}

	public EditTextModified(Context context, AttributeSet attrs) {
	    super(context, attrs);
	    setLongClickable(false);
	}

	public EditTextModified(Context context) {
	    super(context);
	    setLongClickable(false);
	}

	@Override
	protected void onSelectionChanged(int selStart, int selEnd){
		if(changedText || !notBoot){
			Log.d("EditText", "blahblahblah");
			changedText = false;
		}
		else if(isAction)
		{
			//Toast.makeText(getContext(), history.strMerge(), //Toast.LENGTH_SHORT).show();
		}
		else{
			//Toast.makeText(getContext(), "selStart is " + selStart + "selEnd is " + selEnd, //Toast.LENGTH_SHORT).show();
			history.clearRedo();
			if(selStart == selEnd)
			{
				history.add(true, "", lastPos, Type.CURSOR_MOVE);
			}
			//else
			//	history.add(true, Integer.toString(selStart) + "," + Integer.toString(selEnd), Type.SELECTION);
			//Toast.makeText(getContext(), history.strMerge(), //Toast.LENGTH_SHORT).show();
		}
		lastPos = selStart;
		broadcast("Move", "");
	}
	
	@Override
	protected void onTextChanged(CharSequence text, int start, int lengthBefore, int lengthAfter){
		//we don't add the stuff that happens on boot to the history
		if(!notBoot)
		{
			notBoot = true;
			return;
		}
		if(!isAction)
		{
			Log.d("Log", "Before lengthBefore < lengthAfter");
			if(lengthBefore < lengthAfter){
				////Toast.makeText(getContext(), "Added: " + text.subSequence(start, start + lengthAfter), //Toast.LENGTH_SHORT).show();
				history.add(true, text.subSequence(start, start + lengthAfter).toString(), start, Type.CHAR_ADD);
				try {
					broadcast("Add", text.subSequence(start, start + lengthAfter).toString());
					Log.i("Tag", "Hey, we broadcasted an add");
				}
				catch(Exception e) {
					e.printStackTrace();
					//non issue, we're in main
				}
			}
			else if (lengthBefore > lengthAfter){
				String temp = prev.substring(start, start + (lengthBefore - lengthAfter));
				//Toast.makeText(getContext(), "Removed " + Integer.toString(lengthBefore - lengthAfter) + " chars " + temp + " at position: " + start, //Toast.LENGTH_SHORT).show();
				history.add(true, temp, start, Type.CHAR_DELETE);
				try {
					broadcast("Delete", temp);
					Log.i("Tag", "Hey, we broadcasted a delete");
				}
				catch(Exception e) {
					//non issue, we're in main
				}
			}
			history.clearRedo();
		}
		//Toast.makeText(getContext(), history.strMerge(), //Toast.LENGTH_SHORT).show();
		prev = text.toString();
		changedText = true;
	}
	
	public int getCursorLoc()
	{
		return lastPos;
	}
	
	public void UndoRedoHandler(boolean isUndo)
	{
		Tuple<String, Integer, Type> next;
		if(isUndo)
		{
			if(history.undoHistory.size() == 0)
				return;
			next = history.undoHistory.pop();
		}
		else
		{
			if(history.redoHistory.size() == 0)
				return;
			next = history.redoHistory.pop();
		}
		//we need to exclude a cursor move, since the value is actually the previous location
		if(next.third != Type.CURSOR_MOVE)
			history.add(isUndo, next);
		isAction = true;
		switch(next.third)
		{
			//functional
			case CURSOR_MOVE:
				history.add(!isUndo, "", lastPos, Type.CURSOR_MOVE);
				this.setSelection(next.second);
				break;
			//do not handle for now
			case SELECTION:
				break;
			//functional
			case CHAR_ADD:
				if(isUndo)
				{
					this.getText().delete(next.second, next.second + next.first.length());
				}
				else
				{
					this.getText().insert(next.second, next.first);
				}
				break;
			//faulted out once, appears functional now?
			case CHAR_DELETE:
				if(isUndo)
				{
					this.getText().insert(next.second, next.first);
				}
				else
				{
					this.getText().delete(next.second, next.second + next.first.length());
				}
				break;
			default:
				break;
		}
		isAction = false;
	}
	
	public void broadcast(String type, String added)
	{
		if(myclient != null && myclient.inSession()){
			try
			{
				long participantID = myclient.currentSessionParticipantId();
				if(!broadcastJoin){
					EventJoin eventJoin = EventJoin.newBuilder().setPartID(participantID).build();
					myclient.broadcast(eventJoin.toByteArray(), "Join");
					broadcastJoin = true;
				}
				if (type == "Move") {
					EventMove eventMove = EventMove.newBuilder().setPartID(participantID).setNewLoc(lastPos).build();
					myclient.broadcast(eventMove.toByteArray(), type);
				}
				else if (type == "Add") {
					EventAdd eventAdd = EventAdd.newBuilder().setPartID(participantID).setChar(added).build();
					myclient.broadcast(eventAdd.toByteArray(), type);
				}
				else if (type == "Delete") {
					EventDel eventDel = EventDel.newBuilder().setPartID(participantID).build();
					myclient.broadcast(eventDel.toByteArray(), type);
				}
			}
			catch( CollabrifyException e ){Log.e("Tag", "error", e);}   
		}
    }
}