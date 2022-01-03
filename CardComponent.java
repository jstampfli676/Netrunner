/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

/**
 *
 * @author jorda
 */


import java.awt.*;
import java.awt.event.*;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.Serializable;
import javax.swing.*;
import javax.swing.border.*;

public class CardComponent extends JLabel implements Serializable{

  private volatile int screenX = 0;
  private volatile int screenY = 0;
  private volatile int myX = 0;
  private volatile int myY = 0;
  private ImageIcon frontImage;
  private ImageIcon backImage;
  private Icon rotatedFrontImage;
  private Icon rotatedBackImage;
  
  private final String title;
  //private transient BufferedImage rotatedFrontImage;
  //private transient BufferedImage rotatedBackImage;
  private boolean showingFront;
  private boolean rotated = false;
  private boolean mobile = false;
  private static final int WIDTH = 120;
  private static final int HEIGHT = 167;
  private final CardComponent me = this;
  private static int globalId = 0;
  private final int id;
  private final Game game;

  public CardComponent(ImageIcon frontImage, ImageIcon backImage, int id, String title, Game game) {
    this.title = title;
      
    this.frontImage = frontImage;
    this.backImage = backImage;
    
    rotatedFrontImage = new RotatedIcon(frontImage, new Double(90));
    rotatedBackImage = new RotatedIcon(backImage, new Double(90));
    this.id = id;
    //globalId++;
    this.game = new Game(game);
    
    
    
    setImageFront();
    
    //setMinimumSize(new Dimension(WIDTH, HEIGHT));
    //setPreferredSize(new Dimension(WIDTH, HEIGHT));
    //setMaximumSize(new Dimension(WIDTH, HEIGHT)); 
    //setBorder(new LineBorder(Color.BLUE, 3));
    //setBackground(Color.WHITE);
    setBounds(0, 0, WIDTH, HEIGHT);
    setOpaque(false);
    
    ToolTipManager.sharedInstance().setReshowDelay(0);
    ToolTipManager.sharedInstance().setInitialDelay(0);
    ToolTipManager.sharedInstance().setDismissDelay(100000);
    
    JPopupMenu optionsNotMobile = new JPopupMenu();
    JMenuItem playFaceup = new JMenuItem("Play Faceup");
    JMenuItem playFacedown = new JMenuItem("Play Facedown");
    JMenuItem returnDeck = new JMenuItem("Return to Deck");
    
    playFaceup.addActionListener(new ActionListener(){
        public void actionPerformed(ActionEvent e){
            Client.writeData(new Packet(19, null, game, frontImage, id, title));;
        }
    });
    
    playFacedown.addActionListener(new ActionListener(){
        public void actionPerformed(ActionEvent e){
            Client.writeData(new Packet(20, null, game, frontImage, id, title));
        }
    });
    
    returnDeck.addActionListener(new ActionListener(){
        public void actionPerformed(ActionEvent e){
            Client.writeData(new Packet(24, game, me));
        }
    });
    
    optionsNotMobile.add(playFaceup);
    optionsNotMobile.add(playFacedown);
    optionsNotMobile.add(returnDeck);
    
    JPopupMenu optionsMobile = new JPopupMenu();
    JMenuItem rotate = new JMenuItem("Rotate");
    JMenuItem flip = new JMenuItem("Flip");
    JMenuItem discard = new JMenuItem("Discard");
    JMenuItem hand = new JMenuItem("Return to Hand");



    rotate.addActionListener(new ActionListener(){
        public void actionPerformed(ActionEvent e){
            Client.writeData(new Packet(22, me, myX, myY, game));
        }
    });

    flip.addActionListener(new ActionListener(){
        public void actionPerformed(ActionEvent e){
            Client.writeData(new Packet(23, me, myX, myY, game));
        }
    });
    
    discard.addActionListener(new ActionListener(){
        public void actionPerformed(ActionEvent e){
            Client.writeData(new Packet(27, game, me));
        }
    });
    
    hand.addActionListener(new ActionListener(){
        public void actionPerformed(ActionEvent e){
            //CardComponent temp = (CardComponent)optionsMobile.getInvoker();
            System.out.println(me.getId());
            Client.writeData(new Packet(25, game, me));
        }
    });



    optionsMobile.add(rotate);
    optionsMobile.add(flip);
    optionsMobile.add(discard);
    optionsMobile.add(hand);

    addMouseListener(new MouseListener() {

      @Override
      public void mouseClicked(MouseEvent e) { 
          if (mobile){
              optionsMobile.show(me,50,20);
          } else {
              optionsNotMobile.show(me, 50, 20);
          }
      }

      @Override
      public void mousePressed(MouseEvent e) {
        screenX = e.getXOnScreen();
        screenY = e.getYOnScreen();

        myX = getX();
        myY = getY();
      }

      @Override
      public void mouseReleased(MouseEvent e) { 
          screenX = e.getXOnScreen();
          screenY = e.getYOnScreen();

          myX = getX();
          myY = getY();
          if (mobile){
              Client.writeData(new Packet(21, me, myX, myY, game));
          }
      }

      @Override
      public void mouseEntered(MouseEvent e) {
          me.setToolTipText(null);
          int fEnd = frontImage.toString().length()-4;
          int bEnd = backImage.toString().length()-4;
          if (showingFront){
              /*try {
                  try (PrintWriter pw = new PrintWriter(new File("C:/Users/jorda/java/Tester.txt"))) {
                      pw.println(frontImage.toString().substring(106, fEnd));
                  }
              } catch(IOException f){
                  
              }*/
              me.setToolTipText("<html><img src=\"" + getClass().getResource("Images/"+frontImage.toString().substring(106, fEnd)+".png")+ "\">");
              //theList.setToolTipText("<html><img src=\"" + getClass().getResource("Images/Netrunner-"+text+".png")+ "\">");
          } else {
              me.setToolTipText("<html><img src=\"" + getClass().getResource("Images/"+backImage.toString().substring(106, bEnd)+".png")+ "\">");
          }
          System.out.println(frontImage.toString().substring(96, fEnd));
      }

      @Override
      public void mouseExited(MouseEvent e) { 
          //ToolTipManager.sharedInstance().setDismissDelay(0);
      }

    });
    addMouseMotionListener(new MouseMotionListener() {

      @Override
      public void mouseDragged(MouseEvent e) {
        if (mobile){
            int deltaX = e.getXOnScreen() - screenX;
            int deltaY = e.getYOnScreen() - screenY;

            setLocation(myX + deltaX, myY + deltaY);
            
            
        }
      }

      @Override
      public void mouseMoved(MouseEvent e) { }

    });
  }
  
