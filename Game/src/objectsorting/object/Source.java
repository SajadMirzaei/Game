package objectsorting.object;

import java.awt.Color;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class Source implements Serializable{
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private String id = "";
	private int[] position;
	private Map<Integer,Double> proportionMap = new LinkedHashMap<>();
	private int size;
	private Color color;
	
	public Source(String id, int[] position) {
		this.id = id;
		this.position = position;
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

	public Map<Integer, Double> getProportionMap() {
		return proportionMap;
	}

	public void setProportionMap(Map<Integer, Double> proportionMap) {
		this.proportionMap = proportionMap;
	}
	
	public int produceRandomObject(){
		double randomNumber = Math.random();
		List<Double> proportions = new ArrayList<>();
		for (int i : proportionMap.keySet()) {
			proportions.add(proportionMap.get(i));
		}
		double sum = 0;
		for (int i = 0; i < proportions.size(); i++) {
			sum += proportionMap.get(i+1);
			if (randomNumber < sum) {
				return i+1;
			}
		}
		return 0;
	}
	
}
