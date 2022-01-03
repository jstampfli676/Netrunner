/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

/**
 *
 * @author jorda
 */
import java.util.*;
import java.io.*;
import javax.swing.DefaultListModel;
import javax.swing.ImageIcon;

public class Packet implements Serializable{
    private ImageIcon cardImage;
    private int cardId;
    private Card c;
    private PacketType packetType;
    private String cardTitle;
    private String deckTitle;
    private String username;
    private String gameTitle;
    private Message message;
    private DefaultListModel deckList = new DefaultListModel();
    private DefaultListModel<Game> gameList = new DefaultListModel();
    private Game game;
    private int cardTotal;
    private CardComponent cardComponent;
    private int cardX;
    private int cardY;
    private TokenComponent tokenComponent;
    
    public Packet(int type){
        packetType = PacketType.getEnum(type);
    }
    
    public Packet(int type, TokenComponent tokenComponent, int cardX, int cardY, Game game){
        packetType = PacketType.getEnum(type);
        this.tokenComponent = tokenComponent;
        this.cardX = cardX;
        this.cardY = cardY;
        this.game = game;
    }
    
    public Packet(int type, CardComponent cardComponent, int x, int y, Game game){
        this.cardComponent = cardComponent;
        packetType = PacketType.getEnum(type);
        this.game = game;
        cardX = x;
        cardY = y;
    }
    
    public Packet(int type, int cardTotal, String username){
        packetType = PacketType.getEnum(type);
        this.cardTotal = cardTotal;
        this.username = username;
    }
    
    public Packet(int type, String username, int cardTotal, Game game){
        packetType = PacketType.getEnum(type);
        this.username = username;
        this.cardTotal = cardTotal;
        this.game = game;
    }
    
    public Packet(int type, Card c){
        this.c = c;
        packetType = PacketType.getEnum(type);
    }
    
    public Packet(int type, String username, Game game, ImageIcon cardImage, int cardId, String cardTitle){
        packetType = PacketType.getEnum(type);
        this.username = username;
        this.cardImage = cardImage;
        this.game = game;
        this.cardId = cardId;
        this.cardTitle = cardTitle;
    }
    
    public Packet(int type, String title){
        if (type==0) {
            this.cardTitle = title;
        } else if (type==3){
            this.username = title;//type==3
        } else if (type==4){
            this.deckTitle = title;//type==4
        } else if (type==8){
            this.gameTitle = title;
        }
        packetType = PacketType.getEnum(type);
    }
    
    public Packet(int type, String title, String username, DefaultListModel deckList){
        packetType = PacketType.getEnum(type);
        deckTitle = title;
        int i = 0;
        this.deckList.clear();
        while (i<deckList.getSize()) {
            this.deckList.addElement(deckList.get(i));
            i++;
        }
        this.username = username;
    }
    
    public Packet(int type, String username, String deckTitle){
        packetType = PacketType.getEnum(type);
        this.username = username;
        this.deckTitle = deckTitle;
    }
    
    public Packet(int type, DefaultListModel deckList){
        packetType = PacketType.getEnum(type);
        int i = 0;
        this.deckList.clear();
        while (i<deckList.getSize()) {
            if (type==6){
                this.deckList.addElement(deckList.get(i));
            } else {
                this.gameList.addElement((Game)deckList.get(i));//type==10
            }
            i++;
        }
    }
    
    public Packet(int type, Game game, CardComponent cardComponent){
        this.game = game;
        this.cardComponent = cardComponent;
        packetType = PacketType.getEnum(type);
    }
    
    public Packet(int type, Game game){
        packetType = PacketType.getEnum(type);
        this.game = new Game(game);
    }
    
    public Packet(int type, String username, Game game){
        packetType = PacketType.getEnum(type);
        this.username = username;
        this.game = game;
    }
    
    public Packet(int type, Message message, Game game){
        packetType = PacketType.getEnum(type);
        this.message = message;
        this.game = game;
    }
    
    public Packet(int type, String username, Game game, CardComponent cardComponent){
        packetType = PacketType.getEnum(type);
        this.username = username;
        this.game = game;
        this.cardComponent = cardComponent;
    }
    
    public int getCardId(){
        return cardId;
    }
    
    public CardComponent getCardComponent(){
        return cardComponent;
    }
    
