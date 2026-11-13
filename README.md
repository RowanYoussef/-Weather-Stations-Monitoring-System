# -Weather-Stations-Monitoring-System

![image](https://github.com/user-attachments/assets/3f77f66b-97cf-44c1-8bd8-6381a5a16e3e)
# Kibana dashboard
<img width="602" height="176" alt="image" src="https://github.com/user-attachments/assets/84e3be1f-86dd-48ad-bc23-ca0bf17c60a9" />


# Kibana visualization showing the percentage of dropped messages
- The blue section represents received messages .
- The green section represents dropped messages.

<img width="566" height="302" alt="image" src="https://github.com/user-attachments/assets/57eeccfa-28ad-41f2-a121-8ec27c9a82a0" />

# Visualization of battery status distribution data using Kibana
- 30% low
- 30% high
- 40% medium
<img width="669" height="337" alt="image" src="https://github.com/user-attachments/assets/4b65c2b0-0d57-4558-b0a7-dad64832416a" />

# How the System Works
## The project can be divided into several main components:
1. Weather Stations: Java-based applications that simulate multiple weather stations. Each station periodically generates weather readings, including temperature, humidity, and battery status. The readings are sent to the central system.
2. Central Station: A Spring Boot application responsible for receiving and processing incoming weather data from different stations.
3. Apache Kafka: Acts as a messaging system that allows components to communicate asynchronously. It helps decouple data producers from consumers.
4. Rain Processors: Separate Kafka consumers that process rain-related events and generate alerts when certain conditions are met.
5. Custom Bitcask Storage: I implemented a lightweight key-value storage engine inspired by Bitcask. It uses log-based storage and hint files to improve data retrieval, with compaction to manage storage efficiently.
6. Elasticsearch & Kibana: Elasticsearch indexes weather data for searching and analysis, while Kibana provides dashboards to visualize weather information, battery levels, and dropped messages.
7. Parquet & Open-Meteo: Weather data can also be stored in partitioned Parquet files organized by station and time. The project also integrates external weather data through an Open-Meteo channel adapter.
8. Docker & Kubernetes: Used to containerize and deploy the system components.
