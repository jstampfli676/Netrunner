/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

/**
 *
 * @author jorda
 */
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

public class CreateDB {
    public static void createNewDB(String fileName) {
        String url = "jdbc:sqlite:C:/Users/jorda/java/Netrunner/Databases/" + fileName;
        String sql = "CREATE TABLE IF NOT EXISTS logins (\n"
                + "	password text NOT NULL,\n"
                + "	username text PRIMARY KEY\n"
                //+ "     capactity real\n"
                + ");";
        
        try (Connection conn = DriverManager.getConnection(url)) {
            if (conn != null) {
                DatabaseMetaData meta = conn.getMetaData();
                System.out.println("The driver name is " + meta.getDriverName());
                System.out.println("A new database has been created.");
                Statement stmt = conn.createStatement();
                stmt.execute(sql);
            }

        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }
    
    public static void main(String[] args) {
        createNewDB("logins.db");
    } 
}
