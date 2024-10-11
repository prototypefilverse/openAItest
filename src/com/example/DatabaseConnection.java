package com.example;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DatabaseConnection {
	
	private static final String DRIVER_NAME = "com.microsoft.sqlserver.jdbc.SQLServerDriver";
	private static final  String url = "jdbc:sqlserver://reviewsgembb.database.windows.net:1433;database=msaitest;user=morikawasusumu@reviewsgembb;password=00830080gG;encrypt=true;trustServerCertificate=false;hostNameInCertificate=*.database.windows.net;loginTimeout=30;";
    
    public static Connection getConnection() throws SQLException {
        
    try {	
		Class.forName(DRIVER_NAME);
		return DriverManager.getConnection(url);

    } catch (Exception e) {
        e.printStackTrace();
        return null;  
     }
    }
}