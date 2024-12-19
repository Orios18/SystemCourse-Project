package database;

import java.sql.Connection;
import java.sql.Statement;
import java.nio.file.Paths;

public class DataLoader {
    public static void loadCSV() {
        String filePath = Paths.get("src", "resources", "wine.csv").toString();
        try (Connection conn = DBConnection.connect();
             Statement stmt = conn.createStatement()) {

            // Ensure quality column is a VARCHAR, if necessary
            String alterTableQuery = "ALTER TABLE wine_table MODIFY quality VARCHAR(50);";
            stmt.executeUpdate(alterTableQuery);

            // Load the CSV data into the table
            String query = "LOAD DATA LOCAL INFILE '" + filePath.replace("\\", "\\\\") + "' " +
                    "INTO TABLE wine_table " +
                    "FIELDS TERMINATED BY ',' ENCLOSED BY '\"' " +
                    "LINES TERMINATED BY '\\n' IGNORE 1 ROWS " +
                    "(fixed_acidity, volatile_acidity, citric_acid, residual_sugar, " +
                    "chlorides, free_sulfur_dioxide, total_sulfur_dioxide, density, pH, sulphates, alcohol, quality, color)";

            stmt.executeUpdate(query);
            System.out.println("Data loaded successfully.");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
