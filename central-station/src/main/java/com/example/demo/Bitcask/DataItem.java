package com.example.demo.Bitcask;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;

public class DataItem {
    public DataItem(byte[] value, long key) {
        this.value = value;
        this.key = key;
    }

    private long key;
    private byte[] value;

    public long getKey() {
        return key;
    }

    public void setKey(long key) {
        this.key = key;
    }

    public byte[] getValue() {
        return value;
    }

    public void setValue(byte[] value) {
        this.value = value;
    }
    public byte[] toBytes(){
        ByteBuffer byteBuffer = ByteBuffer.allocate(value.length + 12);
        byteBuffer.putLong(key);
        byteBuffer.putInt(value.length);
        byteBuffer.put(value);
        return byteBuffer.array();
    }
    public static DataItem fromBytes(byte[] data){
        ByteBuffer byteBuffer = ByteBuffer.wrap(data);
        long key = byteBuffer.getLong();
        int size = byteBuffer.getInt();
        byte[] dataBytes = new byte[size];
        byteBuffer.get(dataBytes);
        return new DataItem(dataBytes , key);
    }
}
