package UI;

import database.QueryHandler;
import database.QueryHandler.QueryResult;

import javax.swing.*;
import javax.swing.border.TitledBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.List;
import java.util.function.Supplier;
import java.util.logging.Level;
import java.util.logging.Logger;


public class GenericUIApp extends JPanel {


    private static final Logger LOGGER = Logger.getLogger(GenericUIApp.class.getName());

    private JTable outputTable;
    private DefaultTableModel tableModel;
    private JPanel inputPanel;

    private JLabel resultsCountLabel;  // Shows how many wines are displayed
    private JProgressBar progressBar;  // Shows loading progress

    private List<String> activeFilters = new ArrayList<>(); // Regular filters
    private String limitFilter = null;                       // LIMIT filter

    private Map<String, String> filterMap = new HashMap<>();  // Maps display descriptions to filter strings

    // Panel to display active filters
    private JPanel activeFiltersPanel;

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

        // ---------- TOP PANEL (query selection + results count + progress bar + reset filters) ----------
        JPanel topPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        topPanel.setBackground(new Color(141, 141, 141));

        // Create combo box for queries
        String[] queries = {
                "Select Query",
                "Get Limit Wines",
                "Get Wines by Quality",
                "Get Wines by Alcohol Range",
                "Get Wines by Color",
                "Get Wines by ID",
                "Get Wines by Date Range",
                "Get Wines by pH Range"
        };
        JComboBox<String> queryComboBox = new JComboBox<>(queries);
        queryComboBox.addActionListener(e -> {
            String selected = (String) queryComboBox.getSelectedItem();
            if (!"Select Query".equals(selected)) {
                onQuerySelected(selected);
            }
        });
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

        // Reset Filters button
        JButton resetFiltersButton = new JButton("Reset Filters");
        resetFiltersButton.addActionListener(e -> resetFilters());
        topPanel.add(resetFiltersButton);

        add(topPanel, BorderLayout.NORTH);

        // ---------- BOTTOM PANEL (dynamic input fields) ----------
        inputPanel = new JPanel();
        inputPanel.setLayout(new GridBagLayout());
        inputPanel.setBackground(new Color(140, 140, 140));
        add(inputPanel, BorderLayout.SOUTH);

        // ---------- SIDE PANEL (Active Filters) ----------
        activeFiltersPanel = new JPanel();
        activeFiltersPanel.setLayout(new BoxLayout(activeFiltersPanel, BoxLayout.Y_AXIS));
        activeFiltersPanel.setBorder(new TitledBorder("Active Filters"));
        activeFiltersPanel.setBackground(new Color(200, 200, 200));
        activeFiltersPanel.setPreferredSize(new Dimension(250, 0)); // Fixed width, height adjusts automatically
        add(activeFiltersPanel, BorderLayout.EAST);

