package com.example.demo.listeners;

import com.example.demo.channels.PublisherSubscriber;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Service
public class WeatherListener {
    @Autowired
    PublisherSubscriber channel;

    @KafkaListener(topics = "weather", groupId = "my-consumer-group")
    public void listen(String message) {
        System.out.println("Received message: " + message);
        try{
            channel.send(message);

        } catch (Exception e ){
            System.out.println("An Error has occurred with channeling the message: "+ e.getMessage());
        }
    }
}
