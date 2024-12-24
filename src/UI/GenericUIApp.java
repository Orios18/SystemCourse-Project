package UI;

import database.QueryHandler;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

public class GenericUIApp extends JPanel {

    private JTable outputTable;
    private DefaultTableModel tableModel;
    private JPanel inputPanel;

    public void createAndShowGUI() {
        JFrame frame = new JFrame("Wine Database System");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(800, 600);
        frame.setResizable(true);
        this.setBackground(new Color(131, 131, 131)); // Light gray
        frame.add(this);
        frame.setLocationRelativeTo(null); // Center the window
        frame.setVisible(true);
    }

    public GenericUIApp() {
        super(new BorderLayout());

        // Initialize JTable and DefaultTableModel
        tableModel = new DefaultTableModel();
        outputTable = new JTable(tableModel);
        JScrollPane scrollPane = new JScrollPane(outputTable);
        add(scrollPane, BorderLayout.CENTER);

        // Add MouseListener to show tooltips on table rows
        outputTable.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseMoved(MouseEvent e) {
                int row = outputTable.rowAtPoint(e.getPoint());
                if (row != -1) {
                    outputTable.setToolTipText("Row: " + row);
                }
            }
        });

        // Top panel for query selection
        JPanel topPanel = new JPanel();
        topPanel.setBackground(new Color(141, 141, 141)); // Light gray background
        String[] queries = {
                "Select Query",
                "Get All Wines",
                "Get Limit Wines",
                "Get Wines by Quality",
                "Get Wines by Alcohol Range",
                "Get Wines by Color",
                "Get Wines by ID"
        };
        JComboBox<String> queryComboBox = new JComboBox<>(queries);
        queryComboBox.addActionListener(e -> onQuerySelected((String) queryComboBox.getSelectedItem()));
        topPanel.add(queryComboBox);
        add(topPanel, BorderLayout.NORTH);

        // Input panel for dynamic query inputs
        inputPanel = new JPanel();
        inputPanel.setLayout(new GridBagLayout()); // Use GridBagLayout for fine control
        inputPanel.setBackground(new Color(140, 140, 140)); // Light gray background
        add(inputPanel, BorderLayout.SOUTH);
    }

    private void onQuerySelected(String selectedQuery) {
        inputPanel.removeAll(); // Clear any existing inputs
        clearTable(); // Clear previous table data

        // Create a GridBagConstraints object for controlling component placement
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(5, 10, 5, 10); // Add some padding around components

        switch (selectedQuery) {
            case "Get All Wines":
                displayQueryResults(QueryHandler.getAllWines());
                break;
            case "Get Limit Wines":
                addInputField("Limit:", "limit", gbc, 0);
                addExecuteButton(e -> {
                    String limitStr = getFieldValue("limit");
                    if (limitStr.isEmpty()) {
                        showError("Please enter a limit value.");
                        return;
                    }
                    int limit = Integer.parseInt(limitStr);
                    displayQueryResults(QueryHandler.getLimitWines(limit));
                }, gbc, 1);
                break;
            case "Get Wines by Quality":
                addQualityDropdown(gbc, 0);
                addExecuteButton(e -> {
                    String quality = getFieldValue("quality");
                    if (quality.isEmpty()) {
                        showError("Please select a quality.");
                        return;
                    }
                    displayQueryResults(QueryHandler.getWinesByQuality(quality));
                }, gbc, 1);
                break;
            case "Get Wines by Alcohol Range":
                addInputField("Min Alcohol:", "minAlcohol", gbc, 0);
                addInputField("Max Alcohol:", "maxAlcohol", gbc, 1);
                addExecuteButton(e -> {
                    String minStr = getFieldValue("minAlcohol");
                    String maxStr = getFieldValue("maxAlcohol");

                    // Check if both fields are empty
                    if (minStr.isEmpty() && maxStr.isEmpty()) {
                        showError("Please enter either minimum or maximum alcohol values.");
                        return;
                    }

                    double minAlcohol = minStr.isEmpty() ? -1 : Double.parseDouble(minStr); // If empty, treat as -1
                    double maxAlcohol = maxStr.isEmpty() ? -1 : Double.parseDouble(maxStr); // If empty, treat as -1

                    displayQueryResults(QueryHandler.getWinesByAlcoholRange(minAlcohol, maxAlcohol));
                }, gbc, 2);
                break;

            case "Get Wines by Color":
                addColorDropdown(gbc, 0);
                addExecuteButton(e -> {
                    String color = getFieldValue("color");
                    if (color.isEmpty()) {
                        showError("Please select a color.");
                        return;
                    }
                    displayQueryResults(QueryHandler.getWinesByColor(color));
                }, gbc, 1);
                break;
            case "Get Wines by ID":
                addInputField("ID(s) or Range (e.g., 1,2,3 or 5-10):", "ids", gbc, 0);
                addExecuteButton(e -> {
                    String ids = getFieldValue("ids");
                    if (ids.isEmpty()) {
                        showError("Please enter IDs or a range.");
                        return;
                    }
                    displayQueryResults(QueryHandler.getWinesById(ids));
                }, gbc, 1);
                break;
        }

        inputPanel.revalidate();
        inputPanel.repaint();
    }

    private void addInputField(String label, String name, GridBagConstraints gbc, int row) {
        JLabel jLabel = new JLabel(label);
        JTextField jTextField = new JTextField();
        jTextField.setName(name);
        jTextField.setPreferredSize(new Dimension(200, 25)); // Constrain the width
        jTextField.setToolTipText("Enter the " + label.toLowerCase()); // Tooltip for input field

        gbc.gridx = 0;
        gbc.gridy = row;
        gbc.anchor = GridBagConstraints.LINE_START; // Align label to the left
        inputPanel.add(jLabel, gbc);

        gbc.gridx = 1;
        gbc.anchor = GridBagConstraints.CENTER; // Center the text field
        inputPanel.add(jTextField, gbc);
    }

    private void addQualityDropdown(GridBagConstraints gbc, int row) {
        JLabel qualityLabel = new JLabel("Select Quality:");
        String[] qualities = {
                "neutral",
                "slightly dissatisfied",
                "moderately dissatisfied",
                "slightly satisfied",
                "moderately satisfied"
        };
        JComboBox<String> qualityComboBox = new JComboBox<>(qualities);
        qualityComboBox.setName("quality");
        qualityComboBox.setPreferredSize(new Dimension(200, 25)); // Constrain the width
        qualityComboBox.setToolTipText("Choose the wine quality"); // Tooltip for combo box

        gbc.gridx = 0;
        gbc.gridy = row;
        gbc.anchor = GridBagConstraints.LINE_START; // Align label to the left
        inputPanel.add(qualityLabel, gbc);

        gbc.gridx = 1;
        gbc.anchor = GridBagConstraints.CENTER; // Center the combo box
        inputPanel.add(qualityComboBox, gbc);
    }

    private void addColorDropdown(GridBagConstraints gbc, int row) {
        JLabel colorLabel = new JLabel("Select Color:");
        String[] colors = {
                "red",
                "white"
        };
        JComboBox<String> colorComboBox = new JComboBox<>(colors);
        colorComboBox.setName("color");
        colorComboBox.setPreferredSize(new Dimension(200, 25)); // Constrain the width
        colorComboBox.setToolTipText("Choose the wine color"); // Tooltip for combo box

        gbc.gridx = 0;
        gbc.gridy = row;
        gbc.anchor = GridBagConstraints.LINE_START; // Align label to the left
        inputPanel.add(colorLabel, gbc);

        gbc.gridx = 1;
        gbc.anchor = GridBagConstraints.CENTER; // Center the combo box
        inputPanel.add(colorComboBox, gbc);
    }

    private void addExecuteButton(ActionListener actionListener, GridBagConstraints gbc, int row) {
        JButton executeButton = new JButton("Execute");
        executeButton.addActionListener(actionListener);
        executeButton.setToolTipText("Click to execute the query"); // Tooltip for button

        gbc.gridx = 1;
        gbc.gridy = row;
        gbc.anchor = GridBagConstraints.CENTER; // Center the button
        inputPanel.add(executeButton, gbc);
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

    private void clearTable() {
        tableModel.setRowCount(0);
        tableModel.setColumnCount(0);
    }

    private void displayQueryResults(QueryHandler.QueryResult result) {
        clearTable();
        for (String columnName : result.getColumnNames()) {
            tableModel.addColumn(columnName);
        }
        for (String[] row : result.getData()) {
            tableModel.addRow(row);
        }
    }

    private void showError(String message) {
        JOptionPane.showMessageDialog(this, message, "Error", JOptionPane.ERROR_MESSAGE);
    }
}
