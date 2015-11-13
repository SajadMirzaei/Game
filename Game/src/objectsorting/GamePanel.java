package objectsorting;

import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Font;
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
	
//	BufferedImage avatarSo = null;
//	BufferedImage avatarSi = null;
//	BufferedImage avatarC1 = null;
//	BufferedImage avatarC2 = null;
//	BufferedImage avatarCX = null;
//	BufferedImage otherAvatarSo = null;
//	BufferedImage otherAvatarSi = null;
//	BufferedImage otherAvatarSoC = null;
//	BufferedImage otherAvatarSiC = null;
//	
//	private final String IMAGE_CURRENT_SOURCE = "green.png";
//	private final String IMAGE_CURRENT_SINK = "red.png";
//	private final String IMAGE_CURRENT_CARRYING_1 = "green+blue.png";
//	private final String IMAGE_CURRENT_CARRYING_2 = "green+white.png";
//	private final String IMAGE_CURRENT_SINK_CARRYING_X = "red+.png";
//	private final String IMAGE_OTHER_SOURCE = "lightGreen.png";
//	private final String IMAGE_OTHER_SINK = "lightRed.png";
//	private final String IMAGE_OTHER_SOURCE_CARRYING = "green+black.png";
//	private final String IMAGE_OTHER_SINK_CARRYING = "lightRed+.png";
	
	public boolean started = false;
	
	public static Setting setting = new Setting();
	public static GameStatus status = new GameStatus();
	public static Player player = new Player();
	
	BoundedRangeModel overallProgressBarModel;
	BoundedRangeModel individualProgressBarModel;
	
	JLabel progressPercent = new JLabel("0%");
	
	private AvatarMover avatarMover;

    public GamePanel(Client client, JPanel infoPanel) throws Exception{
    	this.client = client;
    	this.id = client.clientID;
    	this.setting = client.setting;
    	for (Player p : setting.playerList) {
			if (p.getId().equals(id)) {
				player.setPosition(p.getPosition());
				avatarLocation[0] = p.getPosition()[0];
				avatarLocation[1] = p.getPosition()[1];
			}
		}
//    	JPanel progressBarPannel = new JPanel();
//    	progressBarPannel.setLayout(new GridLayout(2,2));
//    	overallProgressBarModel = new DefaultBoundedRangeModel((int) (status.rate), 1, 0 , setting.maxDropOffRate*100);
//    	individualProgressBarModel = new DefaultBoundedRangeModel((int) (player.getDropOffs()), 1, 0 , setting.maxDropOffRate*100);
//    	progressBarPannel.add(new JLabel("Individual Progress"));
//    	progressBarPannel.add(new JProgressBar(individualProgressBarModel));
////    	progressBarPannel.add(new JLabel("0.0"));
//    	progressBarPannel.add(new JLabel("Overall Progress"));
//    	progressBarPannel.add(new JProgressBar(overallProgressBarModel));
////    	progressBarPannel.add(progressPercent);
//    	progressBarPannel.setPreferredSize(new Dimension(setting.screenSize[0]/2, 40));
//    	progressBarPannel.setBackground(Color.white);
//    	infoPanel.setBackground(Color.white);
//    	infoPanel.add(progressBarPannel);
//    	setBackground(Color.LIGHT_GRAY);
//		BufferedImage cursorImg = new BufferedImage(16, 16, BufferedImage.TYPE_INT_ARGB);

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
            	avatarMover.setDestination(new int[]{e.getX(), e.getY()});
            }
        });
        
    }
    
	@Override
    public void paintComponent(Graphics g){
        super.paintComponent(g);
        
        if (status.gameRunning) {
        	for (Source source : setting.sourceList) {
        		int[] position = source.getPosition();
        		g.setColor(source.getColor());
        		g.fillRect(position[0], position[1], source.getSize(), source.getSize());
        	}
        	for (Sink sink : setting.sinkList) {
        		int[] position = sink.getPosition();
//        		g.setColor(player.isSourceAttender() ? Color.black : sink.getColor());
        		g.setColor(sink.getColor());
        		g.fillRect(position[0], position[1], sink.getSize(), sink.getSize());
        	}
        	for (Base base : setting.baseList) {
        		int[] position = base.getPosition();
        		g.setColor(base.getColor());
        		g.fillRect(position[0], position[1], base.getSize(), base.getSize());
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
        				if (p.isSourceAttender()) {
        					g.setColor(setting.soOtherColor);
        					g.fillOval(p.getPosition()[0] - setting.soOtherSize/2, p.getPosition()[1] - setting.soOtherSize/2, setting.soOtherSize, setting.soOtherSize);
        					if (p.getCarrying() != 0) {
        						g.setColor(Color.black);
        						g.fillOval(p.getPosition()[0] - setting.soOtherSize/4, p.getPosition()[1] - setting.soOtherSize/4, setting.soOtherSize/2, setting.soOtherSize/2);
        					}
        				}else{
        					g.setColor(setting.siOtherColor);
        					g.fillOval(p.getPosition()[0] - setting.siOtherSize/2, p.getPosition()[1] - setting.siOtherSize/2, setting.siOtherSize, setting.siOtherSize);
        					if (p.getCarrying() != 0) {
        						g.setColor(Color.black);
        						g.fillOval(p.getPosition()[0] - setting.siOtherSize/4, p.getPosition()[1] - setting.siOtherSize/4, setting.siOtherSize/2, setting.siOtherSize/2);
        					}
        				}
        			}
        		}
        	}
        	if (status.wrongObjectAlert) {
    			g.setColor(Color.RED);
    			g.setFont(new Font(Font.DIALOG, Font.BOLD, 30));
    			g.drawChars("LOST".toCharArray(), 0, 4, setting.screenSize[0]/2 - 50, setting.screenSize[1]/2 - 50);
    		}
        	if (player.isSourceAttender()) {
        		g.setColor(setting.soSelfColor);
        		g.fillOval(player.getPosition()[0]-setting.soSelfSize/2, player.getPosition()[1]-setting.soSelfSize/2, setting.soSelfSize, setting.soSelfSize);
        	}else{
        		g.setColor(setting.siSelfColor);
        		g.fillOval(player.getPosition()[0] - setting.siSelfSize/2, player.getPosition()[1] - setting.siSelfSize/2, setting.siSelfSize, setting.siSelfSize);
        	}
        	if (player.getCarrying() != 0) {
        		if (player.isSourceAttender()) {
        			g.setColor(setting.objectColors.get(player.getCarrying()));
        			g.fillOval(player.getPosition()[0] - setting.soSelfSize/4, player.getPosition()[1] - setting.soSelfSize/4, setting.soSelfSize/2, setting.soSelfSize/2);
        		}else{
        			g.setColor(Color.black);
        			g.fillOval(player.getPosition()[0] - setting.siSelfSize/4, player.getPosition()[1] - setting.siSelfSize/4, setting.siSelfSize/2, setting.siSelfSize/2);
        		}
        	}
        	int barLength = 300;
            g.setColor(Color.WHITE);
            g.fillRect(setting.screenSize[0]/2, 1, barLength, 20);
            g.setColor(Color.BLUE);
            g.fillRect(setting.screenSize[0]/2, 1, (int) ((double) player.getDropOffs()/setting.maxDropOffRate*barLength), 20);
            g.setColor(Color.WHITE);
            g.fillRect(setting.screenSize[0]/2, 40, barLength, 20);
            g.setColor(Color.BLUE);
            g.fillRect(setting.screenSize[0]/2, 40, (int) (status.rate/setting.maxDropOffRate*barLength), 20);
            g.setColor(Color.RED);
            g.fillRect((int) ((double)setting.gameEndCriterion/setting.maxDropOffRate*barLength) + setting.screenSize[0]/2, 40, 2, 20);
            
            g.setColor(Color.BLACK);
			g.setFont(new Font(Font.DIALOG, Font.BOLD, 15));
			g.drawChars("Individual Progress".toCharArray(), 0, 19, setting.screenSize[0]/2-150, 15);
			g.setColor(Color.BLACK);
			g.setFont(new Font(Font.DIALOG, Font.BOLD, 15));
			g.drawChars("Group Progress".toCharArray(), 0, 14, setting.screenSize[0]/2-150, 55);
		}else{
			g.setColor(Color.GREEN);
			g.setFont(new Font(Font.DIALOG, Font.BOLD, 72));
			g.drawChars("You Won or Time Out!".toCharArray(), 0, 7, setting.screenSize[0]/2-100, setting.screenSize[1]/2);
			g.setFont(new Font(Font.DIALOG, Font.ITALIC, 36));
			g.drawChars("Wait for the next game to start".toCharArray(), 0, 31, setting.screenSize[0]/2-100, setting.screenSize[1]/2+100);
		}
    }
    
    public void sendPosition() {
		String data = id + Util.ID_SEPERATOR + (int) avatarLocation[0] + Util.POSITION_SEPERATOR + (int) avatarLocation[1];
		client.sender.send(data);
	}
    
