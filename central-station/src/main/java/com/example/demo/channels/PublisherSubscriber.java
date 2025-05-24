package com.example.demo.channels;

import com.example.demo.Bitcask.Bitcask;
import com.example.demo.archive.Aggregator;
import com.example.demo.archive.Archive;
import com.example.demo.archive.EventManager;
import com.example.demo.archive.Listener;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.List;

@Component
public class PublisherSubscriber {
    @Autowired
    Bitcask bitcask;
    EventManager eventManager;
    Listener archive;
    Aggregator aggregator;

    public PublisherSubscriber(){
        eventManager = new EventManager();
        archive = new Archive();
        eventManager.addListener(archive);
        aggregator = new Aggregator(eventManager);
    }
    public void send(String message) throws JSONException, IOException {
        JSONObject json = new JSONObject(message);
        Long key = Long.valueOf((Integer)json.get("station_id"));
        aggregator.send(message);
        bitcask.put(key , message.getBytes());
    }

}
