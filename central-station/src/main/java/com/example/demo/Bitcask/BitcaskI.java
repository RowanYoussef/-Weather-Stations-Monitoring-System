package com.example.demo.Bitcask;

import org.springframework.stereotype.Component;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.List;
@Component
public interface BitcaskI {
    public void put(long key , byte[] value) throws IOException;
    public byte[] get(long key) throws IOException;
    public List<DataItem> getAll() throws IOException;
}
