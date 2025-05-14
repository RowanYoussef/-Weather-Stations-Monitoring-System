package com.example.demo.Bitcask;

import java.util.HashMap;

public class Bitcask implements  BitcaskI{
    HashMap<Long , byte[]>  map;
    String currentFileNo;

    public Bitcask(){
        map = new HashMap<>();
    }
    @Override
    public void put(long key, byte[] value) {

    }

    @Override
    public byte[] get(long key) {
        return new byte[0];
    }

    private void recover(){
        
    }
}
