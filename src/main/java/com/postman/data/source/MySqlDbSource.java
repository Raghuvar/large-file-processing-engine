package com.postman.data.source;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

import java.sql.Connection;
import java.sql.SQLException;

public class MySqlDbSource {

    private static HikariConfig config = new HikariConfig("/mysql-db.properties");
    private static HikariDataSource ds;

    static {
        System.out.println(config.getJdbcUrl());
        ds = new HikariDataSource( config );
    }

    private MySqlDbSource() {}

    public static Connection getConnection() throws SQLException {
        return ds.getConnection();
    }
}