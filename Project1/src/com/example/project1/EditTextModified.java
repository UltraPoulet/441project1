package com.example.project1;

import com.example.project1.History.Type;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.EditText;
import android.widget.Toast;

public class EditTextModified extends EditText{
	
	public boolean changedText = false;
	private History history = new History();
	private String prev;
	private int lastPos;
	private boolean notBoot;
	private boolean isUndoRedoAction;
	
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
			changedText = false;
		}
		else if(isUndoRedoAction)
		{
			Toast.makeText(getContext(), history.strMerge(), Toast.LENGTH_SHORT).show();
		}
		else{
			Toast.makeText(getContext(), "selStart is " + selStart + "selEnd is " + selEnd, Toast.LENGTH_SHORT).show();
			history.clearRedo();
			if(selStart == selEnd)
				history.add(true, "", lastPos, Type.CURSOR_MOVE);
			//else
			//	history.add(true, Integer.toString(selStart) + "," + Integer.toString(selEnd), Type.SELECTION);
			Toast.makeText(getContext(), history.strMerge(), Toast.LENGTH_SHORT).show();
		}
		lastPos = selStart;
	}
	
	@Override
	protected void onTextChanged(CharSequence text, int start, int lengthBefore, int lengthAfter){
		//we don't add the stuff that happens on boot to the history
		if(!notBoot)
		{
			notBoot = true;
			return;
		}
		if(!isUndoRedoAction)
		{
			if(lengthBefore < lengthAfter){
				Toast.makeText(getContext(), "Added: " + text.subSequence(start, start + lengthAfter), Toast.LENGTH_SHORT).show();
				history.add(true, text.subSequence(start, start + lengthAfter).toString(), start, Type.CHAR_ADD);
			}
			else{
				String temp = prev.substring(start, start + (lengthBefore - lengthAfter));
				Toast.makeText(getContext(), "Removed " + Integer.toString(lengthBefore - lengthAfter) + " chars " + temp + " at position: " + start, Toast.LENGTH_SHORT).show();
				history.add(true, temp, start, Type.CHAR_DELETE);
			}
			history.clearRedo();
		}
		Toast.makeText(getContext(), history.strMerge(), Toast.LENGTH_SHORT).show();
		prev = text.toString();
		changedText = true;
	}
	
	/*
	//Volume key shortcut
	@Override
	public boolean onKeyDown(int keycode, KeyEvent event)
	{
		if(keycode == KeyEvent.KEYCODE_VOLUME_DOWN)
		{
			UndoRedoHandler(false);
			return true;
		}
		else if(keycode == KeyEvent.KEYCODE_VOLUME_UP)
		{
			UndoRedoHandler(true);
			return true;
		}
		else
			return super.onKeyDown(keycode, event);
	}
	*/
	
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
		isUndoRedoAction = true;
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
		isUndoRedoAction = false;
	}
}