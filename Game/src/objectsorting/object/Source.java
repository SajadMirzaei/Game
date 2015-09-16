package objectsorting.object;

public class Source {
	private String id = "";
	private int[] position;
	private double firstTypeProductionRate;
	
	public Source(String id, int[] position, double rate) {
		this.id = id;
		this.position = position;
		this.firstTypeProductionRate = rate;
	}
	
	public Source() {
		position = new int[] {0,0};
	}
	
	public String getId() {
		return id;
	}
	public void setId(String id) {
		this.id = id;
	}
	public int[] getPosition() {
		return position;
	}
	public void setPosition(int[] position) {
		this.position = position;
	}
	public double getFirstTypeProductionRate() {
		return firstTypeProductionRate;
	}
	public void setFirstTypeProductionRate(double firstTypeProductionRate) {
		this.firstTypeProductionRate = firstTypeProductionRate;
	}
}
