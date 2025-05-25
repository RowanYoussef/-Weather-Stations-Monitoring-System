package com.example;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Instant;
import java.util.Properties;
import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;

import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.serialization.StringSerializer;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

public class WeatherStation {

    public static double[] generateRandomCoordinates() {
        Random random = new Random();
        
        // Latitude ranges from -90 to 90
        double latitude = -90 + 180 * random.nextDouble();
        
        // Longitude ranges from -180 to 180
        double longitude = -180 + 360 * random.nextDouble();
        
        return new double[]{latitude, longitude};
    }

    public static void main(String[] args) {
        Properties properties = new Properties();
        properties.setProperty(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, "kafka:9092");
        properties.setProperty(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
        properties.setProperty(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());

        double[] coordinates = generateRandomCoordinates();
    
        System.out.printf("Station Location: Latitude: %.6f, Longitude: %.6f%n", 
                             coordinates[0], coordinates[1]);
        double latitude = coordinates[0], longitude = coordinates[1];

        HttpClient client = HttpClient.newHttpClient();
        String url = "https://api.open-meteo.com/v1/forecast?latitude=" + latitude +
             "&longitude=" + longitude +
             "&current=temperature_2m,wind_speed_10m,relative_humidity_2m";
        HttpRequest request = HttpRequest.newBuilder()
            .uri(URI.create(url))
            .build();

        JSONObject message = new JSONObject();
        long s_no = 0;
        String lastTime = "";
        // Extract numeric ID from pod name (e.g., "weather-station-3" → 3)
        String podName = System.getenv("STATION_ID");
        int stationNumber = Integer.parseInt(podName.substring(podName.lastIndexOf("-") + 1));
        message.put("station_id", Long.valueOf(stationNumber));  // store as Long number

        try (KafkaProducer<String, String> producer = new KafkaProducer<>(properties)) {
            while (true) {
                long start = System.currentTimeMillis();

                String battery_status;
                int random = ThreadLocalRandom.current().nextInt(0, 100);
                if (random < 30)
                    battery_status = "low";
                else if (random < 70)
                    battery_status = "medium";
                else
                    battery_status = "high";

                JSONObject weather = new JSONObject();

                weather.put("humidity", ThreadLocalRandom.current().nextInt(0, 101));      // 0-100%
                weather.put("temperature", ThreadLocalRandom.current().nextInt(-130, 136)); // fahrenheit range
                weather.put("wind_speed", ThreadLocalRandom.current().nextInt(0, 301));    // km/h

                if(s_no % 900 == 0)
                    try {
                        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
                        
                        if (response.statusCode() == 200) {
                            // Parse the JSON response
                            JSONParser parser = new JSONParser();
                            JSONObject jsonResponse = (JSONObject) parser.parse(response.body());
                            
                            // Access data from JSON
                            System.out.println("OPEN METEO JSON: " + jsonResponse);
                            System.out.println("weather field: " + jsonResponse.get("current"));
                            JSONObject newWeather = (JSONObject) jsonResponse.get("current");
                            
                            if(newWeather.get("time") != lastTime) {
                                lastTime = (String) newWeather.get("time");
                                System.out.println("updated time: " + lastTime);
                                weather.put("humidity", newWeather.get("relative_humidity_2m"));
                                int degreesInCelsius = (int) ((double) newWeather.get("temperature_2m") * 9 / 5) + 32;
                                weather.put("temperature", degreesInCelsius);    // degrees in fahrenheight
                                weather.put("wind_speed", newWeather.get("wind_speed_10m"));
                            }
                            System.out.println("Created JSON: " + weather.toJSONString());
                        } else {
                            System.out.println("Request failed with status: " + response.statusCode());
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }

                message.put("battery_status", battery_status);
                message.put("weather", weather);
                message.put("status_timestamp", Instant.now().getEpochSecond()); // unix epoch seconds
                message.put("s_no", s_no);

                ProducerRecord<String, String> record = new ProducerRecord<>("weather", message.toString());

                // simulate message drop with 10% chance
                if (ThreadLocalRandom.current().nextInt(0, 100) >= 10) {
                    producer.send(record, (metadata, exception) -> {
                        if (exception != null) {
                            System.err.println("Failed to send message: " + exception.getMessage());
                        } else {
                            System.out.println("Sent message to " + metadata.topic() +
                                    " partition " + metadata.partition() +
                                    " offset " + metadata.offset());
                        }
                    });
                }

                s_no++;
                if (s_no == Long.MAX_VALUE)
                    s_no = 0;

                System.out.println(message.toJSONString());
                System.out.println();

                long finish = System.currentTimeMillis();
                long timeElapsed = finish - start;
                try {
                    Thread.sleep(Math.max(0, 1000 - timeElapsed));
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
