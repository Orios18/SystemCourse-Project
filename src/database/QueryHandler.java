package database;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class QueryHandler {

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

    // Execute a query and return results
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

    // ---------------- Existing Queries ---------------- //

    public static QueryResult getAllWines() {
        String sql = "SELECT * FROM wine_table";
        System.out.println("Executing query: " + sql);
        return executeQuery(sql);
    }

    public static QueryResult getLimitWines(int limit) {
        String sql = "SELECT * FROM wine_table LIMIT " + limit;
        System.out.println("Executing query: " + sql);
        return executeQuery(sql);
    }

    public static QueryResult getWinesByQuality(String quality) {
        String escapedQuality = quality.replace("'", "''");
        String sql = "SELECT * FROM wine_table WHERE quality = '" + escapedQuality + "'";
        System.out.println("Executing query for quality: " + escapedQuality);
        return executeQuery(sql);
    }

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

    public static QueryResult getWinesByColor(String color) {
        String sql = "SELECT * FROM wine_table WHERE color = '" + color + "'";
        System.out.println(sql);
        return executeQuery(sql);
    }

    public static QueryResult getWinesById(String ids) {
        String sql;

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

    // ---------------- New Date-Range Query ---------------- //

    /**
     * Get wines by date or date range.
     * If both startDate & endDate are provided, use BETWEEN.
     * If only startDate is provided, use >= that date.
     * If only endDate is provided, use <= that date.
     * If neither, you could handle differently (currently returns all).
     */
    public static QueryResult getWinesByDateRange(String startDate, String endDate) {
        String sql;
        if (!startDate.isEmpty() && !endDate.isEmpty()) {
            sql = "SELECT * FROM wine_table WHERE `date` BETWEEN '" + startDate + "' AND '" + endDate + "'";
        } else if (!startDate.isEmpty()) {
            sql = "SELECT * FROM wine_table WHERE `date` >= '" + startDate + "'";
        } else if (!endDate.isEmpty()) {
            sql = "SELECT * FROM wine_table WHERE `date` <= '" + endDate + "'";
        } else {
            sql = "SELECT * FROM wine_table"; // or throw an error
        }
        System.out.println("Executing date range query: " + sql);
        return executeQuery(sql);
    }
}
