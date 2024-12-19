package UI;

import database.QueryHandler;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionListener;

public class GenericUIApp extends JPanel {

    private JTextArea outputArea;
    private JPanel inputPanel;

    public void createAndShowGUI() {
        JFrame frame = new JFrame("Wine Database");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.add(this);
        frame.pack();
        frame.setLocationByPlatform(true);
        frame.setVisible(true);
    }

    public GenericUIApp() {
        super(new BorderLayout());

        // Main output area
        outputArea = new JTextArea(20, 60);
        outputArea.setEditable(false);
        outputArea.setFont(new Font("Monospaced", Font.PLAIN, 14));
        JScrollPane scrollPane = new JScrollPane(outputArea);
        add(scrollPane, BorderLayout.CENTER);

        // Top panel for query selection
        JPanel topPanel = new JPanel();
        String[] queries = {
                "Select Query",
                "Get All Wines",
                "Get Limit Wines",
                "Get Wines by Quality",
                "Get Wines by Alcohol Range",
                "Get Wines by Color"
        };
        JComboBox<String> queryComboBox = new JComboBox<>(queries);
        queryComboBox.addActionListener(e -> onQuerySelected((String) queryComboBox.getSelectedItem()));
        topPanel.add(queryComboBox);
        add(topPanel, BorderLayout.NORTH);

        // Input panel for dynamic query inputs
        inputPanel = new JPanel();
        inputPanel.setLayout(new GridLayout(5, 2, 5, 5));
        add(inputPanel, BorderLayout.SOUTH);
    }

    private void onQuerySelected(String selectedQuery) {
        inputPanel.removeAll(); // Clear any existing inputs
        outputArea.setText(""); // Clear output area

        switch (selectedQuery) {
            case "Get All Wines":
                displayQueryResults(QueryHandler.getAllWines());
                break;
            case "Get Limit Wines":
                addInputField("Limit:", "limit");
                addExecuteButton(e -> {
                    int limit = Integer.parseInt(getFieldValue("limit"));
                    displayQueryResults(QueryHandler.getLimitWines(limit));
                });
                break;
            case "Get Wines by Quality":
                addQualityDropdown();
                addExecuteButton(e -> {
                    String quality = getFieldValue("quality");
                    displayQueryResults(QueryHandler.getWinesByQuality(quality));
                });
                break;
            case "Get Wines by Alcohol Range":
                addInputField("Min Alcohol:", "minAlcohol");
                addInputField("Max Alcohol:", "maxAlcohol");
                addExecuteButton(e -> {
                    double min = Double.parseDouble(getFieldValue("minAlcohol"));
                    double max = Double.parseDouble(getFieldValue("maxAlcohol"));
                    displayQueryResults(QueryHandler.getWinesByAlcoholRange(min, max));
                });
                break;
            case "Get Wines by Color":
                addColorDropdown(); // Adding color dropdown for the query
                addExecuteButton(e -> {
                    String color = getFieldValue("color");
                    System.out.println("Selected color: " + color);  // Debugging line
                    displayQueryResults(QueryHandler.getWinesByColor(color));
                });
                break;
        }

        inputPanel.revalidate();
        inputPanel.repaint();
    }

    private void addQualityDropdown() {
        // Quality dropdown values
        String[] qualityValues = {
                "neutral", "slightly dissatisfied", "moderately satisfied",
                "moderately dissatisfied", "slightly satisfied"
        };

        JComboBox<String> qualityComboBox = new JComboBox<>(qualityValues);
        qualityComboBox.setName("quality");
        inputPanel.add(new JLabel("Quality:"));
        inputPanel.add(qualityComboBox);
    }

    private void addColorDropdown() {
        // Color dropdown values
        String[] colorValues = {
                "red", "white"
        };

        JComboBox<String> colorComboBox = new JComboBox<>(colorValues);
        colorComboBox.setName("color");
        inputPanel.add(new JLabel("Color:"));
        inputPanel.add(colorComboBox);
    }

    private void addInputField(String label, String name) {
        JLabel jLabel = new JLabel(label);
        JTextField jTextField = new JTextField();
        jTextField.setName(name);
        inputPanel.add(jLabel);
        inputPanel.add(jTextField);
    }

    private void addExecuteButton(ActionListener actionListener) {
        JButton executeButton = new JButton("Execute");
        executeButton.addActionListener(actionListener);
        inputPanel.add(new JLabel()); // Filler for alignment
        inputPanel.add(executeButton);
    }

    private String getFieldValue(String name) {
        for (Component component : inputPanel.getComponents()) {
            if (component instanceof JTextField && name.equals(component.getName())) {
                return ((JTextField) component).getText();
            } else if (component instanceof JComboBox && name.equals(component.getName())) {
                return (String) ((JComboBox<?>) component).getSelectedItem();
            }
        }
        return "";
    }

    private void displayQueryResults(String results) {
        outputArea.setText(results);
    }
}
