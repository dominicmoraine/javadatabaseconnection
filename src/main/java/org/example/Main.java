package org.example;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
public class Main {
    private static final String DB_URL =
            "jdbc:sqlite:inventory.db";
    private static final int LOW_STOCK_THRESHOLD = 10;
    public static void main(String[] args) {
        System.out.println("Product Inventory Database");
        System.out.println("\nConnecting to database");
        try (Connection connection =
                     DriverManager.getConnection(DB_URL)) {
            System.out.println("Connection successful");
            createTable(connection);
            insertSampleProducts(connection);
            System.out.println("\nAll Products:");
            displayProducts(connection);
            System.out.println("\nLow Stock Products:");
            displayLowStockProducts(connection);
            updateProductQuantity(connection, 6, 500);
            System.out.println("\nUpdated Product List:");
            displayProducts(connection);
        } catch (SQLException e) {
            System.out.println(
                    "Database error: " + e.getMessage()
            );
            e.printStackTrace();
        }
    }
    private static void createTable(Connection connection)
            throws SQLException {

        System.out.println("\nCreating Products table.");
        String sql = """
            CREATE TABLE IF NOT EXISTS Products (
            ProductID INTEGER PRIMARY KEY,
            ProductName TEXT NOT NULL,
            Category TEXT NOT NULL,
            Price REAL NOT NULL,
            Quantity INTEGER NOT NULL
        )
        """;
        try (Statement statement = connection.createStatement()) {

            statement.executeUpdate(sql);

            System.out.println("Table ready.");
        }
    }
    private static void insertSampleProducts(
            Connection connection) throws SQLException {

        System.out.println("\nInserting sample products.");

        String sql = """
            INSERT OR IGNORE INTO Products
            (ProductID, ProductName, Category, Price, Quantity)
            VALUES (?, ?, ?, ?, ?)
                """;

        try (PreparedStatement statement =
                     connection.prepareStatement(sql)) {

            addProduct(statement, 1, "Laptop",
                    "Electronics", 899.99, 7);

            addProduct(statement, 2, "Mouse",
                    "Electronics", 24.99, 35);

            addProduct(statement, 3, "Keyboard",
                    "Electronics", 49.99, 12);

            addProduct(statement, 4, "Desk Chair",
                    "Furniture", 149.99, 5);

            addProduct(statement, 5, "Notebook",
                    "Office Supplies", 3.99, 100);

            addProduct(statement, 6, "Monitor",
                    "Electronics", 199.99, 8);

            System.out.println("Sample products inserted.");
        }
    }
    private static void addProduct(
            PreparedStatement statement,
            int id,
            String name,
            String category,
            double price,
            int quantity) throws SQLException {

        statement.setInt(1, id);
        statement.setString(2, name);
        statement.setString(3, category);
        statement.setDouble(4, price);
        statement.setInt(5, quantity);

        statement.executeUpdate();
    }
    private static void displayProducts(Connection connection)
            throws SQLException {

        String sql = """
            SELECT ProductID, ProductName, Category, Price, Quantity
            FROM Products
            ORDER BY ProductID
                """;

        try (Statement statement = connection.createStatement();
             ResultSet resultSet = statement.executeQuery(sql)) {

            printProducts(resultSet);
        }
    }
    private static void displayLowStockProducts(
            Connection connection) throws SQLException {

        String sql = """
            SELECT ProductID, ProductName, Category, Price, Quantity
            FROM Products
            WHERE Quantity < ?
            ORDER BY ProductID
                """;

        try (PreparedStatement statement =
                     connection.prepareStatement(sql)) {

            statement.setInt(1, LOW_STOCK_THRESHOLD);

            try (ResultSet resultSet = statement.executeQuery()) {

                printProducts(resultSet);
            }
        }
    }
    private static void updateProductQuantity(
            Connection connection,
            int productId,
            int newQuantity) throws SQLException {

        String sql = """
                UPDATE Products
                SET Quantity = ?
                WHERE ProductId = ?
                """;
        System.out.println("\nUpdating quantity for monitors.");
        try (PreparedStatement statement =
                     connection.prepareStatement(sql)) {
            statement.setInt(1, newQuantity);
            statement.setInt(2, productId);
            int rowsUpdated = statement.executeUpdate();

            if (rowsUpdated > 0) {
                System.out.println(
                        "Product quantity updated successfully."
                );
            } else {
                System.out.println("Product not found");
            }
        }
    }
    private static void printProducts(ResultSet resultSet)
            throws SQLException {
        while (resultSet.next()) {
            int id = resultSet.getInt("ProductId");
            String name = resultSet.getString("ProductName");
            String category = resultSet.getString("Category");
            double price = resultSet.getDouble("Price");
            int quantity = resultSet.getInt("Quantity");
            System.out.printf(
                    "%d || %s || %s || $%.2f || Quantity: %d%n",
                    id, name, category, price, quantity
            );
        }
    }
}