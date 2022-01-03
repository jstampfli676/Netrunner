import java.net.Socket;
import java.util.*;
import java.io.ObjectOutputStream;
import java.io.ObjectInputStream;
import java.io.IOException;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import javax.swing.DefaultListModel;

public class MainConnection implements Runnable{
	
	private String connectedUsername;
    
        private Socket socket;
	private ObjectInputStream dis;
	private ObjectOutputStream dos;
	private int id;

	public MainConnection(Socket s) {
		this.socket = s;
		id = 0;
		try {
			dis = new ObjectInputStream(socket.getInputStream());
			dos = new ObjectOutputStream(socket.getOutputStream());
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
        
        public String getConnectedUsername(){
            return connectedUsername;
        }
        
        public void run(){
		try {
			while (socket.isConnected()){
				try {
					Object data = dis.readObject();
					if (data instanceof UsernamePassword){
                                            UsernamePassword p = (UsernamePassword) data;
                                            if (p.getRegister()) {
                                                storeUP(p.getUsername(), p.getPassword());
                                            } else {
                                                if (validUP(p.getUsername(), p.getPassword())) {
                                                    sendData(new UsernamePassword(true));
                                                    connectedUsername = p.getUsername();
                                                    for (MainConnection c:Server.getAllConnections()){
                                                        if (c != this){
                                                            if (c.connectedUsername!=null && c.connectedUsername.equals(this.connectedUsername)){
                                                                sendData("dc");
                                                            }
                                                        }
                                                    }
                                                } else {
                                                    sendData(new UsernamePassword(false));
                                                }
                                            }
					} else if (data instanceof Packet){
                                            Packet p = (Packet) data;
                                            
                                            //check to see if multiple sends work
                                            /*ArrayList<MainConnection> allConnections = Server.getAllConnections();
                                            for (MainConnection c:allConnections){
                                                c.sendData("multiple sends made");
                                            }*/
                                            
                                            int value = p.getPacketType().getValue(p.getPacketType());
                                            if (value==0) {
                                                String search = p.getCardTitle();
                                                Connection conn = this.connectDB();
                                                String selectSQL = "SELECT * FROM cards "
                                                        + "WHERE title LIKE "+"'%"+search+"%'";
                                                try (PreparedStatement pstmt = conn.prepareStatement(selectSQL)) {
                                                    //pstmt.setString(1, search);
                                                    ResultSet rs = pstmt.executeQuery();
                                                    while (rs.next()) {
                                                        Card c = new Card(rs.getString("faction"), rs.getString("type"), rs.getString("subtype"), rs.getString("title"));
                                                        sendData(new Packet(1,c));
                                                    }
                                                } catch (SQLException e) {
                                                    e.printStackTrace();
                                                }
                                            } else if (value==2){
                                                Connection conn = this.connectDB();
                                                String username = p.getUsername();
                                                String deckTitle = p.getDeckTitle();
                                                DefaultListModel<Card> cards = p.getDeckList();//not working
                                                
                                                //delete old deck
                                                String deleteDeckSQL="DELETE FROM decks WHERE username = "+"'"+username+"' "
                                                        +" AND deck_title = "+"'"+deckTitle+"'";
                                                try {
                                                    Statement stmt = conn.createStatement();
                                                    stmt.execute(deleteDeckSQL);
                                                } catch(SQLException e){
                                                    e.printStackTrace();
                                                }
                                                System.out.println("deleted");
                                                //save new deck
                                                String saveDeckSQL = "INSERT INTO decks(username, deck_title, card_title, card_count) VALUES(?,?,?,?)";
                                                int i=0;
                                                
                                                /*while(i<p.getDeckList().getSize()) {
                                                    cards.addElement((Card)p.getDeckList().get(i));
                                                    i++;
                                                }
                                                i=0;*/
                                                while (i<cards.getSize()){
                                                    try {
                                                        PreparedStatement pstmt = conn.prepareStatement(saveDeckSQL);
                                                        pstmt.setString(1,username);
                                                        pstmt.setString(2, deckTitle);
                                                        pstmt.setString(3, cards.get(i).getCardTitle());
                                                        pstmt.setInt(4, cards.get(i).getCount());
                                                        pstmt.executeUpdate();
                                                    } catch(SQLException e){
                                                        e.printStackTrace();
                                                        System.out.println("save error");
                                                    }
                                                    i++;
                                                }
                                                System.out.println("new save");
                                                value=3;
                                            } else if (value==7){
                                                String username = p.getUsername();
                                                String deckTitle = p.getDeckTitle();
                                                Connection conn = this.connectDB();
                                                String deleteSQL = "DELETE FROM decks WHERE username = "+"'"+username+"'"
                                                        +"AND deck_title = "+"'"+deckTitle+"'";
                                                try {
                                                    Statement stmt = conn.createStatement();
                                                    stmt.execute(deleteSQL);
                                                } catch(SQLException e){
                                                    e.printStackTrace();
                                                }
                                                value=3;
                                            }
                                            
                                            if (value==3){
                                                String username = p.getUsername();
                                                Connection conn = this.connectDB();
                                                String selectSQL = "SELECT DISTINCT deck_title FROM decks "
                                                        + "WHERE username = "+"'"+username+"'";
                                                try (PreparedStatement pstmt = conn.prepareStatement(selectSQL)) {
                                                    //pstmt.setString(1, search);
                                                    ResultSet rs = pstmt.executeQuery();
                                                    while (rs.next()) {
                                                        sendData(new Packet(4,rs.getString("deck_title")));
                                                    }
                                                } catch (SQLException e) {
                                                    e.printStackTrace();
                                                }
                                            } else if (value==5){
                                                String username = p.getUsername();
                                                String deckTitle = p.getDeckTitle();
                                                Connection conn = this.connectDB();
                                                String selectSQL = "SELECT * FROM decks INNER JOIN cards ON decks.card_title=cards.title"
                                                        + " WHERE username = "+"'"+username+"'"
                                                        + " AND deck_title = "+"'"+deckTitle+"'";
                                                try (PreparedStatement pstmt = conn.prepareStatement(selectSQL)) {
                                                    //pstmt.setString(1, search);
                                                    ResultSet rs = pstmt.executeQuery();
                                                    DefaultListModel<Card> deckList=new DefaultListModel(); 
                                                    while (rs.next()) {
                                                        deckList.addElement(new Card(rs.getString("faction"), rs.getString("type"), rs.getString("subtype"), rs.getString("title"), rs.getInt("card_count")));
                                                    }
                                                    //System.out.println(deckList);
                                                    sendData(new Packet(6,deckList));
                                                } catch (SQLException e) {
                                                    e.printStackTrace();
                                                }
                                            } else if (value == 8){
                                                //save the new game to databases
                                                String gameTitle = p.getGameTitle();
                                                Connection conn = this.connectDB();
                                                
                                                
                                                try {
                                                    String saveGameSQL = "INSERT INTO games(game_title, username1) VALUES(?,?)";
                                                    PreparedStatement pstmt2 = conn.prepareStatement(saveGameSQL);
                                                    pstmt2.setString(1, gameTitle);
                                                    pstmt2.setString(2, connectedUsername);
                                                    pstmt2.executeUpdate();
                                                    sendData(new Packet(76));
                                                    value=9;
                                                } catch(SQLException b){
                                                    sendData(new Packet(75));
                                                    b.printStackTrace();
                                                }
                                                
                                            } else if (value==12){
                                                Connection conn = this.connectDB();
                                                Game game = p.getGame();
                                                System.out.println(game.getUser1());
                                                System.out.println(game.getUser2());
                                                String updateUsernameSQL1 = "UPDATE games SET username1 = "+"'"+game.getUser1()+"', username2 = "+"'"+game.getUser2()+"'"+" WHERE game_title = "+"'"+game.getTitle()+"'";
                                                try {
                                                    Statement stmt = conn.createStatement();
                                                    stmt.execute(updateUsernameSQL1);
                                                } catch(SQLException e){
                                                    e.printStackTrace();
                                                }
                                                value=9;
                                            } if (value==9){
                                                Connection conn = this.connectDB();
                                                String getGamesSQL = "SELECT * FROM games";
                                                ArrayList<String> gamesCleanup = new ArrayList();
                                                try {
                                                    PreparedStatement pstmt = conn.prepareStatement(getGamesSQL);
                                                    ResultSet rs = pstmt.executeQuery();
                                                    //now have to create all the game objects add them to a list and send them
                                                    DefaultListModel<Game> gameList = new DefaultListModel();
                                                    while (rs.next()){
                                                        String u1 = rs.getString("username1");
                                                        String u2 = rs.getString("username2");
                                                        if (u1!=null && u1.equals("null")) {
                                                            u1=null;
                                                        }
                                                        if (u2!=null && u2.equals("null")){
                                                            u2=null;
                                                        }
                                                        System.out.println(u1+" "+u2+" usernames");
                                                        if (u1==null && u2==null){
                                                            gamesCleanup.add(rs.getString("game_title"));
                                                        } else if (u1!=null && u2!=null){
                                                            System.out.println("neither null");
                                                            gameList.addElement(new Game(rs.getString("game_title"), u1, u2));
                                                        } else {
                                                            System.out.println("1 null");
                                                            gameList.addElement(new Game(rs.getString("game_title"), u1, u2, 1));
                                                        }
                                                    }
                                                    System.out.println(gameList);
                                                    ArrayList<MainConnection> allConnections = Server.getAllConnections();
                                                    for (MainConnection c:allConnections){
                                                        c.sendData(new Packet(10, gameList));
                                                    }
                                                    
                                                } catch(SQLException e){
                                                    e.printStackTrace();
                                                }
                                                deleteGame(gamesCleanup);
                                            } else if (value==13) {
                                                for (MainConnection c:Server.getAllConnections()){
                                                    String cU = c.connectedUsername;
                                                    if (cU.equals(p.getGame().getUser1()) 
                                                            || cU.equals(p.getGame().getUser2())){
                                                        c.sendData(p);
                                                    }
                                                }
                                            } else if (value==14){
                                                for (MainConnection c:Server.getAllConnections()){
                                                    String cU = c.connectedUsername;
                                                    System.out.println(cU);
                                                    if (cU!=null && (cU.equals(p.getGame().getUser1()) 
                                                            || cU.equals(p.getGame().getUser2()))){
                                                        c.sendData(p);
                                                    }
                                                }
                                            } else if (value==15) {
                                                Game curGame = new Game(p.getGame());
                                                String u1 = curGame.getUser1();
                                                String u2 = curGame.getUser2();
                                                System.out.println(u1+" "+u2);
                                                for (MainConnection c:Server.getAllConnections()){
                                                    if (c.connectedUsername.equals(u1) || c.connectedUsername.equals(u2)){
                                                        System.out.println(c.connectedUsername);
                                                        c.sendData(new Packet(16, p.getCardTotal(), p.getUsername()));
                                                    }
                                                }
                                            } else if (value==17){
                                                Game curGame = new Game(p.getGame());
                                                String u1 = curGame.getUser1();
                                                String u2 = curGame.getUser2();
                                                for (MainConnection c:Server.getAllConnections()){
                                                    if (c.connectedUsername.equals(u1) || c.connectedUsername.equals(u2)){
                                                        System.out.println(c.connectedUsername);
                                                        c.sendData(p);
                                                    }
                                                }
                                            } else if (value>=18 && value<=33){
                                                Game curGame = new Game(p.getGame());
                                                String u1 = curGame.getUser1();
                                                String u2 = curGame.getUser2();
                                                for (MainConnection c:Server.getAllConnections()){
                                                    if (c.connectedUsername.equals(u1) || c.connectedUsername.equals(u2)){
                                                        System.out.println(c.connectedUsername);
                                                        if (value>=24 && value<=28){
                                                            c.sendData(new Packet(p.getType(), connectedUsername, curGame, p.getCardComponent()));
                                                        } else if (value==19 || value==20){
                                                            c.sendData(new Packet(p.getType(), connectedUsername, curGame, p.getCardImage(), p.getCardId(), p.getCardTitle()));
                                                        } 
                                                        else {
                                                            System.out.println(value);
                                                            c.sendData(p);
                                                        }
                                                    }
                                                }
                                            } else if (value==100){
                                                Server.allConnections.remove(this);
                                            }
                                        }
					System.out.println(data);
					//sendData(data);
				} catch (ClassNotFoundException e) {
					e.printStackTrace();
				}
			}
		}catch(IOException e){
			e.printStackTrace();
		}
	}
        
        private void deleteGame(ArrayList<String> games){
            Connection conn = this.connectDB();
            for (String s:games){
                String deleteGameSQL = "DELETE FROM games WHERE game_title = "+"'"+s+"'";
                try {
                    Statement stmt = conn.createStatement();
                    stmt.execute(deleteGameSQL);
                } catch(SQLException e){
                    e.printStackTrace();
                }
            }
        }

	public void close (){
		try {
			dos.close();
			dis.close();
			socket.close();
		} catch (IOException e){
			e.printStackTrace();
		}
	}

	public void sendData (Object packet) {
		try {
			dos.writeObject(packet);
			dos.flush();
		} catch(IOException e){
			e.printStackTrace();
		}
	}
        
        public void storeUP (String username, String password){
            String sql = "INSERT INTO logins(password,username) VALUES(?,?)";
            
            try (Connection conn = this.connectDB();
                    PreparedStatement pstmt = conn.prepareStatement(sql)) {
                pstmt.setString(1, password);
                pstmt.setString(2, username);
                pstmt.executeUpdate();
            } catch(SQLException e) {
                e.printStackTrace();
            }
        }
        
        public boolean validUP (String username, String password){
            String sql = "SELECT password, username FROM logins";
            
            try (Connection conn = this.connectDB();
                 Statement stmt = conn.createStatement();
                 ResultSet rs = stmt.executeQuery(sql)) {
                
                while (rs.next()) {
                    if (rs.getString("password").equals(password) && rs.getString("username").equals(username)) {
                        return true;
                    }
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
            return false;
        }
        
        private Connection connectDB (){
            String url = "jdbc:sqlite:/home/jordan/Netrunner/Databases/databases.db";
            Connection conn = null;
            try {
                conn = DriverManager.getConnection(url);
            } catch(SQLException e) {
                e.printStackTrace();
            }
            return conn;
        }
}