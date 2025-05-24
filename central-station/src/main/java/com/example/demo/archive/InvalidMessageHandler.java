package com.example.demo.archive;

import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;

public class InvalidMessageHandler implements  Ihandler{
    private String filePath;

    public InvalidMessageHandler(String filePath) {
        this.filePath = filePath;
    }

    @Override
    public void handle(String message) {
        try (PrintWriter writer = new PrintWriter(new FileWriter(filePath, true))) {
            writer.println("Invalid message: " + message);
            writer.println();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
