package com.example.demo.controllers;

import com.example.demo.channels.PublisherSubscriber;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.Map;

@CrossOrigin()
@RequestMapping("/publish")
@RestController
public class publishAPI {
    @Autowired
    PublisherSubscriber publisherSubscriber;
    @PostMapping("/put")
    public ResponseEntity<?> putEntry(@RequestBody String message) throws JSONException, IOException {
        publisherSubscriber.send(message);
        return new ResponseEntity<>(Map.of("Message", "Message was casked successfully!"), HttpStatus.OK);
    }
}
