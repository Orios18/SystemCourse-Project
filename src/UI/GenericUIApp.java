package UI;

import database.QueryHandler;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionListener;

public class GenericUIApp extends JPanel {

    private JTable outputTable;
    private DefaultTableModel tableModel;
    private JPanel inputPanel;

    public void createAndShowGUI() {
        JFrame frame = new JFrame("Wine Database");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(800, 600);
        frame.setResizable(false);
        this.setBackground(new Color(240, 240, 240)); // Light gray
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

        // Top panel for query selection
        JPanel topPanel = new JPanel();
        topPanel.setBackground(new Color(200, 220, 240)); // Light blue background
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
        inputPanel.setLayout(new GridLayout(5, 2, 5, 5));
        inputPanel.setBackground(new Color(240, 240, 240)); // Light gray background
        add(inputPanel, BorderLayout.SOUTH);
    }

    private void onQuerySelected(String selectedQuery) {
        inputPanel.removeAll(); // Clear any existing inputs
        clearTable(); // Clear previous table data

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
                    JComboBox<String> qualityComboBox = (JComboBox<String>) getComponentByName("quality");
                    if (qualityComboBox != null) {
                        String quality = (String) qualityComboBox.getSelectedItem();
                        displayQueryResults(QueryHandler.getWinesByQuality(quality));
                    }
                });
                break;
            case "Get Wines by Alcohol Range":
                addInputField("Min Alcohol:", "minAlcohol");
                addInputField("Max Alcohol:", "maxAlcohol");
                addExecuteButton(e -> {
                    String minStr = getFieldValue("minAlcohol");
                    String maxStr = getFieldValue("maxAlcohol");
                    double minAlcohol = minStr.isEmpty() ? -1 : Double.parseDouble(minStr);
                    double maxAlcohol = maxStr.isEmpty() ? -1 : Double.parseDouble(maxStr);
                    displayQueryResults(QueryHandler.getWinesByAlcoholRange(minAlcohol, maxAlcohol));
                });
                break;
            case "Get Wines by Color":
                addColorDropdown();
                addExecuteButton(e -> {
                    JComboBox<String> colorComboBox = (JComboBox<String>) getComponentByName("color");
                    if (colorComboBox != null) {
                        String color = (String) colorComboBox.getSelectedItem();
                        displayQueryResults(QueryHandler.getWinesByColor(color));
                    }
                });
                break;
            case "Get Wines by ID":
                addInputField("ID(s) or Range (e.g., 1,2,3 or 5-10):", "ids");
                addExecuteButton(e -> {
                    String ids = getFieldValue("ids");
                    displayQueryResults(QueryHandler.getWinesById(ids));
                });
                break;
        }

        inputPanel.revalidate();
        inputPanel.repaint();
    }

    private void addInputField(String label, String name) {
        JLabel jLabel = new JLabel(label);
        JTextField jTextField = new JTextField();
        jTextField.setName(name);
        inputPanel.add(jLabel);
        inputPanel.add(jTextField);
    }

    private void addQualityDropdown() {
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
        inputPanel.add(qualityLabel);
        inputPanel.add(qualityComboBox);
    }

    private void addColorDropdown() {
        JLabel colorLabel = new JLabel("Select Color:");
        String[] colors = {
                "red",
                "white"
        };
        JComboBox<String> colorComboBox = new JComboBox<>(colors);
        colorComboBox.setName("color");
        inputPanel.add(colorLabel);
        inputPanel.add(colorComboBox);
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
            }
        }
        return "";
    }

    private Component getComponentByName(String name) {
        for (Component component : inputPanel.getComponents()) {
            if (name.equals(component.getName())) {
                return component;
            }
        }
        return null;
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
}
