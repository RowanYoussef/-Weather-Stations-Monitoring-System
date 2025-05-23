package com.example.demo.services;

import com.example.demo.models.WeatherStationStatus;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.WatchKey;
import java.nio.file.WatchService;
import java.nio.file.*;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static java.nio.file.StandardWatchEventKinds.ENTRY_CREATE;

@Service
public class ParquetWatcher {

    @Value("${parquet.watch-dir}")
    private String rootDirPath;

    private final ParquetReader etlService;
    private final WeatherStationIndexer indexer;

    public ParquetWatcher(ParquetReader etlService, WeatherStationIndexer indexer) {
        this.etlService = etlService;
        this.indexer = indexer;
    }

    @PostConstruct
    @Async
    public void startWatching() throws IOException {
        Path rootDir = Paths.get(rootDirPath);
        WatchService watchService = FileSystems.getDefault().newWatchService();
        Map<WatchKey, Path> keyMap = new HashMap<>();
        registerAll(rootDir, watchService, keyMap);

        while (true) {
            WatchKey key;
            try {
                key = watchService.take();
            } catch (InterruptedException e) {
                return;
            }

            Path dir = keyMap.get(key);
            if (dir == null) continue;

            for (WatchEvent<?> event : key.pollEvents()) {
                WatchEvent.Kind<?> kind = event.kind();

                @SuppressWarnings("unchecked")
                WatchEvent<Path> ev = (WatchEvent<Path>) event;
                Path child = dir.resolve(ev.context());

                if (kind == ENTRY_CREATE) {
                    if (Files.isDirectory(child)) {
                        registerAll(child, watchService, keyMap);
                    } else if (child.toString().endsWith(".parquet")) {
                        try {
                            List<WeatherStationStatus> records = etlService.readParquet(child.toFile());
                            indexer.index(records);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                }
            }

            boolean valid = key.reset();
            if (!valid) keyMap.remove(key);
        }
    }

    private void registerAll(Path start, WatchService watchService, Map<WatchKey, Path> keyMap) throws IOException {
        Files.walk(start)
                .filter(Files::isDirectory)
                .forEach(path -> {
                    try {
                        WatchKey key = path.register(watchService, ENTRY_CREATE);
                        keyMap.put(key, path);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                });
    }
}