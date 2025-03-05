package database;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Handles database queries for the Wine Database System.
 */
public class QueryHandler {

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

    /**
     * Executes a SQL query and returns the results.
     *
     * @param sql The SQL query to execute.
     * @return QueryResult containing column names and data rows.
     */
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

    /**
     * Executes a custom SQL query with optional WHERE filters and LIMIT.
     *
     * @param baseQuery     The base SQL query (e.g., "SELECT * FROM wine_table").
     * @param whereFilters  A list of SQL conditions to apply in the WHERE clause.
     * @param limitFilter   The LIMIT clause value (e.g., "10"), or null if not applicable.
     * @return QueryResult containing column names and data rows.
     */
    public static QueryResult executeCustomQuery(String baseQuery, List<String> whereFilters, String limitFilter) {
        StringBuilder sql = new StringBuilder(baseQuery);
        if (whereFilters != null && !whereFilters.isEmpty()) {
            sql.append(" WHERE ");
            sql.append(String.join(" AND ", whereFilters));
        }
        if (limitFilter != null && !limitFilter.isEmpty()) {
            sql.append(" LIMIT ").append(limitFilter);
        }
        System.out.println("Executing custom query: " + sql.toString());
        return executeQuery(sql.toString());
    }

    /**
     * Retrieves all wines without any filters.
     *
     * @return QueryResult containing all wines.
     */
    public static QueryResult getAllWines() {
        String sql = "SELECT * FROM wine_table";
        System.out.println("Executing query: " + sql);
        return executeQuery(sql);
    }

    /**
     * Retrieves wines with a specified limit.
     *
     * @param limit The maximum number of wines to retrieve.
     * @return QueryResult containing the limited set of wines.
     */
    public static QueryResult getLimitWines(int limit) {
        String sql = "SELECT * FROM wine_table LIMIT " + limit;
        System.out.println("Executing query: " + sql);
        return executeQuery(sql);
    }

    /**
     * Retrieves wines by a specific quality.
     *
     * @param quality The quality to filter by (e.g., "slightly satisfied").
     * @return QueryResult containing wines of the specified quality.
     */
    public static QueryResult getWinesByQuality(String quality) {
        String escapedQuality = quality.replace("'", "''");
        String sql = "SELECT * FROM wine_table WHERE quality = '" + escapedQuality + "'";
        System.out.println("Executing query for quality: " + escapedQuality);
        return executeQuery(sql);
    }

    /**
     * Retrieves wines within a specified alcohol range.
     *
     * @param minAlcohol The minimum alcohol level.
     * @param maxAlcohol The maximum alcohol level.
     * @return QueryResult containing wines within the specified alcohol range.
     */
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

    /**
     * Retrieves wines by color.
     *
     * @param color The color to filter by (e.g., "red").
     * @return QueryResult containing wines of the specified color.
     */
    public static QueryResult getWinesByColor(String color) {
        String escapedColor = color.replace("'", "''");
        String sql = "SELECT * FROM wine_table WHERE color = '" + escapedColor + "'";
        System.out.println("Executing color query: " + sql);
        return executeQuery(sql);
    }

    /**
     * Retrieves wines by ID(s) or a range of IDs.
     *
     * @param ids The IDs or range to filter by (e.g., "1,2,3" or "5-10").
     * @return QueryResult containing wines matching the specified IDs or range.
     */
    public static QueryResult getWinesById(String ids) {
        String sql;

        if (ids.contains("-")) {
            // Range case (e.g., 5-10)
            String[] range = ids.split("-");
            if (range.length == 2) {
                sql = "SELECT * FROM wine_table WHERE id BETWEEN " + range[0].trim() + " AND " + range[1].trim();
            } else {
                // Invalid range format
                System.err.println("Invalid ID range format.");
                return new QueryResult(new String[]{}, new String[][]{});
            }
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

    /**
     * Retrieves wines within a specified date range.
     *
     * @param startDate The start date (inclusive) in 'YYYY-MM-DD' format.
     * @param endDate   The end date (inclusive) in 'YYYY-MM-DD' format.
     * @return QueryResult containing wines within the specified date range.
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
            sql = "SELECT * FROM wine_table"; // No date filters applied
        }
        System.out.println("Executing date range query: " + sql);
        return executeQuery(sql);
    }

    /**
     * Retrieves wines within a specified pH range.
     *
     * @param minPHStr The minimum pH level as a string.
     * @param maxPHStr The maximum pH level as a string.
     * @return QueryResult containing wines within the specified pH range.
     */
    public static QueryResult getWinesByPH(String minPHStr, String maxPHStr) {
        String sql;

        boolean hasMin = (minPHStr != null && !minPHStr.isEmpty());
        boolean hasMax = (maxPHStr != null && !maxPHStr.isEmpty());

        if (hasMin && hasMax) {
            // BETWEEN
            sql = "SELECT * FROM wine_table WHERE `pH` BETWEEN " + minPHStr + " AND " + maxPHStr;
        } else if (hasMin) {
            // >=
            sql = "SELECT * FROM wine_table WHERE `pH` >= " + minPHStr;
        } else if (hasMax) {
            // <=
            sql = "SELECT * FROM wine_table WHERE `pH` <= " + maxPHStr;
        } else {
            // No pH filters applied
            sql = "SELECT * FROM wine_table";
        }

        System.out.println("Executing pH query: " + sql);
        return executeQuery(sql);
    }
}
