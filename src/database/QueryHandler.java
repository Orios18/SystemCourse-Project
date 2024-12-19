package database;

import java.sql.*;

public class QueryHandler {

    // Method to get all wines
    public static String getAllWines() {
        String sql = "SELECT * FROM wine_table";
        System.out.println("Executing query: " + sql);
        return executeQuery(sql);
    }

    public static String getLimitWines(int limit) {
        String sql = "SELECT * FROM wine_table LIMIT " + limit;
        System.out.println("Executing query: " + sql);
        return executeQuery(sql);
    }

    // Method to get wines based on a specific quality
    public static String getWinesByQuality(String quality) {
        // Escape single quotes in the quality string to prevent SQL syntax errors
        String escapedQuality = quality.replace("'", "''");
        String sql = "SELECT * FROM wine_table WHERE quality = '" + escapedQuality + "'";
        System.out.println("Executing query for quality: " + escapedQuality);
        return executeQuery(sql);
    }

    // Method to get wines based on alcohol content range
    public static String getWinesByAlcoholRange(double minAlcohol, double maxAlcohol) {
        String sql = "SELECT * FROM wine_table WHERE alcohol BETWEEN " + minAlcohol + " AND " + maxAlcohol;
        System.out.println(sql);
        return executeQuery(sql);
    }

    // Method to get wines based on color
    public static String getWinesByColor(String color) {
        String sql = "SELECT * FROM wine_table WHERE color = '" + color + "'";
        System.out.println(sql);
        return executeQuery(sql);
    }

    // Method to execute a query and return results as a string
    private static String executeQuery(String sql) {
        StringBuilder result = new StringBuilder();
        try (Connection conn = DBConnection.connect();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            // Print column headers
            int columnCount = rs.getMetaData().getColumnCount();
            result.append("Results:\n");

            // Add headers with proper alignment
            for (int i = 1; i <= columnCount; i++) {
                String columnName = rs.getMetaData().getColumnName(i);
                result.append(String.format("%-20s", columnName)); // Align header to the left with space padding
            }
            result.append("\n");

            // Add a separator line
            result.append(new String(new char[320]).replace("\0", "-")).append("\n");

            // Print data row by row
            while (rs.next()) {
                for (int i = 1; i <= columnCount; i++) {
                    String value = rs.getString(i); // Get the value from the column
                    if (value == null) {
                        value = "NULL"; // If the value is null, display "NULL"
                    }
                    result.append(String.format("%-25s", value)); // Align data under headers with space padding
                }
                result.append("\n");
            }
        } catch (SQLException e) {
            result.append("Error executing query: ").append(e.getMessage());
        }

        return result.toString();  // Return the formatted string containing the query results
    }
}
