package General;

public class PairInt {
	private int x;
	private int y;
	
	public PairInt() {}
	
	public PairInt(int first, int second) {
		this.x = first;
		this.y = second;
	}
	
	public int first() {
		return x;
	}
	
	public int second() {
		return y;
	}
	
	public void first(int first) {
		this.x = first;
	}
	
	public void second(int second) {
		this.y = second;
	}
}
