package objectsorting;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextArea;
import javax.swing.JTextField;

import objects.Util;
import objectsorting.object.Base;
import objectsorting.object.GameStatus;
import objectsorting.object.Player;
import objectsorting.object.Setting;
import objectsorting.object.Sink;
import objectsorting.object.Source;
import shuffling.WaveManager;
import shuffling.ObjectSortingGame;

import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.input.SAXBuilder;

public class Server extends JFrame {

	public InetAddress group = InetAddress.getByName(Util.GROUP_ADDRESS);
	JTextArea clientTextArea = new JTextArea(10, 20);
	JButton button;
	DatagramSocket sendSocket;
	Thread senderThread;

	Sender sender;
	public boolean started;
	public boolean startButtonPushed;
	private Set<String> readyClients = new HashSet<String>();
	private Set<String> clients = new HashSet<String>();

	public Setting setting;
	public GameStatus status;
	public WaveManager wavemngr;

	private String sconfig;
	private int playInitialPos[][];
	
	private static Server server;

	public Server() throws Exception {
		super("Server");
		status = new GameStatus();
		sconfig = "../setting.xml";
		readConfigs(sconfig);

		// save initial player positions
		int numOfPlayers = status.players.size();
		playInitialPos = new int[numOfPlayers][2];
		for (int i = 0; i < numOfPlayers; i++) {
			playInitialPos[i] = status.players.get(i).getPosition();
		}

		status.setSetting(setting);
		initializeFrame();
		started = false;

		wavemngr = new WaveManager(this);
		wavemngr.loadConfiguration(sconfig);
	}

	public void stopCurrentGame() {
		synchronized (this) {
			started = false;
			startButtonPushed = false;
		}
		clientTextArea.append("Current game is finished or time out! \n");

		// try {
		// Thread.sleep(15000);
		// } catch (InterruptedException e) {
		// e.printStackTrace();
		// }
	}

	public void broadCastAllFinished() {
		synchronized (this) {
			started = false;
			startButtonPushed = false;
		}
		clientTextArea.append("All the waves has been finished! \n");
	}

	private void initialGameInfo() {
		// reset to the initial positions
		int numOfPlayers = status.players.size();
		for (int i = 0; i < numOfPlayers; i++) {
			updateStatus(status.players.get(i), playInitialPos[i]);
		}
	}

