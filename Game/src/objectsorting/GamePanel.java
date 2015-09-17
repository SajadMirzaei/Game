package objectsorting;

import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.GridLayout;
import java.awt.Point;
import java.awt.Robot;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;
import java.awt.image.BufferedImage;
import java.io.IOException;
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
import objectsorting.object.Base;
import objectsorting.object.GameStatus;
import objectsorting.object.Player;
import objectsorting.object.Setting;
import objectsorting.object.Sink;
import objectsorting.object.Source;

public class GamePanel extends JPanel{
	
	private Point lastScreenPoint = new Point();
//	private Point lastRealPoint = new Point();
	public double[] avatarLocation = new double[2];
	Map<String, int[]> objectPositions = new HashMap<String, int[]>();
	final Dimension d = new Dimension(50, 50);
	private String id;
	public Client client;
	
//	private List<int[]> sourcePositions = new ArrayList<>();
//	private List<int[]> sinkPositions = new ArrayList<>();
//	
//	private List<Boolean> sinkFirstAcceptanceList = new ArrayList<>();
//	private int line1Position = 2;
//	private int line2Position = 490;
	
	
	
//	private boolean sourceAttender = true;
//	private boolean carryingBlue = false;
//	private boolean carryingWhite = false;
	
//	private double rate;
	
	BufferedImage avatarSo = null;
	BufferedImage avatarSi = null;
	BufferedImage avatarC1 = null;
	BufferedImage avatarC2 = null;
	BufferedImage avatarCX = null;
	BufferedImage otherAvatarSo = null;
	BufferedImage otherAvatarSi = null;
	BufferedImage otherAvatarSoC = null;
	BufferedImage otherAvatarSiC = null;
	
	private final String IMAGE_CURRENT_SOURCE = "green.png";
	private final String IMAGE_CURRENT_SINK = "red.png";
	private final String IMAGE_CURRENT_CARRYING_1 = "green+blue.png";
	private final String IMAGE_CURRENT_CARRYING_2 = "green+white.png";
	private final String IMAGE_CURRENT_SINK_CARRYING_X = "red+.png";
	private final String IMAGE_OTHER_SOURCE = "lightGreen.png";
	private final String IMAGE_OTHER_SINK = "lightRed.png";
	private final String IMAGE_OTHER_SOURCE_CARRYING = "green+black.png";
	private final String IMAGE_OTHER_SINK_CARRYING = "lightRed+.png";
	
	public boolean started = false;
	
	public Setting setting = new Setting();
	public GameStatus status = new GameStatus();
	public Player player = new Player();
	
	BoundedRangeModel overallProgressBarModel;
	BoundedRangeModel individualProgressBarModel;
	
	JLabel progressPercent = new JLabel("0%");
	
	private AvatarMover avatarMover;

