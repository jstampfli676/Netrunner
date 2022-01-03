
import static com.sun.java.accessibility.util.AWTEventMonitor.addMouseListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.io.Serializable;
import javax.swing.ImageIcon;
import javax.swing.JLabel;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

/**
 *
 * @author jorda
 */
public class TokenComponent extends JLabel implements Serializable{
    private static int globalId = 0;
    private final int id;
    private volatile int screenX = 0;
    private volatile int screenY = 0;
    private volatile int myX = 0;
    private volatile int myY = 0;
    private final ImageIcon frontImage;
    private final Game game;
    private final TokenComponent me = this;
    
    public TokenComponent(ImageIcon frontImage, Game game, int id){
        this.frontImage = frontImage;
        this.id = id;
        this.game = game;
        //globalId++;
        
        setIcon(frontImage);
        
        setBounds(0, 0, 30, 30);
        setOpaque(false);
        
        addMouseListener(new MouseListener(){
            public void mouseEntered(MouseEvent e) {
                
            }
            
            public void mouseClicked(MouseEvent e) {
                
            }
            
            public void mousePressed(MouseEvent e){
                screenX = e.getXOnScreen();
                screenY = e.getYOnScreen();

                myX = getX();
                myY = getY();
            }
            
            
            
            public void mouseReleased(MouseEvent e){
                screenX = e.getXOnScreen();
                screenY = e.getYOnScreen();

                myX = getX();
                myY = getY();
                
                Client.writeData(new Packet(33, me, myX, myY, game));
            }
            
            public void mouseExited(MouseEvent e) {
                
            }
        });
        addMouseMotionListener(new MouseMotionListener() {

            @Override
            public void mouseDragged(MouseEvent e) {

                  int deltaX = e.getXOnScreen() - screenX;
                  int deltaY = e.getYOnScreen() - screenY;

                  setLocation(myX + deltaX, myY + deltaY);



            }

            @Override
            public void mouseMoved(MouseEvent e) { }

        });
    }
    
    public TokenComponent(ImageIcon frontImage, Game game){
        this(frontImage, game, globalId);
        globalId++;
    }
    
    public TokenComponent(TokenComponent t){
        this(t.frontImage, t.game, t.id);
    }
    
    public int getId(){
        return id;
    }
    
    public static void setGlobalId(int globalId) {
        TokenComponent.globalId = globalId;
    }
    
    public boolean equals(Object o){
        if (o==this){
            return true;
        }
        if (!(o instanceof TokenComponent)){
            return false;
        }
        TokenComponent t = (TokenComponent)o;
        return t.id == id;
    }
}
