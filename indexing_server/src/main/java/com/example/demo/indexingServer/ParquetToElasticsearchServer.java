//package com.example.demo.indexingServer;
//
//import java.io.File;
//import java.io.IOException;
//import java.util.ArrayList;
//import java.util.List;
//import java.util.Map;
//
//import com.example.demo.models.WeatherStationStatus;
//import org.apache.hadoop.fs.Path;
//import org.apache.parquet.avro.AvroParquetReader;
//import org.apache.avro.generic.GenericRecord;
//
//import co.elastic.clients.elasticsearch.ElasticsearchClient;
//import co.elastic.clients.elasticsearch.core.IndexRequest;
//import co.elastic.clients.elasticsearch.core.IndexResponse;
//import co.elastic.clients.transport.rest_client.RestClientTransport;
//import co.elastic.clients.json.jackson.JacksonJsonpMapper;
//
//import org.apache.http.HttpHost;
//import org.apache.parquet.hadoop.ParquetReader;
//import org.elasticsearch.client.RestClient;
//
//import com.fasterxml.jackson.databind.ObjectMapper;
//
//
//public class ParquetToElasticsearchServer {
//
//    public static void main(String[] args) throws IOException {
//        System.out.println("Starting indexing...");
//
//        ParquetToElasticsearchServer server = new ParquetToElasticsearchServer();
//
//        File root = new File("/parquet");
//        if (!root.exists()) {
//            System.err.println("Directory does not exist: " + root.getAbsolutePath());
//            return;
//        }
//
//        List<File> parquetFiles = server.findParquetFiles(root);
//
//        if (parquetFiles.isEmpty()) {
//            System.out.println("No parquet files found in " + root.getAbsolutePath());
//        }
//
//        for (File file : parquetFiles) {
//            System.out.println("Processing file: " + file.getAbsolutePath());
//            List<WeatherStationStatus> statuses = server.readParquet(file);
//            server.indexToElasticsearch(statuses);
//            System.out.println("Indexed file: " + file.getPath() + " with " + statuses.size() + " records.");
//        }
//
//        System.out.println("Finished indexing.");
//    }
//
//    public void indexToElasticsearch(List<WeatherStationStatus> statuses) throws IOException {
//        RestClient restClient = RestClient.builder(new HttpHost("localhost", 9200)).build();
//
//        RestClientTransport transport = new RestClientTransport(restClient, new JacksonJsonpMapper());
//
//        ElasticsearchClient client = new ElasticsearchClient(transport);
//
//        ObjectMapper mapper = new ObjectMapper();
//
//        for (WeatherStationStatus status : statuses) {
//            Map<String, Object> jsonMap = mapper.convertValue(status, Map.class);
//
//            IndexRequest<Map<String, Object>> request = IndexRequest.of(i -> i
//                    .index("weather-statuses")
//                    .document(jsonMap)
//            );
//
//            IndexResponse response = client.index(request);
//            System.out.println("Indexed document with id: " + response.id());
//        }
//
//        restClient.close();
//    }
//
//
//    public List<WeatherStationStatus> readParquet(File parquetFile) throws IOException {
//        List<WeatherStationStatus> result = new ArrayList<>();
//        Path path = new Path(parquetFile.getAbsolutePath());
//        try (ParquetReader<GenericRecord> reader = AvroParquetReader.<GenericRecord>builder(path).build();
//        ) {
//            GenericRecord record;
//            while ((record = reader.read()) != null) {
//                System.out.println(record);
//                WeatherStationStatus status = new WeatherStationStatus();
//                status.station_id = (Long) record.get("station_id");
//                status.s_no = (Long) record.get("s_no");
//                status.battery_status = record.get("battery_status").toString();
//                status.status_timestamp = (Long) record.get("status_timestamp");
//
//                WeatherStationStatus.Weather weather = new WeatherStationStatus.Weather();
//                weather.humidity = (Integer) record.get("humidity");
//                weather.temperature = (Integer) record.get("temperature");
//                weather.wind_speed = (Integer) record.get("wind_speed");
//                status.weather = weather;
//
//                result.add(status);
//            }
//
//        }
//        return result;
//    }
//
//    public List<File> findParquetFiles(File root) {
//        List<File> files = new ArrayList<>();
//        File[] entries = root.listFiles();
//
//        if (entries != null) {
//            for (File file : entries) {
//                if (file.isDirectory()) {
//                    files.addAll(findParquetFiles(file));
//                } else if (file.getName().endsWith(".parquet")) {
//                    files.add(file);
//                }
//            }
//        }
//        return files;
//    }
//}