    public GamePanel(Client client, JPanel infoPanel) throws Exception{
    	this.client = client;
    	this.id = client.clientID;
    	this.setting = client.setting;
    	
    	JPanel progressBarPannel = new JPanel();
    	progressBarPannel.setLayout(new GridLayout(2,2));
    	overallProgressBarModel = new DefaultBoundedRangeModel((int) (status.rate), 1, 0 , setting.maxDropOffRate*100);
    	individualProgressBarModel = new DefaultBoundedRangeModel((int) (player.getDropOffs()), 1, 0 , setting.maxDropOffRate*100);
    	progressBarPannel.add(new JLabel("Individual Progress"));
    	progressBarPannel.add(new JProgressBar(individualProgressBarModel));
//    	progressBarPannel.add(new JLabel("0.0"));
    	progressBarPannel.add(new JLabel("Overall Progress"));
    	progressBarPannel.add(new JProgressBar(overallProgressBarModel));
//    	progressBarPannel.add(progressPercent);
    	progressBarPannel.setPreferredSize(new Dimension(setting.screenSize[0]/2, 40));
    	progressBarPannel.setBackground(Color.white);
    	infoPanel.setBackground(Color.white);
    	infoPanel.add(progressBarPannel);
    	
    	avatarLocation = new double[] {10,10};
    	setBackground(Color.LIGHT_GRAY);
		try {
			avatarSo = ImageIO.read(Util.load("icons/" + IMAGE_CURRENT_SOURCE));
			avatarSi = ImageIO.read(Util.load("icons/" + IMAGE_CURRENT_SINK));
			avatarC1 = ImageIO.read(Util.load("icons/" + IMAGE_CURRENT_CARRYING_1));
			avatarC2 = ImageIO.read(Util.load("icons/" + IMAGE_CURRENT_CARRYING_2));
			avatarCX = ImageIO.read(Util.load("icons/" + IMAGE_CURRENT_SINK_CARRYING_X));
			otherAvatarSo = ImageIO.read(Util.load("icons/" + IMAGE_OTHER_SOURCE));
			otherAvatarSi = ImageIO.read(Util.load("icons/" + IMAGE_OTHER_SINK));
			otherAvatarSoC = ImageIO.read(Util.load("icons/" + IMAGE_OTHER_SOURCE_CARRYING));
			otherAvatarSiC = ImageIO.read(Util.load("icons/" + IMAGE_OTHER_SINK_CARRYING));
		} catch (IOException e1) {
			e1.printStackTrace();
		}
		BufferedImage cursorImg = new BufferedImage(16, 16, BufferedImage.TYPE_INT_ARGB);

		// Create a new blank cursor.
//		Cursor blankCursor = getToolkit().createCustomCursor(
//		    cursorImg, new Point(0, 0), "blank cursor");

		// Set the blank cursor to the JFrame.
//    	Cursor c = getToolkit().createCustomCursor(avatar , new Point(d.width/2,d.height/2), "img");
//    	setCursor (blankCursor);
    	final Robot r = new Robot();
    	
    	avatarMover = new AvatarMover(this, 10);
    	Thread avatarMoverThread = new Thread(avatarMover);
    	avatarMoverThread.start();
    	
        addMouseMotionListener(new MouseMotionAdapter() {
            @Override
            public void mouseMoved(MouseEvent e) {
//            	lastScreenPoint= new Point(e.getX(), e.getY());
            	avatarMover.setDestination(new int[]{e.getX(), e.getY()});
//            	if (sourceAttender) {
//	            	if (e.getX() < line1Position) {
//	            		lastRealPoint = new Point((int) e.getLocationOnScreen().getX()+1,(int) e.getLocationOnScreen().getY());
//	            		r.mouseMove((int) e.getLocationOnScreen().getX()+1, (int) e.getLocationOnScreen().getY());
//	            		lastScreenPoint = new Point(line1Position+1, e.getY());
//					}
//				}else{
//					if (e.getX() > line2Position) {
//	            		lastRealPoint = new Point((int) e.getLocationOnScreen().getX()-1,(int) e.getLocationOnScreen().getY());
//	            		r.mouseMove((int) e.getLocationOnScreen().getX()-1, (int) e.getLocationOnScreen().getY());
//	            		lastScreenPoint = new Point(line2Position-1, e.getY());
//					}
//				}
//            	if (e.getY() < d.height/2) {
//            		lastRealPoint = new Point((int) e.getLocationOnScreen().getX(),(int) e.getLocationOnScreen().getY()+1);
//					r.mouseMove((int) e.getLocationOnScreen().getX(), (int) e.getLocationOnScreen().getY()+1);
//				}
//            	if (e.getY() > getSize().height-d.height/2) {
//            		lastRealPoint = new Point((int) e.getLocationOnScreen().getX(),(int) e.getLocationOnScreen().getY()-1);
//            		r.mouseMove((int) e.getLocationOnScreen().getX(), (int) e.getLocationOnScreen().getY()-1);
//				}
//            	playersPositions.put("1",new int[] {e.getX(), e.getY()});
//            	repaint();
//            	sendPosition();
            }
        });
        
    }
    
	@Override
    public void paintComponent(Graphics g){
        super.paintComponent(g);
        for (Source source : setting.sourceList) {
        	int[] position = source.getPosition();
        	g.setColor(Color.BLACK);
			g.fillRect(position[0], position[1], Util.SOURCE_SIZE, Util.SOURCE_SIZE);
		}
        for (Sink sink : setting.sinkList) {
        	int[] position = sink.getPosition();
        	g.setColor(sink.isAcceptingFirstTypeObject() ? Color.BLUE : Color.WHITE);
			g.fillRect(position[0], position[1], Util.SOURCE_SIZE, Util.SOURCE_SIZE);
		}
        for (Base base : setting.baseList) {
        	int[] position = base.getPosition();
        	g.setColor(Color.ORANGE);
			g.fillRect(position[0], position[1], Util.SOURCE_SIZE, Util.SOURCE_SIZE);
		}
        g.setColor(Color.RED);
        if (player.isSourceAttender()) {
        	g.drawLine(setting.line1Position, 0, setting.line1Position, this.getHeight());
		}else{
			g.drawLine(setting.line2Position, 0, setting.line2Position, this.getHeight());
		}
        synchronized (status) {
			for (Player p : status.players) {
				if (!p.getId().equals(player.getId())) {
					if (p.getCarrying() != 0) {
						if (p.isSourceAttender()) {
							g.drawImage(otherAvatarSoC, p.getPosition()[0]-Util.PLAYER_SIZE/2, p.getPosition()[1]-Util.PLAYER_SIZE/2, Util.PLAYER_SIZE, Util.PLAYER_SIZE , null);
						}else{
							g.drawImage(otherAvatarSiC, p.getPosition()[0]-Util.PLAYER_SIZE/2, p.getPosition()[1]-Util.PLAYER_SIZE/2, Util.PLAYER_SIZE, Util.PLAYER_SIZE , null);
						}
        			}else{
        				if (p.isSourceAttender()) {
							g.drawImage(otherAvatarSo, p.getPosition()[0]-Util.PLAYER_SIZE/2, p.getPosition()[1]-Util.PLAYER_SIZE/2, Util.PLAYER_SIZE, Util.PLAYER_SIZE , null);
						}else{
							g.drawImage(otherAvatarSi, p.getPosition()[0]-Util.PLAYER_SIZE/2, p.getPosition()[1]-Util.PLAYER_SIZE/2, Util.PLAYER_SIZE, Util.PLAYER_SIZE , null);
						}
        			}
				}
			}
		}
        if (player.getCarrying() != 0) {
        	if (player.isSourceAttender()) {
        		if (player.getCarrying() == 1) {
        			g.drawImage(avatarC1, (int) avatarLocation[0]-Util.PLAYER_SIZE/2, (int) avatarLocation[1]-Util.PLAYER_SIZE/2, Util.PLAYER_SIZE, Util.PLAYER_SIZE , null);
        		}else if (player.getCarrying() == 2){
        			g.drawImage(avatarC2, (int) avatarLocation[0]-Util.PLAYER_SIZE/2, (int) avatarLocation[1]-Util.PLAYER_SIZE/2, Util.PLAYER_SIZE, Util.PLAYER_SIZE , null);
        		}
			}else{
				g.drawImage(avatarCX, (int) avatarLocation[0]-Util.PLAYER_SIZE/2, (int) avatarLocation[1]-Util.PLAYER_SIZE/2, Util.PLAYER_SIZE, Util.PLAYER_SIZE , null);
			}
		}else {
			if (player.isSourceAttender()) {
				g.drawImage(avatarSo, (int) avatarLocation[0]-Util.PLAYER_SIZE/2, (int) avatarLocation[1]-Util.PLAYER_SIZE/2, Util.PLAYER_SIZE, Util.PLAYER_SIZE , null);
			}else{
				g.drawImage(avatarSi, (int) avatarLocation[0]-Util.PLAYER_SIZE/2, (int) avatarLocation[1]-Util.PLAYER_SIZE/2, Util.PLAYER_SIZE, Util.PLAYER_SIZE , null);
			}
    	}
    }
    
