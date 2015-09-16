package objectsorting;

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
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextArea;

import objects.Util;
import objectsorting.object.GameStatus;
import objectsorting.object.Player;
import objectsorting.object.Setting;
import objectsorting.object.Sink;
import objectsorting.object.Source;

import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.input.SAXBuilder;

public class Server_Old extends JFrame {

	public List<Player> players = new ArrayList<>();
	public Map<String, Player> playersMap = new LinkedHashMap<String, Player>();
	public InetAddress group = InetAddress.getByName(Util.GROUP_ADDRESS);
	JTextArea clientTextArea = new JTextArea(10, 20);
	JButton button;
	DatagramSocket sendSocket;
	Thread senderThread;

	SenderOld sender;
	public boolean started;
	public boolean startButtonPushed;
	private Set<String> readyClients = new HashSet<String>();
	private Set<String> clients = new HashSet<String>();

	public int numberOfSinkedObjects;
	public int numberOfRightSinkedObjects;
	
	public Setting setting;
	public GameStatus status;

	public Server_Old() throws Exception {
		super("Server");
		readConfigs("setting.xml");
		initializeFrame();
		started = false;
	}

	private void readConfigs(String url) {
		setting = new Setting();
		SAXBuilder sxb = new SAXBuilder();
		try {
			Document configDoc = sxb.build(Util.load(url));
			Element rootElmt = configDoc.getRootElement();
			Iterator<Element> iterator = rootElmt.getChildren().iterator();
			while (iterator.hasNext()) {
				Element element = iterator.next();
				if (element.getName().equals("SourceDescription")) {
					List<Element> sourceList = element.getChildren();
					for (Element sourceElement : sourceList) {
						Source source = new Source();
						source.setId(sourceElement.getAttributeValue("Number"));
						source.setPosition(new int[] {
								Integer.valueOf(sourceElement
										.getChild("Location").getChild("X")
										.getValue()),
								Integer.valueOf(sourceElement
										.getChild("Location").getChild("Y")
										.getValue()) });
						source.setFirstTypeProductionRate(Double.valueOf(sourceElement.getChild("Proportion").getValue()));
						setting.sourceList.add(source);
					}
				}else if (element.getName().equals("SinkDescription")) {
					List<Element> sinkList = element.getChildren();
					for (Element sinkElement : sinkList) {
						Sink sink = new Sink();
						sink.setId(sinkElement.getAttributeValue("Number"));
						sink.setPosition(new int[] {
								Integer.valueOf(sinkElement
										.getChild("Location").getChild("X")
										.getValue()),
								Integer.valueOf(sinkElement
										.getChild("Location").getChild("Y")
										.getValue()) });
						sink.setAcceptingFirstTypeObject(sinkElement.getChild("TargetColor").getValue().equals("1"));
						setting.sinkList.add(sink);
					}
				}else if (element.getName().equals("PlayerDescription")) {
//					List<Element> playerList = element.getChildren();
//					for (Element playerElement : playerList) {
//						Player player = new Player();
//						player.setId(playerElement.getAttributeValue("Number"));
//						player.setPosition(new int[] {
//								Integer.valueOf(playerElement
//										.getChild("Location").getChild("X")
//										.getValue()),
//								Integer.valueOf(playerElement
//										.getChild("Location").getChild("Y")
//										.getValue()) });
//						player.setSourceAttender((playerElement.getChild("Type").getValue().equals("1")));
//						setting.playerList.add(player);
//					}
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private void initializeGameSetup() {
		if (setting == null){
			setting = new Setting();
			Source so1 = new Source(Util.OBJ_SOURCE + 1, new int[] { 400, 100 },
					0.5);
			Source so2 = new Source(Util.OBJ_SOURCE + 2, new int[] { 400, 300 },
					0.5);
			Sink si1 = new Sink(Util.OBJ_SINK + 1, new int[] { 20, 300 }, true);
			Sink si2 = new Sink(Util.OBJ_SINK + 2, new int[] { 20, 100 }, false);

			setting.sourceList.add(so1);
			setting.sourceList.add(so2);
			setting.sinkList.add(si1);
			setting.sinkList.add(si2);
			for (int i = 0; i < players.size(); i++) {
				Player p = players.get(i);
				if (i % 2 == 0) {
					p.setPosition(new int[] { 475, 100 });
					playersMap.put(p.getId(), p);
				} else {
					p.setPosition(new int[] { 25, 100 });
					playersMap.put(p.getId(), p);
					p.setSourceAttender(false);
				}
			}
		}
		
	}

	public void startThreads() {
		sender = new SenderOld(this);
		Thread receiver = new Thread(new ReceiverOld(this), "Sender");
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
				initializeGameSetup();
				synchronized (this) {
					// System.out.println("startButtonPushed = true");
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
		setSize(new Dimension(300, 300));
		setVisible(true);
	}

	public String getDataFromPositions() {
		String result = "";
		for (String key : playersMap.keySet()) {
			Player p = playersMap.get(key);
			String newKey = key.toString();
			if (p.getCarrying() == 1) {
				newKey += Util.CARRYING_SIGN_1;
			} else if (p.getCarrying() == 2) {
				newKey += Util.CARRYING_SIGN_2;
			}
			result += newKey + Util.ID_SEPERATOR + p.getPosition()[0]
					+ Util.POSITION_SEPERATOR + p.getPosition()[1];
			result += Util.MAJOR_SEPERATOR;
		}
		result = result.substring(0, result.length() - 1);
		if (numberOfSinkedObjects == 0) {
			result += Util.MAJOR_SEPERATOR + Util.RATE_INDICATOR
					+ Util.ID_SEPERATOR + 0;
		} else {
			result += Util.MAJOR_SEPERATOR + Util.RATE_INDICATOR
					+ Util.ID_SEPERATOR + (double) numberOfRightSinkedObjects
					/ (double) numberOfSinkedObjects;
		}
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
					clientTextArea.append("Player connected from IP Address"
							+ inetAddress + "\n");
				}
			} else {
				// ACKNOWLEDGMENT by client in sent
				String[] splited = sentence.split(Util.ID_SEPERATOR);
				readyClients.add(splited[0]);
				if (readyClients.size() == players.size()) {
					System.out.println("All acks received");
					synchronized (this) {
						// System.out.println("started = true");
						started = true;
					}
				}
			}
		} else {
			// Update positions
			String[] data = sentence.split(Util.ID_SEPERATOR);
			int[] position = new int[] {
					Integer.valueOf(data[1].split(Util.POSITION_SEPERATOR)[0]),
					Integer.valueOf(data[1].split(Util.POSITION_SEPERATOR)[1]) };
			for (Player player : players) {
				if (data[0].equals(player.getId())) {
					updateStatus(player, position);
				}
			}
		}
	}

	private void updateStatus(Player player, int[] position) {
		if (position[0] == 237) {
			System.out.println();
		}
		player.setPosition(position);
		if (player.isSourceAttender()) {
			if (player.getCarrying() != 0) {
				for (Player otherPlayers : players) {
					if (!player.equals(otherPlayers)
							&& !otherPlayers.isSourceAttender()
							&& otherPlayers.getCarrying() == 0) {
						int x1 = player.getPosition()[0];
						int y1 = player.getPosition()[1];
						int x2 = otherPlayers.getPosition()[0];
						int y2 = otherPlayers.getPosition()[1];
						if (Math.sqrt(Math.pow(x1 - x2, 2)
								+ Math.pow(y1 - y2, 2)) < Util.PLAYER_SIZE) {
							otherPlayers.setCarrying(player.getCarrying());
							player.setCarrying(0);
						}
					}
				}
			} else {
				for (Source source : setting.sourceList) {
					int[] sourcePosition = source.getPosition();
					if (position[0] > sourcePosition[0]
							&& position[0] < sourcePosition[0]
									+ Util.SOURCE_SIZE
							&& position[1] > sourcePosition[1]
							&& position[1] < sourcePosition[1]
									+ Util.SOURCE_SIZE) {
						double randomNumber = Math.random();
						if (randomNumber < source.getFirstTypeProductionRate()) {
							player.setCarrying(1);
						} else {
							player.setCarrying(2);
						}
					}
				}
			}
		} else {
			if (player.getCarrying() != 0) {
				for (Sink sink : setting.sinkList) {
					int[] sinkPosition = sink.getPosition();
					if (position[0] > sinkPosition[0]
							&& position[0] < sinkPosition[0] + Util.SOURCE_SIZE
							&& position[1] > sinkPosition[1]
							&& position[1] < sinkPosition[1] + Util.SOURCE_SIZE) {
						if ((player.getCarrying() == 1 && sink
								.isAcceptingFirstTypeObject())
								|| (player.getCarrying() == 2 && !sink
										.isAcceptingFirstTypeObject())) {
							numberOfRightSinkedObjects++;
							numberOfSinkedObjects++;
						} else {
							numberOfSinkedObjects++;
						}
						player.setCarrying(0);
					}
				}
			} else {
				for (Player otherPlayers : players) {
					if (!player.equals(otherPlayers)
							&& otherPlayers.isSourceAttender()
							&& otherPlayers.getCarrying() != 0) {
						int x1 = player.getPosition()[0];
						int y1 = player.getPosition()[1];
						int x2 = otherPlayers.getPosition()[0];
						int y2 = otherPlayers.getPosition()[1];
						if (Math.sqrt(Math.pow(x1 - x2, 2)
								+ Math.pow(y1 - y2, 2)) < Util.PLAYER_SIZE) {
							player.setCarrying(otherPlayers.getCarrying());
							otherPlayers.setCarrying(0);
						}
					}
				}
			}
		}
	}

	public static void main(String args[]) {
		try {
			Server_Old server = new Server_Old();
			server.startThreads();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}

class ReceiverOld implements Runnable {

	Server_Old server;
	DatagramSocket receiverSocket;

	public ReceiverOld(Server_Old server) {
		this.server = server;
		try {
			receiverSocket = new DatagramSocket(Util.UNI_PORT);
			// System.out.println("Sdfsd " + receiverSocket.getInetAddress());
		} catch (SocketException e) {
			e.printStackTrace();
		}
	}

	@Override
	public void run() {
		while (true) {
			try {
				byte[] receiveData = new byte[1024];
				DatagramPacket receivePacket = new DatagramPacket(receiveData,
						receiveData.length);
				receiverSocket.receive(receivePacket);
				String sentence = new String(receivePacket.getData(), 0,
						receivePacket.getLength());
				// System.out.println("Received: " + sentence);
				server.updateServer(sentence, receivePacket.getAddress());
			} catch (SocketException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
}

class SenderOld implements Runnable {

	Server_Old server;
	DatagramSocket sendSocket;

	public SenderOld(Server_Old server) {
		this.server = server;
		try {
			sendSocket = new DatagramSocket();
		} catch (SocketException e) {
			e.printStackTrace();
		}
	}

	public void send(String s) {
		// System.out.println("Sending: " + s);
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
		while (true) {
			try {
				if (server.started) {
//					String positionData = server.getDataFromPositions();
					String positionData = server.status.toString();
					send(positionData);
					Thread.sleep(10);
				} else if (server.startButtonPushed) {
					send(server.setting.toString());
					Thread.sleep(1000);
				} else {
					Thread.sleep(1000);
				}
			} catch (InterruptedException e) {
				server.clientTextArea.append(e.toString());
				e.printStackTrace();
			}
		}
	}
}