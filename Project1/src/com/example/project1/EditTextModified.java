package com.example.project1;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.EditText;
import android.widget.Toast;

public class EditTextModified extends EditText{
	
	public boolean addedText = false;
	
	public EditTextModified(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
	}

	public EditTextModified(Context context, AttributeSet attrs) {
	    super(context, attrs);
	}

	public EditTextModified(Context context) {
	    super(context);
	}

	@Override
	protected void onSelectionChanged(int selStart, int selEnd){
		if(addedText){
			addedText = false;
		}
		else{
			Toast.makeText(getContext(), "selStart is " + selStart + "selEnd is " + selEnd, Toast.LENGTH_SHORT).show();
		}
	}
	
	@Override
	protected void onTextChanged(CharSequence text, int start, int lengthBefore, int lengthAfter){
		if(lengthBefore < lengthAfter){
			Toast.makeText(getContext(), "Added: " + text.subSequence(start, start + lengthAfter), Toast.LENGTH_SHORT).show();
		}
		else{
			Toast.makeText(getContext(), "Removed " + Integer.toString(lengthBefore - lengthAfter) + "chars at position: " + start, Toast.LENGTH_SHORT).show();
		}
		addedText = true;
	}
}