    public void sendPosition() {
		String data = id + "-" + (int) avatarLocation[0] + "," + (int) avatarLocation[1];
		client.sender.send(data);
	}
    
    public void updatePositions(String received){
    	synchronized (status) {
    		status = new GameStatus(received);
    		overallProgressBarModel.setValue((int) (status.rate*100));
			progressPercent.setText(String.valueOf((new DecimalFormat("#.##").format(status.rate))) + "/" + setting.maxDropOffRate);
			for (Player p : status.players) {
				if (p.getId().equals(id)) {
					individualProgressBarModel.setValue((int) (p.getDropOffs()*100));
					player = p;
				}
			}
    	}
		repaint();
    }
    
    public void updatePositions(GameStatus st){
    	synchronized (status) {
    		status = st;
    		overallProgressBarModel.setValue((int) (status.rate*100));
			progressPercent.setText(String.valueOf((new DecimalFormat("#.##").format(status.rate))) + "/" + setting.maxDropOffRate);
			for (Player p : status.players) {
				if (p.getId().equals(id)) {
					individualProgressBarModel.setValue((int) (p.getDropOffs()*100));
					player = p;
				}
			}
    	}
		repaint();
    }
}

class AvatarMover implements Runnable{
	
	GamePanel gamePanel;
	private int[] destination;
	private int velocity = 15;
	private long updateIntervalMillis = 10;
	
	public AvatarMover(GamePanel gamePanel, int speed) {
		this.gamePanel = gamePanel;
//		this.velocity = speed;
		destination = new int[] {(int) gamePanel.avatarLocation[0], (int) gamePanel.avatarLocation[1]};
	}
	
	public void setSpeed(int speed) {
		this.velocity = speed;
	}
	
	public void setDestination(int[] destination) {
		this.destination = destination;
	}
	
	@Override
	public void run() {
		while (true) {
			double deltaX = destination[0] - gamePanel.avatarLocation[0];
			double deltaY = destination[1] - gamePanel.avatarLocation[1];
			double direction = Math.atan2(deltaY, deltaX);
			double xMove = velocity*Math.cos(direction)/updateIntervalMillis;
			double yMove = velocity*Math.sin(direction)/updateIntervalMillis;
			if (xMove < 0) {
				gamePanel.avatarLocation[0] = gamePanel.avatarLocation[0] + Math.max(xMove, destination[0]-gamePanel.avatarLocation[0]);
			}else{
				gamePanel.avatarLocation[0] = gamePanel.avatarLocation[0] + Math.min(xMove, destination[0]-gamePanel.avatarLocation[0]);
			}
			
			if (yMove < 0) {
				gamePanel.avatarLocation[1] = gamePanel.avatarLocation[1] + Math.max(yMove, destination[1]-gamePanel.avatarLocation[1]);
			}else{
				gamePanel.avatarLocation[1] = gamePanel.avatarLocation[1] + Math.min(yMove, destination[1]-gamePanel.avatarLocation[1]);
			}
			
			if (gamePanel.player.isSourceAttender()) {
				gamePanel.avatarLocation[0] = Math.max(gamePanel.avatarLocation[0], gamePanel.setting.line1Position);
			}else{
				gamePanel.avatarLocation[0] = Math.min(gamePanel.avatarLocation[0], gamePanel.setting.line2Position);
			}
			gamePanel.sendPosition();
			try {
				Thread.sleep(updateIntervalMillis);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}
}
