package com.helei;

import java.sql.Connection;
import java.sql.DriverManager;

public class MySQLConnectionTest {
    public static void main(String[] args) {
        String url = "jdbc:mysql://192.168.1.3:3306/shinanoquanti?useSSL=false&allowPublicKeyRetrieval=true&keepAlive=true";
        String user = "helei";
        String password = "962464";

        try (Connection connection = DriverManager.getConnection(url, user, password)) {
            if (connection != null) {
                System.out.println("Connected to the database!");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
