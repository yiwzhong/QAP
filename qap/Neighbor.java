package qap;

/**
 * 
 * @author yiwen zhong
 *
 */
public class Neighbor {
	private int x;
	private int y;
	private int delta;
	
	public Neighbor(int x, int y, int delta) {
		this.x = x;
		this.y = y;
		this.delta = delta;
	}
	
	public int getX() { return x; }
	public int getY() { return y; }
	public int getDelta() { return delta; }
}
