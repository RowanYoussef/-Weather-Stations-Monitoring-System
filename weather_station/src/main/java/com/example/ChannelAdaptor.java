package com.example;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

public class ChannelAdaptor {
    private final HttpClient client;
    private final HttpRequest request;
    private String lastTime = "";

    public ChannelAdaptor(double latitude, double longitude) {
        client = HttpClient.newHttpClient();
        String url = "https://api.open-meteo.com/v1/forecast?latitude=" + latitude +
                     "&longitude=" + longitude +
                     "&current=temperature_2m,wind_speed_10m,relative_humidity_2m";
        request = HttpRequest.newBuilder().uri(URI.create(url)).build();
    }

    public JSONObject fetchWeather() {
        try {
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() == 200) {
                JSONParser parser = new JSONParser();
                JSONObject jsonResponse = (JSONObject) parser.parse(response.body());
                JSONObject current = (JSONObject) jsonResponse.get("current");
                System.out.println("OPEN METEO DATA" + jsonResponse);
                String time = (String) current.get("time");
                if (!time.equals(lastTime)) {
                    lastTime = time;
                    JSONObject weather = new JSONObject();
                    weather.put("humidity", current.get("relative_humidity_2m"));
                    int degreesF = (int) ((double) current.get("temperature_2m") * 9 / 5) + 32;
                    weather.put("temperature", degreesF);
                    weather.put("wind_speed", current.get("wind_speed_10m"));
                    return weather;
                }
            } else {
                System.out.println("Request failed with status: " + response.statusCode());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
}