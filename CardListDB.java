import javax.swing.*;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.net.MalformedURLException;
import java.awt.image.BufferedImage;
import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import javax.imageio.ImageIO;
import java.sql.Statement;

public class CardListDB {
	
	private static int cull (String s, char c) {
		int x = 0; 
		while (s.charAt(x)!=c) {
			x++;
		}
		return x;
	}
        
        private static String fix(String s) {
            char[] letters = s.toCharArray();
            int start = -1;
            int symbol = -1;
            for (int i = 0; i<letters.length; i++) {
                //System.out.println(i);
                if (letters[i]=='&') {
                    start=i;
                    //System.out.println(start);
                    if (letters[i+1]=='#') {
                        symbol = 1;
                    } else if (letters[i+1]=='q') {
                        symbol = 2;
                    }
                } else if (letters[i]==';') {
                    if (symbol==1 && start>=0) {
                        if (i>=s.length()-1) {
                            s = s.substring(0, start)+"'";
                        } else {
                            s = s.substring(0, start)+"'"+s.substring(i+1);
                            i=start;
                            letters = s.toCharArray();
                        }
                        start = -1;
                    } else if (symbol==2 && start>=0) {
                        if (i>=s.length()-1) {
                            s = s.substring(0, start)+"\"";
                        } else {
                            s = s.substring(0, start)+"\""+s.substring(i+1);
                            i=start;
                            letters = s.toCharArray();
                        }
                        start = -1;
                    } else if (start>=0) {
                        if (i>=s.length()-1) {
                            s = s.substring(0, start)+"&";
                        } else {
                            s = s.substring(0, start)+"&"+s.substring(i+1);
                            i=start;
                            letters = s.toCharArray();
                        }
                        start = -1;
                    }
                }
            }
            return s;
        }

	public static void main(String[] args) {

		try {
			// Make a URL to the web page
                    URL url = new URL("https://www.acoo.net/netrunner-cards-list/");

                    // Get the input stream through URL Connection
                    URLConnection con = url.openConnection();
                    InputStream is =con.getInputStream();

                    // Once you have the Input Stream, it's just plain old Java IO stuff.

                    // For this case, since you are interested in getting plain-text web page
                    // I'll use a reader and output the text content to System.out.

                    // For binary content, it's better to directly read the bytes from stream and write
                    // to the target file.


                    BufferedReader br = new BufferedReader(new InputStreamReader(is));

                    String line = null;

                    int i = 0;

                    String dburl = "jdbc:sqlite:C:/Users/jorda/java/Netrunner/Databases/databases.db";
                    Connection conn = null;
                    try {
                        conn = DriverManager.getConnection(dburl);
                        String sql = "CREATE TABLE IF NOT EXISTS cards (\n"
                        + "     title text PRIMARY KEY,\n"
                        + "     faction text,\n"
                        + "     type text,\n"
                        + "     subtype text\n"
                        + ");";
                        Statement stmt = conn.createStatement();
                        stmt.execute(sql);
                        String sqlInsert = "INSERT INTO cards(title,faction,type,subtype) VALUES(?,?,?,?)";

                        while ((line = br.readLine()) != null) {
                            if (i>161 && i<1551) {
                                line = line.substring(11);
                                if (i==162) {
                                        line = line.substring(24);
                                }

                                line = line.substring(cull(line, ' ')+10);

                                int temp = cull(line, '"');
                                String faction = line.substring(0,temp);
                                //System.out.print(faction+", ");
                                line = line.substring(temp+9);


                                temp = cull(line, '"');
                                String type = line.substring(0, temp);
                                //System.out.print(type+", ");
                                line = line.substring(temp+11);
                                //System.out.println(line);
                                temp = cull(line, '"');
                                String subType = line.substring(0, temp);
                                line = line.substring(temp+9);
                                //System.out.print(subType+", ");
                                //System.out.println(line);
                                temp = cull(line, '"');
                                String title = line.substring(0, temp);
                                title = fix(title);
                                //System.out.println(title);
                                try (PreparedStatement pstmt = conn.prepareStatement(sqlInsert)) {
                                    pstmt.setString(1, title);
                                    pstmt.setString(2, faction);
                                    pstmt.setString(3, type);
                                    pstmt.setString(4, subType);
                                    pstmt.executeUpdate();
                                } catch(SQLException e) {
                                    e.printStackTrace();
                                }
                                
                            }
                            i++;
                            //System.out.println(line);
                        }
                        
                        
                    } catch(SQLException e) {
                        e.printStackTrace();
                    }

	        // read each line and write to System.out
	        
		} catch(MalformedURLException u){
			System.out.println("bad url");
		}catch(IOException e){
			System.out.println("cant access");
		}

		/*try {
            String webPage = "https://docs.google.com/spreadsheets/d/1ZkJZesxsD-mzF-OSNpTPCESLCB5UZB8drsB-OVr7ab0/edit#gid=0";
            URL url = new URL(webPage);
            URLConnection urlConnection = url.openConnection();
            InputStream is = urlConnection.getInputStream();
            InputStreamReader isr = new InputStreamReader(is);

            int numCharsRead;
            char[] charArray = new char[2024];
            StringBuffer sb = new StringBuffer();
            int i=0;
            while ((numCharsRead = isr.read(charArray)) > 0) {
                if (i>=0) {
                	sb.append(charArray, 0, numCharsRead);
                }
                i++;
            }
            String result = sb.toString();

            System.out.println("*** BEGIN ***");
            System.out.println(result);
            System.out.println("*** END ***");
        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }*/


		/*JFrame frame = new JFrame("My First GUI");
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.setSize(300,300);
		JButton button = new JButton("Press");
		frame.getContentPane().add(button); // Adds Button to content pane of frame
		frame.setVisible(true);*/
	}
}