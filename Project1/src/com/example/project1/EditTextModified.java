package com.example.project1;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.ListIterator;

import com.example.project1.EventProtocols.EventAdd;
import com.example.project1.EventProtocols.EventDel;
import com.example.project1.EventProtocols.EventJoin;
import com.example.project1.EventProtocols.EventMove;
import com.example.project1.History.Type;

import edu.umich.imlc.collabrify.client.CollabrifyClient;
import edu.umich.imlc.collabrify.client.exceptions.CollabrifyException;

import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.widget.EditText;

//look at broadcasting undos...moving the cursor to the position it was at is messing things up
//when it's the last character you're undoing

public class EditTextModified extends EditText{
	
	public boolean changedText = false;
	public History history = new History();
	private String prev;
	private int lastPos;
	private boolean notBoot;
	public boolean isAction;
	public CollabrifyClient myclient = null;
	//subId, eventType, char deleted, location, bool
	public ArrayList<Event> allEvents = new ArrayList<Event>();
	public boolean subActivity = false;
	public ArrayDeque<Integer> longActivity = new ArrayDeque<Integer>();
	private String Tag = "WeWrite ETM";
	private boolean sendMove = false;
	public Event leastRecent = null;
	
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
			Log.d(Tag, "textChanged or booting");
			changedText = false;
		}
		else if(sendMove)
		{
			//TODO:not sending our own move to our cursorLocs
			Log.d(Tag, "Position: " + selStart);
			broadcast("Move", "", selStart);
			sendMove = false;
		}
		lastPos = selStart;
	}
	
	@Override
	public boolean onTouchEvent(MotionEvent event) {
		if(event.getAction() == MotionEvent.ACTION_UP)
		{
			Log.d(Tag, "User tap screen");
			sendMove = true;
		}
		return super.onTouchEvent(event);
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
			Log.d(Tag, "Before lengthBefore < lengthAfter");
			if(lengthBefore < lengthAfter){
				////Toast.makeText(getContext(), "Added: " + text.subSequence(start, start + lengthAfter), //Toast.LENGTH_SHORT).show();
				history.clearRedo();
				history.add(true, text.subSequence(start, start + lengthAfter).toString(), start, Type.CHAR_ADD);
				try {
					broadcast("Add", text.subSequence(start, start + lengthAfter).toString(), start);
					Log.d(Tag, "Hey, we broadcasted an add");
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
					broadcast("Delete", temp, start);
					Log.d(Tag, "Hey, we broadcasted a delete");
				}
				catch(Exception e) {
					//non issue, we're in main
				}
			}
			history.clearRedo();
		}
		Log.d(Tag, history.strMerge());
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
					broadcast("Move","", next.second+1);
					broadcast("Delete",next.first,next.second);
					broadcast("Move","",lastPos);
					this.getText().delete(next.second, next.second + next.first.length());
				}
				else
				{
					broadcast("Move","",next.second);
					broadcast("Add",next.first,next.second);
					broadcast("Move","",lastPos);
					this.getText().insert(next.second, next.first);
				}
				break;
			//faulted out once, appears functional now?
			case CHAR_DELETE:
				if(isUndo)
				{
					broadcast("Move","",next.second+1);
					broadcast("Add",next.first,next.second);
					broadcast("Move","",lastPos);
					this.getText().insert(next.second, next.first);
				}
				else
				{
					broadcast("Move","", next.second);
					broadcast("Delete",next.first,next.second);
					broadcast("Move","",lastPos);
					this.getText().delete(next.second, next.second + next.first.length());
				}
				break;
			default:
				break;
		}
		isAction = false;
	}
	
	//may need to run this on ui thread. The issue is that we're hitting a concurrency exception
	//this should now only trigger if user taps while we're updating.
	public void broadcast(String type, String added, int pos)
	{
		if(myclient != null && myclient.inSession()){
			try
			{
				long participantID = myclient.currentSessionParticipantId();
				if (type == "Move") {
					EventMove eventMove = EventMove.newBuilder().setPartID(participantID).setNewLoc(pos).build();
					int sub = myclient.broadcast(eventMove.toByteArray(), type);
					Log.d(Tag, "Added Move to locals");
					if(leastRecent == null)
					{
						Log.d(Tag, "leastRecent set!");
						leastRecent = new Event(sub, type, "", pos, true, true);
						allEvents.add(leastRecent);
					}
					else
					{
						Log.d(Tag, "Least recent not null: " + leastRecent.eventType + " " + leastRecent.location + " " + leastRecent.deleted);
						allEvents.add(new Event(sub, type, "", pos, true, false));
					}
				}
				else if (type == "Add") {
					EventAdd eventAdd = EventAdd.newBuilder().setPartID(participantID).setChar(added).build();
					int sub = myclient.broadcast(eventAdd.toByteArray(), type);
					Log.d(Tag, "Added Add to locals at " + pos);
					if (isAction){
						longActivity.add(sub);
					}
					if(leastRecent == null)
					{
						Log.d(Tag, "leastRecent set!");
						leastRecent = new Event(sub, type, added, pos, true, true);
						allEvents.add(leastRecent);
					}
					else
					{
						Log.d(Tag, "Least recent not null: " + leastRecent.eventType + " " + leastRecent.location + " " + leastRecent.deleted);
						allEvents.add(new Event(sub, type, added, pos, true, false));
					}
				}
				else if (type == "Delete") {
					EventDel eventDel = EventDel.newBuilder().setPartID(participantID).build();
					int sub = myclient.broadcast(eventDel.toByteArray(), type);
					Log.d(Tag, "Added Delete to locals at " + pos);
					if(isAction){
						longActivity.add(sub);
					}
					if(leastRecent == null)
					{
						Log.d(Tag, "leastRecent set!");
						leastRecent = new Event(sub, type, added, pos, true, true);
						allEvents.add(leastRecent);
					}
					else
					{
						Log.d(Tag, "Least recent not null: " + leastRecent.eventType + " " + leastRecent.location + " " + leastRecent.deleted);
						allEvents.add(new Event(sub, type, added, pos, true, false));
					}
				}
			}
			catch( CollabrifyException e ){Log.e(Tag, "error", e);}   
		}
    }
}