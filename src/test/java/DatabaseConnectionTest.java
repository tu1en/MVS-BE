import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DatabaseConnectionTest {
    public static void main(String[] args) {
        String[] connectionStrings = {
            "jdbc:sqlserver://FON-KUN\\\\MSSQLSERVER01;databaseName=SchoolManagementDB;encrypt=false;trustServerCertificate=true;loginTimeout=30;socketTimeout=30;",
            "jdbc:sqlserver://localhost\\\\MSSQLSERVER01;databaseName=SchoolManagementDB;encrypt=false;trustServerCertificate=true;loginTimeout=30;socketTimeout=30;",
            "jdbc:sqlserver://FON-KUN:1433;databaseName=SchoolManagementDB;encrypt=false;trustServerCertificate=true;loginTimeout=30;socketTimeout=30;",
            "jdbc:sqlserver://localhost:1433;databaseName=SchoolManagementDB;encrypt=false;trustServerCertificate=true;loginTimeout=30;socketTimeout=30;"
        };
        
        String username = "app_user";
        String password = "123456";
        
        for (int i = 0; i < connectionStrings.length; i++) {
            System.out.println("Testing connection " + (i + 1) + ": " + connectionStrings[i]);
            try (Connection conn = DriverManager.getConnection(connectionStrings[i], username, password)) {
                System.out.println("✅ SUCCESS: Connection " + (i + 1) + " works!");
                System.out.println("Database: " + conn.getCatalog());
                break;
            } catch (SQLException e) {
                System.out.println("❌ FAILED: " + e.getMessage());
            }
            System.out.println();
        }
    }
}
