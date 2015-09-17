package objectsorting.object;

import java.awt.Color;
import java.io.Serializable;

public class Source implements Serializable{
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private String id = "";
	private int[] position;
	private double firstTypeProductionRate;
	private int size;
	private Color color;
	
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
	public int getSize() {
		return size;
	}
	public void setSize(int size) {
		this.size = size;
	}
	public void setColor(Color color) {
		this.color = color;
	}
	public Color getColor() {
		return color;
	}
}
