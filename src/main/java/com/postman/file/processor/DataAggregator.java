package com.postman.file.processor;

import java.sql.SQLException;

public interface DataAggregator {

    /*
    * fetch aggregate result and store them in separate table.
    */
    void aggregateByNameAndThenStore() throws InterruptedException;

    void aggregateByNameAndThenStoreUsingBatchUpdate() throws SQLException, InterruptedException;
}
