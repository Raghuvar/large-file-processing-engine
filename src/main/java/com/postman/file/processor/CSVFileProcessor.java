package com.postman.file.processor;

import com.postman.data.source.MySqlDbSource;
import com.postman.executor.ExecutorFactory;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;

import java.io.FileReader;
import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.*;
import java.util.concurrent.ExecutorService;

public class CSVFileProcessor implements FileProcessor {

    private static final Integer BATCH_SIZE = 35000;
    public static Integer ERROR_COUNT = 1;
    Connection con;
    ExecutorService executor;

    public CSVFileProcessor(Connection connection, ExecutorService executorService) throws SQLException {
        // Use one executor in one processor
        this.con = connection;

        // Number of threads should be configurable via properties/env
        this.executor = executorService;
    }

    @Override
    public void saveFileToDB(String filePath) throws InterruptedException {
        String cvsSplitBy = ",";
        try {
            List<String[]> lines = new ArrayList<>();
            int count = 0;

            CSVParser csvParser = new CSVParser(new FileReader(filePath),
                    CSVFormat.DEFAULT.withFirstRecordAsHeader());
            for (CSVRecord csvRecord : csvParser) {
                lines.add(new String[]{csvRecord.get(0), csvRecord.get(1), csvRecord.get(2)});
                count++;
                if (count == BATCH_SIZE) {
                    executor.submit(new CSVWriter(lines, cvsSplitBy, con));
                    count = 0;
                    lines = new ArrayList<>();
                }
            }
            if (count > 0) {
                executor.submit(new CSVWriter(lines, cvsSplitBy, con));
            }

        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            executor.shutdown();
            while (!executor.isShutdown()){
                Thread.sleep(1000);
                //waiting for shutdown
            }
            System.out.println("Executor shutdown successfully");
            System.out.println(ERROR_COUNT);
        }
    }
}
