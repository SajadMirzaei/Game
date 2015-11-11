package objectsorting;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.Image;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowEvent;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.util.ArrayList;
import java.util.Enumeration;

import javax.imageio.ImageIO;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.JToggleButton;
import javax.swing.plaf.metal.MetalToggleButtonUI;

import objects.Util;
import objectsorting.object.GameStatus;
import objectsorting.object.Setting;

public class Client extends JFrame{
	
	public static String[] avatarNames = new String[27];
	public JTextField serverField;
	public JTextField nameField;
	public GamePanel gamePanel;
	public String clientID;
	
	public ClientUDPSender sender;
	public static boolean started = false;
	public static boolean connected = false;
	
	public static Setting setting;
	
	private boolean bPasued=false;
    private JFrame frame;
    private boolean bsameClientId=false;
    
    public static boolean bGroupIpReceived=false;
    public static ArrayList<String> ipAddresses=new ArrayList<String>();
    
	static{
		for (int i = 0; i < avatarNames.length; i++) {
			avatarNames[i] = String.valueOf(i+1);
		}
	}
	
	public Client() throws Exception {
		super("Client");
		
		getMachineIP();
        frame = new JFrame("Client");
		Point p = new Point(400, 400);
		setLocation(p.x, p.y);
		//Server Pane
		JPanel serverPane = new JPanel();
		serverPane.setLayout(new GridLayout(7,1));
		serverField = new JTextField("localhost", 20);
		serverField.selectAll();
		nameField = new JTextField();
		
		serverPane.add(new JLabel("Server:"));
		serverPane.add(serverField);
		serverPane.add(new JLabel("Name:"));
		serverPane.add(nameField);
		getContentPane().add(serverPane, BorderLayout.NORTH);
		
		//Button Pane 
		JPanel buttonPane = new JPanel();
		JButton button = new JButton("OK");
		button.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				initiateWaitingDialog();
			}
		});
		buttonPane.add(button);
		getContentPane().add(buttonPane, BorderLayout.PAGE_END);
		setDefaultCloseOperation(DISPOSE_ON_CLOSE);
		pack();
		setVisible(true);
		
		
	}
	
	public void startThreads(){
		Thread receiver = new Thread(new ClientGroupIPReceiver(this));
		receiver.start();
		
		sender = new ClientUDPSender(this);
		Thread senderThread = new Thread(sender);
		senderThread.start();
	}
	