        // Initially load all wines
        runCustomQuery();
    }

    // -------------------------------------------------------
    //                  Query Selection Logic
    // -------------------------------------------------------

    /**
     * Called whenever the user selects an item from the query combo box.
     * Adds a new filter based on the selection and updates the table.
     *
     * @param selectedQuery The query selected by the user.
     */
    private void onQuerySelected(String selectedQuery) {
        // Clear any existing input fields
        inputPanel.removeAll();
        inputPanel.revalidate();
        inputPanel.repaint();

        // Create a GridBagConstraints object for controlling component placement
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(5, 10, 5, 10); // Add some padding around components

        switch (selectedQuery) {
            case "Get All Wines":
                resetFilters();
                runCustomQuery();
                break;

            case "Get Limit Wines":
                addInputField("Limit:", "limit", gbc, 0);
                addExecuteButton(e -> {
                    String limitStr = getFieldValue("limit");
                    if (limitStr.isEmpty()) {
                        showError("Please enter a limit value.");
                        return;
                    }
                    try {
                        int limit = Integer.parseInt(limitStr);
                        // If a LIMIT filter already exists, remove it first
                        if (limitFilter != null) {
                            removeActiveFilter("LIMIT " + limitFilter);
                        }
                        limitFilter = String.valueOf(limit);
                        runCustomQuery();
                        addActiveFilter("LIMIT " + limit, "LIMIT " + limit);
                    } catch (NumberFormatException ex) {
                        showError("Limit must be a valid integer.");
                    }
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
                    String filterString = "quality = '" + quality.replace("'", "''") + "'";
                    String displayDescription = "Quality: " + quality;
                    if (!activeFilters.contains(filterString)) {
                        activeFilters.add(filterString);
                        runCustomQuery();
                        addActiveFilter(displayDescription, filterString);
                    } else {
                        showError("This quality filter is already applied.");
                    }
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

                    try {
                        double minAlcohol = minStr.isEmpty() ? -1 : Double.parseDouble(minStr);
                        double maxAlcohol = maxStr.isEmpty() ? -1 : Double.parseDouble(maxStr);

                        String filter = "";
                        String displayFilter = "";
                        if (minAlcohol >= 0 && maxAlcohol >= 0) {
                            filter = "alcohol BETWEEN " + minAlcohol + " AND " + maxAlcohol;
                            displayFilter = "Alcohol: " + minAlcohol + " - " + maxAlcohol;
                        } else if (minAlcohol >= 0) {
                            filter = "alcohol >= " + minAlcohol;
                            displayFilter = "Alcohol >= " + minAlcohol;
                        } else if (maxAlcohol >= 0) {
                            filter = "alcohol <= " + maxAlcohol;
                            displayFilter = "Alcohol <= " + maxAlcohol;
                        }

                        if (!activeFilters.contains(filter)) {
                            activeFilters.add(filter);
                            runCustomQuery();
                            addActiveFilter(displayFilter, filter);
                        } else {
                            showError("This alcohol range filter is already applied.");
                        }
                    } catch (NumberFormatException ex) {
                        showError("Alcohol values must be valid numbers.");
                    }
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
                    String filterString = "color = '" + color.replace("'", "''") + "'";
                    String displayDescription = "Color: " + color;
                    if (!activeFilters.contains(filterString)) {
                        activeFilters.add(filterString);
                        runCustomQuery();
                        addActiveFilter(displayDescription, filterString);
                    } else {
                        showError("This color filter is already applied.");
                    }
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

                    String filterString;
                    String displayDescription = "ID(s): " + ids;

                    if (ids.contains("-")) {
                        // Range case (e.g., 2-7)
                        String[] range = ids.split("-");
                        if (range.length == 2) {
                            String start = range[0].trim();
                            String end = range[1].trim();

                            // Validate that start and end are integers
                            if (isValidInteger(start) && isValidInteger(end)) {
                                filterString = "id BETWEEN " + start + " AND " + end;
                                displayDescription = "ID Range: " + start + " - " + end;
                            } else {
                                showError("Invalid ID range. Please enter valid integers.");
                                return;
                            }
                        } else {
                            showError("Invalid range format. Use format x-y (e.g., 2-7).");
                            return;
                        }
                    } else if (ids.contains(",")) {
                        // Multiple IDs case (e.g., 1,2,3)
                        String[] idArray = ids.split(",");
                        boolean allValid = true;
                        StringBuilder validIds = new StringBuilder();

                        for (String id : idArray) {
                            String trimmedId = id.trim();
                            if (isValidInteger(trimmedId)) {
                                if (validIds.length() > 0) {
                                    validIds.append(",");
                                }
                                validIds.append(trimmedId);
                            } else {
                                allValid = false;
                                break;
                            }
                        }

                        if (allValid) {
                            filterString = "id IN (" + validIds.toString() + ")";
                        } else {
                            showError("Invalid ID format. Please enter valid integers separated by commas.");
                            return;
                        }
                    } else {
                        // Single ID case (e.g., 5)
                        String trimmedId = ids.trim();
                        if (isValidInteger(trimmedId)) {
                            filterString = "id = " + trimmedId;
                        } else {
                            showError("Invalid ID format. Please enter a valid integer.");
                            return;
                        }
                    }

                    // Check for duplicate filters
                    if (!activeFilters.contains(filterString)) {
                        activeFilters.add(filterString);
                        runCustomQuery();
                        addActiveFilter(displayDescription, filterString);
                    } else {
                        showError("This ID filter is already applied.");
                    }
                }, gbc, 1);
                break;

            case "Get Wines by Date Range":
                addDateSpinner("Start Date (YYYY-MM-DD):", "startDateSpinner", gbc, 0);
                addDateSpinner("End Date (YYYY-MM-DD):", "endDateSpinner", gbc, 1);
                addExecuteButton(e -> {
                    String startDate = getSpinnerDateValue("startDateSpinner");
                    String endDate = getSpinnerDateValue("endDateSpinner");

                    if (startDate.isEmpty() && endDate.isEmpty()) {
                        showError("Please select at least one date.");
                        return;
                    }

                    String filter = "";
                    String displayFilter = "";
                    if (!startDate.isEmpty() && !endDate.isEmpty()) {
                        filter = "`date` BETWEEN '" + startDate + "' AND '" + endDate + "'";
                        displayFilter = "Date: " + startDate + " - " + endDate;
                    } else if (!startDate.isEmpty()) {
                        filter = "`date` >= '" + startDate + "'";
                        displayFilter = "Date >= " + startDate;
                    } else if (!endDate.isEmpty()) {
                        filter = "`date` <= '" + endDate + "'";
                        displayFilter = "Date <= " + endDate;
                    }

                    if (!activeFilters.contains(filter)) {
                        activeFilters.add(filter);
                        runCustomQuery();
                        addActiveFilter(displayFilter, filter);
                    } else {
                        showError("This date range filter is already applied.");
                    }
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

                    try {
                        double minPH = minPHStr.isEmpty() ? -1 : Double.parseDouble(minPHStr);
                        double maxPH = maxPHStr.isEmpty() ? -1 : Double.parseDouble(maxPHStr);

                        String filter = "";
                        String displayFilter = "";
                        if (minPH >= 0 && maxPH >= 0) {
                            filter = "`pH` BETWEEN " + minPH + " AND " + maxPH;
                            displayFilter = "pH: " + minPH + " - " + maxPH;
                        } else if (minPH >= 0) {
                            filter = "`pH` >= " + minPH;
                            displayFilter = "pH >= " + minPH;
                        } else if (maxPH >= 0) {
                            filter = "`pH` <= " + maxPH;
                            displayFilter = "pH <= " + maxPH;
                        }

                        if (!activeFilters.contains(filter)) {
                            activeFilters.add(filter);
                            runCustomQuery();
                            addActiveFilter(displayFilter, filter);
                        } else {
                            showError("This pH range filter is already applied.");
                        }
                    } catch (NumberFormatException ex) {
                        showError("pH values must be valid numbers.");
                    }
                }, gbc, 2);
                break;
        }

        inputPanel.revalidate();
        inputPanel.repaint();
    }

    // -------------------------------------------------------
    //      MULTI-THREADING WITH SWINGWORKER
    // -------------------------------------------------------

    /**
     * Runs the given query in the background using SwingWorker,
     * shows a progress bar while running, and displays the result
     * in the table when complete.
     *
     * @param querySupplier The supplier that provides the QueryResult.
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
     * Executes a custom query with all active filters and limit.
     */
    private void runCustomQuery() {
        String baseQuery = "SELECT * FROM wine_table";
        runQueryInBackground(() -> QueryHandler.executeCustomQuery(baseQuery, activeFilters, limitFilter));
    }

    /**
     * Helper to show/hide the progress bar.
     *
     * @param visible True to show, false to hide.
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
     *
     * @param result The QueryResult to display.
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

    /**
     * Displays an error message dialog.
     *
     * @param message The error message to display.
     */
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

    /**
     * Adds a labeled input field to the input panel.
     *
     * @param label The label text.
     * @param name  The name identifier for the field.
     * @param gbc   The GridBagConstraints for layout.
     * @param row   The row number in the grid.
     */
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

    /**
     * Adds an execute button to the input panel.
     *
     * @param actionListener The action to perform when clicked.
     * @param gbc            The GridBagConstraints for layout.
     * @param row            The row number in the grid.
     */
    private void addExecuteButton(ActionListener actionListener, GridBagConstraints gbc, int row) {
        JButton executeButton = new JButton("Execute");
        executeButton.addActionListener(actionListener);
        executeButton.setToolTipText("Click to execute the query");

        gbc.gridx = 1;
        gbc.gridy = row;
        gbc.anchor = GridBagConstraints.CENTER;
        inputPanel.add(executeButton, gbc);
    }

    /**
     * Retrieves the value from an input field by its name.
     *
     * @param name The name identifier of the field.
     * @return The text value of the field.
     */
    private String getFieldValue(String name) {
        for (Component component : inputPanel.getComponents()) {
            if (component instanceof JTextField && name.equals(component.getName())) {
                return ((JTextField) component).getText().trim();
            } else if (component instanceof JComboBox && name.equals(component.getName())) {
                Object selected = ((JComboBox<?>) component).getSelectedItem();
                return selected != null ? selected.toString() : "";
            }
        }
        return "";
    }

    /**
     * Adds a quality dropdown to the input panel.
     *
     * @param gbc The GridBagConstraints for layout.
     * @param row The row number in the grid.
     */
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

    /**
     * Adds a color dropdown to the input panel.
     *
     * @param gbc The GridBagConstraints for layout.
     * @param row The row number in the grid.
     */
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

    /**
     * Adds a date spinner to the input panel.
     *
     * @param labelText    The label text.
     * @param spinnerName  The name identifier for the spinner.
     * @param gbc          The GridBagConstraints for layout.
     * @param row          The row number in the grid.
     */
    private void addDateSpinner(String labelText, String spinnerName, GridBagConstraints gbc, int row) {
        JLabel label = new JLabel(labelText);
        gbc.gridx = 0;
        gbc.gridy = row;
        gbc.anchor = GridBagConstraints.LINE_START;
        inputPanel.add(label, gbc);

        // Define the start date, end date, and initial date
        Calendar calendar = Calendar.getInstance();
        calendar.set(2022, Calendar.JANUARY, 1); // Start date: January 1, 2022
        Date startDate = calendar.getTime();

        calendar.set(2024, Calendar.OCTOBER, 12); // End date: October 12, 2024
        Date endDate = calendar.getTime();

        Date initialDate = startDate; // Initial date set to the start date

        // Create the SpinnerDateModel with the start, end, and initial dates
        SpinnerDateModel dateModel = new SpinnerDateModel(initialDate, null, null, Calendar.DAY_OF_MONTH);
        JSpinner dateSpinner = new JSpinner(dateModel);
        dateSpinner.setName(spinnerName);

        // Configure how the spinner displays dates: "yyyy-MM-dd"
        JSpinner.DateEditor editor = new JSpinner.DateEditor(dateSpinner, "yyyy-MM-dd");
        dateSpinner.setEditor(editor);

        // Add a change listener to enforce the date range
        dateSpinner.addChangeListener(e -> {
            Date currentDate = (Date) dateSpinner.getValue();
            if (currentDate.before(startDate)) {
                dateSpinner.setValue(startDate); // Clamp to the start date
            } else if (currentDate.after(endDate)) {
                dateSpinner.setValue(endDate); // Clamp to the end date
            }
        });

        gbc.gridx = 1;
        gbc.anchor = GridBagConstraints.CENTER;
        inputPanel.add(dateSpinner, gbc);
    }

    // -------------------------------------------------------
    //                  Active Filters Management
    // -------------------------------------------------------

    /**
     * Adds a visual representation of an active filter to the activeFiltersPanel.
     *
     * @param displayDescription The display text for the filter (e.g., "Quality: neutral").
     * @param filterString       The actual SQL filter string (e.g., "quality = 'neutral'").
     */
    private void addActiveFilter(String displayDescription, String filterString) {
        // Add to filterMap
        filterMap.put(displayDescription, filterString);

        JPanel filterPanel = new JPanel(new BorderLayout());
        filterPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 30));
        filterPanel.setBackground(new Color(173, 216, 230)); // Light blue background
        filterPanel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));

        JLabel filterLabel = new JLabel(displayDescription);
        filterLabel.setFont(new Font("Arial", Font.PLAIN, 12));

        // Remove Button
        JButton removeButton = new JButton("X");
        removeButton.setMargin(new Insets(2, 5, 2, 5));
        removeButton.setFocusable(false);
        removeButton.setToolTipText("Remove this filter");

        // Action to remove the filter
        removeButton.addActionListener(e -> removeActiveFilter(displayDescription));

        filterPanel.add(filterLabel, BorderLayout.CENTER);
        filterPanel.add(removeButton, BorderLayout.EAST);

        activeFiltersPanel.add(filterPanel);
        activeFiltersPanel.revalidate();
        activeFiltersPanel.repaint();
    }

    /**
     * Removes a specific active filter from the activeFiltersPanel and updates the data.
     *
     * @param displayDescription The display text for the filter to remove.
     */
    private void removeActiveFilter(String displayDescription) {
        String filterString = filterMap.get(displayDescription);
        if (filterString == null) {
            showError("Filter not found.");
            return;
        }

        if (displayDescription.startsWith("LIMIT")) {
            // Handle LIMIT filter
            limitFilter = null;
        } else {
            // Handle regular filters
            activeFilters.remove(filterString);
        }

        // Remove the filter tag from the GUI
        Component toRemove = null;
        for (Component comp : activeFiltersPanel.getComponents()) {
            if (comp instanceof JPanel) {
                JPanel panel = (JPanel) comp;
                JLabel label = (JLabel) panel.getComponent(0);
                if (label.getText().equals(displayDescription)) {
                    toRemove = panel;
                    break;
                }
            }
        }
        if (toRemove != null) {
            activeFiltersPanel.remove(toRemove);
            activeFiltersPanel.revalidate();
            activeFiltersPanel.repaint();
        }

        // Remove from filterMap
        filterMap.remove(displayDescription);

        // Refresh the table with remaining filters
        runCustomQuery();
    }

    /**
     * Removes all visual representations of active filters from the activeFiltersPanel.
     */
    private void clearActiveFiltersDisplay() {
        activeFiltersPanel.removeAll();
        activeFiltersPanel.revalidate();
        activeFiltersPanel.repaint();
        filterMap.clear();
    }

    // -------------------------------------------------------
    //                  Utility Methods
    // -------------------------------------------------------

    /**
     * Resets all active filters and refreshes the table data.
     */
    private void resetFilters() {
        activeFilters.clear();
        limitFilter = null;
        clearActiveFiltersDisplay();
        runCustomQuery();
        JOptionPane.showMessageDialog(this, "All filters have been reset.", "Reset Filters", JOptionPane.INFORMATION_MESSAGE);
    }

    /**
     * Retrieves the date value from a spinner by its name.
     *
     * @param spinnerName The name identifier of the spinner.
     * @return The formatted date string, or empty string if not found.
     */
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
    /**
     * Checks if a given string is a valid integer.
     *
     * @param str The string to check.
     * @return True if valid integer, false otherwise.
     */
    private boolean isValidInteger(String str) {
        try {
            Integer.parseInt(str);
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }

}
