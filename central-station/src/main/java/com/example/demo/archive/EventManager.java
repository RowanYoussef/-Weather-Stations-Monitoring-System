package com.example.demo.archive;

import java.util.LinkedList;
import java.util.List;

public class EventManager {
    private final List<Listener> listeners;
    EventManager(){
        listeners = new LinkedList<>();
    }
    public void addListener(Listener l){
        listeners.add(l);
    }
    public void removeListener(Listener l){
        for(Listener listener : listeners){
            if (listener.equals(l)) listeners.remove(l);
        }
    }
}
