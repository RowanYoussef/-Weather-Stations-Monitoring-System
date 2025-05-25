package com.example;

import java.time.Instant;
import java.util.Properties;
import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;

import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.serialization.StringSerializer;
import org.json.simple.JSONObject;

public class WeatherStation {

    public static double[] generateRandomCoordinates() {
        Random random = new Random();
        double latitude = -90 + 180 * random.nextDouble();
        double longitude = -180 + 360 * random.nextDouble();
        return new double[]{latitude, longitude};
    }

    public static void main(String[] args) {
        Properties properties = new Properties();
        properties.setProperty(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, "kafka:9092");
        properties.setProperty(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
        properties.setProperty(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());

        double[] coordinates = generateRandomCoordinates();
        System.out.printf("Station Location: Latitude: %.6f, Longitude: %.6f%n", coordinates[0], coordinates[1]);
        double latitude = coordinates[0], longitude = coordinates[1];

        ChannelAdaptor adaptor = new ChannelAdaptor(latitude, longitude);

        JSONObject message = new JSONObject();
        long s_no = 0;

        String podName = System.getenv("STATION_ID");
        int stationNumber = Integer.parseInt(podName.substring(podName.lastIndexOf("-") + 1));
        message.put("station_id", Long.valueOf(stationNumber));
        System.out.println(stationNumber);

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
                weather.put("humidity", ThreadLocalRandom.current().nextInt(0, 101));
                weather.put("temperature", ThreadLocalRandom.current().nextInt(-130, 136));
                weather.put("wind_speed", ThreadLocalRandom.current().nextInt(0, 301));

                if (s_no % 900 == 0) {
                    JSONObject updatedWeather = adaptor.fetchWeather();
                    if (updatedWeather != null) {
                        weather = updatedWeather;
                        System.out.println("Updated weather from API: " + weather.toJSONString());
                    }
                }

                message.put("battery_status", battery_status);
                message.put("weather", weather);
                message.put("status_timestamp", Instant.now().getEpochSecond());
                message.put("s_no", s_no);

                ProducerRecord<String, String> record = new ProducerRecord<>("weather", message.toString());

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