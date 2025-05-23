package com.example.demo.services;
import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch.core.IndexRequest;
import co.elastic.clients.elasticsearch.core.IndexResponse;
import com.example.demo.models.WeatherStationStatus;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.List;
import java.util.Map;

@Service
public class WeatherStationIndexer {

    private final ElasticsearchClient client;
    private final ObjectMapper mapper = new ObjectMapper();

    public WeatherStationIndexer(ElasticsearchClient client) {
        this.client = client;
    }

    public void index(List<WeatherStationStatus> statuses) throws IOException {
        for (WeatherStationStatus status : statuses) {
            Map<String, Object> jsonMap = mapper.convertValue(status, Map.class);

            IndexRequest<Map<String, Object>> request = IndexRequest.of(i -> i
                    .index("weather-statuses")
                    .document(jsonMap)
            );

            IndexResponse response = client.index(request);
            System.out.println("Indexed document with id: " + response.id());
        }
    }
}