    public int getCardTotal(){
        return cardTotal;
    }
    
    public int getX(){
        return cardX;
    }
    
    public int getY(){
        return cardY;
    }
    
    public Game getGame(){
        return game;
    }
    
    public Message getMessage(){
        return message;
    }
    
    public DefaultListModel getGameList(){
        return gameList;
    }
    
    public String getGameTitle(){
        return gameTitle;
    }
    
    public int getType(){
        return packetType.value;
    }
    
    public String getUsername(){
        return username;
    }
    
    public Card getCard(){
        return c;
    }
    
    public String getCardTitle() {
        if (c==null) {
            return cardTitle;
        } else {
            return c.getCardTitle();
        }
    }
    
    public String getDeckTitle(){
        return deckTitle;
    }
    
    public DefaultListModel getDeckList(){
        System.out.println(deckList);
        return this.deckList;
    }
    
    public PacketType getPacketType(){
        return packetType;
    }
    
    public TokenComponent getTokenComponent(){
        return tokenComponent;
    }
    
    public ImageIcon getCardImage(){
        return cardImage;
    }
    
    public enum PacketType {
        CARDSEARCH(0),//just card title
        CARDRETURN(1),//full card
        DECKSAVE(2),//title, username, and decklist
        DECKTITLEREQUEST(3),//just username
        DECKTITLERETURN(4),//just deckTitle
        DECKCARDSREQUEST(5),//deckTitle and username, uses same constructor as 7
        DECKCARDRETURN(6),//just deckList, uses the same constructor as 10
        DECKDELETE(7),//deckTitle and username, uses same constructor as 5
        GAMECREATE(8),//just game title, uses the same constructor as 11
        GAMEREQUEST(9),//no other fields except for the type, uses the same ocnstructor as 12
        GAMERETURN(10),//just the game list, uses the same constructor as 6
        GAMEDELETE(11),//just the game title, uses the same constructor as 8
        GAMEUSERNAMEUPDATE(12),//just the game, uses the same constructor as 13
        GAMELOCALUPDATE(13),//just the game, uses the same constructor as 12, sends both ways
        SENDMESSAGE(14),//message and game, sends both ways
        SENDCARDTOTAL(15),//username, cardTotal, and game
        RETRUNCARDTOTAL(16),//cardTotal and username
        DREWCARD(17),//username and game, uses the same constructor as 18,sends both ways
        OPPONENTLEFT(18),//username and game,uses the same constructor as 17, sends both ways
        PLAYFACEUP(19),//game, and card image and cardId and card title, uses the same constructor as 20
        PLAYFACEDOWN(20),//game, and card image and cardId and card title, uses the same constructor as 19
        MOVECARD(21),//card component and game and cardx and cardy, sends both ways
        ROTATECARD(22),//uses same constructor as 21, sends both ways
        FLIPCARD(23),//uses same constructor as 21, sends both ways
        RETURNTODECK(24),//uses , game, and card component, sends both ways(when sending from server also uses username)
        RETURNTOHAND(25),//uses game and card component, sends both ways (when sending from server also uses username)
        INVALIDGAMETITLE(75),//no fields except type, uses the same constructor as 9
        GOODGAMETITLE(76),//no fields except type, uses the same constructor as 9
        DRAWSPECIFICCARD(26),//card component, username, game, sends both ways, same as 24 on back
        DISCARD(27),//game and card component, sends both ways(uses username when sending from server)
        RETURNTOBOARD(28),//username game and card component, same as 26
        CREATEGENERALCOMPONENT(29),//type and game sends both ways
        CREATEBRAINDAMAGE(30),//type and game sends both ways
        CREATETAG(31), //type and game sends both ways
        CREATEBADPUBLICITY(32),//type and game sends both ways
        MOVECOMPONENT(33),//token component cardx cardy and game, sends both ways
        EXITINGAPPLICATION(100);
        
        
        private static final Map<Integer, PacketType> intPair = new HashMap<>();
        private int value;
        
        static {
            for (PacketType t:PacketType.values()) {
                intPair.put(t.value, t);
            }
        }
        
        private PacketType(int value){
            this.value = value;
        }
        
        public static int getValue(PacketType t) {
            return t.value;
        }
        
        public static PacketType getEnum(int val) {
            return intPair.get(val);
        }
    }
    
    
    
}
