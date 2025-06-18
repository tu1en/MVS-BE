import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;

public class DatabaseConnectionTest {
    public static void main(String[] args) {
        String url = "jdbc:sqlserver://localhost:1433;databaseName=SchoolManagementDB;encrypt=false;trustServerCertificate=true;loginTimeout=10;socketTimeout=10;connectRetryCount=0;";
        String username = "app_user";
        String password = "123456";
        
        System.out.println("Testing database connection...");
        
        try {
            // Load the driver
            Class.forName("com.microsoft.sqlserver.jdbc.SQLServerDriver");
            System.out.println("Driver loaded successfully");
            
            // Create connection
            System.out.println("Attempting connection to: " + url);
            Connection connection = DriverManager.getConnection(url, username, password);
            System.out.println("Connection successful!");
            
            // Test query
            Statement stmt = connection.createStatement();
            ResultSet rs = stmt.executeQuery("SELECT 1 as test");
            
            if (rs.next()) {
                System.out.println("Query test successful: " + rs.getInt("test"));
            }
            
            rs.close();
            stmt.close();
            connection.close();
            System.out.println("Connection closed successfully");
            
        } catch (Exception e) {
            System.err.println("Connection failed:");
            e.printStackTrace();
        }
    }
}
