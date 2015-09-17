package objectsorting.object;

import java.io.Serializable;
import java.net.InetAddress;

import objects.Util;

public class Player implements Serializable{
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private String id = "";
	private transient InetAddress ipAdress;
	private transient int port;
	private boolean sourceAttender = true;
	private int carrying = 0; // 1 for blue 2 for white
	private int[] position;
	private boolean assigned = false;
	private boolean recentlyChanged = false;
	private int dropOffs = 0;
	
	public transient static final String SEPERATOR = "_";
	
	public Player(String s) {
		String[] split = s.split(SEPERATOR);
		id = split[1];
		sourceAttender = Boolean.valueOf(split[2]);
		carrying = Integer.valueOf(split[3]);
		position = new int[] {Integer.valueOf(split[4]), Integer.valueOf(split[5])};
		dropOffs = Integer.valueOf(split[6]);
	}
	
	public Player() {
	}
	
	public InetAddress getIpAdress() {
		return ipAdress;
	}
	public void setIpAdress(InetAddress ipAdress) {
		this.ipAdress = ipAdress;
	}
	public void setPort(int port) {
		this.port = port;
	}
	public int getPort() {
		return port;
	}
	public String getId() {
		return id;
	}
	public void setId(String id) {
		this.id = id;
	}
	public boolean isSourceAttender() {
		return sourceAttender;
	}
	public void setSourceAttender(boolean sourceAttender) {
		this.sourceAttender = sourceAttender;
	}
	public int getCarrying() {
		return carrying;
	}
	public void setCarrying(int carrying) {
		this.carrying = carrying;
	}
	public int[] getPosition() {
		return position;
	}
	public void setPosition(int[] position) {
		this.position = position;
	}
	public boolean isAssigned() {
		return assigned;
	}
	public void setAssigned(boolean assigned) {
		this.assigned = assigned;
	}
	public void setRecentlyChanged(boolean recentlyChanged) {
		this.recentlyChanged = recentlyChanged;
	}
	public boolean isRecentlyChanged() {
		return recentlyChanged;
	}
	public void setDropOffs(int dropOffs) {
		this.dropOffs = dropOffs;
	}
	public int getDropOffs() {
		return dropOffs;
	}
	
	@Override
	public String toString() {
		return Util.OBJ_PLAYER + SEPERATOR + id + SEPERATOR + sourceAttender + SEPERATOR + carrying + SEPERATOR + position[0] + SEPERATOR + position[1] + SEPERATOR + dropOffs;
	}
}
