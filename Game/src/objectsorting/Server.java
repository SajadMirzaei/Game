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
import java.net.UnknownHostException;
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
	
	ManageSender mngSender;
	public ArrayList<GroupSender> grpsenders=new ArrayList<GroupSender>();
	
	public static boolean started;
	public static boolean startButtonPushed;
	public static boolean allClientsReceivedGIp;
	private Set<String> readyClients = new HashSet<String>();
	private Set<String> clients = new HashSet<String>();
	private ArrayList<String> clients_info=new ArrayList<String>();
	public static ArrayList<String> clients_ip=new ArrayList<String>();
	public static ArrayList<String> temp_clients=new ArrayList<String>();
	private Set<String> clients_getGIp = new HashSet<String>();//record the clients already received group id 
	
	public static ArrayList<Integer> groupIndex=new ArrayList<Integer>(); // for groupIndex[i], save the group number of i
	public static ArrayList<String> newGroupIp=new ArrayList<String>();
	public static ArrayList<Boolean> allGroupsRunning=new ArrayList<Boolean>();
	
	public static ArrayList<Setting> settingList=new ArrayList<Setting>();
	public static ArrayList<GameStatus> statusList=new ArrayList<GameStatus>();
	public static int totalPlayerNum=0;
	public static boolean bGroupIpAllSet=false;
	public int totalAssigned=0;

	private static Server server;
	public static WaveManager wavemngr;
	public static boolean bGamePaused;
    public boolean bfirstGame;
    public static boolean bAllGameNotifiedToStop=false;
    public static boolean bNewClientIdReached=true;
        
	public Server() throws Exception {
		super("Server");
		
		initializeFrame();
		started = false;
		startButtonPushed=false;
		allClientsReceivedGIp=false;
        bGamePaused=false;
        bfirstGame=true;
        bGroupIpAllSet=false;
        settingList.clear();
        statusList.clear();
        totalPlayerNum=0;
        clients.clear();
        readyClients.clear();
        clients_getGIp.clear();
        clients_info.clear();
        clients_ip.clear();
        grpsenders.clear();
        groupIndex.clear();
        newGroupIp.clear();
        allGroupsRunning.clear();
        bAllGameNotifiedToStop=false;
        temp_clients.clear();
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


	public static void main(String args[]) {
		try {
			server = new Server();
			wavemngr = new WaveManager(server);
			String sconfig = "../setting.xml";
            //String sconfig = "G:\\setting.xml";
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
	
    private void delayAWhile(int msec)
    {
        try {
                    Thread.sleep(msec);
            } catch (InterruptedException e) {
                    e.printStackTrace();
            }
    }  
	
    public void sendPauseCmd()
    {
        try{
        	int nGroups=Server.settingList.size();
    		for(int i=0;i<nGroups;i++){
//System.out.println("Server send PAUSE to group: "+String.valueOf(i));
    			GroupSender gsender=this.grpsenders.get(i);
    			
	            ByteArrayOutputStream baos = new ByteArrayOutputStream();
	            ObjectOutputStream oos = new ObjectOutputStream(baos);
	            String content="PAUSE";
	            oos.writeObject(content);
	            gsender.send(baos.toByteArray());
	            
	            baos.flush();
				baos.close();
				oos.close();
	            
    		}
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
			
			for(int i=0;i<Server.statusList.size();i++){// all groups need to be paused
				Server.statusList.get(i).gameRunning=false;
				allGroupsRunning.set(i, false);
			}
		}
		clientTextArea.append("Current game is finished or time out! \n");
		
	}
	
	public void broadCastAllFinished() {
		synchronized (this) {
			started = false;
			startButtonPushed = false;
		}
		clientTextArea.append("All the waves has been finished! \n");
	}
	
    public void setSettingStatusList(ObjectSortingGame curGame){
    	Server.settingList.clear();
    	Server.statusList.clear();
    	int nGroups=curGame.groupSettingList.size();
    	
    	int start=20;
    	for(int i=0;i<nGroups;i++){
    		
    		String newip="224.0.0."+String.valueOf(start+i);   		
    		newGroupIp.add(newip);
    		allGroupsRunning.add(true);
    		
    		Setting curSetting=curGame.groupSettingList.get(i);
    		GameStatus curStatus=curGame.groupStatusList.get(i);
    		Server.settingList.add(curSetting);
    		curStatus.setSetting(curSetting);
    		Server.statusList.add(curStatus);
    		
    		int ngroupNumSize=curSetting.playerList.size();
    		totalPlayerNum+=ngroupNumSize;
    		
    		for(int j=0;j<ngroupNumSize;j++){
    			this.groupIndex.add(-1);
    		}	
    	}

//System.out.println("Total number of players is "+String.valueOf(totalPlayerNum));
    	
    	for(int i=0;i<nGroups;i++){
    		Setting curSetting=curGame.groupSettingList.get(i);
    		int ngroupNumSize=curSetting.playerList.size();
    		for(int j=0;j<ngroupNumSize;j++){
    			int curId=Integer.parseInt(curSetting.playerList.get(j).getId());
    			this.groupIndex.set(curId-1, i);
    		}
    	}   	
    }
	
    public void cleanOldGameStatus(){
    	synchronized (this) {
	    	allClientsReceivedGIp=false;
			clients.clear();
			clients_info.clear();
			clients_ip.clear();
			clients_getGIp.clear();
			this.readyClients.clear();
			groupIndex.clear();
			bGroupIpAllSet=false;
//System.out.println("clean group senders");
			for(int i=0;i<grpsenders.size();i++)
				grpsenders.get(i).closeSocket();
			grpsenders.clear();
			newGroupIp.clear();
			allGroupsRunning.clear();
			temp_clients.clear();
		}
    }
    
	public void startNewGame(ObjectSortingGame curGame) {
		
		cleanOldGameStatus();
		
		try {
			Thread.sleep(15000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		
		totalPlayerNum=0;
		totalAssigned=0;
		setSettingStatusList(curGame);
		restartGame();
		
		clientTextArea.append("New wave started! \n");
		synchronized (this) {
            bGamePaused=false;
		}

		
		mngSender = null;
		mngSender = new ManageSender(this);
		senderThread=null;
		senderThread = new Thread(mngSender, "ManageSender");
		senderThread.start();
		
	}
		
	public void startThreads() {
		Thread receiver = new Thread(new Receiver(this), "Receiver");
		receiver.start();
		
		mngSender = new ManageSender(this);
		senderThread = new Thread(mngSender, "ManageSender");
		senderThread.start();
	}
    
	
	public void startGroupSenderThreads(){
		int nGroups=Server.settingList.size();
		for(int i=0;i<nGroups;i++){
			String newip=Server.newGroupIp.get(i);
			GroupSender gsender=new GroupSender(this,newip,i);
			grpsenders.add(gsender);
			Thread gsenderThread=new Thread(gsender,"GroupSender");
			gsenderThread.start();
		}
		
//System.out.println("Sender size is:" + String.valueOf(grpsenders.size()));
	}
	
	public void restartGame() {	
		for(GameStatus status:Server.statusList){
			status.gameRunning = true;
			status.rate = 0;
			for (Player player : status.players) {
				status.playerDropOffMap.get(player.getId()).clear();
				player.setCarrying(0);
				player.setDropOffs(0);
			}		
		}
	}
	
	public void setGameFinished(){
		wavemngr.setGameFinished(true);
	}
    
	
    public void updateServer(String sentence, InetAddress inetAddress) {	
       if (sentence.contains(",")==false) {
			if (sentence.contains("RECEIVED")==false) {
				if(sentence.contains("GROUPIPGET")==false){
					if (clients.contains(sentence)==false) {
						//if(bNewClientIdReached==true)
						//{
						clients.add(sentence);
						clients_info.add(sentence);
						//}
						String ipaddress=inetAddress.getHostAddress();
						if(ipaddress.equals("localhost"))
							ipaddress="127.0.0.1";
						clients_ip.add(ipaddress);
						clientTextArea.append("Player connected from IP Address"
								+ inetAddress.getHostAddress() + "\n");
						
						if(clients.size()==Server.totalPlayerNum){
							for(int i=0;i<clients_info.size();i++){
								int igroup=groupIndex.get(i);
								GameStatus iStatus=Server.statusList.get(igroup);
								iStatus.assignPlayer(clients_info.get(i));
							}
							
							for(int i=0;i<Server.statusList.size();i++){
			                	GameStatus status=Server.statusList.get(i);
			                	totalAssigned += status.getNumberOfAssignedPlayers();
			                }
						}
						
						if(clients.size()== Server.totalPlayerNum && bfirstGame==false){
		                    startButtonPushed = true;
		                }
					}
				}
				else{
					if(!clients_getGIp.contains(sentence)){
						clients_getGIp.add(sentence);
						
						clientTextArea.append("Player with IP Address"
								+ inetAddress + " received new Group IP!\n");

//System.out.println(String.valueOf(clients_getGIp.size())+"_"+String.valueOf(Server.totalPlayerNum));
						if(clients_getGIp.size()==Server.totalPlayerNum){
							allClientsReceivedGIp=true;
						}
					}
				}
				
            }
			else {
				// ACKNOWLEDGMENT by client in sent
				String[] splited = sentence.split(Util.ID_SEPERATOR);                               
                readyClients.add(splited[0]);
                
				if (readyClients.size() == totalAssigned) {				
					synchronized (this) {
						// System.out.println("started = true");
						long starttime=System.currentTimeMillis();
						for(int i=0;i<Server.statusList.size();i++){
		                	GameStatus status=Server.statusList.get(i);
							status.startTimeMillis = starttime;
							
							started = true;
							status.gameRunning = true;
						}
					}
				}
            }
		} else {
//System.out.println("update outside");
//            if(startButtonPushed==false)
//            {
//System.out.println("update inside");
//                this.sendPauseCmd();
//            }
			bNewClientIdReached=false;
			// Update positions
			String[] data = sentence.split(Util.ID_SEPERATOR);
			int[] position = new int[] {
					Integer.valueOf(data[1].split(Util.POSITION_SEPERATOR)[0]),
					Integer.valueOf(data[1].split(Util.POSITION_SEPERATOR)[1]) };
			
			//check belong to which group, then get the status and setting
			String clientId=data[0];
			int groupId=-1;
			for(int i=0;i<clients_info.size();i++){
//System.out.println(clientId+"    "+clients_info.get(i));
				if(clients_info.get(i).equals(clientId) == true){
					groupId = groupIndex.get(i);
					break;
				}
			}
			
			if(groupId==-1) return;
			
			GameStatus status=this.statusList.get(groupId);
			Setting setting=this.settingList.get(groupId);
			
			for (Player player : status.players) {
				if (data[0].equals(player.getId())) {

					updateStatus(player, position, setting, status);
				}
			}
		}
	}

	private void updateStatus(Player player, int[] position, Setting setting, GameStatus status) {
		if (status.wrongObjectAlert) {
			status.wrongObjectAlertCounter ++;
			if (status.wrongObjectAlertCounter > GameStatus.COUNTER_MAX){
				status.wrongObjectAlertCounter = 0;
				status.wrongObjectAlert = false;
			}
		}
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
							// right sink
							status.playerDropOffMap.get(player.getId()).add(
									System.currentTimeMillis());
							player.setCarrying(0);
						}else{
							// wrong sink
							status.wrongObjectAlert = true;
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
			
			try {
				byte[] receiveData = new byte[1024];
				DatagramPacket receivePacket = new DatagramPacket(receiveData,
						receiveData.length);
				receiverSocket.receive(receivePacket);
				String sentence = new String(receivePacket.getData(), 0,
						receivePacket.getLength());
				// System.out.println("Received: " + sentence);
							
				if(server.bGamePaused==false)
	            {
					server.updateServer(sentence, receivePacket.getAddress());
	            }
	            else{
	            	if(Server.bAllGameNotifiedToStop==false){
//System.out.println(String.valueOf(Server.totalPlayerNum)+"  "+sentence);
						if (sentence.contains(",")==true)
								server.sendPauseCmd();
		            	if (sentence.contains(",")==false && sentence.contains("-RECEIVED")==false 
		            			&& sentence.contains("-GROUPIPGET")==false && Server.temp_clients.contains(sentence)==false) {	            		
		            		Server.temp_clients.add(sentence);
		            	
		            		if(Server.temp_clients.size()==Server.totalPlayerNum){
//System.out.println("All clients notified!!!");
		            			Server.bAllGameNotifiedToStop=true;
		            			Server.bNewClientIdReached=true;
		            		}
		            		
			                try{
			                    Thread.sleep(1000);
			                }catch (InterruptedException e) {				
			                	e.printStackTrace();
			                }
		            	}
	            	}
	            }
			} catch (SocketException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
}


class GroupSender implements Runnable {
	Server server;
	DatagramSocket sendSocket;
	InetAddress newgroup;
	int groupId;
	public GroupSender(Server server, String newip, int gId) {
		this.server = server;
		try {
			newgroup = InetAddress.getByName(newip);
			sendSocket = new DatagramSocket();
			groupId=gId;
		} catch (SocketException | UnknownHostException e) {
			e.printStackTrace();
		}
	}

	public void send(String s) {//how to send to specified groups
		// System.out.println("Sending: " + s);
		byte[] sendData = s.getBytes();
		DatagramPacket sendPacket = new DatagramPacket(sendData,
				sendData.length, newgroup, Util.MULTI_PORT); 
		try {
			sendSocket.send(sendPacket);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void send(byte[] b) {//object 
		// System.out.println("Sending: " + s);
		DatagramPacket sendPacket = new DatagramPacket(b, b.length,
				newgroup, Util.MULTI_PORT);
		try {
			sendSocket.send(sendPacket);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public void closeSocket(){
		sendSocket.close();
	}

	@Override
	public void run() {
		while (true) {
			try {
				
				ByteArrayOutputStream baos = new ByteArrayOutputStream();
				ObjectOutputStream oos = new ObjectOutputStream(baos);
				if (server.started) {
					
					GameStatus status=server.statusList.get(groupId);
					status.update();
					
					server.allGroupsRunning.set(groupId, status.gameRunning);
					if(status.gameRunning==true){
						oos.writeObject(status);
						send(baos.toByteArray());	
					}
					else{
						String notifySucceed="Succeed";
						oos.writeObject(notifySucceed);
						send(baos.toByteArray());
					}
				} 
				else if (server.startButtonPushed && server.allClientsReceivedGIp) {					
					Setting setting=server.settingList.get(groupId);						
					oos.writeObject(setting);
					send(baos.toByteArray());
					Thread.sleep(1000);
				} else {
					Thread.sleep(1000);
				}
				baos.flush();
				baos.close();
				oos.close();
				
				
				if(server.started==false && server.startButtonPushed==false && server.bGamePaused==true){
//System.out.println("Check whether game finished!!!");
					boolean btemp=false;
					for(int i=0;i<server.allGroupsRunning.size();i++){
						btemp= btemp || server.allGroupsRunning.get(i);
					}
										
					if (btemp==false) {
//System.out.println("Group Sender break loop!!!");
						server.setGameFinished();									
						break;
					}
				}
				
			} catch (InterruptedException e) {
				server.clientTextArea.append(e.toString());
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
}


class ManageSender implements Runnable {
	Server server;
	DatagramSocket sendSocket;

	public ManageSender(Server server) {
		this.server = server;
		try {
			sendSocket = new DatagramSocket();
		} catch (SocketException e) {
			e.printStackTrace();
		}
	}

	public void send(String s) {//how to send to specified groups to
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
				if (server.startButtonPushed) {
					int nplayers=server.clients_ip.size();
					for(int i=0;i<nplayers;i++){//for each player, we broadcast its
						int groupid=server.groupIndex.get(i);
						String newgip=server.newGroupIp.get(groupid);
						String content="GROUP_IP:"+server.clients_ip.get(i)+"_"+newgip;
//System.out.println(content);
						ByteArrayOutputStream baos = new ByteArrayOutputStream();
						ObjectOutputStream oos = new ObjectOutputStream(baos);
						oos.writeObject(content);
						send(baos.toByteArray());
						baos.flush();
						baos.close();
						oos.close();
						Thread.sleep(1000);
					}
					
					if(server.allClientsReceivedGIp==true){		
						sendSocket.close();
						server.startGroupSenderThreads();
						break;
					}
					
					Thread.sleep(100);
				} else {

					Thread.sleep(1000);
				}
				
			} catch (InterruptedException e) {
				server.clientTextArea.append(e.toString());
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
}