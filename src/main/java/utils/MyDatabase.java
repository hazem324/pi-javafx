package utils;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class MyDatabase {

    private final String URL="jdbc:mysql://localhost:3306/culturespacedbbb";
    private final String USER="root";
    private final String PASSWORD="";
    private Connection cnx;
    private static MyDatabase instance;

    private MyDatabase() {
        try {
            cnx = DriverManager.getConnection(URL, USER, PASSWORD);
            System.out.println("Database connection established");
        }
        catch (SQLException e) {
            System.err.println(e.getMessage());
        }
    }

    public static MyDatabase getInstance(){

        if (instance == null)
        {
            instance = new MyDatabase();
        }
        return instance;
    }
    public Connection getCnx() {
        return cnx;
    }
}
