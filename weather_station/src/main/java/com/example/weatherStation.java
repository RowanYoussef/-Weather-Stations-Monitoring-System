package com.example;

import java.time.Instant;
import java.util.Properties;
import java.util.concurrent.ThreadLocalRandom;

import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.serialization.StringSerializer;
import org.json.simple.JSONObject;

public class weatherStation {
    public static void main(String[] args) {
        Properties properties = new Properties();
        properties.setProperty(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG,
        "127.0.0.1:9092");
        properties.setProperty(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG,
        StringSerializer.class.getName());
        properties.setProperty(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG,
        StringSerializer.class.getName());
        JSONObject message = new JSONObject();
        long s_no = 0;
        message.put("station_id", 1);
        while(true) {
            try (KafkaProducer<String,String> producer = new KafkaProducer<>(properties)) {

                long start = System.currentTimeMillis();

                // simulates the variations in battery status
                String battery_status = "Low";
                int random = ThreadLocalRandom.current().nextInt(0, 100);
                if(random < 30)
                    battery_status = "LOW";

                else if(random < 70)
                    battery_status = "Medium";

                else
                    battery_status = "High";
                
                JSONObject weather = new JSONObject();

                // random weather data
                weather.put("humidity", ThreadLocalRandom.current().nextInt(1, 100 + 1));
                weather.put("temperature", ThreadLocalRandom.current().nextInt(-130, 134 + 1));
                weather.put("wind_speed", ThreadLocalRandom.current().nextInt(0, 300 + 1));

                message.put("weather", weather);
                message.put("time_stamp", Instant.now());
                message.put("s_no", s_no);

                ProducerRecord<String, String> record = new ProducerRecord<>("weather",
                message.toString());

                // simulates the message dropping
                if(ThreadLocalRandom.current().nextInt(0, 100) >= 10)
                    producer.send(record);
                s_no++;

                // reset the s_no when it reaches the maximum long value
                if(s_no == Long.MAX_VALUE)
                    s_no = 0;
                
                System.out.println(message.toString());
                System.out.println();
                System.out.println();
                System.out.println();
                long finish = System.currentTimeMillis();
                long timeElapsed = finish - start;
                try {
                    Thread.sleep(1000 - timeElapsed);
                } catch (InterruptedException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            }
        }
    }
}