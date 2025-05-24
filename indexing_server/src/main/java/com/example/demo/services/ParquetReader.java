package com.example.demo.services;

import com.example.demo.models.WeatherStationStatus;
import org.apache.avro.generic.GenericRecord;
import org.apache.hadoop.fs.Path;
import org.apache.parquet.avro.AvroParquetReader;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Service
public class ParquetReader {
    public List<WeatherStationStatus> readParquet(File parquetFile) throws IOException {
        List<WeatherStationStatus> result = new ArrayList<>();
        Path path = new Path(parquetFile.getAbsolutePath());

        try (org.apache.parquet.hadoop.ParquetReader<GenericRecord> reader = AvroParquetReader.<GenericRecord>builder(path).build()) {
            GenericRecord record;
            while ((record = reader.read()) != null) {
                WeatherStationStatus status = new WeatherStationStatus();
                status.station_id = (Long) record.get("station_id");
                status.s_no = (Long) record.get("s_no");
                status.battery_status = record.get("battery_status").toString();
                status.status_timestamp = (Long) record.get("status_timestamp");

                WeatherStationStatus.Weather weather = new WeatherStationStatus.Weather();
                weather.humidity = (Integer) record.get("humidity");
                weather.temperature = (Integer) record.get("temperature");
                weather.wind_speed = (Integer) record.get("wind_speed");
                status.weather = weather;

                result.add(status);
            }
        }

        return result;
    }
}
