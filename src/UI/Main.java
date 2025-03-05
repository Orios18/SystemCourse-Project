package UI;

import javax.swing.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

/**
 * The main class to run the Wine Database System application.
 */
public class Main {
    public static void main(String[] args) {
        // Ensure the UI is created on the Event Dispatch Thread
        SwingUtilities.invokeLater(() -> {
            JFrame frame = new JFrame("Wine Database System");
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            frame.setSize(1000, 700);
            frame.setResizable(true);

            // Create an instance of GenericUIApp and add it to the frame
            GenericUIApp appPanel = new GenericUIApp();
            frame.add(appPanel);

            // Center the frame on the screen
            frame.setLocationRelativeTo(null);

            // Add a window listener to perform cleanup when the window is closed
            frame.addWindowListener(new WindowAdapter() {
                @Override
                public void windowClosing(WindowEvent e) {
                    // Perform any necessary cleanup here
                    // For example, close database connections if not handled elsewhere
                    System.exit(0); // Ensure the application exits
                }
            });

            // Make the frame visible
            frame.setVisible(true);
        });
    }
}
