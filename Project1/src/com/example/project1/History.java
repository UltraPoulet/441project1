package com.example.project1;

import java.util.ArrayDeque;
import java.util.Iterator;

import android.util.Log;

public class History
{
	enum Type { CURSOR_MOVE, SELECTION, CHAR_ADD, CHAR_DELETE }
	public ArrayDeque<Tuple<String, Integer, Type>> undoHistory;
	public ArrayDeque<Tuple<String, Integer, Type>> redoHistory;
	
	
	public History()
	{
		undoHistory = new ArrayDeque<Tuple<String,Integer,Type>>();
		redoHistory = new ArrayDeque<Tuple<String,Integer,Type>>();
	}
	
	public void adjustIndexes(int pos, boolean isAdd)
	{
		Iterator<Tuple<String,Integer,Type>> iter = undoHistory.iterator();
		Tuple<String,Integer,Type> curr;
		while(iter.hasNext())
		{
			curr = iter.next();
			Log.d("History", "Comparing " + curr.second + " to " + pos);
			if(isAdd)
			{
				if(curr.second >= pos)
				{
					Log.i("History", "Adjusted item: " + curr.first + "," + curr.second + " to pos " + (curr.second + 1));
					curr.second++;
				}
			}
			else
			{
				Log.d("history", String.valueOf(curr.second >= pos));
				if(curr.second >= pos)
				{
					Log.i("History", "Adjusted item: " + curr.first + "," + curr.second + " to pos " + (curr.second - 1));
					curr.second--;
				}
			}
		}
		iter = redoHistory.iterator();
		while(iter.hasNext())
		{
			curr = iter.next();
			Log.d("History", "Comparing " + curr.second + " to " + pos);
			if(isAdd)
				if(curr.second >= pos)
				{
					Log.i("History", "Adjusted item: " + curr.first + "," + curr.second + " to pos " + (curr.second + 1));
					curr.second++;
				}
			else
				if(curr.second >= pos)
				{
					Log.i("History", "Adjusted item: " + curr.first + "," + curr.second + " to pos " + (curr.second - 1));
					curr.second--;
				}
		}
	}
	
	//used by EditTextModified
	public void add(boolean isUndo, String data, int pos, Type type)
	{
		Tuple<String, Integer, Type> toAdd = new Tuple<String, Integer, Type>(data, pos, type);
		if(isUndo)
		{
			undoHistory.addFirst(toAdd);
		}
		else
		{
			redoHistory.addFirst(toAdd);
		}
	}
	
	//We're taking an action, so we add to the opposite ArrayDeque
	public void add(boolean isUndo, Tuple<String, Integer, Type> entry)
	{
		if(isUndo)
		{
			redoHistory.addFirst(entry);
		}
		else
		{
			undoHistory.addFirst(entry);
		}
	}
	
	public void clearRedo()
	{
		redoHistory.clear();
	}
	
	//for debugging history lists
	public String strMerge()
	{
		String ret = "undoHistory: ";
		Iterator<Tuple<String, Integer, Type>> itr = undoHistory.iterator();
		Tuple<String, Integer, Type> tup;
		while(itr.hasNext())
		{
			tup = itr.next();
			ret += tup.first + "," + tup.second + " ";
		}
		ret += "redoHistory: ";
		itr = redoHistory.iterator();
		while(itr.hasNext())
		{
			tup = itr.next();
			ret += tup.first + "," + tup.second+ " ";
		}
		return ret;
	}
}

