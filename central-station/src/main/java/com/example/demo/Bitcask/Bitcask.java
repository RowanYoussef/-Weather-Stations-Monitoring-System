package com.example.demo.Bitcask;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class Bitcask implements  BitcaskI{
    private final String baseDataDir = "bin/";
    private final String baseHintDir = "hint/";
    private HashMap<Long , CaskItem>  map = new HashMap<>();;
    private String currentFileNo;
    private RandomAccessFile currentFile;
    private long currentOffset;
    private final int maxFileSize = 1024;
    private int fileCounter = 0;
    public Bitcask () throws IOException{
        recoverFromHintFiles();
        partitionLogs();
        cleanupSchedule();
    }

    private void cleanupSchedule() {
        Executors.newSingleThreadScheduledExecutor().scheduleAtFixedRate(()->{
            try{
                compactFiles();
            }catch(Exception ignored){}
        }, 1, 5, TimeUnit.MINUTES);
    }

    private void compactFiles() {
    }

    private void partitionLogs() throws IOException {
        if (currentFile == null || currentFile.length() > maxFileSize){
            if (currentFile != null) currentFile.close();
            currentFileNo = String.format("%08d.data", ++fileCounter);
            currentFile = new RandomAccessFile(baseDataDir+currentFileNo, "rw");
            currentOffset = 0;
        }
    }

    private void recoverFromHintFiles() throws IOException {
        File hintDir = new File(baseHintDir);
        File[] hints = hintDir.listFiles((file, name) -> name.endsWith(".hint"));
        if (hints == null) return;
        for (File hint: hints){
            DataInputStream input = new DataInputStream(new FileInputStream(hint));
            while (input.available() > 0){
                long key = input.readLong();
                int valLength = input.readInt();
                long offset = input.readLong();
                String fileName = hint.getName().replace(".hint", ".data");
                map.put(key, new CaskItem(fileName, offset, valLength));
            }
        }
    }

    @Override
    public void put(long key, byte[] value) {

    }

    @Override
    public byte[] get(long key) {
        return new byte[0];
    }


}
