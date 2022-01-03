import java.io.*;

public class Card implements Serializable, Comparable<Card>{
	String faction;
	String type;
	String subtype;
	String title;
        int count = 0;

	public Card(String faction, String type, String subtype,String title){
		this.faction = faction;
		this.type = type;
		this.subtype = subtype;
		this.title = title;
	}
        
        public Card(String faction, String type, String subtype,String title, int count){
            this(faction, type, subtype, title);
            this.count = count;
        }
        
        public Card(String title){
            this(null, null, null, title, 0);
        }
        
        /**
         *
         * @param c
         */
        public Card(Card c){
            this(c.faction, c.type, c.subtype, c.title);
        }
        
        public String getCardTitle(){
            return title;
        }
        
        public String getFaction(){
            return faction;
        }
        
        public String getType(){
            return type;
        }
        
        public String getSubtype(){
            return subtype;
        }
        
        public int getCount(){
            return count;
        }
        
        public void incCount(){
            count++;
        }
        
        public void decCount(){
            count--;
        }
        
        public boolean equals(Object o){
            if (o==this) {
                return true;
            }
            if (!(o instanceof Card)) {
                return false;
            }
            Card c = (Card)o;
            return c.title.equals(this.title);
        }
        
        public int compareTo(Card c){
            return this.title.compareTo(c.title);
        }
        
        public String toString(){
            return title+" "+"x"+count;
        }
}