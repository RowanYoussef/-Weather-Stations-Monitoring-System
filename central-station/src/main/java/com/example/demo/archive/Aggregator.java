package com.example.demo.archive;

import java.util.ArrayList;
import java.util.List;

public class Aggregator {
    EventManager event;
    List<String> messageBuffer;
    public Aggregator(EventManager eventManager){
        this.event = eventManager;
        messageBuffer = new ArrayList<>();
    }

    public void send(String message){
        messageBuffer.add(message);
        if(messageBuffer.size() >= 10000) {
            event.notifyListeners(messageBuffer);
            messageBuffer.clear();
        }
    }
}
