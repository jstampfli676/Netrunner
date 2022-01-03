import java.util.*;
import java.net.Socket;
import java.net.SocketException;
import java.net.ConnectException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.io.ObjectOutputStream;
import java.io.ObjectInputStream;
import java.io.IOException;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.concurrent.TimeUnit;
import javax.swing.border.LineBorder;

public class Client implements Runnable{
	
        private final ImageIcon corpCardBack = new ImageIcon(getClass().getResource("ResizedImages/Netrunner-corp-card-back.jpg"));
        private final ImageIcon runnerCardBack = new ImageIcon(getClass().getResource("ResizedImages/Netrunner-runner-card-back.jpg"));
        
        private Game game = null;
        private JLayeredPane center = new JLayeredPane();
        private Box myHand = Box.createHorizontalBox();
        private JLabel myDisc = new JLabel();
        private JLabel oppDisc = new JLabel();
        
        private boolean doneAddingCards = false;
        private Boolean waitingCreateGame = null;
        
        private String globalUsername;
        private String globalPassword;
        private Boolean loginApproved = null;
        private DefaultListModel<Card> searchListModel = new DefaultListModel();
        private DefaultListModel<Card> deckCardListModel = new DefaultListModel();
        private DefaultListModel<String> deckListModel = new DefaultListModel();
        private DefaultListModel<Game> gameListModel = new DefaultListModel();
        private DefaultListModel<Message> messageListModel = new DefaultListModel();
        
        
        private ArrayList<CardComponent> oppDiscList = new ArrayList<>();
        private int oppDiscCount = 0;
        private ArrayList<CardComponent> myDiscList = new ArrayList<>();
        private int myDiscCount = 0;
        private ArrayList<Card> curDeck = new ArrayList<>();
        private int curMyCards = 0;
        //private int everDrawnCards = 0;
        
        private int curOppCards = 0;
        
        private JLabel myDeck = new JLabel();
        private JLabel oppDeck = new JLabel();
        private Box oppHand = Box.createHorizontalBox();
        
        private boolean deleteDecks = false;
    
        private int windowsOpen = 1;
        private JFrame central;
        
        private String host;
	private int port;

	private Socket socket;
	private static ObjectOutputStream dos;
	private ObjectInputStream dis;
	private boolean running = false;

	public Client (String host, int port) {
		this.host = host;
		this.port = port;
	}

	//connect to server
	public void connect (){
		try {
			socket = new Socket(host, port);
			dos = new ObjectOutputStream(socket.getOutputStream());
			dis = new ObjectInputStream(socket.getInputStream());
			new Thread(this).start();
		} catch(IOException e){
			e.printStackTrace();
		} 
	}

	//disconnect from the server
	public void disconnect (){
		try {
			running = false;
			//tell the server we disconnected
			//writeData("dc");

			dos.close();
			dis.close();
			socket.close();
		} catch(IOException e) {
			e.printStackTrace();
		}
	}

