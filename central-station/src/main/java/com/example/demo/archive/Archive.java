package com.example.demo.archive;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.avro.Schema;
import org.apache.avro.generic.GenericData;
import org.apache.avro.generic.GenericRecord;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.Path;
import org.apache.parquet.avro.AvroParquetWriter;
import org.apache.parquet.hadoop.ParquetWriter;
import org.apache.parquet.hadoop.metadata.CompressionCodecName;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;


public class Archive implements  Listener{
    private static final Schema schema = loadSchema();
    private static final ObjectMapper objectMapper = new ObjectMapper();
    private static final DateTimeFormatter dateFormatter =
            DateTimeFormatter.ofPattern("yyyy-MM-dd").withZone(ZoneId.of("UTC"));
    InvalidMessageHandler invalidMessageHandler = new InvalidMessageHandler("invalid_messages.log");
    public Archive(){
        new File("archive/").mkdirs();
    }
    @Override
    public void update(List<String> buffer) {
        Map<String, List<GenericRecord>> groupedRecords = new HashMap<>();

        for (String json : buffer) {
            try {
                JsonNode node = objectMapper.readTree(json);

                long stationId = node.get("station_id").asLong();
                long timestamp = node.get("status_timestamp").asLong();
                String date = dateFormatter.format(Instant.ofEpochSecond(timestamp));
                String partitionKey = String.format("station_id=%d/date=%s", stationId, date);

                GenericRecord genericRecord = new GenericData.Record(schema);
                genericRecord.put("station_id", stationId);
                genericRecord.put("s_no", node.get("s_no").asLong());
                genericRecord.put("battery_status", node.get("battery_status").asText());
                genericRecord.put("status_timestamp", timestamp);

                JsonNode weather = node.get("weather");
                genericRecord.put("humidity", weather.get("humidity").asInt());
                genericRecord.put("temperature", weather.get("temperature").asInt());
                genericRecord.put("wind_speed", weather.get("wind_speed").asInt());

                groupedRecords.computeIfAbsent(partitionKey, k -> new ArrayList<>()).add(genericRecord);

            } catch (Exception e) {
               invalidMessageHandler.handle(json);
            }
        }

        for (Map.Entry<String, List<GenericRecord>> entry : groupedRecords.entrySet()) {
            String key = entry.getKey();
            List<GenericRecord> records = entry.getValue();
            String timeSuffix = String.valueOf(Instant.now().getEpochSecond());
            String outputPath = "/parquet/" + key + "/statuses_" + timeSuffix + ".parquet";
            writeParquetFile(outputPath, records);
        }
    }

    private void writeParquetFile(String pathStr, List<GenericRecord> records) {
        Path path = new Path(pathStr);
        Configuration conf = new Configuration();

        try (ParquetWriter<GenericRecord> writer = AvroParquetWriter.<GenericRecord>builder(path)
                .withSchema(schema)
                .withConf(conf)
                .withCompressionCodec(CompressionCodecName.SNAPPY)
                .withPageSize(1024 * 1024)
                .withRowGroupSize(128 * 1024 * 1024)
                .build()) {

            for (GenericRecord record : records) {
                writer.write(record);
            }

        } catch (Exception e) {
            System.err.println("Failed to write Parquet file: " + pathStr);
            e.printStackTrace();
        }
    }

    private static Schema loadSchema() {
        try (InputStream in = Archive.class.getClassLoader().getResourceAsStream("avro/schema.avsc")) {
            if (in == null) {
                throw new RuntimeException("Schema file not found");
            }
            String schemaStr = new BufferedReader(new InputStreamReader(in))
                    .lines()
                    .collect(Collectors.joining("\n"));
            return new Schema.Parser().parse(schemaStr);
        } catch (Exception e) {
            throw new RuntimeException("Failed to load schema", e);
        }
    }

}
