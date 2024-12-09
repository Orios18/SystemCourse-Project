package main;

import database.DBConnection;
import database.QueryHandler;

import java.awt.*;
import java.awt.event.*;
import java.sql.Connection;
import javax.swing.*;

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

class GenericUIApp extends JPanel {
    private JTextArea outputArea;
    private JTextField limitField;  // TextField for inputting limit
    private JButton executeButton;  // Button to trigger query execution

    // This method will create and show the UI window
    public void createAndShowGUI() {
        JFrame frame = new JFrame("Wine Database");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE); // Ensures program exits when window is closed
        frame.add(this);
        frame.pack(); // Sets the window to fit the natural size of its content pane
        frame.setLocationByPlatform(true);
        frame.setVisible(true);
    }

    // Constructor to initialize the panel content
    public GenericUIApp() {
        super(new BorderLayout());

        // Set a modern background color for the entire panel
        setBackground(new Color(240, 240, 240)); // Light gray background

        // Create a top panel for dropdown and search fields
        JPanel topPanel = new JPanel();
        topPanel.setLayout(new BorderLayout(10, 10)); // 10px gap between components

        // Create combo box (drop-down list) on the left
        String[] queryOptions = {"Select Query", "Get All Wines", "Get Limit Wines"};
        JComboBox<String> queryComboBox = new JComboBox<>(queryOptions);
        queryComboBox.setFont(new Font("Arial", Font.PLAIN, 14)); // Set a good font
        queryComboBox.setBackground(Color.WHITE); // White background for combo box
        queryComboBox.setPreferredSize(new Dimension(200, 30)); // Set preferred size
        queryComboBox.addActionListener(e -> {
            String selectedQuery = (String) queryComboBox.getSelectedItem();
            String queryResult = "";

            // Handle query selection
            if ("Get All Wines".equals(selectedQuery)) {
                queryResult = QueryHandler.getAllWines();
            } else if ("Get Limit Wines".equals(selectedQuery)) {
                // Show the input field and execute button for the limit query
                limitField.setText("");  // Clear any previous input
                executeButton.setEnabled(true);  // Enable the execute button
            } else {
                // Hide the input field and reset the UI for other queries
                limitField.setText("");
                executeButton.setEnabled(false);  // Disable the execute button
            }

            // Update the output area with the query result
            updateOutputArea(queryResult);
        });
        topPanel.add(queryComboBox, BorderLayout.WEST); // Align dropdown to the left

        // Create and style the text area for displaying output
        outputArea = new JTextArea(15, 50); // A larger text area for query results
        outputArea.setEditable(false); // Make it non-editable
        outputArea.setFont(new Font("Monospaced", Font.PLAIN, 14)); // Set a monospaced font for readability
        outputArea.setBackground(new Color(212, 212, 212)); // White background
        outputArea.setBorder(BorderFactory.createLineBorder(new Color(0, 0, 0), 2)); // Border for the text area
        JScrollPane scrollPane = new JScrollPane(outputArea); // Make the text area scrollable
        add(scrollPane, BorderLayout.CENTER); // Add the text area to the center

        // Add top panel (dropdown) to the main panel
        add(topPanel, BorderLayout.NORTH);

        // Panel for the limit input field and button (initially hidden)
        JPanel limitPanel = new JPanel();
        limitPanel.setLayout(new FlowLayout());

        JLabel limitLabel = new JLabel("Enter Limit: ");
        limitField = new JTextField(10); // TextField for limit input
        executeButton = new JButton("Execute Query");

        // Button action to execute the query
        executeButton.addActionListener(e -> executeLimitQuery());

        limitPanel.add(limitLabel);
        limitPanel.add(limitField);
        limitPanel.add(executeButton);

        // Initially hide the limitPanel
        limitPanel.setVisible(true);
        add(limitPanel, BorderLayout.SOUTH);
    }

    // Method to handle limit query execution
    private void executeLimitQuery() {
        try {
            int limit = Integer.parseInt(limitField.getText());  // Parse the limit from the input field
            String queryResult = QueryHandler.getLimitWines(limit);  // Get wines with the provided limit
            updateOutputArea(queryResult);  // Display the result in the output area
        } catch (NumberFormatException ex) {
            updateOutputArea("Invalid limit value. Please enter a valid number.");
        }
    }

    // Method to update the output area (can be used to display query results)
    public void updateOutputArea(String queryResult) {
        outputArea.setText(queryResult); // Display the query result in the output area
    }
}
