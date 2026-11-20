// Step 1: Import the database - java.sql is the built-in package used for JDBC
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 * Creates the connection to MySQL. The DAO classes call DBConnection.connect()
 * and close the connection themselves (try-with-resources).
 *
 * JDBC steps used across the DAO classes:
 *   1. Import the database      (java.sql imports)
 *   2. Load and register driver (here)
 *   3. Create a connection      (here)
 *   4. Create a statement       (prepareStatement in the DAOs)
 *   5. Execute the query        (executeUpdate / executeQuery)
 *   6. Process the results      (while rs.next())
 *   7. Close the connection     (try-with-resources)
 */
public final class DBConnection {
    private DBConnection() { }

    // ---- CHANGE THESE to match your MySQL setup ----
    // (Codespaces sets the DB_* environment variables for you, so nothing to change there.)
    private static final String URL      = setting("DB_URL", "jdbc:mysql://localhost:3306/banking_system");
    private static final String USER     = setting("DB_USER", "root");
    private static final String PASSWORD = setting("DB_PASSWORD", "password");

    // Uses the environment variable if it is set, otherwise the default above
    private static String setting(String name, String fallback) {
        String value = System.getenv(name);
        return (value == null || value.isEmpty()) ? fallback : value;
    }

    // Steps 2 and 3: load the driver and create a connection
    public static Connection connect() throws SQLException {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
        } catch (ClassNotFoundException e) {
            throw new SQLException("MySQL driver not found. Add mysql-connector-j to the project.");
        }
        return DriverManager.getConnection(URL, USER, PASSWORD);
    }
}
