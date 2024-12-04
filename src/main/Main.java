package main;

import database.DBConnection;
import database.QueryHandler;
import java.sql.Connection;
import java.awt.BorderLayout;
import javax.swing.BorderFactory;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.WindowConstants;

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

        // Now the program will keep running until the GUI is closed
        System.out.println("Exiting the system.");
    }
}

class GenericUIApp extends JPanel {
    // This method will create and show the UI window
    public void createAndShowGUI() {
        JFrame frame = new JFrame("Generic UI App");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE); // Ensures program exits when window is closed
        frame.add(this);
        frame.pack(); // Sets the window to fit the natural size of its content pane
        frame.setLocationByPlatform(true);
        frame.setVisible(true);
    }

    // Constructor to initialize the panel content
    public GenericUIApp() {
        super(new BorderLayout());
        add(new JLabel("Put your UI Contents here."));
        setBorder(BorderFactory.createMatteBorder(200, 200, 200, 200, getBackground()));
    }
}

