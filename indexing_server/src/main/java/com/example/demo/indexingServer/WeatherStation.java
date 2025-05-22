package com.example.demo.indexingServer;

public class WeatherStation {
    public long station_id;
    public long s_no;
    public String battery_status;
    public long status_timestamp;
    public Weather weather;

    public static class Weather {
        public int humidity;
        public int temperature;
        public int wind_speed;
    }
}
