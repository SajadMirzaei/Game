package objectsorting;

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
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.imageio.ImageIO;
import javax.swing.BoundedRangeModel;
import javax.swing.DefaultBoundedRangeModel;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JProgressBar;

import objects.Util;

public class GamePanel0 extends JPanel{
	
	private Point lastScreenPoint = new Point();
	Point lastRealPoint = new Point();
	Map<String, int[]> objectPositions = new HashMap<String, int[]>();
	final Dimension d = new Dimension(50, 50);
	private String id;
	public Client client;
	private String settingString;
	
	private List<int[]> sourcePositions = new ArrayList<>();
	private List<int[]> sinkPositions = new ArrayList<>();
	
	private List<Boolean> sinkFirstAcceptanceList = new ArrayList<>();
	private int line1Position = 2;
	private int line2Position = 490;
	
	private boolean sourceAttender = true;
	private boolean carryingBlue = false;
	private boolean carryingWhite = false;
	
	private double rate;
	
	
	BufferedImage avatar = null;
	BufferedImage avatarC1 = null;
	BufferedImage avatarC2 = null;
	BufferedImage avatarCX = null;
	BufferedImage otherAvatar = null;
	BufferedImage otherAvatarC = null;
	
	private final String IMAGE_CURRENT = "green.png";
	private final String IMAGE_CURRENT_CARRYING_1 = "green+blue.png";
	private final String IMAGE_CURRENT_CARRYING_2 = "green+white.png";
	private final String IMAGE_CURRENT_CARRYING_X = "green+black.png";
	private final String IMAGE_OTHER = "red.png";
	private final String IMAGE_OTHER_CARRYING = "red+.png";
	
	public boolean started = false;
	
	BoundedRangeModel progressBarModel = new DefaultBoundedRangeModel((int) (rate*100), 1, 0 , 100);
	
	JLabel progressPercent = new JLabel("0%");

