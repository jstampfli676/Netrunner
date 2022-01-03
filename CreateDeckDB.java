
import java.io.IOException;
import java.net.MalformedURLException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

/**
 *
 * @author jorda
 */
public class CreateDeckDB {
    
    
    public static void main(String[] args){
        String dburl = "jdbc:sqlite:C:/Users/jorda/java/Netrunner/Databases/databases.db";
        Connection conn = null;
        try {
            conn = DriverManager.getConnection(dburl);
            String sql = "CREATE TABLE IF NOT EXISTS decks (\n"
            + "     username text,\n"
            + "     deck_title text,\n"
            + "     card_title text,\n"
            + "     card_count text\n"
            + ");";
            Statement stmt = conn.createStatement();
            stmt.execute(sql);


                //System.out.println(line);

    // read each line and write to System.out
	        
        } catch(SQLException e){
            e.printStackTrace();
            System.out.println("bad url");
        }
    }
}
