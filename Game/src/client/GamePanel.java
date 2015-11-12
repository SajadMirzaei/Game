package client;

import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.Point;
import java.awt.Robot;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.net.UnknownHostException;
import java.util.HashMap;
import java.util.Map;

import javax.imageio.ImageIO;
import javax.swing.JPanel;

import objects.Util;

public class GamePanel extends JPanel{
	
	private Point lastScreenPoint = new Point();
	Point lastRealPoint = new Point();
	BufferedImage avatar = null;
	Map<String, int[]> playersPositions = new HashMap<String, int[]>();
	Image[] images = new Image[27];
	final Dimension d = new Dimension(50, 50);
	private String selectedAvatar;
	public UDPClient client;
	
	public boolean started = false;

    public GamePanel(UDPClient client) throws Exception{
    	this.client = client;
    	this.selectedAvatar = client.selectedAvatar;
    	setBackground(Color.white);
		try {
			avatar = ImageIO.read(Util.load("icons/" + selectedAvatar + ".png"));
			for (int i = 0; i < images.length; i++) {
				images[i] = ImageIO.read(Util.load("icons/" + String.valueOf(i+1) + ".png"));
			}
		} catch (IOException e1) {
			e1.printStackTrace();
		}
		BufferedImage cursorImg = new BufferedImage(16, 16, BufferedImage.TYPE_INT_ARGB);

		// Create a new blank cursor.
		Cursor blankCursor = getToolkit().createCustomCursor(
		    cursorImg, new Point(0, 0), "blank cursor");

		// Set the blank cursor to the JFrame.
//    	Cursor c = getToolkit().createCustomCursor(avatar , new Point(d.width/2,d.height/2), "img");
    	setCursor (blankCursor);
    	final Robot r = new Robot();
        addMouseMotionListener(new MouseMotionAdapter() {
            @Override
            public void mouseMoved(MouseEvent e) {
            	lastScreenPoint= new Point(e.getX(), e.getY());
//            	if (e.getX() < d.width/2) {
//            		lastRealPoint = new Point((int) e.getLocationOnScreen().getX()+1,(int) e.getLocationOnScreen().getY());
//            		r.mouseMove((int) e.getLocationOnScreen().getX()+1, (int) e.getLocationOnScreen().getY());
//				}
//            	if (e.getY() < d.height/2) {
//            		lastRealPoint = new Point((int) e.getLocationOnScreen().getX(),(int) e.getLocationOnScreen().getY()+1);
//					r.mouseMove((int) e.getLocationOnScreen().getX(), (int) e.getLocationOnScreen().getY()+1);
//				}
//            	if (e.getX() > getSize().width-d.width/2) {
//            		lastRealPoint = new Point((int) e.getLocationOnScreen().getX()-1,(int) e.getLocationOnScreen().getY());
//            		r.mouseMove((int) e.getLocationOnScreen().getX()-1, (int) e.getLocationOnScreen().getY());
//				}
//            	if (e.getY() > getSize().height-d.height/2) {
//            		lastRealPoint = new Point((int) e.getLocationOnScreen().getX(),(int) e.getLocationOnScreen().getY()-1);
//            		r.mouseMove((int) e.getLocationOnScreen().getX(), (int) e.getLocationOnScreen().getY()-1);
//				}
//            	playersPositions.put("1",new int[] {e.getX(), e.getY()});
            	repaint();
            	sendPosition();
            }
        });
        
//        addMouseListener(new MouseAdapter() {
//        	@Override
//        	public void mouseExited(MouseEvent e) {
//        		lastScreenPoint= new Point(e.getX(), e.getY());
//        		playersPositions.put("1",new int[] {e.getX(), e.getY()});
//        	}
//        	
//        	@Override
//        	public void mouseEntered(MouseEvent e) {
//        		r.mouseMove((int) lastScreenPoint.x, lastRealPoint.y);
//        		super.mouseEntered(e);
//        	}
//		});
        
        
    }
    
    @Override
    public void paintComponent(Graphics g){
        super.paintComponent(g);
        g.drawImage(avatar, lastScreenPoint.x-d.width/2, lastScreenPoint.y-d.height/2, d.width, d.height , null);
        for (String key : playersPositions.keySet()) {
        	int[] position = playersPositions.get(key);
        	if (!key.equals(selectedAvatar)) {
        		g.drawImage(images[Integer.valueOf(key)-1], position[0]-d.width/2, position[1]-d.height/2, d.width, d.height , null);
			}
		}
    }
    
    private void renewPositions() {
		for (int[] position : playersPositions.values()) {
			position[0] += Math.random()*11 - Math.random()*10;
			position[1] += Math.random()*11 - Math.random()*10;
		}
	}
    
    private void sendPosition() {
		String data = selectedAvatar + "-" + (int) lastScreenPoint.getX() + "," + (int) lastScreenPoint.getY();
		client.sender.send(data);
	}
    
    public void updatePositions(String received){
    	String[] positionSplit = received.split(Util.MAJOR_SEPERATOR);
		for (String string : positionSplit) {
			String[] split = string.split(Util.ID_SEPERATOR);
			playersPositions.put(split[0], new int[]{Integer.valueOf(split[1].split(Util.POSITION_SEPERATOR)[0]),Integer.valueOf(split[1].split(Util.POSITION_SEPERATOR)[1])});
		}
		repaint();
    }
}
