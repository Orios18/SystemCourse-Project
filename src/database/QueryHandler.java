package database;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class QueryHandler {


    public static QueryResult getWinesById(String ids) {
        String sql = "";

        if (ids.contains("-")) {
            // Range case (e.g., 5-10)
            String[] range = ids.split("-");
            sql = "SELECT * FROM wine_table WHERE id BETWEEN " + range[0] + " AND " + range[1];
        } else if (ids.contains(",")) {
            // Multiple IDs case (e.g., 1,2,3)
            sql = "SELECT * FROM wine_table WHERE id IN (" + ids + ")";
        } else {
            // Single ID case (e.g., 5)
            sql = "SELECT * FROM wine_table WHERE id = " + ids;
        }

        System.out.println("Executing ID query: " + sql);
        return executeQuery(sql);
    }
    // Method to get all wines
    public static QueryResult getAllWines() {
        String sql = "SELECT * FROM wine_table";
        System.out.println("Executing query: " + sql);
        return executeQuery(sql);
    }

    // Method to get limited wines
    public static QueryResult getLimitWines(int limit) {
        String sql = "SELECT * FROM wine_table LIMIT " + limit;
        System.out.println("Executing query: " + sql);
        return executeQuery(sql);
    }

    // Method to get wines by quality
    public static QueryResult getWinesByQuality(String quality) {
        String escapedQuality = quality.replace("'", "''");
        String sql = "SELECT * FROM wine_table WHERE quality = '" + escapedQuality + "'";
        System.out.println("Executing query for quality: " + escapedQuality);
        return executeQuery(sql);
    }

    // Method to get wines based on alcohol content range (updated)
    public static QueryResult getWinesByAlcoholRange(double minAlcohol, double maxAlcohol) {
        String sql = "";
        if (minAlcohol != -1 && maxAlcohol != -1) {
            sql = "SELECT * FROM wine_table WHERE alcohol BETWEEN " + minAlcohol + " AND " + maxAlcohol;
        } else if (minAlcohol != -1) {
            sql = "SELECT * FROM wine_table WHERE alcohol >= " + minAlcohol;
        } else if (maxAlcohol != -1) {
            sql = "SELECT * FROM wine_table WHERE alcohol <= " + maxAlcohol;
        }
        System.out.println("Executing alcohol range query: " + sql);
        return executeQuery(sql);
    }


    // Method to get wines by color
    public static QueryResult getWinesByColor(String color) {
        String sql = "SELECT * FROM wine_table WHERE color = '" + color + "'";
        System.out.println(sql);
        return executeQuery(sql);
    }

    // Method to execute a query and return results for JTable
    private static QueryResult executeQuery(String sql) {
        List<String[]> data = new ArrayList<>();
        List<String> columnNames = new ArrayList<>();

        try (Connection conn = DBConnection.connect();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            // Retrieve column names
            ResultSetMetaData metaData = rs.getMetaData();
            int columnCount = metaData.getColumnCount();

            for (int i = 1; i <= columnCount; i++) {
                columnNames.add(metaData.getColumnName(i));
            }

            // Retrieve data rows
            while (rs.next()) {
                String[] row = new String[columnCount];
                for (int i = 1; i <= columnCount; i++) {
                    row[i - 1] = rs.getString(i) != null ? rs.getString(i) : "NULL";
                }
                data.add(row);
            }

        } catch (SQLException e) {
            System.err.println("Error executing query: " + e.getMessage());
        }

        return new QueryResult(
                columnNames.toArray(new String[0]),
                data.toArray(new String[0][])
        );
    }

    // Utility class to store query results
    public static class QueryResult {
        private final String[] columnNames;
        private final String[][] data;

        public QueryResult(String[] columnNames, String[][] data) {
            this.columnNames = columnNames;
            this.data = data;
        }

        public String[] getColumnNames() {
            return columnNames;
        }

        public String[][] getData() {
            return data;
        }
    }
}
