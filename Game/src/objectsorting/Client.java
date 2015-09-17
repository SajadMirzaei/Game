package objectsorting;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.Image;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.net.SocketException;
import java.net.SocketTimeoutException;

import javax.imageio.ImageIO;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.JToggleButton;
import javax.swing.plaf.metal.MetalToggleButtonUI;

import objects.Util;
import objectsorting.object.Setting;

public class Client extends JFrame{
	
	public static String[] avatarNames = new String[27];
	public JTextField serverField;
	public JTextField nameField;
	public GamePanel gamePanel;
	public String clientID;
	
	public ClientUDPSender sender;
	public boolean started = false;
	public boolean connected = false;
	
	public Setting setting;
	
	static{
		for (int i = 0; i < avatarNames.length; i++) {
			avatarNames[i] = String.valueOf(i+1);
		}
	}

	public Client() throws Exception {
		super("Client");
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
		sender = new ClientUDPSender(this);
		Thread receiver = new Thread(new ClientUDPReceiver(this));
		receiver.start();
		Thread senderThread = new Thread(sender);
		senderThread.start();
	}
	
	public void updateClient(String s){
		if (s.contains("setting")) {
			setting = new Setting(s);
			connected = true;
		}else if (s.contains("status")){
			if (!started) {
				started = true;
				initiateGamePanel();
			}
			gamePanel.updatePositions(s);
		}
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
		JFrame frame = new JFrame("Client");
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

	public static void main(String[] a) {
		try {
			Client client = new Client();
//			client.startThreads();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}

class ClientUDPReceiver implements Runnable{
	
	Client client;
	MulticastSocket receiverSocket;
	
	public ClientUDPReceiver(Client client) {
		this.client = client;
		try {
			receiverSocket = new MulticastSocket(Util.MULTI_PORT);
			InetAddress group = InetAddress.getByName(Util.GROUP_ADDRESS);
			receiverSocket.joinGroup(group);
		} catch (SocketException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	@Override
	public void run() {
		byte[] receiveData = new byte[1024];
		DatagramPacket receivePacket = new DatagramPacket(receiveData, receiveData.length);
		while (true) {
			try{
				receiverSocket.setSoTimeout(2000);
				receiverSocket.receive(receivePacket);
				String received = new String(receivePacket.getData(),0,receivePacket.getLength());
//				System.out.println("Received: " + received);
				client.updateClient(received);
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
		while (!client.started) {
			if (!client.connected) {
				send(client.clientID);
			}else{
				send(new String(client.clientID + "-RECEIVED"));
			}
			try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}
}
