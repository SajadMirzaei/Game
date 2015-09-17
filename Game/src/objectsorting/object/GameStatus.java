package objectsorting.object;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import objects.Util;

public class GameStatus implements Serializable{
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	public transient long startTimeMillis; 
	public List<Player> players = new ArrayList<>();
	public transient Map<String, List<Long>> playerDropOffMap = new HashMap<String, List<Long>>();
	public double rate;
	
	public transient Setting setting;

	public GameStatus(String s) {
		String[] objects = s.split(Util.MAJOR_SEPERATOR);
		for (String string : objects) {
			if (!string.contains("status")) {
				if (string.contains(Util.OBJ_PLAYER)) {
					Player p = new Player(string);
					players.add(p);
				}else{
					rate = Double.valueOf(string);
				}
			}
		}
	}

	public GameStatus() {
	}
	
	public int getNumberOfAssignedPlayers(){
		int number = 0;
		for (Player player : players) {
			if (player.isAssigned()) {
				number++;
			}
		}
		return number;
	}
	
	@Override
	public String toString() {
		String result = "status";
		for (Player p : players) {
			p.setDropOffs(getIndividualRate(p.getId()));
			result += Util.MAJOR_SEPERATOR + p.toString();
		}
		result += Util.MAJOR_SEPERATOR + getOverallRate();
		return result;
	}
	
	private double getOverallRate() {
		double sum = 0;
		for (Player player : players) {
			sum+= getIndividualRate(player.getId());
		}
		return sum/players.size();
	}

	public int getIndividualRate(String playerID){
		List<Long> dropOffList = playerDropOffMap.get(playerID);
		int counter = 0;
		if (dropOffList != null) {
			for (Long long1 : dropOffList) {
				if (System.currentTimeMillis() - long1 < setting.timeWindow*1000) {
					counter++;
				}
			}
		}
		return counter;
	}
	
	public void assignPlayer(String id){
		for (Player player : players) {
			if (player.getId().equals(id)) {
				return;
			}
		}
		for (Player player : players) {
			if (!player.isAssigned()) {
				playerDropOffMap.put(id, playerDropOffMap.get(player.getId()));
				playerDropOffMap.remove(player.getId());
				player.setId(id);
				player.setAssigned(true);
				
				break;
			}
		}
	}
	
	public void update() {
		for (Player p : players) {
			p.setDropOffs(getIndividualRate(p.getId()));
		}
		rate = getOverallRate();
	}
	
	public void setSetting(Setting setting) {
		this.setting = setting;
	}
}
