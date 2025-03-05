# Wine Database System
- Ori Azuviv , 208741009
- Amit Amsalem , 322795923
- Yehiel Roimi , 315037234

## Description
The Wine Database System is a Java-based application that allows users to perform queries on a large wine database and display the results in a graphical user interface (GUI). The backend is built with Java, the GUI with Java Swing, and the database is managed using MySQL.

## Features
- Perform complex queries on a wine database.
- Display query results in a user-friendly GUI.
- Built with Java, Java Swing, and MySQL.

## Installation

### Prerequisites
Before running the application, ensure you have the following installed:
- **Java Development Kit (JDK)**: Version 8 or higher.
- **MySQL**: Installed and running on your machine.
- **MySQL Connector/J**: The Java JDBC driver for MySQL (included in the project or available [here](https://dev.mysql.com/downloads/connector/j/)).

### Steps to Set Up
1. **Clone the repository** (if applicable):
   ```bash
   git clone https://github.com/Orios18/SystemCourse-Project.git


2. **Set up the MySQL database**:
- Open MySQL and create a new database (e.g., `wine_db`).
- Use the following command to log in to MySQL:
  ```
  mysql -u your_username -p
  ```
- Create the database:
  ```
  CREATE DATABASE wine_db;
  USE wine_db;
  ```

3. **Import the CSV file into MySQL**:
- Ensure the CSV file is in the correct format and located in the project directory.
- Create a table in MySQL to match the structure of the CSV file. For example:
  ```
  CREATE TABLE wines (
    id INT PRIMARY KEY AUTO_INCREMENT,
    fixed_acidity DECIMAL(4,2),
    volatile_acidity DECIMAL(4,2),
    citric_acid DECIMAL(4,2),
    residual_sugar DECIMAL(5,2),
    chlorides DECIMAL(4,3),
    free_sulfur_dioxide INT,
    total_sulfur_dioxide INT,
    density DECIMAL(6,5),
    pH DECIMAL(3,2),
    sulphates DECIMAL(4,2),
    alcohol DECIMAL(4,1),
    quality VARCHAR(50),
    color ENUM('red', 'white'),
    date DATE
);

  
- Use the `LOAD DATA INFILE` command to import the CSV file into the `wines` table:
  ```
  LOAD DATA INFILE 'path/to/your/wine.csv'
  INTO TABLE wines
  FIELDS TERMINATED BY ','
  ENCLOSED BY '"'
  LINES TERMINATED BY '\n'
  IGNORE 1 ROWS;
  
  Replace `path/to/your/wine.csv` with the actual path to your CSV file.
  ```
-Use the `ALTER TABLE` command to add an id column to your table.
 ```
 ALTER TABLE your_table ADD COLUMN id INT AUTO_INCREMENT PRIMARY KEY;
 ``` 

4. **Configure the database connection**:

- Open the project in your IDE (e.g., IntelliJ IDEA, Eclipse).
- Locate the database configuration file (e.g., `DBConfig.java` or similar).
- Update the database connection details (URL, username, password) to match your MySQL setup. For example:
  
  String url = "jdbc:mysql://localhost:3306/wine_db";
  String username = "your_username";
  String password = "your_password";
  

5. **Add MySQL Connector/J to your project**:
- If the connector is not already included, download the JAR file from the [official MySQL website](https://dev.mysql.com/downloads/connector/j/).
- Add the JAR file to your project’s build path in your IDE.

6. **Run the application**:
- Open the main class (e.g., `Main.java`) in your IDE.
- Click the **Run** button to start the application.

## Usage
1. Launch the application.
2. Use the GUI to choose your query.
3. View the results displayed in the GUI.

## Built With
- **Backend**: Java
- **GUI**: Java Swing
- **Database**: MySQL

## Contact
If you have any questions or feedback, feel free to reach out:
- **Email**: Oriazuviv111@gmail.com
- **GitHub**: [Orios18](https://github.com/Orios18)