//	public void updateClient(String s){
//		if (s.contains("setting")) {
//			setting = new Setting(s);
//			connected = true;
//		}else if (s.contains("status")){
//			if (!started) {
//				started = true;
//				initiateGamePanel();
//			}
//			gamePanel.updatePositions(s);
//		}
//	}
	
    private void delayAWhile(int msec)
    {
        try {
                    Thread.sleep(msec);
            } catch (InterruptedException e) {
                    e.printStackTrace();
            }
    }
        
	public boolean updateClient(byte[] b){
		boolean bfinish=false;
	    try {
	    	ByteArrayInputStream bis = new ByteArrayInputStream(b);
	    	ObjectInputStream ois = new ObjectInputStream(bis);
            Object object = ois.readObject();
                          
            if (object instanceof Setting) {
            	bGroupIpReceived=false;
                setting = (Setting) object;
                connected = true;
                bsameClientId=false;
            }else if (object instanceof GameStatus){
                if (!started) {
                    started = true;
                    frame.getContentPane().removeAll();
                    initiateGamePanel();
                    frame.repaint();
                }
                gamePanel.updatePositions((GameStatus) object);
            }
//            else if(((String)object).contains("SendSettingNow")){
//System.out.println("Received sendsetting now");
//            	bGroupIpReceived=false;
//            }
            else if(((String)object).contains("PAUSE")){
                connected=false;
                started=false;
                if(bsameClientId==false)
                {
                    clientID = nameField.getText() + System.currentTimeMillis();
                    bsameClientId=true;
                }
                
                bfinish=true;
                delayAWhile(1000);
            }
            bis.close();
            ois.close();
        } catch (ClassNotFoundException | IOException e) {
                e.printStackTrace();
        }
	    return bfinish;
	}
	
	public void startReceiver(String sgroupIp){
		Thread receiver = new Thread(new ClientUDPReceiver(this, sgroupIp));
		receiver.start();
	}
	
	public void startGroupIpReceiver(){
		Thread receiver = new Thread(new ClientGroupIPReceiver(this));
		receiver.start();
	}
	
	public void initiateWaitingDialog(){
		clientID = nameField.getText() + System.currentTimeMillis();
		setEnabled(false);
		JDialog dialog = new JDialog(this);
		dialog.getContentPane().add(new JLabel("Please wait for server to start the game"));
		dialog.setDefaultCloseOperation(JDialog.DO_NOTHING_ON_CLOSE);
		dialog.setSize(300, 100);
		dialog.setLocation(getLocation().x+50,getLocation().y+50);
		dialog.setVisible(!started);
		startThreads();
	}
	
	public void initiateGamePanel(){
		setVisible(false);
		
		try {
			JPanel infoPanel = new JPanel();
			infoPanel.setPreferredSize(new Dimension(setting.screenSize[0], 50));
			gamePanel = new GamePanel(this, infoPanel);
			gamePanel.setPreferredSize(new Dimension(setting.screenSize[0], setting.screenSize[1]));
			frame.getContentPane()
			.add(infoPanel, BorderLayout.NORTH);
			frame.getContentPane()
			.add(gamePanel, BorderLayout.CENTER);
		} catch (Exception e1) {
			e1.printStackTrace();
		}
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		// frame.setLocation(new Point(100, 100));
		frame.setSize(setting.screenSize[0], setting.screenSize[1]+50);
                              
		frame.setVisible(true);
		frame.setResizable(false);
	}
	
    public void getMachineIP(){
    	ipAddresses.clear();
    	try{
	    	Enumeration en = NetworkInterface.getNetworkInterfaces();
	    	while(en.hasMoreElements()){
	    	    NetworkInterface ni=(NetworkInterface) en.nextElement();
	    	    Enumeration ee = ni.getInetAddresses();
	    	    while(ee.hasMoreElements()) {
	    	        InetAddress ia= (InetAddress) ee.nextElement();
	    	        ipAddresses.add(ia.getHostAddress());
System.out.println(ia.getHostAddress());
	    	    }
	    	 }
    	}catch(Exception e){
    		e.printStackTrace();
    	}
    }
	
	public static void main(String[] a) {
		try {
			Client client = new Client();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}


//will change the group address
class ClientGroupIPReceiver implements Runnable{
	
	Client client;
	MulticastSocket receiverSocket;
	InetAddress group;
	
	public ClientGroupIPReceiver(Client client) {
		this.client = client;
		try {
			receiverSocket = new MulticastSocket(Util.MULTI_PORT);
			group = InetAddress.getByName(Util.GROUP_ADDRESS);
			receiverSocket.joinGroup(group);
		} catch (SocketException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	@Override
	public void run() {
		byte[] receiveData = new byte[4096];
		DatagramPacket receivePacket = new DatagramPacket(receiveData, receiveData.length);
		while (true) {
			try{
				receiverSocket.setSoTimeout(2000);
				receiverSocket.receive(receivePacket);

				//client.updateClient(receivePacket.getData());
				ByteArrayInputStream bis = new ByteArrayInputStream(receivePacket.getData());
		    	ObjectInputStream ois = new ObjectInputStream(bis);
	            Object object = ois.readObject();
	            String content=(String)object;
	            if(content.contains("GROUP_IP:")){	            	
	            	String scontent=content.substring(9);
	            	String[] parts = scontent.split("_");
	            	String cip=parts[0];//received client ip
	            	if(cip.equals("localhost"))
	            		cip="127.0.0.1";
	            	String groupIp=parts[1];//group ip for the received client ip
	            	
					boolean bMatch=false;
					for(int i=0;i<client.ipAddresses.size();i++)
					{
System.out.println(client.ipAddresses.get(i)+"_"+cip);
		            	if(cip.equals(client.ipAddresses.get(i)))
		            	{
		            		bMatch=true;
		            		break;
		            	}
					}
					
					if(bMatch==true){
	            		receiverSocket.leaveGroup(group);
		            	receiverSocket.close();
	            		client.startReceiver(groupIp);
	            		client.bGroupIpReceived=true;
	            		break;
	            	}
	            }
				
			}catch (SocketTimeoutException ex){
//				sendSocket.send(sendPacket);
			} catch (SocketException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
			catch (ClassNotFoundException e) {
                e.printStackTrace();
			}
		}
	}
}


class ClientUDPReceiver implements Runnable{
	
	Client client;
	MulticastSocket receiverSocket;
	InetAddress group;
	
	public ClientUDPReceiver(Client client, String sgroupIp) {
		this.client = client;
		try {
			receiverSocket = new MulticastSocket(Util.MULTI_PORT);
			group = InetAddress.getByName(sgroupIp);
			receiverSocket.joinGroup(group);
		} catch (SocketException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	@Override
	public void run() {
		byte[] receiveData = new byte[4096];
		DatagramPacket receivePacket = new DatagramPacket(receiveData, receiveData.length);
		while (true) {
			try{
//System.out.println("Client UDP receiver of group " + group.getHostAddress() + " working now....");
				receiverSocket.setSoTimeout(2000);
				receiverSocket.receive(receivePacket);
//				String received = new String(receivePacket.getData(),0,receivePacket.getLength());
//				System.out.println("Received: " + received);
				boolean bpause=client.updateClient(receivePacket.getData());
				if(bpause==true){
					receiverSocket.leaveGroup(group);
	            	receiverSocket.close();
	            	client.startGroupIpReceiver();
	            	client.bGroupIpReceived=false;
	            	break;
				}
//				client.updateClient(received);
			}catch (SocketTimeoutException ex){
//				sendSocket.send(sendPacket);
			} catch (SocketException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
}



class ClientUDPSender implements Runnable{
	
	Client client;
	DatagramSocket sendSocket;
	
	public ClientUDPSender(Client client) {
		this.client = client;
		try {
			sendSocket = new DatagramSocket();
		} catch (SocketException e) {
			e.printStackTrace();
		}
	}
	
	public void send(String s){
//		System.out.println("Sending: " + s);
		if(client.started==true && client.connected==true)
		{
			byte[] sendData = s.getBytes();
			try {
				DatagramPacket sendPacket = new DatagramPacket(sendData,
						sendData.length, InetAddress.getByName(client.serverField.getText()), Util.UNI_PORT);
				sendSocket.send(sendPacket);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
	
	public void connect(String s){
//		System.out.println("Sending: " + s);
		
		byte[] sendData = s.getBytes();
		try {
			DatagramPacket sendPacket = new DatagramPacket(sendData,
					sendData.length, InetAddress.getByName(client.serverField.getText()), Util.UNI_PORT);
			sendSocket.send(sendPacket);
		} catch (IOException e) {
			e.printStackTrace();
		}		
	}
	
	@Override
	public void run() {
		try {
	        while (true) {
	            //JOptionPane.showMessageDialog(null, "test");
	            if(!client.started){
	            	
	            	if (client.bGroupIpReceived){
	                	connect(new String(client.clientID + "-GROUPIPGET"));	                	
	                    Thread.sleep(500);	                    
	                }
	            	
	                if (client.connected==false) {
	                	connect(client.clientID);
	                }
	                else{
	                    connect(new String(client.clientID + "-RECEIVED"));
	                }
	            }
	            	            
	            Thread.sleep(1000);
	            
	        }
		} catch (InterruptedException e) {
            e.printStackTrace();
        }
	}
}
