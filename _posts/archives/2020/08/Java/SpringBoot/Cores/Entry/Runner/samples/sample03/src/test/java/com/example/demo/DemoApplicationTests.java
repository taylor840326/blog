package com.example.demo;

import org.junit.Before;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Collections;

@SpringBootTest
class DemoApplicationTests {
    private Connection conn;


    @Test
    void contextLoads() throws SQLException, ClassNotFoundException {
        Class.forName("com.mysql.cj.jdbc.Driver");
        Connection conn = DriverManager.getConnection("jdbc:mysql://192.168.100.128:3306/testdb","root","111111");
        conn.setAutoCommit(false);
        PreparedStatement stmt = conn.prepareStatement("INSERT INTO t_primary_1k(data) VALUES(?)");
        for(long i = 1;i<= 100000000;i++){
            stmt.setString(1, String.join("",Collections.nCopies(1016,"*")));
            stmt.addBatch();
            if(i % 10000 == 0){
                stmt.executeBatch();
                conn.commit();
            }
        }
        conn.commit();
        stmt.close();

    }

}