	public void startNewGame(ObjectSortingGame curGame) {
		try {
			Thread.sleep(15000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}

		clientTextArea.append("New game started! \n");
		initialGameInfo();

		synchronized (this) {
			started = true;
			startButtonPushed = true;
		}

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
				if (element.getName().equals("GeneralSetting")) {
					Element screenSizeChild = element.getChild("ScreenSize");
					setting.screenSize[0] = Integer.valueOf(screenSizeChild
							.getAttributeValue("x"));
					setting.screenSize[1] = Integer.valueOf(screenSizeChild
							.getAttributeValue("y"));
					List<Element> settingElements = element.getChildren("Object");
					for (Element element2 : settingElements) {
						Color color = new Color(Integer.valueOf(element2.getAttributeValue("r")),Integer.valueOf(element2.getAttributeValue("g")),Integer.valueOf(element2.getAttributeValue("b")));
						setting.objectColors.put(Integer.valueOf(element2.getAttributeValue("type")), color);
					}
					Element settingElement = element.getChild("SourceAttender");
					Element colorElement = settingElement.getChild("SelfColor");
					Color color = new Color(Integer.valueOf(colorElement.getAttributeValue("r")),Integer.valueOf(colorElement.getAttributeValue("g")),Integer.valueOf(colorElement.getAttributeValue("b")));
					setting.soSelfColor = color;
					colorElement = settingElement.getChild("OtherColor");
					color = new Color(Integer.valueOf(colorElement.getAttributeValue("r")),Integer.valueOf(colorElement.getAttributeValue("g")),Integer.valueOf(colorElement.getAttributeValue("b")));
					setting.soOtherColor = color;
					setting.soSelfSize = Integer.valueOf(settingElement.getChild("SelfSize").getValue());
					setting.soOtherSize = Integer.valueOf(settingElement.getChild("OtherSize").getValue());
					setting.soSpeedCarrying = Integer.valueOf(settingElement.getChild("SpeedCarrying").getValue());
					setting.soSpeedUnladen = Integer.valueOf(settingElement.getChild("SpeedUnladen").getValue());
					settingElement = element.getChild("SinkAttender");
					colorElement = settingElement.getChild("SelfColor");
					color = new Color(Integer.valueOf(colorElement.getAttributeValue("r")),Integer.valueOf(colorElement.getAttributeValue("g")),Integer.valueOf(colorElement.getAttributeValue("b")));
					setting.siSelfColor = color;
					colorElement = settingElement.getChild("OtherColor");
					color = new Color(Integer.valueOf(colorElement.getAttributeValue("r")),Integer.valueOf(colorElement.getAttributeValue("g")),Integer.valueOf(colorElement.getAttributeValue("b")));
					setting.siOtherColor = color;
					setting.siSelfSize = Integer.valueOf(settingElement.getChild("SelfSize").getValue());
					setting.siOtherSize = Integer.valueOf(settingElement.getChild("OtherSize").getValue());
					setting.siSpeedCarrying = Integer.valueOf(settingElement.getChild("SpeedCarrying").getValue());
					setting.siSpeedUnladen = Integer.valueOf(settingElement.getChild("SpeedUnladen").getValue());
					// SOURCE READ
				} else if (element.getName().equals("SourceDescription")) {
					setting.line1Position = Integer.valueOf(element.getChild(
							"LeftBoundary").getValue());
					List<Element> sourceList = element.getChildren("Source");
					for (Element sourceElement : sourceList) {
						Source source = new Source();
						source.setId(sourceElement.getAttributeValue("Number"));
						List<Element> objects = sourceElement.getChild("Proportions").getChildren();
						double sum = 0;
						for (Element element2 : objects) {
							source.getProportionMap().put(Integer.valueOf(element2.getAttributeValue("type")), Double.valueOf(element2.getAttributeValue("proportion")));
							sum += Double.valueOf(element2.getAttributeValue("proportion"));
						}
						if (sum != 1) {
							throw new Exception("Proportion values should add up to 1");
						}
						source.setPosition(new int[] {
								Integer.valueOf(sourceElement
										.getChild("Location").getChild("X")
										.getValue()),
								Integer.valueOf(sourceElement
										.getChild("Location").getChild("Y")
										.getValue()) });
						source.setSize(Integer.valueOf(sourceElement
								.getChild("Appearance").getChild("Size")
								.getValue()));
						Element colorElement = sourceElement.getChild("Appearance").getChild("Color");
						source.setColor(new Color(Integer.valueOf(colorElement
								.getAttributeValue("r")), Integer
								.valueOf(colorElement.getAttributeValue("g")),
								Integer.valueOf(colorElement
										.getAttributeValue("b"))));
						setting.sourceList.add(source);
					}
					// SINK READ
				} else if (element.getName().equals("SinkDescription")) {
					setting.line2Position = Integer.valueOf(element.getChild(
							"RightBoundary").getValue());
					List<Element> sinkList = element.getChildren("Sink");
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
						sink.setAcceptingObject(Integer.valueOf(sinkElement.getChild("TargetType").getValue()));
						sink.setSize(Integer.valueOf(sinkElement
								.getChild("Appearance").getChild("Size")
								.getValue()));
						Element colorElement = sinkElement.getChild("Appearance").getChild("Color");
						sink.setColor(new Color(Integer.valueOf(colorElement
								.getAttributeValue("r")), Integer
								.valueOf(colorElement.getAttributeValue("g")),
								Integer.valueOf(colorElement
										.getAttributeValue("b"))));
						setting.sinkList.add(sink);
					}
					// BASE READ
				} else if (element.getName().equals("BaseDescription")) {
					List<Element> baseList = element.getChildren();
					for (Element baseElement : baseList) {
						Base base = new Base();
						base.setId(baseElement.getAttributeValue("Number"));
						base.setPosition(new int[] {
								Integer.valueOf(baseElement
										.getChild("Location").getChild("X")
										.getValue()),
								Integer.valueOf(baseElement
										.getChild("Location").getChild("Y")
										.getValue()) });
						base.setSize(Integer.valueOf(baseElement
								.getChild("Appearance").getChild("Size")
								.getValue()));
						Element colorElement = baseElement.getChild("Appearance").getChild("Color");
						base.setColor(new Color(Integer.valueOf(colorElement
								.getAttributeValue("r")), Integer
								.valueOf(colorElement.getAttributeValue("g")),
								Integer.valueOf(colorElement
										.getAttributeValue("b"))));
						if (baseElement.getAttribute("enabled")
								.getBooleanValue()) {
							setting.baseList.add(base);
						}
					}
				} else if (element.getName().equals("PlayerDescription")) {
					List<Element> playerList = element.getChildren();
					for (Element playerElement : playerList) {
						Player player = new Player();
						player.setId(playerElement.getAttributeValue("Number"));
						player.setSpeedMultiplier(Double.valueOf(playerElement.getChild("SpeedMultiplier").getValue()));
						player.setPosition(new int[] {
								Integer.valueOf(playerElement
										.getChild("Location").getChild("X")
										.getValue()),
								Integer.valueOf(playerElement
										.getChild("Location").getChild("Y")
										.getValue()) });
						player.setSourceAttender((playerElement
								.getChild("Type").getValue().equals("1")));
						setting.playerList.add(player);
						status.players.add(player);
						status.playerDropOffMap.put(player.getId(),
								new ArrayList<Long>());
					}
				} else if (element.getName().equals("FeedbackDisplay")) {
					setting.gameEndCriterion = Integer.valueOf(element
							.getChild("GameEndCriterion").getValue());
					setting.maxDropOffRate = Integer.valueOf(element.getChild(
							"MaxDropOffRate").getValue());
					setting.timeWindow = Integer.valueOf(element.getChild(
							"TimeWindow").getValue());
				}
			}
		} catch (Exception e) {
			JDialog d = new JDialog(this);
			d.getContentPane()
					.add(new JTextArea("Error Reading Setting File: \n" + e.getMessage()));
			d.setSize(300, 300);
			d.setVisible(true);
			e.printStackTrace();
		}
	}

	public void startThreads() {
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
				// initializeGameSetup();
				synchronized (this) {
					// System.out.println("startButtonPushed = true");
					startButtonPushed = true;
				}
				wavemngr.start(); // start the thread
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

	// public String getDataFromPositions() {
	// String result = "";
	// for (String key : playersMap.keySet()) {
	// Player p = playersMap.get(key);
	// String newKey = key.toString();
	// if (p.getCarrying() == 1) {
	// newKey += Util.CARRYING_SIGN_1;
	// } else if (p.getCarrying() == 2) {
	// newKey += Util.CARRYING_SIGN_2;
	// }
	// result += newKey + Util.ID_SEPERATOR + p.getPosition()[0]
	// + Util.POSITION_SEPERATOR + p.getPosition()[1];
	// result += Util.PLAYER_SEPERATOR;
	// }
	// result = result.substring(0, result.length() - 1);
	// if (numberOfSinkedObjects == 0) {
	// result += Util.PLAYER_SEPERATOR + Util.RATE_INDICATOR
	// + Util.ID_SEPERATOR + 0;
	// } else {
	// result += Util.PLAYER_SEPERATOR + Util.RATE_INDICATOR
	// + Util.ID_SEPERATOR + (double) numberOfRightSinkedObjects
	// / (double) numberOfSinkedObjects;
	// }
	// return result;
	// }

	public void updateServer(String sentence, InetAddress inetAddress) {
		if (!sentence.contains(",")) {
			if (!sentence.contains("RECEIVED")) {
				if (!clients.contains(sentence)) {
					status.assignPlayer(sentence);
					clients.add(sentence);
					clientTextArea.append("Player connected from IP Address"
							+ inetAddress + "\n");
				}
			} else {
				// ACKNOWLEDGMENT by client in sent
				String[] splited = sentence.split(Util.ID_SEPERATOR);
				readyClients.add(splited[0]);
				if (readyClients.size() == status.getNumberOfAssignedPlayers()) {
					// System.out.println("All acks received");
					synchronized (this) {
						// System.out.println("started = true");
						status.startTimeMillis = System.currentTimeMillis();
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
			for (Player player : status.players) {
				if (data[0].equals(player.getId())) {
					updateStatus(player, position);
				}
			}
		}
	}

	private void updateStatus(Player player, int[] position) {
		player.setPosition(position);
		if (player.isSourceAttender()) {
			if (player.getCarrying() != 0) {
				for (Player otherPlayers : status.players) {
					if (!player.equals(otherPlayers)
							&& !otherPlayers.isSourceAttender()
							&& otherPlayers.getCarrying() == 0) {
						int x1 = player.getPosition()[0];
						int y1 = player.getPosition()[1];
						int x2 = otherPlayers.getPosition()[0];
						int y2 = otherPlayers.getPosition()[1];
						if (Math.sqrt(Math.pow(x1 - x2, 2)
								+ Math.pow(y1 - y2, 2)) < setting.soOtherSize) {
							status.playerDropOffMap.get(player.getId()).add(
									System.currentTimeMillis());
							otherPlayers.setCarrying(player.getCarrying());
							player.setCarrying(0);
							break;
						}
					}
				}
			} else {
				for (Source source : setting.sourceList) {
					int[] sourcePosition = source.getPosition();
					if (position[0] > sourcePosition[0]
							&& position[0] < sourcePosition[0]
									+ source.getSize()
							&& position[1] > sourcePosition[1]
							&& position[1] < sourcePosition[1]
									+ source.getSize()) {
						player.setCarrying(source.produceRandomObject());
					}
				}
			}
		} else {
			if (player.getCarrying() != 0) {
				for (Sink sink : setting.sinkList) {
					int[] sinkPosition = sink.getPosition();
					if (position[0] > sinkPosition[0]
							&& position[0] < sinkPosition[0] + sink.getSize()
							&& position[1] > sinkPosition[1]
							&& position[1] < sinkPosition[1] + sink.getSize()) {
						if (player.getCarrying() == sink.getAcceptingObject()) {
							status.playerDropOffMap.get(player.getId()).add(
									System.currentTimeMillis());
							player.setCarrying(0);
						}
					}
				}
			} else {
				for (Player otherPlayers : status.players) {
					if (!player.equals(otherPlayers)
							&& otherPlayers.isSourceAttender()
							&& otherPlayers.getCarrying() != 0) {
						int x1 = player.getPosition()[0];
						int y1 = player.getPosition()[1];
						int x2 = otherPlayers.getPosition()[0];
						int y2 = otherPlayers.getPosition()[1];
						if (Math.sqrt(Math.pow(x1 - x2, 2)
								+ Math.pow(y1 - y2, 2)) < setting.siOtherSize) {
							status.playerDropOffMap.get(otherPlayers.getId())
									.add(System.currentTimeMillis());
							player.setCarrying(otherPlayers.getCarrying());
							otherPlayers.setCarrying(0);
							break;
						}
					}
				}
			}
		}
		// checking bases
		if (player.getCarrying() == 0) {
			boolean inside = false;
			for (Base base : setting.baseList) {
				int[] basePosition = base.getPosition();
				if (position[0] > basePosition[0]
						&& position[0] < basePosition[0] + base.getSize()
						&& position[1] > basePosition[1]
						&& position[1] < basePosition[1] + base.getSize()) {
					inside = true;
					if (!player.isRecentlyChanged()) {
						player.setSourceAttender(!player.isSourceAttender());
						player.setRecentlyChanged(true);
					}
				}
			}
			if (!inside) {
				player.setRecentlyChanged(false);
			}
		}
	}

	public static void main(String args[]) {
		try {
			server = new Server();
			server.startThreads();
		} catch (Exception e) {
			JDialog d = new JDialog(server);
			d.getContentPane()
					.add(new JTextArea("Error: \n" + e.getMessage() + "\n" + e.toString()));
			d.setSize(300, 300);
			d.setVisible(true);
			e.printStackTrace();
		}
	}
}

class Receiver implements Runnable {

	Server server;
	DatagramSocket receiverSocket;

	public Receiver(Server server) {
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

class Sender implements Runnable {

	Server server;
	DatagramSocket sendSocket;

	public Sender(Server server) {
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

	public void send(byte[] b) {
		// System.out.println("Sending: " + s);
		DatagramPacket sendPacket = new DatagramPacket(b, b.length,
				server.group, Util.MULTI_PORT);
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
				ByteArrayOutputStream baos = new ByteArrayOutputStream();
				ObjectOutputStream oos = new ObjectOutputStream(baos);
				if (server.started) {
					// String positionData = server.status.toString();
					server.status.update();
					oos.writeObject(server.status);
					send(baos.toByteArray());
					// send(positionData);
					Thread.sleep(10);
				} else if (server.startButtonPushed) {
					oos.writeObject(server.setting);
					send(baos.toByteArray());
					// send(server.setting.toString());
					Thread.sleep(1000);
				} else {
					Thread.sleep(1000);
				}
				baos.flush();
				baos.close();
				oos.close();
			} catch (InterruptedException e) {
				server.clientTextArea.append(e.toString());
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
}