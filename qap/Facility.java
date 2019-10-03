package qap;

public class Facility implements Comparable<Facility>{
	private int ID;
	private double avrFlow;
	
	public Facility(int ID, double avrFlow) {
		this.ID = ID;
		this.avrFlow = avrFlow;
	}
	
	public int getID() { return ID;}
	public double getAveFlow() { return avrFlow; }

	@Override
	public int compareTo(Facility o) {
		if (this.avrFlow < o.avrFlow) {
			return +1;
		} else if (this.avrFlow == o.avrFlow) {
			return 0;
		} else {
			return -1;
		}
	}
	
	@Override
	public String toString() {
		return ID + "-" + avrFlow;
	}
}
