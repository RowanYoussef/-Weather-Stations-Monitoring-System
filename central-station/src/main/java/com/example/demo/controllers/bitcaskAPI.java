package com.example.demo.controllers;
import com.example.demo.Bitcask.DataItem;
import org.json.JSONException;
import org.json.JSONObject;

import com.example.demo.Bitcask.Bitcask;
import com.example.demo.Bitcask.BitcaskI;
import netscape.javascript.JSObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;

@CrossOrigin()
@RequestMapping("/bitcask")
@RestController
public class bitcaskAPI {
    @Autowired
    BitcaskI cask;
    @GetMapping("/{id}")
    public ResponseEntity<?> getEntry(@PathVariable("id") String stationId) throws IOException, JSONException {
        long station = Long.parseLong(stationId);
        byte[] pureData = cask.get(station);
        String message = new String(pureData, StandardCharsets.UTF_8);
        return new ResponseEntity<>(message, HttpStatus.OK);
    }
    @GetMapping("/")
    public ResponseEntity<?> getAllEntries() throws IOException {
        Map<Long, String> m = cask.getAll();
        return new ResponseEntity<>(m, HttpStatus.OK);
    }

}
