
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JPopupMenu;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

/**
 *
 * @author jorda
 */
public class HandComponent extends JLabel{
    
    private ImageIcon frontImage;
    private static final JPopupMenu options = new JPopupMenu();
    
    public HandComponent(ImageIcon frontImage){
        this.frontImage = frontImage;
        this.setIcon(frontImage);
        options.add("Play faceup");
        options.add("Play facedown");
        
        addMouseListener(new MouseListener(){
            @Override
            public void mouseClicked(MouseEvent e) { }

            @Override
            public void mousePressed(MouseEvent e) {
              
            }

            @Override
            public void mouseReleased(MouseEvent e) { }

            @Override
            public void mouseEntered(MouseEvent e) {
                int fEnd = frontImage.toString().length()-4;
                setToolTipText("<html><img src=\"" + getClass().getResource(frontImage.toString().substring(88, fEnd)+".png")+ "\">");
                System.out.println(frontImage.toString().substring(88, fEnd));
            }

            @Override
            public void mouseExited(MouseEvent e) { }
        });
    }
}
