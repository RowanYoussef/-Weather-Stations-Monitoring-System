package com.example.demo.Bitcask;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;

public class Bitcask implements  BitcaskI{
    private final String baseDataDir = "bin/";
    private final String baseHintDir = "hint/";
    private HashMap<Long , CaskItem>  map = new HashMap<>();;
    private String currentFileNo;
    private RandomAccessFile currentFile;
    private long currentOffset;

    public Bitcask () throws IOException{
        recoverFromHintFiles();
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
