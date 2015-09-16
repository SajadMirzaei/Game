package objectsorting.object;

public class Base {
	private String id = "";
	private int[] position;
	private int size;
	
	public Base(String id, int[] position) {
		this.id = id;
		this.position = position;
	}
	
	public Base() {
		position = new int[] {0,0};
		size = 50;
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
	public void setSize(int size) {
		this.size = size;
	}
	public int getSize() {
		return size;
	}
}