//    public void updatePositions(String received){
//    	synchronized (status) {
//    		status = new GameStatus(received);
//    		overallProgressBarModel.setValue((int) (status.rate*100));
//			progressPercent.setText(String.valueOf((new DecimalFormat("#.##").format(status.rate))) + "/" + setting.maxDropOffRate);
//			for (Player p : status.players) {
//				if (p.getId().equals(id)) {
//					individualProgressBarModel.setValue((int) (p.getDropOffs()*100));
//					player = p;
//				}
//			}
//    	}
//		repaint();
//    }
    
    public void updatePositions(GameStatus st){
    	synchronized (status) {
    		status = st;
//    		overallProgressBarModel.setValue((int) (status.rate*100));
			progressPercent.setText(String.valueOf((new DecimalFormat("#.##").format(status.rate))) + "/" + setting.maxDropOffRate);
			for (Player p : status.players) {
				if (p.getId().equals(id)) {
//					individualProgressBarModel.setValue((int) (p.getDropOffs()*100));
					player = p;
					if (player.getCarrying() == 0) {
						if (player.isSourceAttender()) {
							avatarMover.setSpeed(setting.soSpeedUnladen*player.getSpeedMultiplier());
						}else{
							avatarMover.setSpeed(setting.siSpeedUnladen*player.getSpeedMultiplier());
						}
					}else{
						if (player.isSourceAttender()) {
							avatarMover.setSpeed(setting.soSpeedCarrying*player.getSpeedMultiplier());
						}else{
							avatarMover.setSpeed(setting.siSpeedCarrying*player.getSpeedMultiplier());
						}
					}
				}
			}
    	}
		repaint();
    }
}

class AvatarMover implements Runnable{
	
	GamePanel gamePanel;
	private int[] destination;
	private double velocity = 15;
	private long updateIntervalMillis = 10;
	
	public AvatarMover(GamePanel gamePanel, int speed) {
		this.gamePanel = gamePanel;
//		this.velocity = speed;
		destination = new int[] {(int) gamePanel.avatarLocation[0], (int) gamePanel.avatarLocation[1]};
	}
	
	public void setSpeed(double speed) {
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
