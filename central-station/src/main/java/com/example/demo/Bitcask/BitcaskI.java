package com.example.demo.Bitcask;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.List;

public interface BitcaskI {
    public void put(long key , byte[] value) throws IOException;
    public byte[] get(long key) throws IOException;
    public List<DataItem> getAll() throws IOException;
}
