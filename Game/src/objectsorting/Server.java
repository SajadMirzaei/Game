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
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextArea;

import objects.Util;
import objectsorting.object.Base;
import objectsorting.object.GameStatus;
import objectsorting.object.Player;
import objectsorting.object.Setting;
import objectsorting.object.Sink;
import objectsorting.object.Source;

import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.input.SAXBuilder;

import shuffling.ObjectSortingGame;
import shuffling.WaveManager;

public class Server extends JFrame {

	public InetAddress group = InetAddress.getByName(Util.GROUP_ADDRESS);
	JTextArea clientTextArea = new JTextArea(10, 20);
	JButton button;
	DatagramSocket sendSocket;
	Thread senderThread;

	Sender sender;
	public static boolean started;
	public static boolean startButtonPushed;
	private Set<String> readyClients = new HashSet<String>();
	private Set<String> clients = new HashSet<String>();

	public static Setting setting;
	public static GameStatus status;
	

	private String sconfig;
	private int playInitialPos[][];
	
	private static Server server;
	public static WaveManager wavemngr;
	public static boolean bGamePaused;
        public boolean bfirstGame;
        
	public Server() throws Exception {
		super("Server");
		
		initializeFrame();
		started = false;
		startButtonPushed=false;
                bGamePaused=false;
                bfirstGame=true;
                
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
                                        bfirstGame=false;
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


	public static void main(String args[]) {
		try {
			server = new Server();			
			wavemngr = new WaveManager(server);
			String sconfig = "../setting.xml";
                        //String sconfig = "G:\\Others\\DepOfPhy\\Game-master\\Game\\bin\\test_wave1.xml";
			wavemngr.loadConfiguration(sconfig);
			wavemngr.setFirstGame();
			
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
        
    public void setSettingStatus(Setting setting, GameStatus status){
		this.setting=setting;
		this.status=status;
		this.status.setSetting(setting);
                
        int m=status.players.size();
        int n1=setting.screenSize[0];
        int n2=setting.screenSize[1];
                
	}
	
    public void sendPauseCmd()
    {
        try{
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            ObjectOutputStream oos = new ObjectOutputStream(baos);
            String content="PAUSE";
            oos.writeObject(content);
            sender.send(baos.toByteArray());
        } catch (IOException e) {
                e.printStackTrace();
        }
    }
        
	public void stopCurrentGame() {
		//send out "PAUSE" command to all players
                
		synchronized (this) {
                        bGamePaused=true;
			started = false;
			startButtonPushed = false;
			clients.clear();
			this.readyClients.clear();
		}
		clientTextArea.append("Current game is finished or time out! \n");
		                

		//this.button.setEnabled(true);
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
	

	public void startNewGame(ObjectSortingGame curGame) {
		try {
			Thread.sleep(15000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		
		setSettingStatus(curGame.getSettings(),curGame.getStatus());
		restartGame();
		
		clientTextArea.append("New game started! \n");
		
		synchronized (this) {
                        bGamePaused=false;
		}
	}
	
	
	public void startThreads() {
		sender = new Sender(this);
		Thread receiver = new Thread(new Receiver(this), "Sender");
		receiver.start();
		senderThread = new Thread(sender, "Receiver");
		senderThread.start();
	}
        
	public void restartGame() {
		status.gameRunning = true;
		status.rate = 0;
		for (Player player : status.players) {
			status.playerDropOffMap.get(player.getId()).clear();
			player.setCarrying(0);
			player.setDropOffs(0);
		}
	}
	
	public void setGameFinished(){
		wavemngr.setGameFinished(true);
	}
        
    private void delayAWhile(int msec)
    {
        try {
                    Thread.sleep(msec);
            } catch (InterruptedException e) {
                    e.printStackTrace();
            }
    }
        
    public void updateServer(String sentence, InetAddress inetAddress) {
       if (!sentence.contains(",")) {
			if (!sentence.contains("RECEIVED")) {
				if (!clients.contains(sentence)) {
					status.assignPlayer(sentence);
					clients.add(sentence);
					clientTextArea.append("Player connected from IP Address"
							+ inetAddress + "\n");
                                                                               
                                        if(clients.size()== setting.playerList.size() && bfirstGame==false)
                                        {                                            
                                            startButtonPushed = true;
                                        }
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
						status.gameRunning = true;

					}
				}
            }
		} else {
            if(startButtonPushed==false)
            {
                this.sendPauseCmd();
            }
                        
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
			if(server.bGamePaused==false)
            {
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
            else{
                try{
                    Thread.sleep(1000);
                }catch (InterruptedException e) {				
                	e.printStackTrace();
                }
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

	public void send(byte[] b) {//
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
//					Thread.sleep(10);
					if (!server.status.gameRunning) {
						//Thread.sleep(5000);
						//server.restartGame();//
						server.setGameFinished();
					}
				} else if (server.startButtonPushed) {
					Setting asetting=server.setting;
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