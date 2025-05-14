package com.example.demo.Bitcask;

public interface BitcaskI {
    public void put(long key , byte[] value);
    public byte[] get(long key);
}
