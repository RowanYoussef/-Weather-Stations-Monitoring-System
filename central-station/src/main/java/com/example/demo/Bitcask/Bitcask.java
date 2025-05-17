package com.example.demo.Bitcask;

import org.springframework.stereotype.Component;

import javax.xml.crypto.Data;
import java.io.*;
import java.util.*;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
@Component
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
        ensureDirectoriesExist();
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

    private void compactFiles() throws IOException {
        HashMap<Long,DataItem> newValues = new HashMap<>();
        for(Map.Entry<Long, CaskItem> entry : map.entrySet()){
            byte[] value = get(entry.getKey());
            if (value != null) {
                newValues.put(entry.getKey(), new DataItem(value , entry.getKey()));
            }
        }
        deleteFiles(baseDataDir);
        deleteFiles(baseHintDir);
        String compactFile = String.format("%08d.data", ++fileCounter);
        try (RandomAccessFile randomAccessFile = new RandomAccessFile(baseDataDir + compactFile, "rw")) {
            long offset = 0;
            for (DataItem dataItem : newValues.values()) {
                byte[] data = dataItem.toBytes();
                randomAccessFile.write(data);
                CaskItem caskItem = new CaskItem(compactFile, offset, data.length);
                map.put(dataItem.getKey(), caskItem);
                writeToHintFile(caskItem , dataItem.getKey());
                offset += data.length;
            }
        }

    }
    private void deleteFiles(String directory){
        File dir = new File(directory);
        for(File file : Objects.requireNonNull(dir.listFiles())){
            file.delete();
        }
    }
    private void ensureDirectoriesExist() {
        new File(baseDataDir).mkdirs();
        new File(baseHintDir).mkdirs();
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
    public void put(long key, byte[] value) throws IOException {
        DataItem dataItem = new DataItem(value , key);
        byte[] dataStored = dataItem.toBytes();
        partitionLogs();
        currentFile.seek(currentOffset);
        currentFile.write(dataStored);
        CaskItem caskItem = new CaskItem(currentFileNo , currentOffset , dataStored.length);
        map.put(key , caskItem);
        writeToHintFile(caskItem , key);
        currentOffset += dataStored.length;
    }
    @Override
    public List<DataItem> getAll() throws IOException {
        List<DataItem> result = new LinkedList<>();
        for(Map.Entry<Long, CaskItem> e : map.entrySet()){
            result.add(DataItem.fromBytes(get(e.getKey())));
        }
        return result;

    }
    @Override
    public byte[] get(long key) throws IOException {
        CaskItem caskItem = map.get(key);
        if(caskItem == null) return null;
        RandomAccessFile randomAccessFile = new RandomAccessFile(baseDataDir + caskItem.getFileName() , "r");
        randomAccessFile.seek(caskItem.getOffset());
        byte[] data = new byte[caskItem.getSize()];
        randomAccessFile.readFully(data);;
        return DataItem.fromBytes(data).getValue();
    }
    private void writeToHintFile(CaskItem caskItem , long key) throws IOException {
        DataOutputStream dataOutputStream = new DataOutputStream(
                new FileOutputStream(baseDataDir + caskItem.getFileName().replace(".data", ".hint"), true));
                    dataOutputStream.writeLong(key);
                    dataOutputStream.writeInt(caskItem.getSize());
                    dataOutputStream.writeLong(currentOffset);
    }

}
