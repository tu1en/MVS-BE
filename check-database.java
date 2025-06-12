import java.sql.*;

public class CheckDatabase {
    public static void main(String[] args) {
        String url = "jdbc:h2:mem:testdb";
        String username = "sa";
        String password = "";
        
        try {
            // Load H2 driver
            Class.forName("org.h2.Driver");
            
            // Connect to database
            Connection conn = DriverManager.getConnection(url, username, password);
            System.out.println("Connected to H2 database successfully!");
            
            // Check if users table exists
            DatabaseMetaData dbmd = conn.getMetaData();
            ResultSet tables = dbmd.getTables(null, null, "USERS", null);
            
            if (tables.next()) {
                System.out.println("USERS table exists!");
                
                // Query users table
                Statement stmt = conn.createStatement();
                ResultSet rs = stmt.executeQuery("SELECT username, email, full_name, role_id FROM users");
                
                System.out.println("\n=== USERS IN DATABASE ===");
                while (rs.next()) {
                    System.out.println("Username: " + rs.getString("username"));
                    System.out.println("Email: " + rs.getString("email"));
                    System.out.println("Full Name: " + rs.getString("full_name"));
                    System.out.println("Role ID: " + rs.getInt("role_id"));
                    System.out.println("---");
                }
            } else {
                System.out.println("USERS table does not exist!");
            }
            
            conn.close();
            
        } catch (Exception e) {
            System.out.println("Error: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
