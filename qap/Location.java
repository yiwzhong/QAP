package qap;

public class Location implements Comparable<Location>{
	private int ID;
	private double avrDist;
	
	public Location(int ID, double avrDist) {
		this.ID = ID;
		this.avrDist = avrDist;
	}
	
	
	
	public int getID() { return ID;}
	public double getAveDist() { return avrDist; }



	@Override
	public int compareTo(Location loc) {
		if (this.avrDist < loc.avrDist) {
			return -1;
		} else if (this.avrDist == loc.avrDist)  {
			return 0;
		} else {
			return +1;
		}
	}
	
	@Override
	public String toString() {
		return ID + "-" + avrDist;
	}
}
