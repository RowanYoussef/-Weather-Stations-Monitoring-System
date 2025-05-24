package com.example.demo.Bitcask;

import java.io.DataInputStream;
import java.io.IOException;

public class CaskItem {
    private final String fileName;
    private final long offset;
    private final int size;
    CaskItem(String fileName, long offset, int size){
        this.fileName = fileName;
        this.offset = offset;
        this.size = size;
    }

    public int getSize() {
        return size;
    }

    public long getOffset() {
        return offset;
    }

    public String getFileName() {
        return fileName;
    }


}
