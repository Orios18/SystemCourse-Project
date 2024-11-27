package database;

import java.sql.*;

public class QueryHandler {

    // Method to get all wines
    public static void getAllWines() {
        String sql = "SELECT * FROM wine_table";
        System.out.println("Executing query: " + sql);
        executeQuery(sql);
    }

    public static void getLimitWines(int limit) {
        String sql = "SELECT * FROM wine_table LIMIT " + limit;
        System.out.println("Executing query: " + sql);
        executeQuery(sql);
    }


    // Method to get wines based on a specific quality
    public static void getWinesByQuality(int quality) {
        String sql = "SELECT * FROM wine_table WHERE quality = " + quality;
        executeQuery(sql);
    }

    // Method to get wines based on alcohol content range
    public static void getWinesByAlcoholRange(double minAlcohol, double maxAlcohol) {
        String sql = "SELECT * FROM wine_table WHERE alcohol BETWEEN " + minAlcohol + " AND " + maxAlcohol;
        executeQuery(sql);
    }

    // Method to get wines based on color
    public static void getWinesByColor(String color) {
        String sql = "SELECT * FROM wine_table WHERE color = '" + color + "'";
        executeQuery(sql);
    }

    // Method to execute a query and print results
    private static void executeQuery(String sql) {
        try (Connection conn = DBConnection.connect();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            // Print column headers (optional)
            int columnCount = rs.getMetaData().getColumnCount();
            System.out.print("Columns: \n\n");
            for (int i = 1; i <= columnCount; i++) {
                System.out.print(rs.getMetaData().getColumnName(i) + "\t");
            }
            System.out.println();  // End the header line

            // Print data row by row
            while (rs.next()) {
                // Retrieve each column by name and print
                String fixedAcidity = rs.getString("fixed_acidity");
                String volatileAcidity = rs.getString("volatile_acidity");
                String citricAcid = rs.getString("citric_acid");
                String residualSugar = rs.getString("residual_sugar");
                String chlorides = rs.getString("chlorides");
                String freeSulfurDioxide = rs.getString("free_sulfur_dioxide");
                String totalSulfurDioxide = rs.getString("total_sulfur_dioxide");
                String density = rs.getString("density");
                String pH = rs.getString("pH");
                String sulphates = rs.getString("sulphates");
                String alcohol = rs.getString("alcohol");
                String quality = rs.getString("quality");
                String color = rs.getString("color");

                // Print out each row's data in a readable format
                System.out.println(fixedAcidity + "\t" + volatileAcidity + "\t" + citricAcid + "\t" + residualSugar +
                        "\t" + chlorides + "\t" + freeSulfurDioxide + "\t" + totalSulfurDioxide + "\t" +
                        density + "\t" + pH + "\t" + sulphates + "\t" + alcohol + "\t" + quality + "\t" + color);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}