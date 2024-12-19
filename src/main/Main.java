package main;

import UI.GenericUIApp;
import database.DBConnection;

import java.sql.Connection;

public class Main {
    public static void main(String[] args) {
        // Start by connecting to the database
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

        // Initialize and display the UI
        System.out.println("Launching the UI...");
        GenericUIApp uiApp = new GenericUIApp();
        uiApp.createAndShowGUI();
    }
}

