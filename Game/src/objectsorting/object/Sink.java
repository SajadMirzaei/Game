package objectsorting.object;

public class Sink {
	private String id;
	private int [] position;
	private boolean acceptingFirstTypeObject;
	public Sink(String string, int[] is, boolean b) {
		this.id = string;
		this.position = is;
		acceptingFirstTypeObject = b;
	}
	
	public Sink() {
		this.id = "";
		this.position = new int[] {0,0};
		acceptingFirstTypeObject = true;
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
	public boolean isAcceptingFirstTypeObject() {
		return acceptingFirstTypeObject;
	}
	public void setAcceptingFirstTypeObject(boolean acceptingFirstTypeObject) {
		this.acceptingFirstTypeObject = acceptingFirstTypeObject;
	}
	
	
}
