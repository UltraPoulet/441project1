package com.example.project1;

public class Event{
    public int subId;
    public String eventType; 
    public String deleted; 
    public int location; 
    public boolean isLocal;
    public boolean skip;
    public Event(int id, String type, String deletedChar, int loc, boolean isLocal, boolean skip) { 
        this.subId = id; 
        this.eventType = type; 
        this.deleted = deletedChar;
        this.location = loc;
        this.isLocal = isLocal;
        this.skip = skip;
    } 
} 