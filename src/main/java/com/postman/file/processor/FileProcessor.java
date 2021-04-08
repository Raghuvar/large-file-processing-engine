package com.postman.file.processor;

public interface FileProcessor {

    /**
     * Read file and save to DB
     * @param filePath
     */
    void saveFileToDB(String filePath) throws InterruptedException;
}
