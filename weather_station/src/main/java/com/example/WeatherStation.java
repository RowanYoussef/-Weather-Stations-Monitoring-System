package com.example;

import java.time.Instant;
import java.util.Properties;
import java.util.concurrent.ThreadLocalRandom;

import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.serialization.StringSerializer;
import org.json.simple.JSONObject;

public class WeatherStation {
    public static void main(String[] args) {
        Properties properties = new Properties();
        properties.setProperty(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG,
        "kafka:9092");
        properties.setProperty(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG,
        StringSerializer.class.getName());
        properties.setProperty(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG,
        StringSerializer.class.getName());
        JSONObject message = new JSONObject();
        long s_no = 0;

        // Extract numeric ID from pod name (e.g., "weather-station-3" → 3)
        String podName = System.getenv("STATION_ID");
        int stationNumber = Integer.parseInt(podName.substring(podName.lastIndexOf("-") + 1));
        message.put("station_id", String.valueOf(stationNumber));  // Stores 0-9

        
        try (KafkaProducer<String, String> producer = new KafkaProducer<>(properties)) {
            while (true) {
                System.out.println(podName);
                long start = System.currentTimeMillis();

                String battery_status;
                int random = ThreadLocalRandom.current().nextInt(0, 100);
                if (random < 30)
                    battery_status = "LOW";
                else if (random < 70)
                    battery_status = "Medium";
                else
                    battery_status = "High";

                JSONObject weather = new JSONObject();
                weather.put("humidity", ThreadLocalRandom.current().nextInt(1, 101));
                weather.put("temperature", ThreadLocalRandom.current().nextInt(-130, 135));
                weather.put("wind_speed", ThreadLocalRandom.current().nextInt(0, 301));

                message.put("battery_status", battery_status);
                message.put("weather", weather);
                message.put("time_stamp", Instant.now().toString());
                message.put("s_no", s_no);

                ProducerRecord<String, String> record = new ProducerRecord<>("weather", message.toString());

                // simulate message drop
                if (ThreadLocalRandom.current().nextInt(0, 100) >= 10)
                    producer.send(record, (metadata, exception) -> {
                        if (exception != null) {
                            System.err.println("Failed to send message: " + exception.getMessage());
                        } else {
                            System.out.println("Sent message to " + metadata.topic() + 
                                            " partition " + metadata.partition() + 
                                            " offset " + metadata.offset());
                        }
                    });

                s_no++;
                if (s_no == Long.MAX_VALUE)
                    s_no = 0;

                System.out.println(message.toString());
                System.out.println();

                long finish = System.currentTimeMillis();
                long timeElapsed = finish - start;
                Thread.sleep(Math.max(0, 1000 - timeElapsed));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}