	//send data to server
	public static void writeData (Object packet) {
		try {
			dos.writeObject(packet);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
        
        public void addToCenter(JComponent c){
            center.add(c);
        }
        
        public void removeFromHand(JComponent c){
            myHand.remove(c);
        }

	public void run(){
		try {
			running = true;
			while (running) {
				try {
					Object data = dis.readObject();
					if (data instanceof UsernamePassword) {
                                            UsernamePassword p = (UsernamePassword) data;
                                            System.out.println("login successful");
                                            if (p.getRegister()) {
                                                loginApproved = true;
                                            } else {
                                                loginApproved = false;
                                            }
                                        } else if (data instanceof Packet) {
                                            Packet p = (Packet)data;
                                            int value = p.getPacketType().getValue(p.getPacketType());
                                            //System.out.println(p.getCardTitle());
                                            if (value==1){
                                                Card c = p.getCard();
                                                searchListModel.addElement(c);
                                            } else if (value==4){
                                                String deckTitle = p.getDeckTitle();
                                                if (!deckListModel.contains(deckTitle)){
                                                    deckListModel.addElement(deckTitle);
                                                }
                                            } else if (value==6){
                                                int i = 0;
                                                while (i<p.getDeckList().getSize()) {
                                                    Card c =(Card) p.getDeckList().get(i);
                                                    deckCardListModel.addElement(c);
                                                    int x = 0; 
                                                    while (x<c.getCount()) {
                                                        //System.out.println(c);
                                                        curDeck.add(c);
                                                        
                                                        x++;
                                                    }
                                                    i++;
                                                }
                                                curMyCards = curDeck.size();
                                                myDeck.setText(String.valueOf(curMyCards));
                                                doneAddingCards = true;
                                                if (game!=null){
                                                    writeData(new Packet(15, globalUsername, curMyCards, game));
                                                }
                                            } else if (value==10){
                                                //System.out.println("in value 10");
                                                gameListModel.clear();
                                                int i = 0;
                                                while (i<p.getGameList().getSize()) {
                                                    gameListModel.addElement((Game)p.getGameList().get(i));
                                                    i++;
                                                }
                                                //System.out.println(gameListModel);
                                            } else if (value==13){
                                                game = new Game(p.getGame());
                                                if (curMyCards!=0){
                                                    writeData(new Packet(15, globalUsername, curMyCards, game));
                                                }
                                            } else if (value==14){
                                                messageListModel.addElement(p.getMessage());
                                            } else if (value==16){
                                                //System.out.println("opp card total received"+curOppCards);
                                                if (!globalUsername.equals(p.getUsername())){
                                                    curOppCards = p.getCardTotal();
                                                    oppDeck.setText(String.valueOf(curOppCards));
                                                }
                                            } else if (value == 17){
                                                if (!globalUsername.equals(p.getUsername())){
                                                    System.out.println("opp drew card");
                                                    JLabel newCard = new JLabel();
                                                    newCard.setIcon(runnerCardBack);
                                                    oppHand.add(newCard);
                                                    curOppCards--;
                                                    oppDeck.setText(String.valueOf(curOppCards));
                                                }
                                            } else if (value==18){
                                                if (!globalUsername.equals(p.getUsername())){
                                                    System.out.println("opp left game");
                                                    oppHand.removeAll();
                                                    curOppCards = 0;
                                                    oppDeck.setText(String.valueOf(curOppCards));
                                                }
                                            } else if (value==19 || value==20){
                                                CardComponent card = new CardComponent(p.getCardImage(), runnerCardBack, p.getCardId(),p.getCardTitle(), game);
                                                if (globalUsername.equals(p.getUsername())){
                                                    System.out.println("on return"+card.getId()+", "+p.getCardImage());
                                                    card.setCardBack(corpCardBack);
                                                    
                                                    myHand.add(card);
                                                    myHand.remove(card);
                                                    myHand.revalidate();
                                                    myHand.repaint();
                                                } else {
                                                    oppHand.remove(0);
                                                    oppHand.revalidate();
                                                    oppHand.repaint();
                                                }
                                                if (value == 20) {
                                                    card.setImageBack();
                                                } else {
                                                    card.setImageFront();
                                                }
                                                center.add(card);
                                                card.setLocation(100, 100);
                                                card.setMobile(true);
                                                center.validate();

                                            } else if (value>=21 && value<=23){
                                                CardComponent movedCard = new CardComponent(p.getCardComponent());
                                                movedCard.setMobile(true);                                                
                                                center.add(movedCard);
                                                center.remove(movedCard);
                                                center.remove(movedCard);
                                                
                                                center.add(movedCard);
                                                movedCard.setLocation(p.getX(), p.getY());
                                                if (value==22){
                                                    movedCard.setRotateImage();
                                                } else if (value==23){
                                                    movedCard.flipImage();
                                                }
                                                center.revalidate();
                                                center.repaint();
                                            } else if (value==75){
                                                System.out.println("name already taken");
                                                waitingCreateGame = false;
                                            } else if (value==76){
                                                System.out.println("name not taken");
                                                waitingCreateGame = true;
                                            } else if(value==24){//will change later to a larger number
                                                
                                                CardComponent card = new CardComponent(p.getCardComponent());
                                                System.out.println(card.getId());
                                                //card.setId(p.getCardId());
                                                if (globalUsername.equals(p.getUsername())){
                                                    curMyCards++;
                                                    myDeck.setText(String.valueOf(curMyCards));
                                                    curDeck.add(new Card(card.getTitle()));
                                                    
                                                    //card.setCardBack(corpCardBack);
                                                    
                                                    
                                                    
                                                    myHand.add(card);
                                                    myHand.remove(card);
                                                    myHand.remove(card);
                                                    myHand.revalidate();
                                                    myHand.repaint();
                                                    
                                                } else {
                                                    oppHand.remove(0);
                                                    oppHand.revalidate();
                                                    oppHand.repaint();
                                                }
                                            } else if (value==25){
                                                CardComponent card = new CardComponent(p.getCardComponent());
                                                
                                                

                                             
                                                center.add(card);
                                                center.remove(card);
                                                center.remove(card);
                                                center.revalidate();
                                                center.repaint();
                                                if (globalUsername.equals(p.getUsername())){
                                                    //curMyCards++;
                                                    //myDeck.setText(String.valueOf(curMyCards));
                                                    //curDeck.add(new Card(card.getTitle()));
                                                    //card.setCardBack(corpCardBack);
                                                    
                                                    
                                                    
                                                    //myHand.add(card);
                                                    myHand.remove(card);
                                                    myHand.remove(card);
                                                    myHand.add(card);
                                                    card.setMobile(false);
                                                    card.setImageFront();
                                                    myHand.revalidate();
                                                    myHand.repaint();
                                                    
                                                } else {
                                                    JLabel newCard = new JLabel();
                                                    newCard.setIcon(runnerCardBack);
                                                    oppHand.add(newCard);
                                                    oppHand.revalidate();
                                                    oppHand.repaint();
                                                }
                                            } else if (value==26){
                                                CardComponent card = new CardComponent(p.getCardComponent());
                                                System.out.println(card.getId());
                                                if (p.getUsername().equals(globalUsername)){
                                                    myHand.add(card);
                                                    card.setMobile(false);
                                                    card.setImageFront();
                                                    curMyCards--;
                                                    curDeck.remove(new Card(card.getTitle()));
                                                    myDeck.setText(String.valueOf(curMyCards));
                                                } else {
                                                    curOppCards--;
                                                    oppDeck.setText(String.valueOf(curOppCards));
                                                    JLabel newCard = new JLabel();
                                                    newCard.setIcon(runnerCardBack);
                                                    oppHand.add(newCard);
                                                }
                                            } else if (value==27){
                                                CardComponent card = new CardComponent(p.getCardComponent());
                                                center.add(card);
                                                center.remove(card);
                                                center.remove(card);
                                                center.revalidate();
                                                center.repaint();
                                                if (globalUsername.equals(p.getUsername())){
                                                    myDiscCount++;
                                                    myDiscList.add(card);
                                                    //card.setMobile(false);
                                                    //card.setImageFront();
                                                    myDisc.setIcon(card.getFrontImage());
                                                    myDisc.setText(String.valueOf(myDiscCount));
                                                } else {
                                                    oppDiscCount++;
                                                    oppDiscList.add(card);
                                                    //card.setMobile(false);
                                                    //card.setImageFront();
                                                    oppDisc.setIcon(card.getFrontImage());
                                                    oppDisc.setText(String.valueOf(oppDiscCount));
                                                }
                                            } else if (value==28){
                                                CardComponent card = new CardComponent(p.getCardComponent());
                                                center.add(card);
                                                card.setMobile(true);
                                                card.setImageFront();
                                                center.revalidate();
                                                center.repaint();
                                                if (globalUsername.equals(p.getUsername())){
                                                    myDiscCount--;
                                                    myDiscList.remove(card);
                                                    if (myDiscCount>0){
                                                        myDisc.setIcon(myDiscList.get(myDiscList.size()-1).getFrontImage());
                                                    } else {
                                                        myDisc.setIcon(null);
                                                    }
                                                    myDisc.setText(String.valueOf(myDiscCount));
                                                } else {
                                                    oppDiscCount--;
                                                    oppDiscList.remove(card);
                                                    if (oppDiscCount>0){
                                                        oppDisc.setIcon(oppDiscList.get(oppDiscList.size()-1).getFrontImage());
                                                    } else {
                                                        oppDisc.setIcon(null);
                                                    }
                                                    oppDisc.setText(String.valueOf(oppDiscCount));
                                                }
                                            } else if (value>=29 && value<=32){
                                                System.out.println("creating token");
                                                ImageIcon frontImage = new ImageIcon(getClass().getResource("Images/Netrunner-general-token.png"));
                                                if (value==30){
                                                    frontImage = new ImageIcon(getClass().getResource("Images/Netrunner-brain-damage.png"));
                                                } else if (value==31){
                                                    frontImage = new ImageIcon(getClass().getResource("Images/Netrunner-tag.png"));
                                                } else if (value==32){
                                                    frontImage = new ImageIcon(getClass().getResource("Images/Netrunner-bad-publicity.png"));
                                                }
                                                
                                                TokenComponent temp = new TokenComponent(frontImage, game);
                                                center.add(temp);
                                                temp.setLocation(100, 100);
                                                temp.setVisible(true);
                                                center.moveToFront(temp);
                                                center.revalidate();
                                                center.repaint();
                                            } else if (value==33){
                                                
                                                TokenComponent temp = new TokenComponent(p.getTokenComponent());
                                                System.out.println(temp.getId());                                                
                                                center.add(temp);
                                                center.remove(temp);
                                                center.remove(temp);
                                                center.add(temp);
                                                temp.setLocation(p.getX(), p.getY());
                                                center.moveToFront(temp);
                                                center.revalidate();
                                                center.repaint();
                                            }
                                            //System.out.println(searchListModel);
                                            //createMainWindow();
                                        } else {
                                            writeData(new Packet(100));
                                            System.exit(1);
                                        }
                                        //System.out.println(data);
				} catch(ClassNotFoundException e) {
					e.printStackTrace();
				} catch(SocketException e) {
					disconnect();
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
        
        private void createChooseDeckWindow(){
            windowsOpen++;
            JFrame main = new JFrame("Online Netrunner");
            main.setSize(400, 400);
            
            main.addWindowListener(new WindowAdapter(){
                public void windowClosing(WindowEvent e){
                    windowsOpen--;
                }
            });
            
            JList deckList = new JList();
            deckList.setModel(deckListModel);
            deckList.addMouseListener(new MouseAdapter(){
                public void mouseClicked(MouseEvent e){
                    JList list = (JList)e.getSource();
                    String deckTitle = (String)list.getSelectedValue();
                    if (e.getClickCount() == 2) {
                        curDeck.clear();
                        
                        writeData(new Packet(5, globalUsername, deckTitle));
                        main.dispose();
                        windowsOpen--;
                        //System.out.println(curDeck);
                    }
                }
            });
            
            main.add(deckList);
            main.setVisible(true);
        }
        
        private void createGameWindow(){
            windowsOpen++;
            JFrame gameBoard = new JFrame(game.getTitle());
            gameBoard.setSize(1800,1200);
            
            
            
            
            BorderLayout layout = new BorderLayout();
            
            center = new JLayeredPane();
            FlowLayout centerLayout = new FlowLayout();
            center.setLayout(null);
            center.setBorder(new LineBorder(Color.BLUE, 3));
            
            Box messageBox = Box.createVerticalBox();
            Box typeBox = Box.createHorizontalBox();
            
            Box buttonBox = Box.createVerticalBox();
            
            Box myDeckHandDisc = Box.createHorizontalBox();
            Box oppDeckHandDisc = Box.createHorizontalBox();
            
            myDeckHandDisc.createRigidArea(new Dimension(300, 900));
            //myDeckHandDisc.setOpaque(true);
            //myDeckHandDisc.setBackground(Color.BLUE);
            
            oppDeckHandDisc.createRigidArea(new Dimension(300, 900));
            //oppDeckHandDisc.setOpaque(true);
            //oppDeckHandDisc.setBackground(Color.RED);
            
            myHand = Box.createHorizontalBox();
            oppHand = Box.createHorizontalBox();
            myHand.setSize(500, 100);
            oppHand.setSize(500, 100);
            
            
            JPopupMenu allCardsMenu = new JPopupMenu();
            boolean createdMenu = false;
            
            
            myDeck = new JLabel();
            myDeck.setHorizontalTextPosition(JLabel.CENTER);
            myDeck.setVerticalTextPosition(JLabel.CENTER);
            myDeck.setIcon(corpCardBack);
            myDeck.setText(String.valueOf(curMyCards));
            myDeck.setForeground(Color.WHITE);
            myDeck.setFont(new Font("Serif", Font.BOLD, 30));
            myDeck.addMouseListener(new MouseAdapter(){
                public void mouseClicked(MouseEvent e) {
                    
                    
                    if (e.getClickCount()==2){
                        if (curDeck.size()>0){
                            int rand = (int)(Math.random()*curDeck.size());
                            String title = curDeck.get(rand).getCardTitle();
                            String cardFile = MyList.fix(title);
                            ImageIcon newCard = new ImageIcon(getClass().getResource("ResizedImages/Netrunner-"+cardFile+".jpg"));
                            CardComponent card = new CardComponent(newCard, corpCardBack/*, everDrawnCards*/,title, game);
                            
                            
                            curDeck.remove(rand);
                            
                            //System.out.println(cardFile);
                            
                            card.setImageFront();
                            myHand.add(card);
                            curMyCards--;
                            //everDrawnCards++;
                            myDeck.setText(String.valueOf(curMyCards));
                            //writeData(new Packet(15, globalUsername, curMyCards, game));
                            writeData(new Packet(17, globalUsername, game));
                        }
                        
                    } 
                }
                
                @Override
                public void mouseEntered(MouseEvent e){
                    allCardsMenu.removeAll();
                    for (Card c : curDeck){
                        JMenuItem temp = new JMenuItem(c.getCardTitle());
                        
                        temp.addActionListener(new ActionListener(){
                            public void actionPerformed(ActionEvent e){
                                String title = c.getCardTitle();
                                String cardFile = MyList.fix(title);
                                ImageIcon newCard = new ImageIcon(getClass().getResource("ResizedImages/Netrunner-"+cardFile+".jpg"));
                                CardComponent card = new CardComponent(newCard, corpCardBack,title, game);
                                writeData(new Packet(26, globalUsername, game, card));
                            }
                        });
                        
                        allCardsMenu.add(temp);
                    }
                    if (allCardsMenu.isShowing()){
                        allCardsMenu.setVisible(true);
                        allCardsMenu.setEnabled(true);
                    } else {
                        allCardsMenu.show(myDeck, 50, 20);
                        
                    }
                }
            });
            
            oppDeck = new JLabel();
            oppDeck.setIcon(runnerCardBack);
            oppDeck.setHorizontalTextPosition(JLabel.CENTER);
            oppDeck.setVerticalTextPosition(JLabel.CENTER);
            oppDeck.setText(String.valueOf(curOppCards));
            oppDeck.setForeground(Color.WHITE);
            oppDeck.setFont(new Font("Serif", Font.BOLD, 30));
            
            JPopupMenu discMenu = new JPopupMenu();
            myDisc = new JLabel();
            myDisc.setHorizontalTextPosition(JLabel.CENTER);
            myDisc.setVerticalTextPosition(JLabel.CENTER);
            myDisc.setText(String.valueOf(myDiscCount));
            myDisc.setForeground(Color.GREEN);
            myDisc.setFont(new Font("Serif", Font.BOLD, 30));
            
            myDisc.addMouseListener(new MouseAdapter(){
                
                
                @Override
                public void mouseEntered(MouseEvent e){
                    discMenu.removeAll();
                    for (CardComponent c : myDiscList){
                        JMenuItem temp = new JMenuItem(c.getTitle());
                        
                        temp.addActionListener(new ActionListener(){
                            public void actionPerformed(ActionEvent e){
                                String title = c.getTitle();
                                String cardFile = MyList.fix(title);
                                ImageIcon newCard = new ImageIcon(getClass().getResource("ResizedImages/Netrunner-"+cardFile+".jpg"));
                                //CardComponent card = new CardComponent(newCard, corpCardBack,title, game);
                                writeData(new Packet(28, globalUsername, game, c));//need to implement the packet
                            }
                        });
                        
                        discMenu.add(temp);
                    }
                    if (discMenu.isShowing()){
                        discMenu.setVisible(true);
                        discMenu.setEnabled(true);
                    } else {
                        discMenu.show(myDisc, 50, 20);
                        
                    }
                }
            });
            
            oppDisc = new JLabel();
            oppDisc.setHorizontalTextPosition(JLabel.CENTER);
            oppDisc.setVerticalTextPosition(JLabel.CENTER);
            oppDisc.setText(String.valueOf(myDiscCount));
            oppDisc.setForeground(Color.GREEN);
            oppDisc.setFont(new Font("Serif", Font.BOLD, 30));
            
            JLabel myExile = new JLabel();
            JLabel oppExile = new JLabel();
            
            JButton importDeck = new JButton("Import Deck");
            importDeck.addActionListener(new ActionListener(){
                public void actionPerformed(ActionEvent e){
                    createChooseDeckWindow();
                }
            });
            
            JButton generalToken = new JButton("General Token");
            generalToken.addActionListener(new ActionListener(){
                public void actionPerformed(ActionEvent e){
                    writeData(new Packet(29, game));
                }
            });
            JButton brainDamage = new JButton("Brain Damage");
            brainDamage.addActionListener(new ActionListener(){
                public void actionPerformed(ActionEvent e){
                    writeData(new Packet(30, game));
                }
            });
            JButton tag = new JButton("Tag");
            tag.addActionListener(new ActionListener(){
                public void actionPerformed(ActionEvent e){
                    writeData(new Packet(31, game));
                }
            });
            JButton badPublicity = new JButton("Bad Publicity");
            badPublicity.addActionListener(new ActionListener(){
                public void actionPerformed(ActionEvent e){
                    writeData(new Packet(32, game));
                }
            });
            
            JList messages = new JList();
            messages.setModel(messageListModel);
            JTextField typeMessage = new HintTextField("Send Message");
            JButton sendMessage = new JButton("Send");
            sendMessage.addActionListener(new ActionListener(){
                public void actionPerformed(ActionEvent e){
                    String messageText = typeMessage.getText();
                    if (!messageText.equals("")){
                        writeData(new Packet(14, new Message(globalUsername, messageText), game));
                    }
                }
            });
            
            myDeckHandDisc.add(myExile);
            myDeckHandDisc.add(myDisc);
            myDeckHandDisc.add(myHand);
            myDeckHandDisc.add(myDeck);
            
            oppDeckHandDisc.add(oppExile);
            oppDeckHandDisc.add(oppDisc);
            oppDeckHandDisc.add(oppHand);
            oppDeckHandDisc.add(oppDeck);//the eventual layout plan
            
            //myDeckHandDisc.add(new JLabel("me"));
            //oppDeckHandDisc.add(new JLabel("enemy"));
            
            typeBox.add(typeMessage);
            typeBox.add(sendMessage);
            
            messageBox.add(messages);
            messageBox.add(typeBox);
            
            buttonBox.add(importDeck);
            buttonBox.add(generalToken);
            buttonBox.add(brainDamage);
            buttonBox.add(tag);
            buttonBox.add(badPublicity);
            
            gameBoard.setLayout(layout);
            gameBoard.add(myDeckHandDisc, BorderLayout.SOUTH);
            gameBoard.add(oppDeckHandDisc, BorderLayout.NORTH);
            gameBoard.add(buttonBox, BorderLayout.WEST);
            gameBoard.add(messageBox, BorderLayout.EAST);
            gameBoard.add(center, BorderLayout.CENTER);
            gameBoard.setVisible(true);
            
            gameBoard.addWindowListener(new WindowAdapter(){
                public void windowClosing(WindowEvent e){
                    central.setEnabled(true);
                    windowsOpen--;
                    if (globalUsername.equals(game.getUser1())){
                        game.setUser1(null);
                    } else {
                        game.setUser2(null);
                    }
                    //System.out.println("delete username written");
                    //System.out.println(game);
                    messageListModel.clear();
                    myHand.removeAll();
                    curMyCards = 0;
                    //writeData(new Packet(15, globalUsername, curMyCards, game));
                    writeData(new Packet(18, globalUsername, game));
                    writeData(new Packet(13, game));                    
                    writeData(new Packet(12, game));
                    
                }
            });
        }
        
        private void createGameCreationWindow(){
            windowsOpen++;
            JFrame createGame = new JFrame("Online Netrunner");
            createGame.setSize(400, 400);
            
            createGame.addWindowListener(new WindowAdapter(){
                public void windowClosing(WindowEvent e) {
                    central.setEnabled(true);
                    windowsOpen--;
                    if (windowsOpen==0) {
                        writeData(new Packet(100));
                        System.exit(0);
                    }
                }
            });
            
            Box main = Box.createVerticalBox();
            JButton addGame = new JButton("Create Game");
            JTextField gameTitleField = new HintTextField("Game Title");
            
            addGame.addActionListener(new ActionListener(){
                public void actionPerformed(ActionEvent e){
                    String gameTitle = gameTitleField.getText();
                    if (!gameTitle.equals("")){
                        writeData(new Packet(8, gameTitle));
                        while(waitingCreateGame==null){
                            System.out.println("waiting create game");
                        }
                        if (waitingCreateGame){
                            waitingCreateGame = null;
                            curDeck.clear();
                            myDiscList.clear();
                            oppDiscList.clear();
                            CardComponent.setGlobalId(0);
                            TokenComponent.setGlobalId(0);
                            
                            createGame.dispose();
                            game = new Game(gameTitle, globalUsername);
                            createGameWindow();
                            windowsOpen--;
                        }
                        waitingCreateGame = null;
                    }
                }
            });
            
            main.add(addGame);
            main.add(gameTitleField);
            
            createGame.getContentPane().add(main);
            createGame.setVisible(true);
        }
        
        private void createDeckWindow(){
            windowsOpen++;
            
            central = new JFrame("Online Netrunner");
            central.setSize(900, 900);
            
            central.addWindowListener(new WindowAdapter(){
                public void windowClosing(WindowEvent e) {
                    windowsOpen--;
                    if (windowsOpen==0) {
                        writeData(new Packet(100));
                        System.exit(0);
                    }
                }
            });
            
            BorderLayout deckLayout = new BorderLayout();
            
            Box center = Box.createVerticalBox();
            
            JList deckList = new JList();//have to add a click listener so that on 
            //double click it will open the edit deck window with the cards that are already in the deck
            //this should also delete all the cards that were in the deck 
            deckList.setModel(deckListModel);
            
            deckList.addMouseListener(new MouseAdapter(){
                public void mouseClicked(MouseEvent e){
                    JList list = (JList)e.getSource();
                    String deckTitle = (String)list.getSelectedValue();
                    if (e.getClickCount() == 2) {
                        if (!deleteDecks){
                            central.setEnabled(false);
                            deckCardListModel.clear();
                            writeData(new Packet(5, globalUsername, deckTitle));
                            while (!doneAddingCards) {
                                System.out.println("adding cards");
                            }
                            doneAddingCards = false;
                            createDeckEditing(deckTitle);
                        } else {
                            writeData(new Packet(7, globalUsername, deckTitle));
                            deckListModel.clear();
                        }
                    }
                }
            });
            
            JButton newDeck = new JButton("Create New Deck");
            newDeck.addActionListener(new ActionListener(){
                public void actionPerformed(ActionEvent e){
                    deckCardListModel.clear();
                    createDeckEditing(null);
                    central.setEnabled(false);
                }
            });
            
            JButton deleteDeck = new JButton("Delete Deck Toggle");
            deleteDeck.addActionListener(new ActionListener(){
                public void actionPerformed(ActionEvent e){
                    if (deleteDecks) {
                        deleteDecks = false;
                        deleteDeck.setBackground(null);
                    } else {
                        deleteDecks = true;
                        deleteDeck.setBackground(Color.RED);
                    }
                }
            });
            
            Box delNew = Box.createHorizontalBox();
            delNew.add(newDeck);
            delNew.add(deleteDeck);
            
            center.add(delNew);
            center.add(deckList);
            
            JPanel deck = new JPanel();
            
            deck.setLayout(deckLayout);
            deck.add(center, BorderLayout.CENTER);
            
            JPanel games = new JPanel();
            //games.setLayout(deckLayout);
            
            Box gameBox = Box.createVerticalBox();
            
            JList gameList = new JList();
            gameList.setModel(gameListModel);
            gameList.addMouseListener(new MouseAdapter(){
                public void mouseClicked(MouseEvent e){
                    JList list = (JList)e.getSource();
                    game = (Game)list.getSelectedValue();
                    String gameTitle = game.getTitle();
                    if (e.getClickCount() == 2) {
                        String user1 = game.getUser1();
                        String user2 = game.getUser2();
                        if (user1==null || user2==null){
                            if (user1==null){
                                game.setUser1(globalUsername);
                            } else {
                                game.setUser2(globalUsername);
                            }
                            createGameWindow();
                            central.setEnabled(false);
                            writeData(new Packet(12, game));//need to change the game of the other client
                            //will need to create a new packet for this, both clients connected to the game will change their local game variable
                            writeData(new Packet(13, game));
                        }
                    }
                }
            });
            
            JButton createGame = new JButton("Create Game");
            createGame.addActionListener(new ActionListener(){
                public void actionPerformed(ActionEvent e){
                    central.setEnabled(false);
                    createGameCreationWindow();
                }
            });
            
            
            gameBox.add(createGame);
            gameBox.add(gameList);
            
            games.add(gameBox);
            
            JTabbedPane tabs = new JTabbedPane(JTabbedPane.TOP);
            tabs.addTab("Games", games);
            tabs.addTab("Deck Builder", deck);
            central.getContentPane().add(tabs);
            central.setVisible(true);
            writeData(new Packet(3, globalUsername));
            writeData(new Packet(9));
        }
        
        private void createDeckEditing(String dt) {
            windowsOpen++;
            JFrame main = new JFrame("Online Netrunner");
            main.setSize(900, 900);
            //main.setExtendedState(JFrame.MAXIMIZED_BOTH);
            //main.setUndecorated(true);
            
            main.addWindowListener(new WindowAdapter(){
                public void windowClosing(WindowEvent e) {
                    central.setEnabled(true);
                    windowsOpen--;
                    if (windowsOpen==0) {
                        writeData(new Packet(100));
                        System.exit(0);
                    }
                }
            });
            
            searchListModel = new DefaultListModel();
            searchListModel.ensureCapacity(1500);
            
            BorderLayout deckLayout = new BorderLayout();
            
            Box titleSave = Box.createHorizontalBox();
            
            Box searchBarButton = Box.createHorizontalBox();
            Box cardSearch = Box.createVerticalBox();
            cardSearch.setPreferredSize(new Dimension(600,900));
            Box deckCenter = Box.createHorizontalBox();
            Box deckSide = Box.createVerticalBox();
            deckSide.setPreferredSize(new Dimension(300,900));
            //String[] temp = {"hello", "working"};//will later be all the user's decks from the database
            //String[] temp2 = {"place", "holder"};//will later be the current list of cards that match search
            /*DefaultListModel model = new DefaultListModel();
            model.addElement("one");
            model.addElement("two");
            model.addElement("three");
            model.addElement("four");*/
            
            //PopupFactory pf = new PopupFactory(); 
            //JLabel temp = new JLabel("pop");
            
            JList deckList = new MyList();
            JList searchList = new MyList();
            searchList.setFixedCellHeight(15);
            deckList.setModel(deckCardListModel);
            searchList.setModel(searchListModel);
            
            ToolTipManager.sharedInstance().registerComponent(deckList);
            ToolTipManager.sharedInstance().registerComponent(searchList);
            ToolTipManager.sharedInstance().setReshowDelay(0);
            ToolTipManager.sharedInstance().setInitialDelay(0);
            ToolTipManager.sharedInstance().setDismissDelay(100000);
            
            
            //deckList.setPreferredSize(new Dimension(450, 800));
            //searchList.setPreferredSize(new Dimension(450, 880));
            searchList.addMouseListener(new MouseAdapter(){
                public void mouseClicked(MouseEvent e){
                    JList list = (JList)e.getSource();
                    if (e.getClickCount() == 2) {

                        // Double-click detected
                        //int index = list.locationToIndex(e.getPoint());
                        Card c = new Card((Card)list.getSelectedValue());
                        int index = deckCardListModel.indexOf(c);
                        if (index>=0) {
                            Card temp = deckCardListModel.get(index);
                            temp.incCount();
                            deckCardListModel.setElementAt(temp, index);
                        } else {
                            c.incCount();
                            deckCardListModel.addElement(c);
                        }                      
                    }
                }
            });//add to deck when clicked
            deckList.addMouseListener(new MouseAdapter(){
                public void mouseClicked(MouseEvent e){
                    JList list = (JList)e.getSource();
                    if (e.getClickCount() == 2) {

                        // Double-click detected
                        int index = list.locationToIndex(e.getPoint());
                        Card c = deckCardListModel.get(index);
                        if (c.getCount()>1) {
                            c.decCount();
                            deckCardListModel.setElementAt(c, index);
                        } else {
                            deckCardListModel.removeElementAt(index);
                        }
                    }
                }
            });//open cards from deck when clicked, in new list of cards remove card when clicked
            
            JTextField search = new HintTextField("Card Search");
            JButton searchButton = new JButton("Search");
            searchButton.addActionListener(new ActionListener(){
                public void actionPerformed(ActionEvent e){
                    searchListModel.clear();
                    writeData(new Packet(0, search.getText()));
                }
            });
            
            JTextField deckTitle = new HintTextField("Deck Title");
            if (dt!=null) {
                deckTitle.setText(dt);
            }
            JButton saveDeck = new JButton("Save");
            saveDeck.addActionListener(new ActionListener(){
                public void actionPerformed(ActionEvent e){
                    if (!deckTitle.getText().equals("")) {
                        System.out.println("sending data");
                        //System.out.println(deckCardListModel);
                        writeData(new Packet(2, deckTitle.getText(), globalUsername, deckCardListModel));
                    }
                }
            });
            
            titleSave.add(deckTitle);
            titleSave.add(saveDeck);
            
            JScrollPane searchScroll = new JScrollPane(searchList);
            searchScroll.setPreferredSize(new Dimension(600,880));
            JScrollPane deckScroll = new JScrollPane(deckList);
            deckScroll.setPreferredSize(new Dimension(200, 880));
            
            
            JPanel deck = new JPanel();
            searchBarButton.add(search);
            searchBarButton.add(searchButton);
            cardSearch.add(searchBarButton);
            cardSearch.add(searchScroll);
            deckSide.add(titleSave);
            deckSide.add(deckScroll);
            deckCenter.add(cardSearch);
            deckCenter.add(deckSide);
        
            
            deck.setLayout(deckLayout);
            deck.add(deckCenter,BorderLayout.CENTER);
            
            main.getContentPane().add(deck);
            main.setVisible(true);
            
        }

	public void startWindow (){
		JFrame first = new JFrame("Online Netrunner");
		first.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		first.setSize(600,600);
                
                first.addWindowListener(new WindowAdapter(){
                    public void windowClosing(WindowEvent e) {
                        windowsOpen--;
                        if (windowsOpen==0) {
                            writeData(new Packet(100));
                            System.exit(0);
                        }
                    }
                });

		JTextField username = new HintTextField("Username");
		JTextField password = new HintTextField("Password");

		JButton login = new JButton("Login");
		JButton register = new JButton("Register");

		login.addActionListener(new ActionListener(){

			public void actionPerformed(ActionEvent e) {
				String curUser = username.getText();
				String curPass = password.getText();
                                UsernamePassword temp = new UsernamePassword(false, curUser, curPass);
                                writeData(temp);
                                while (loginApproved == null){
                                    System.out.println("waiting");
                                }
                                if (loginApproved) {
                                    globalUsername = curUser;
                                    globalPassword = curPass;
                                    createDeckWindow();
                                    first.dispose();
                                    windowsOpen--;
                                } else {
                                    loginApproved = null;
                                }
			}

		});

		register.addActionListener(new ActionListener(){

			public void actionPerformed(ActionEvent e) {
				String curUser = username.getText();
				String curPass = password.getText();
				if (!(curUser.equals("") || curPass.equals(""))) {
					UsernamePassword temp = new UsernamePassword(true, curUser, curPass);
					writeData(temp);
				}
			}

		});

		Box loginRegister = Box.createHorizontalBox();
		loginRegister.add(login);
		loginRegister.add(register);
		Box userPass = Box.createVerticalBox();
		userPass.setMaximumSize(new Dimension(200, 50));
		userPass.add(username);
		userPass.add(password);
		Box mainBox = Box.createVerticalBox();
		mainBox.add(userPass);
		mainBox.add(loginRegister);
		first.getContentPane().add(mainBox); // Adds Button to content pane of frame
		first.setVisible(true);
	}

	public static void main (String[] args) {
		
		//try {
			String host = "97.107.134.162";//InetAddress.getLocalHost().getHostName();
			Client c = new Client(host, 50000);
			c.startWindow();
			c.connect();
			c.writeData("hello");
			//c.disconnect();
		/*} catch (UnknownHostException e) {
			e.printStackTrace();
		}*/
		
	}
}