    public GamePanel0(Client client) throws Exception{
    	add(new JProgressBar(progressBarModel));
    	add(progressPercent);
    	this.client = client;
    	this.id = client.clientID;
    	this.settingString = client.settingString;
    	initiateSettings();
    	setBackground(Color.LIGHT_GRAY);
		try {
			avatar = ImageIO.read(Util.load("icons/" + IMAGE_CURRENT));
			avatarC1 = ImageIO.read(Util.load("icons/" + IMAGE_CURRENT_CARRYING_1));
			avatarC2 = ImageIO.read(Util.load("icons/" + IMAGE_CURRENT_CARRYING_2));
			avatarCX = ImageIO.read(Util.load("icons/" + IMAGE_CURRENT_CARRYING_X));
			otherAvatar = ImageIO.read(Util.load("icons/" + IMAGE_OTHER));
			otherAvatarC = ImageIO.read(Util.load("icons/" + IMAGE_OTHER_CARRYING));
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
            	if (sourceAttender) {
	            	if (e.getX() < line1Position) {
	            		lastRealPoint = new Point((int) e.getLocationOnScreen().getX()+1,(int) e.getLocationOnScreen().getY());
	            		r.mouseMove((int) e.getLocationOnScreen().getX()+1, (int) e.getLocationOnScreen().getY());
	            		lastScreenPoint = new Point(line1Position+1, e.getY());
					}
				}else{
					if (e.getX() > line2Position) {
	            		lastRealPoint = new Point((int) e.getLocationOnScreen().getX()-1,(int) e.getLocationOnScreen().getY());
	            		r.mouseMove((int) e.getLocationOnScreen().getX()-1, (int) e.getLocationOnScreen().getY());
	            		lastScreenPoint = new Point(line2Position-1, e.getY());
					}
				}
//            	if (e.getY() < d.height/2) {
//            		lastRealPoint = new Point((int) e.getLocationOnScreen().getX(),(int) e.getLocationOnScreen().getY()+1);
//					r.mouseMove((int) e.getLocationOnScreen().getX(), (int) e.getLocationOnScreen().getY()+1);
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
        
    }
    
    private void initiateSettings() {
		String[] settings = settingString.split(Util.MAJOR_SEPERATOR);
		for (String s : settings) {
			if (!s.contains("setting")) {
				String[] specs = s.split(Util.ID_SEPERATOR);
				if (specs[0].contains(Util.OBJ_SOURCE)) {
					String[] position = specs[1].split(Util.POSITION_SEPERATOR);
					sourcePositions.add(new int[] {Integer.valueOf(position[0]),Integer.valueOf(position[1])});
				}else if (specs[0].contains(Util.OBJ_SINK)) {
					String[] position = specs[1].split(Util.POSITION_SEPERATOR);
					sinkPositions.add(new int[] {Integer.valueOf(position[0]),Integer.valueOf(position[1])});
					if (specs[0].contains(Util.CARRYING_SIGN_1)) {
						sinkFirstAcceptanceList.add(true);
					}else{
						sinkFirstAcceptanceList.add(false);
					}
				}else if (specs[0].contains(Util.OBJ_LINE_ONE)){
					String[] position = specs[1].split(Util.POSITION_SEPERATOR);
					line1Position = Integer.valueOf(position[0]);
				}else if (specs[0].contains(Util.OBJ_LINE_TWO)){
					String[] position = specs[1].split(Util.POSITION_SEPERATOR);
					line2Position = Integer.valueOf(position[0]);
				}else if (specs[0].contains(id)){
					if (!specs[0].contains(Util.SOURCE_ATTENDER_SIGN)) {
						sourceAttender = false;
					}
				}
			}
		}
	}

	@Override
    public void paintComponent(Graphics g){
        super.paintComponent(g);
        for (int[] position : sourcePositions) {
        	g.setColor(Color.BLACK);
			g.fillRect(position[0], position[1], Util.SOURCE_SIZE, Util.SOURCE_SIZE);
		}
        for (int i = 0; i < sinkPositions.size(); i++) {
        	int[] position = sinkPositions.get(i);
        	g.setColor(sinkFirstAcceptanceList.get(i) ? Color.BLUE : Color.WHITE);
			g.fillRect(position[0], position[1], Util.SOURCE_SIZE, Util.SOURCE_SIZE);
		}
        g.setColor(Color.RED);
        if (sourceAttender) {
        	g.drawLine(line1Position, 0, line1Position, this.getHeight());
		}else{
			g.drawLine(line2Position, 0, line2Position, this.getHeight());
		}
        synchronized (objectPositions) {
        	for (String key : objectPositions.keySet()) {
        		int[] position = objectPositions.get(key);
        		if (!key.contains(id)) {
        			if (key.contains(Util.CARRYING_SIGN_1) || key.contains(Util.CARRYING_SIGN_2)) {
        				g.drawImage(otherAvatarC, position[0]-Util.PLAYER_SIZE/2, position[1]-Util.PLAYER_SIZE/2, Util.PLAYER_SIZE, Util.PLAYER_SIZE , null);
        			}else{
        				g.drawImage(otherAvatar, position[0]-Util.PLAYER_SIZE/2, position[1]-Util.PLAYER_SIZE/2, Util.PLAYER_SIZE, Util.PLAYER_SIZE , null);
        			}
        		}else{
        			if (key.contains(Util.CARRYING_SIGN_1)){
        				carryingBlue = true;
        				carryingWhite = false;					
        			}
        			else if (key.contains(Util.CARRYING_SIGN_2)){
        				carryingBlue = false;
        				carryingWhite = true;
        			}else{
        				carryingBlue = false;
        				carryingWhite = false;
        			}
        		}
        	}
		}
        if (carryingBlue || carryingWhite) {
        	if (sourceAttender) {
        		if (carryingBlue) {
        			g.drawImage(avatarC1, lastScreenPoint.x-Util.PLAYER_SIZE/2, lastScreenPoint.y-Util.PLAYER_SIZE/2, Util.PLAYER_SIZE, Util.PLAYER_SIZE , null);
        		}else if (carryingWhite){
        			g.drawImage(avatarC2, lastScreenPoint.x-Util.PLAYER_SIZE/2, lastScreenPoint.y-Util.PLAYER_SIZE/2, Util.PLAYER_SIZE, Util.PLAYER_SIZE , null);
        		}
			}else{
				g.drawImage(avatarCX, lastScreenPoint.x-Util.PLAYER_SIZE/2, lastScreenPoint.y-Util.PLAYER_SIZE/2, Util.PLAYER_SIZE, Util.PLAYER_SIZE , null);
			}
		}else {
    		g.drawImage(avatar, lastScreenPoint.x-Util.PLAYER_SIZE/2, lastScreenPoint.y-Util.PLAYER_SIZE/2, Util.PLAYER_SIZE, Util.PLAYER_SIZE , null);
    	}
    }
    
    private void renewPositions() {
		for (int[] position : objectPositions.values()) {
			position[0] += Math.random()*11 - Math.random()*10;
			position[1] += Math.random()*11 - Math.random()*10;
		}
	}
    
    private void sendPosition() {
		String data = id + "-" + (int) lastScreenPoint.getX() + "," + (int) lastScreenPoint.getY();
		client.sender.send(data);
	}
    
    public void updatePositions(String received){
    	String[] positionSplit = received.split(Util.MAJOR_SEPERATOR);
    	synchronized (objectPositions) {
			objectPositions.clear();
			for (String string : positionSplit) {
				String[] split = string.split(Util.ID_SEPERATOR);
				if (string.contains(Util.RATE_INDICATOR)) {
					rate = Double.valueOf(split[1]);
					progressBarModel.setValue((int) (rate*100));
					progressPercent.setText(String.valueOf((new DecimalFormat("#.##").format(rate*100))) + "%");
				}else{
					objectPositions.put(
							split[0],
							new int[] {
									Integer.valueOf(split[1]
											.split(Util.POSITION_SEPERATOR)[0]),
											Integer.valueOf(split[1]
													.split(Util.POSITION_SEPERATOR)[1]) });
				}
			}
    	}
		repaint();
    }
}
