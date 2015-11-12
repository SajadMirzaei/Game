package server;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextArea;

import objects.Util;
import objectsorting.object.Player;

public class UDPServer extends JFrame{
	public List<Player> players = new ArrayList<>();
	public Map<String, String> positions = new HashMap<String, String>();
	public InetAddress group = InetAddress.getByName(Util.GROUP_ADDRESS);
	JTextArea clientTextArea = new JTextArea(10,20);
	JButton button;
	DatagramSocket sendSocket;
	Thread senderThread;
	
	Sender sender;
	public boolean started;
	public boolean startButtonPushed;
	private Set<String> readyClients = new HashSet<String>();
	private Set<String> clients = new HashSet<String>();
	
	public UDPServer() throws Exception {
		super("Server");
		initializeFrame();
		started = false;
	}
	
	public void startThreads(){
		sender = new Sender(this);
		Thread receiver = new Thread(new Receiver(this), "Sender");
		receiver.start();
		senderThread = new Thread(sender, "Receiver");
		senderThread.start();
	}
	private void initializeFrame() {
		// Client Pane
		JPanel clientPane = new JPanel(new BorderLayout());
		JLabel labels = new JLabel("Clients:");
		clientPane.add(labels, BorderLayout.NORTH);
		
		clientPane.add(clientTextArea, BorderLayout.CENTER);
		
		button = new JButton("Start");
		button.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				button.setEnabled(false);
				synchronized (this) {
//					System.out.println("startButtonPushed = true");
					startButtonPushed = true;
				}
			}
		});
		getContentPane().add(clientPane, BorderLayout.NORTH);
		getContentPane().add(button, BorderLayout.PAGE_END);
		
		addWindowListener(new java.awt.event.WindowAdapter() {
		    @Override
		    public void windowClosing(java.awt.event.WindowEvent windowEvent) {
		    	System.exit(0);
		    }
		});
		
		// Set up
		setDefaultCloseOperation(DISPOSE_ON_CLOSE);
		pack();
		setSize(new Dimension(300,300));
		setVisible(true);
	}
	public String getDataFromPositions() {
		String result = "";
		for (String key : positions.keySet()) {
			result += key + Util.ID_SEPERATOR + positions.get(key);
			result += Util.MAJOR_SEPERATOR;
		}
		result = result.substring(0,result.length()-1);
		return result;
	}
	public void updateServer(String sentence, InetAddress inetAddress) {
		if (!sentence.contains(",")) {
			if (!sentence.contains("RECEIVED")) {
				if (!clients.contains(sentence)) {
					Player player = new Player();
					player.setId(sentence);
					players.add(player);
					clients.add(sentence);
					positions.put(sentence, sentence + "0" + Util.POSITION_SEPERATOR + sentence + "0");
					clientTextArea.append("Player connected from IP Address" + inetAddress + "\n");
				}
			}else{
				// ACKNOWLEDGMENT by client in sent
				String[] splited = sentence.split(Util.ID_SEPERATOR);
				readyClients.add(splited[0]);
				if (readyClients.size() == players.size()) {
					System.out.println("All acks received");
					synchronized (this) {
//						System.out.println("started = true");
						started = true;
					}
				}
			}
		}else{
			// Update positions
			String[] data = sentence.split(Util.ID_SEPERATOR);
			positions.put(data[0], data[1]);
		}
	}
	public static void main(String args[])  {
		try {
			UDPServer server = new UDPServer();
			server.startThreads();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}

class Receiver implements Runnable{
	
	UDPServer server;
	DatagramSocket receiverSocket;
	
	public Receiver(UDPServer server) {
		this.server = server;
		try {
			receiverSocket = new DatagramSocket(Util.UNI_PORT);
//			System.out.println("Sdfsd " + receiverSocket.getInetAddress());
		} catch (SocketException e) {
			e.printStackTrace();
		}
	}

	@Override
	public void run() {
		while(true){
			try {
				byte[] receiveData = new byte[1024];
				DatagramPacket receivePacket = new DatagramPacket(receiveData,
						receiveData.length);
				receiverSocket.receive(receivePacket);
				String sentence = new String(receivePacket.getData(),0,receivePacket.getLength());
//				System.out.println("Received: " + sentence);
				server.updateServer(sentence, receivePacket.getAddress());
			} catch (SocketException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
}

class Sender implements Runnable{
	
	UDPServer server;
	DatagramSocket sendSocket;
	
	public Sender(UDPServer server) {
		this.server = server;
		try {
			sendSocket = new DatagramSocket();
		} catch (SocketException e) {
			e.printStackTrace();
		}
	}
	
	public void send(String s){
//		System.out.println("Sending: " + s);
		byte[] sendData = s.getBytes();
		DatagramPacket sendPacket = new DatagramPacket(sendData,
				sendData.length, server.group, Util.MULTI_PORT);
		try {
			sendSocket.send(sendPacket);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	@Override
	public void run() {
		while(true){
			try {
				if (server.started) {
					String positionData = server.getDataFromPositions();
					send(positionData);
					Thread.sleep(10);
				}else if (server.startButtonPushed){
					send("GO");
					Thread.sleep(1000);
				}else{
					Thread.sleep(1000);
				}
			} catch (InterruptedException e) {
				server.clientTextArea.append(e.toString());
				e.printStackTrace();
			}
		}
	}
}