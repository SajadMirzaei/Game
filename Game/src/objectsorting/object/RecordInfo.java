package objectsorting.object;

import objectsorting.object.GameStatus;

public class RecordInfo {
	
	public String stime;
	public String playerId;
	public String X;
	public String Y;
	public String carrying;
	public String dropoff;
	
	public RecordInfo(String stime, String playerId, String x, String y, String carrying, String dropoff){
		this.stime=stime;
		this.playerId=playerId;
		this.X=x;
		this.Y=y;
		this.carrying=carrying;
		this.dropoff=dropoff;
	}
}
