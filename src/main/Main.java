package main;

import database.DBConnection;
import database.QueryHandler;

import java.sql.Connection;

public class Main {
    public static void main(String[] args) {
        System.out.println("Starting Wine Database System...");
        try (Connection connection = DBConnection.connect()) {
            if (connection != null) {
                System.out.println("Database connected successfully!");
            } else {
                System.out.println("Failed to connect to the database.");
            }
        } catch (Exception e) {
            System.err.println("Error while connecting to the database:");
            e.printStackTrace();
        }

        QueryHandler.getLimitWines(10);
        QueryHandler.getWinesByAlcoholRange(10,10.1);



        System.out.println("Exiting the system.");
    }
}
