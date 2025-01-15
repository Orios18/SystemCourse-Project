package UI;

import database.QueryHandler;
import database.QueryHandler.QueryResult;

import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.function.Supplier;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * A UI application that displays wine data from a database.
 * Uses SwingWorkers to run queries on background threads,
 * shows a progress bar while running, and displays
 * the results in a JTable.
 *
 * This version does NOT attempt to load any icon or background images.
 */
public class GenericUIApp extends JPanel {

    // -------------------------------------------------------
    //                       Fields
    // -------------------------------------------------------

    private static final Logger LOGGER = Logger.getLogger(GenericUIApp.class.getName());

    private JTable outputTable;
    private DefaultTableModel tableModel;
    private JPanel inputPanel;

    private JLabel resultsCountLabel;  // Shows how many wines are displayed
    private JProgressBar progressBar;  // Shows loading progress

    private static final String ICON_PATH = "src/resources/wine-barrel.png";
    // -------------------------------------------------------
    //                    Constructor
    // -------------------------------------------------------

    public GenericUIApp() {
        super(new BorderLayout());

        // Log which thread the UI is running on (usually the Event Dispatch Thread)
        LOGGER.info("UI is running on thread: " + Thread.currentThread().getName());

        // Initialize JTable and DefaultTableModel
        tableModel = new DefaultTableModel();
        outputTable = new JTable(tableModel);
        JScrollPane scrollPane = new JScrollPane(outputTable);
        add(scrollPane, BorderLayout.CENTER);

        // MouseListener to show tooltips on table rows
        outputTable.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseMoved(MouseEvent e) {
                int row = outputTable.rowAtPoint(e.getPoint());
                if (row != -1) {
                    outputTable.setToolTipText("Row: " + row);
                }
            }
        });

        // ---------- TOP PANEL (query selection + results count + progress bar) ----------
        JPanel topPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        topPanel.setBackground(new Color(141, 141, 141));

        // Create combo box for queries
        String[] queries = {
                "Select Query",
                "Get All Wines",
                "Get Limit Wines",
                "Get Wines by Quality",
                "Get Wines by Alcohol Range",
                "Get Wines by Color",
                "Get Wines by ID",
                "Get Wines by Date Range",
                "Get Wines by pH Range"
        };
        JComboBox<String> queryComboBox = new JComboBox<>(queries);
        queryComboBox.addActionListener(e -> onQuerySelected((String) queryComboBox.getSelectedItem()));
        topPanel.add(queryComboBox);

        // Label that displays how many wines are currently in the table
        resultsCountLabel = new JLabel("Wines displayed: 0");
        resultsCountLabel.setForeground(Color.BLACK);
        topPanel.add(resultsCountLabel);

        // Progress bar for background tasks
        progressBar = new JProgressBar();
        progressBar.setIndeterminate(true);
        progressBar.setVisible(false);  // Hidden by default
        progressBar.setStringPainted(true);
        progressBar.setString("Loading...");
        topPanel.add(progressBar);

        add(topPanel, BorderLayout.NORTH);

        // ---------- BOTTOM PANEL (dynamic input fields) ----------
        inputPanel = new JPanel();
        inputPanel.setLayout(new GridBagLayout());
        inputPanel.setBackground(new Color(140, 140, 140));
        add(inputPanel, BorderLayout.SOUTH);
    }

    /**
     * Creates the main frame and displays this UI.
     * There is no icon or background image in this version.
     */
    public void createAndShowGUI() {
        JFrame frame = new JFrame("Wine Database System");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(800, 600);
        frame.setResizable(true);

        frame.add(this);
        frame.setLocationRelativeTo(null); // Center the window
        frame.setVisible(true);

    BufferedImage iconImage = null;
        try {
        iconImage = ImageIO.read(new File(ICON_PATH));
        frame.setIconImage(iconImage);
    } catch (
    IOException e) {
        LOGGER.log(Level.WARNING, "Could not load icon image: {0}", e.getMessage());
    }
    }

    // -------------------------------------------------------
    //                  Query Selection Logic
    // -------------------------------------------------------

    private void onQuerySelected(String selectedQuery) {
        // Clear any existing inputs or table data
        inputPanel.removeAll();
        clearTable();

        // Create a GridBagConstraints object for controlling component placement
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(5, 10, 5, 10); // Add some padding around components

        switch (selectedQuery) {
            case "Get All Wines":
                runQueryInBackground(QueryHandler::getAllWines);
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
                    runQueryInBackground(() -> QueryHandler.getLimitWines(limit));
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
                    runQueryInBackground(() -> QueryHandler.getWinesByQuality(quality));
                }, gbc, 1);
                break;

            case "Get Wines by Alcohol Range":
                addInputField("Min Alcohol:", "minAlcohol", gbc, 0);
                addInputField("Max Alcohol:", "maxAlcohol", gbc, 1);
                addExecuteButton(e -> {
                    String minStr = getFieldValue("minAlcohol");
                    String maxStr = getFieldValue("maxAlcohol");

                    if (minStr.isEmpty() && maxStr.isEmpty()) {
                        showError("Please enter either minimum or maximum alcohol values.");
                        return;
                    }

                    double minAlcohol = minStr.isEmpty() ? -1 : Double.parseDouble(minStr);
                    double maxAlcohol = maxStr.isEmpty() ? -1 : Double.parseDouble(maxStr);

                    runQueryInBackground(() ->
                            QueryHandler.getWinesByAlcoholRange(minAlcohol, maxAlcohol)
                    );
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
                    runQueryInBackground(() -> QueryHandler.getWinesByColor(color));
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
                    runQueryInBackground(() -> QueryHandler.getWinesById(ids));
                }, gbc, 1);
                break;

            case "Get Wines by Date Range":
                addDateSpinner("Start Date (YYYY-MM-DD):", "startDateSpinner", gbc, 0);
                addDateSpinner("End Date (YYYY-MM-DD):",   "endDateSpinner",   gbc, 1);
                addExecuteButton(e -> {
                    String startDate = getSpinnerDateValue("startDateSpinner");
                    String endDate   = getSpinnerDateValue("endDateSpinner");

                    if (startDate.isEmpty() && endDate.isEmpty()) {
                        showError("Please select at least one date.");
                        return;
                    }
                    runQueryInBackground(() ->
                            QueryHandler.getWinesByDateRange(startDate, endDate)
                    );
                }, gbc, 2);
                break;

            case "Get Wines by pH Range":
                addInputField("Min pH:", "minPH", gbc, 0);
                addInputField("Max pH:", "maxPH", gbc, 1);
                addExecuteButton(e -> {
                    String minPHStr = getFieldValue("minPH");
                    String maxPHStr = getFieldValue("maxPH");

                    if (minPHStr.isEmpty() && maxPHStr.isEmpty()) {
                        showError("Please enter at least one pH value (min or max).");
                        return;
                    }

                    runQueryInBackground(() -> QueryHandler.getWinesByPH(minPHStr, maxPHStr));
                }, gbc, 2);
                break;
        }

        inputPanel.revalidate();
        inputPanel.repaint();
    }

    // -------------------------------------------------------
    //          MULTI-THREADING WITH SWINGWORKER
    // -------------------------------------------------------

    /**
     * Runs the given query in the background using SwingWorker,
     * shows a progress bar while running, and displays the result
     * in the table when complete.
     */
    private void runQueryInBackground(Supplier<QueryResult> querySupplier) {
        // Show the progress bar
        showProgressBar(true);

        SwingWorker<QueryResult, Void> worker = new SwingWorker<>() {
            @Override
            protected QueryResult doInBackground() {
                LOGGER.info("Executing query on thread: " + Thread.currentThread().getName());
                return querySupplier.get();
            }

            @Override
            protected void done() {
                // Hide the progress bar
                showProgressBar(false);

                try {
                    QueryResult result = get();
                    displayQueryResults(result);
                } catch (Exception e) {
                    LOGGER.log(Level.SEVERE,
                            "Error retrieving query results on thread "
                                    + Thread.currentThread().getName() + ": {0}",
                            e.getMessage());
                    showError("Error retrieving query results:\n" + e.getMessage());
                }
            }
        };

        // Start the background task
        worker.execute();
    }

    /**
     * Helper to show/hide the progress bar.
     */
    private void showProgressBar(boolean visible) {
        progressBar.setVisible(visible);
        progressBar.setIndeterminate(visible);
    }

    // -------------------------------------------------------
    //       TABLE DISPLAY + ERROR HANDLING
    // -------------------------------------------------------

    /**
     * Displays query results in the table AND updates the resultsCountLabel.
     * Shows an error if the result set is empty.
     */
    private void displayQueryResults(QueryResult result) {
        // Clear old data first
        clearTable();

        // Add columns
        for (String columnName : result.getColumnNames()) {
            tableModel.addColumn(columnName);
        }

        // Add rows
        for (String[] row : result.getData()) {
            tableModel.addRow(row);
        }

        // Update the count label
        int rowCount = result.getData().length;
        resultsCountLabel.setText("Wines displayed: " + rowCount);

        // If no rows found, show an error message
        if (rowCount == 0) {
            showError("No wines found for the specified query!");
        }
    }

    private void showError(String message) {
        JOptionPane.showMessageDialog(this, message, "Error", JOptionPane.ERROR_MESSAGE);
    }

    /**
     * Clears the table of all rows and columns.
     */
    private void clearTable() {
        tableModel.setRowCount(0);
        tableModel.setColumnCount(0);
    }

    // -------------------------------------------------------
    //       INPUT FIELD HELPERS AND UI COMPONENTS
    // -------------------------------------------------------

    private void addInputField(String label, String name, GridBagConstraints gbc, int row) {
        JLabel jLabel = new JLabel(label);
        JTextField jTextField = new JTextField();
        jTextField.setName(name);
        jTextField.setPreferredSize(new Dimension(200, 25));
        jTextField.setToolTipText("Enter the " + label.toLowerCase());

        gbc.gridx = 0;
        gbc.gridy = row;
        gbc.anchor = GridBagConstraints.LINE_START;
        inputPanel.add(jLabel, gbc);

        gbc.gridx = 1;
        gbc.anchor = GridBagConstraints.CENTER;
        inputPanel.add(jTextField, gbc);
    }

    private void addExecuteButton(ActionListener actionListener, GridBagConstraints gbc, int row) {
        JButton executeButton = new JButton("Execute");
        executeButton.addActionListener(actionListener);
        executeButton.setToolTipText("Click to execute the query");

        gbc.gridx = 1;
        gbc.gridy = row;
        gbc.anchor = GridBagConstraints.CENTER;
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
        qualityComboBox.setPreferredSize(new Dimension(200, 25));
        qualityComboBox.setToolTipText("Choose the wine quality");

        gbc.gridx = 0;
        gbc.gridy = row;
        gbc.anchor = GridBagConstraints.LINE_START;
        inputPanel.add(qualityLabel, gbc);

        gbc.gridx = 1;
        gbc.anchor = GridBagConstraints.CENTER;
        inputPanel.add(qualityComboBox, gbc);
    }

    private void addColorDropdown(GridBagConstraints gbc, int row) {
        JLabel colorLabel = new JLabel("Select Color:");
        String[] colors = {"red", "white"};
        JComboBox<String> colorComboBox = new JComboBox<>(colors);
        colorComboBox.setName("color");
        colorComboBox.setPreferredSize(new Dimension(200, 25));
        colorComboBox.setToolTipText("Choose the wine color");

        gbc.gridx = 0;
        gbc.gridy = row;
        gbc.anchor = GridBagConstraints.LINE_START;
        inputPanel.add(colorLabel, gbc);

        gbc.gridx = 1;
        gbc.anchor = GridBagConstraints.CENTER;
        inputPanel.add(colorComboBox, gbc);
    }

    private void addDateSpinner(String labelText, String spinnerName, GridBagConstraints gbc, int row) {
        JLabel label = new JLabel(labelText);
        gbc.gridx = 0;
        gbc.gridy = row;
        gbc.anchor = GridBagConstraints.LINE_START;
        inputPanel.add(label, gbc);

        SpinnerDateModel dateModel = new SpinnerDateModel();
        JSpinner dateSpinner = new JSpinner(dateModel);
        dateSpinner.setName(spinnerName);

        // Configure how the spinner displays dates: "yyyy-MM-dd"
        JSpinner.DateEditor editor = new JSpinner.DateEditor(dateSpinner, "yyyy-MM-dd");
        dateSpinner.setEditor(editor);

        gbc.gridx = 1;
        gbc.anchor = GridBagConstraints.CENTER;
        inputPanel.add(dateSpinner, gbc);
    }

    private String getSpinnerDateValue(String spinnerName) {
        for (Component comp : inputPanel.getComponents()) {
            if (comp instanceof JSpinner && spinnerName.equals(comp.getName())) {
                JSpinner spinner = (JSpinner) comp;
                Object value = spinner.getValue();
                if (value instanceof Date) {
                    // Convert to "yyyy-MM-dd"
                    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
                    return sdf.format((Date) value);
                }
            }
        }
        return "";
    }
}