  public CardComponent(ImageIcon frontImage, ImageIcon backImage, String title, Game game){
      
      this(frontImage, backImage, globalId, title, game);
      globalId++;
  }
  
  public CardComponent (CardComponent c) {
      this(c.frontImage, c.backImage, c.id, c.title, c.game);
      this.showingFront = c.showingFront;
      this.rotated = c.rotated;
      
      if (!showingFront){
          setIcon(backImage);
      }
      if (rotated){
          this.rotated = false;
          setRotateImage();
      }
  }
  
  public String getTitle(){
      return title;
  }
  
  
  
  public ImageIcon getFrontImage(){
      return frontImage;
  }
  
  /*public BufferedImage rotateImage(BufferedImage image, Double degrees){
      double radians = Math.toRadians(degrees);
      double sin = Math.abs(Math.sin(radians));
      double cos = Math.abs(Math.cos(radians));
      int newWidth = HEIGHT;
      int newHeight = WIDTH;
      
      BufferedImage rotate = new BufferedImage(newWidth, newHeight, BufferedImage.TYPE_INT_ARGB);
      Graphics2D g2d = rotate.createGraphics();
      // Calculate the "anchor" point around which the image will be rotated
      int x = (newWidth - image.getWidth()) / 2;
      int y = (newHeight - image.getHeight()) / 2;
      // Transform the origin point around the anchor point
      AffineTransform at = new AffineTransform();
      at.setToRotation(radians, x + (image.getWidth() / 2), y + (image.getHeight() / 2));
      at.translate(x, y);
      g2d.setTransform(at);
      // Paint the originl image
      g2d.drawImage(image, 0, 0, null);
      g2d.dispose();
      return rotate;
  }*/
  
  public void flipImage(){
      if (showingFront) {
          showingFront = false;
          if (rotated){
              setIcon(rotatedBackImage);
          } else {
              setIcon(backImage);
          }
      } else {
          showingFront = true;
          if (rotated){
              setIcon(rotatedFrontImage);
          } else {
              setIcon(frontImage);
          }
      }
  }
  
  public void setImageFront(){
      me.setIcon(frontImage);
      showingFront = true;
  }
  
  public void setRotateImage(){
      Icon curImage = this.getIcon();
      if (rotated) {
          rotated = false;
          
          if (curImage.equals(rotatedFrontImage)){
              me.setIcon(frontImage);
          } else {
              me.setIcon(backImage);
          }
          setBounds(me.getX(),me.getY(),WIDTH,HEIGHT);
      } else {
          rotated = true;
          
          if (curImage.equals(frontImage)){
              System.out.println("rotating front");
              me.setIcon(rotatedFrontImage);
          } else {
              System.out.println("rotating");
              me.setIcon(rotatedBackImage);
          }
          setBounds(me.getX(),me.getY(),HEIGHT,WIDTH);
      }
  }
  
  public void setImageBack(){
      me.setIcon(backImage);
      showingFront = false;
  }
  
  public void setMobile(boolean mobile){
      this.mobile = mobile;
  }
  
  public void setCardBack(ImageIcon img){
      this.backImage = img;
  }
  
  public int getId(){
      return id;
  }
  
  public static void setGlobalId(int globalId){
      CardComponent.globalId = globalId;
  }
  
  @Override
  public boolean equals(Object o) {
      
      if (this==o) {
          return true;
      }
      if (!(o instanceof CardComponent)){
          return false;
      }
      CardComponent c = (CardComponent)o;
      boolean idWorking = c.id==this.id;
      //boolean imageWorking
      System.out.println("checking equals "+c.id+", "+c.frontImage+"="+this.id+", "+this.frontImage);
      return c.id==this.id;
  }

}
