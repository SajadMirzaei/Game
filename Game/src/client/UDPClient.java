package client;

import java.awt.BorderLayout;
import java.awt.Color;
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

public class UDPClient extends JFrame{
	
	public static String[] avatarNames = new String[27];
	JToggleButton[] avatarBottons = new JToggleButton[avatarNames.length];
	public String selectedAvatar = "";
	public JTextField serverField;
	DatagramSocket sendSocket;
	MulticastSocket receiveSocket;
	public GamePanel gamePanel;
	
	ClientUDPSender sender;
	public boolean started = false;
	public boolean connected = false;
	
	static{
		for (int i = 0; i < avatarNames.length; i++) {
			avatarNames[i] = String.valueOf(i+1);
		}
	}

	public UDPClient() throws Exception {
		super("Client");
		Point p = new Point(400, 400);
		setLocation(p.x, p.y);
		//Server Pane
		JPanel serverPane = new JPanel();
		serverPane.setLayout(new BorderLayout());
		serverPane.add(new JLabel("Server:"), BorderLayout.NORTH);
		serverField = new JTextField("localhost");
		serverField.selectAll();
		serverPane.add(serverField, BorderLayout.CENTER);
		getContentPane().add(serverPane, BorderLayout.NORTH);
		
		serverPane.add(new JLabel("Select Your Avatar"), BorderLayout.SOUTH);
		// Avatar Pane
		JPanel avatarPane = new JPanel(new GridLayout(5,10));
		for (int i = 0; i < avatarBottons.length; i++) {
			ImageIcon icon = new ImageIcon(ImageIO.read(Util.load("icons/"+ avatarNames[i] + ".png")));
			Image img = icon.getImage(); 
			Image newimg = img.getScaledInstance(50, 50,  java.awt.Image.SCALE_SMOOTH);  
			icon = new ImageIcon(newimg);
			avatarBottons[i] = new JToggleButton(icon);
			avatarBottons[i].setFocusPainted(false);
			avatarBottons[i].setName(avatarNames[i]);
			avatarBottons[i].setUI(new MetalToggleButtonUI() {
			    @Override
			    protected Color getSelectColor() {
			        return Color.BLUE;
			    }
			});
			avatarBottons[i].addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					JToggleButton button = (JToggleButton) e.getSource();
					selectedAvatar = button.getName();
					for (JToggleButton jButton : avatarBottons) {
						if (!jButton.equals(button)) {
							jButton.setSelected(false);
							jButton.setBackground(null);
						}
					}
				}
			});
			avatarPane.add(avatarBottons[i], BorderLayout.CENTER);
		}
		getContentPane().add(avatarPane, BorderLayout.CENTER);
		
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
		if (s.contains("GO")) {
			connected = true;
		}else if (s.contains(Util.POSITION_SEPERATOR)){
			if (!started) {
				started = true;
				initiateGamePanel();
			}
			gamePanel.updatePositions(s);
		}
	}
	
	public void initiateWaitingDialog(){
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
			gamePanel = new GamePanel(this);
			frame.getContentPane()
			.add(gamePanel, BorderLayout.CENTER);
		} catch (Exception e1) {
			e1.printStackTrace();
		}
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		// frame.setLocation(new Point(100, 100));
		frame.setSize(500, 500);
		frame.setVisible(true);
		frame.setResizable(false);
	}

	public static void main(String[] a) {
		try {
			UDPClient client = new UDPClient();
//			client.startThreads();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}

class ClientUDPReceiver implements Runnable{
	
	UDPClient client;
	MulticastSocket receiverSocket;
	
	public ClientUDPReceiver(UDPClient client) {
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
	
	UDPClient client;
	DatagramSocket sendSocket;
	
	public ClientUDPSender(UDPClient client) {
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
				send(client.selectedAvatar);
			}else{
				send(new String(client.selectedAvatar + "-RECEIVED"));
			}
			try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}
}