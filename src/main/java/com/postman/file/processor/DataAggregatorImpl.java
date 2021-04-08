package com.postman.file.processor;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;

public class DataAggregatorImpl implements DataAggregator{
    private static final String AGGREGATE_BY_NAME_STATEMENT = "insert into product_aggregation (`product_name`, `count`) " +
            " select name, count(*) as count from product p group by name " +
            " ON DUPLICATE KEY UPDATE `product_name` = VALUES(product_name), count = count;";

    private static final Integer AGG_BATCH_SIZE = 30000;

    Connection connection;
    ExecutorService executorService;

    public DataAggregatorImpl(Connection connection, ExecutorService executorService) {
        this.connection = connection;
        this.executorService = executorService;
    }

    @Override
    public void aggregateByNameAndThenStore() throws InterruptedException {
        try {
            PreparedStatement preparedStatement = connection.prepareStatement(AGGREGATE_BY_NAME_STATEMENT);
            preparedStatement.executeLargeUpdate();
        } catch (SQLException throwables) {
            System.out.println("Some error occurred and storing the results" + throwables);
            //  Should have retrial with exponential back-off if doing just after ingestion.
            //  System.out.println("retrying the aggregation");
            //  Thread.sleep(10000);
            //  aggregateByNameAndThenStore();
            //  getAggregateByName();
        }
    }

    @Override
    public void aggregateByNameAndThenStoreUsingBatchUpdate() throws SQLException, InterruptedException {
        String aggregateSql = " select name, count(*) as count from product group by name ";
        ResultSet result=null;
        try {
            PreparedStatement aggregateStatement = connection.prepareStatement(aggregateSql);
            result = aggregateStatement.executeQuery();
        } catch (SQLException throwables) {
            System.out.println("Some error occurred and storing the results" + throwables);
        }
        List<String[]> batchResult = new ArrayList<>();
        if(result != null){
            while(result.next()){
                String productName = result.getString("name");
                String count = result.getString("count");
                batchResult.add(new String[]{productName, count});

                if(batchResult.size() == AGG_BATCH_SIZE){
                    // writeResultToAggTable(batchResult);
                    List<String[]> finalBatchResult = batchResult;
                    executorService.submit(new Runnable() {
                        @Override
                        public void run() {
                            writeResultToAggTable(finalBatchResult);
                        }
                    });

                    batchResult = new ArrayList<>();
                }
            }
            if(batchResult.size() >0){
                writeResultToAggTable(batchResult);
            }
        }
        executorService.shutdown();
        while(!executorService.isShutdown()){
            Thread.sleep(1000);
        }
        System.out.println("Aggregations result written to DB and executor has been shutDonw.");
    }

    private void writeResultToAggTable(List<String[]> aggResults){
        String sql = "INSERT INTO product_aggregation (`product_name`,count) VALUES (?,?) " +
                "ON DUPLICATE KEY UPDATE `product_name`=VALUES(product_name),`count`=VALUES(count);";
        System.out.println("executing batch of size = " + aggResults.size() + " with thread = "
                + Thread.currentThread().getName());
        try {
            PreparedStatement preparedStatement = connection.prepareStatement(sql);
            for (String[] data : aggResults) {
                preparedStatement.setString(1, data[0]);
                preparedStatement.setInt(2, Integer.valueOf(data[1]));
                preparedStatement.addBatch();
            }
            long[] rows = preparedStatement.executeLargeBatch();
    } catch (SQLException throwables) {
            throwables.printStackTrace();
        }
    }
}
