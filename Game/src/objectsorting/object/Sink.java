package objectsorting.object;

import java.awt.Color;
import java.io.Serializable;

public class Sink implements Serializable{
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private String id;
	private int [] position;
	private int acceptingObject;
	private int size;
	private Color color;
	public int getSize() {
		return size;
	}

	public void setSize(int size) {
		this.size = size;
	}

	public Color getColor() {
		return color;
	}

	public void setColor(Color color) {
		this.color = color;
	}

	public Sink(String string, int[] is) {
		this.id = string;
		this.position = is;
	}
	
	public Sink() {
		this.id = "";
		this.position = new int[] {0,0};
		acceptingObject = 1;
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

	public int getAcceptingObject() {
		return acceptingObject;
	}

	public void setAcceptingObject(int acceptingObject) {
		this.acceptingObject = acceptingObject;
	}
}
