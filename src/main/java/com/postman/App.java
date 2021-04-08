package com.postman;

import com.postman.data.source.MySqlDbSource;
import com.postman.executor.ExecutorFactory;
import com.postman.file.processor.CSVFileProcessor;
import com.postman.file.processor.DataAggregator;
import com.postman.file.processor.DataAggregatorImpl;
import com.postman.file.processor.FileProcessor;

import java.sql.SQLException;
import java.util.Date;

public class App {
    public static void main(String[] args) throws SQLException, InterruptedException {
        System.out.println("Large File Processor com.postman.file.processor.App Started");
        System.out.println("hello".substring(0,0));
        System.out.println("Data Ingestion start at = " + new Date());
        long currentMills = System.currentTimeMillis();
        Thread.sleep(1000*6);
        String fileToSave = "conf/products.csv";
        FileProcessor fileProcessor = new CSVFileProcessor(MySqlDbSource.getConnection(),
                ExecutorFactory.getBlockingExecutorService(5));
        fileProcessor.saveFileToDB(fileToSave);


        /*
        Now, run Data Aggregations
        :improvement/suggestion -- This should be a kind of trigger .... once the data ingestion is done
        we should trigger the aggregation job. And in case of high volumes/velocity of ingestion,
        we should make this a cron job.
         */

        long ingCompMills = System.currentTimeMillis();
        System.out.println("Data Ingestion completed in " + (ingCompMills - currentMills) + "ms spent");

        // wait for a minute before running aggregate
        Thread.sleep(30000);

        System.out.println("Aggregation starts at = " + new Date());

        DataAggregator dataAggregator = new DataAggregatorImpl(MySqlDbSource.getConnection(),
                ExecutorFactory.getBlockingExecutorService(5));
//        dataAggregator.aggregateByNameAndThenStore();
        dataAggregator.aggregateByNameAndThenStoreUsingBatchUpdate();

        System.out.println("Data Aggregation completed in = " + (System.currentTimeMillis()-ingCompMills) +" ms");
    }
}
