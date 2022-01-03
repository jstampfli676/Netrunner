/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

/**
 *
 * @author jorda
 */
import java.awt.EventQueue;
import java.awt.event.*;
import javax.swing.*;
import java.util.*;

public class MyList extends JList{
    
    
    public static String fix(String s) {
        if (s.length()>=6 && s.substring(0,6).equals("Shi.Ky")){
            return "shikyu";
        } else if (s.length()>=4 && s.substring(4, s.length()).equals("ju")){
            return "miraju";
        } else if (s.length()>=14 && s.substring(14, s.length()).equals("Sustainable Growth")){
            return "palana-foods-sustainable-growth";
        } else if (s.length()>=5 && s.substring(1, 5).equals("Tori")){
            return "tori-hanzo";
        } else if (s.substring(0, s.length()-1).equals("Expos")){
            return "expose";
        } else if (s.substring(0, s.length()-1).equals("Drac")){
            return "draco";
        } else if (s.length()>=6 && s.substring(5, s.length()).equals("Vu")){
            return "deja-vu";
        } else if (s.length()>=2 && s.substring(1, s.length()).equals("ifr")){
            return "sifr";
        } else if (s.length()>=3 && s.substring(2, s.length()).equals("nya")){
            return "sunya";
        } else if (s.length()>=3 && s.substring(2, s.length()).equals("ui")){
            return "maui";
        } else if (s.length()>=6 && s.substring(0, 6).equals("Doppel")){
            return "doppelganger";
        } else if (s.length()>=5 && s.substring(4, s.length()).equals("jin Contract")){
            return "temujin-contract";
        } else if (s.length()>=5 && s.substring(0, 5).equals("Aaron")){
            return "aaron-marron";
        } else if (s.length()>=5 && s.substring(0, 5).equals("Chaos")){
            return "chaos-theory-wunderkind";
        }
        
        String answer = "";
        char[] temp = s.toCharArray();
        ArrayList<Character> letters = new ArrayList<>();
        for (char c:temp) {
            letters.add(Character.toLowerCase(c));
        }
        for (int i = 0; i<letters.size(); i++) {
            char cur = letters.get(i);
            if (cur==' ') {
                letters.set(i, '-');
            } else if (cur=='-'){
                continue;
            } else if (!(Character.isDigit(cur) || Character.isLetter(cur))) {
                letters.remove(i);
                i--;
            }
        }
        for (char c:letters) {
            answer+=c;
        }
        return answer;
    }
    
    public MyList() {
        super();
        
        
        // Attach a mouse motion adapter to let us know the mouse is over an item and to show the tip.
        addMouseMotionListener(new MouseMotionAdapter() {

            @Override
            public void mouseMoved(MouseEvent e) {//need to make better hovering check 
                
                //if (!inList) {
                    MyList theList = (MyList) e.getSource();
                    ListModel model = theList.getModel();
                    int index = theList.locationToIndex(e.getPoint());
                    /*boundXl = indexToLocation(index).getX();
                    boundYl = indexToLocation(index).getY();
                    boundXh = boundXl+theList.getFixedCellWidth();
                    boundYh = boundYl+theList.getFixedCellHeight();
                    ImageIcon im = new ImageIcon(getClass().getResource("Images/MandatoryUpgrades.png"));
                    temp.setIcon(im);
                    popup = pf.getPopup((JFrame)SwingUtilities.getWindowAncestor(theList), temp, 180, 100);*/
                    if (index > -1 && theList.getModel().getSize()!=0) {
                        //ToolTipManager.sharedInstance().setDismissDelay(60000);
                        theList.setToolTipText(null);
                        Card curCard = (Card) model.getElementAt(index);
                        String text = MyList.fix(curCard.getCardTitle());
                        System.out.println(text);
                        theList.setToolTipText("<html><img src=\"" + getClass().getResource("Images/Netrunner-"+text+".png")+ "\">");
                        //curText = theList.getToolTipText();
                        //popup.show();
                        //inList = true;
                    }
                /*} else {
                    if ((e.getX()<boundXl || e.getX()>boundXh) && (e.getY()<boundYl || e.getY()>boundYh)) {
                        System.out.println("not hovering");
                        popup.hide();
                        inList = false;
                    }
                }*/
            }
        });
    }

    // Expose the getToolTipText event of our JList
    @Override
    public String getToolTipText(MouseEvent e) {
        return super.getToolTipText();
    }
}
