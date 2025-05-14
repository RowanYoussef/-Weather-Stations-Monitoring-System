package com.example.demo.Bitcask;

import java.io.FileNotFoundException;
import java.io.IOException;

public interface BitcaskI {
    public void put(long key , byte[] value) throws IOException;
    public byte[] get(long key) throws IOException;
}
