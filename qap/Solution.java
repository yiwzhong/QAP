package qap;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.PriorityQueue;
import java.util.Random;
import java.util.Vector;

/**
 * @author yiwen zhong
 *
 */
public class Solution implements Comparable<Solution> {

	public Solution(boolean initial) {
		this.cityDistance = Problems.getProblem().getCityDistance();
		this.flowData = Problems.getProblem().getFlowData();
		this.pairs = Problems.getProblem().getPairs();
		cityNumber= cityDistance.length;
		neighborSize = pairs.length;
		match = new int[cityNumber]; 
		if (Solution.rand == null ) { Solution.rand = new Random(); }
		if (Solution.cityList == null || Solution.cityList.size() != cityNumber) {
			Solution.cityList = new ArrayList<Integer>();
			for (int i = 0; i <cityNumber; i++) {
				Solution.cityList.add(i);
			}
		}
		if ( initial ) { 
			this.randScheduling(); 
			//this.greedyScheduling();
		}
		neighborIndex = -1;
	}

	/**
	 * Use parameter to clone a new Solution
	 * 
	 * @param solution
	 */
	public Solution(Solution solution) {
		this.cityDistance = Problems.getProblem().getCityDistance();
		this.flowData = Problems.getProblem().getFlowData();
		pairs = solution.pairs;
		cityNumber= cityDistance.length;
		neighborSize = solution.neighborSize;
		match = solution.match.clone(); 
		makespan = solution.makespan;
		lastImproving = solution.lastImproving;
		neighborIndex = -1;
	}
	
	/**
	 * Use parameter to update this object
	 * 
	 * @param s
	 */
	public void update(Solution s) {
		for (int i = 0; i <cityNumber; i++) {
			match[i] = s.match[i];
		}
		makespan = s.makespan;
	}

	/*
	 * This method randomly selects a facility for each location
	 */
	private void randScheduling() {
		Vector<Integer> rdyTsk = new Vector<Integer>();
		//to generate a rand position
		for (int i=0; i<cityNumber; i++) {
			rdyTsk.add(new Integer(i));
		}
		int pos=0;
		Solution.rand.setSeed(Solution.seed++);
		//position=new int[cityNumber];
		while (!rdyTsk.isEmpty()) {
			int idx;
			int city;
			idx = Solution.rand.nextInt(rdyTsk.size());
			city = ((Integer)rdyTsk.elementAt(idx)).intValue();
			rdyTsk.remove(idx);
			match[pos++] = city;
		}
		makespan = calcuMakespan(match);
		//System.out.println(makespan);
	}
	
	private void greedyScheduling() {
		PriorityQueue<Location> locQueue = new PriorityQueue<>();
		Location[] locations = Problems.getProblem().getLocations();
		//average distance form small to big
		for (Location loc : locations) {
			locQueue.add(loc);
		}

		Facility[] facilities = Problems.getProblem().getFacilities();
		List<Facility> faciList = new ArrayList<>();
		for (Facility faci : facilities) {
			faciList.add(faci);
		}
		Collections.sort(faciList); //average flow from big to small
		//int[] m = new int[cityNumber];
		while (!locQueue.isEmpty()) {
			int idx = Solution.rand.nextInt(faciList.size()) % 2;
			match[locQueue.remove().getID()] = faciList.remove(idx).getID();
		}
		makespan = calcuMakespan(match);
		//System.out.println(makespan);
	}

	
	public int caluMakespan() {
		makespan = calcuMakespan(match);
		return makespan;
	}

	/**
	 * Calculate the fitness of an assignment match
	 * 
	 * @param match
	 * @return
	 */
	public int calcuMakespan(int[] match) {
		int makespan = 0;
		for (int i=0; i<match.length; i++) {
			int task1;
			task1 = match[i];
			for (int j=0; j<match.length;j++) {
				int task2;
				if (j!=i) {
					task2 = match[j];
					makespan += cityDistance[i][j]*flowData[task1][task2];
				}
			}
		}
		return makespan;
	}

	/**
	 * Produce a random neighbor
	 * 
	 * @return
	 */
	public Neighbor randNeighbor() {
		int nIdx = Solution.rand.nextInt(pairs.length);
		int x = pairs[nIdx][0];
		int y = pairs[nIdx][1];
		int delta = different(x, y);
		return new Neighbor(x, y, delta);
	}
	
	/**
	 * To produce a neighbor where the swapped facilities are specified by idx
	 * 
	 * @param idx
	 * @return
	 */
	public Neighbor nextNeighbor(int idx) {
		int x = pairs[idx][0];
		int y = pairs[idx][1];
		int delta = different(x, y);
		return new Neighbor(x, y, delta);
	}
	
	/**
	 * To produce a neighbor where the swapped facilities are specified by x and y
	 * 
	 * @param idx
	 * @return
	 */
	public Neighbor nextNeighbor(int x, int y) {
		int delta = different(x, y);
		return new Neighbor(x, y, delta);
	}
	
	/**
	 * To produce a neighbor in sequence specified by attribute neighborIndex 
	 * 
	 * @return
	 */
	public Neighbor nextNeighbor() {
		neighborIndex = (neighborIndex+1) % pairs.length;
		int x = pairs[neighborIndex][0];
		int y = pairs[neighborIndex][1];
		int delta = different(x, y);
		return new Neighbor(x, y, delta);
	}
	
	public void swap(Neighbor neighbor) {
		this.swap(neighbor.getX(), neighbor.getY(), neighbor.getDelta());
	}
	
	public void swap(int idx, int delta) {
		int x = pairs[idx][0];
		int y = pairs[idx][1];
		this.swap(x, y, delta);
	}

	public void swap(int i, int j, int delta) {
		int temp = match[i];
		match[i] = match[j];
		match[j] = temp;
		makespan = makespan +delta;
	}

	protected int different(int k) {
		int i = pairs[k][0];
		int j = pairs[k][1];
		return different(match, i, j);
	}
	
	protected int different(int i, int j) {
		return different(match, i, j);
	}

	/**
	* Compute the cost difference if elements i and j are swapped.
	*/
	protected int different(int[] match, int i, int j) {
		int k;
		int diff = 0;
		if ( i == j )
			return 0;
		diff = (cityDistance[i][i]-cityDistance[j][j]) * (flowData[match[j]][match[j]]-flowData[match[i]][match[i]]);
		diff += (cityDistance[i][j]-cityDistance[j][i]) * (flowData[match[j]][match[i]]-flowData[match[i]][match[j]]);
		for (k=0; k<match.length;k++) {
			if (k!=i && k!=j) {
				diff += (cityDistance[j][k]-cityDistance[i][k]) * (flowData[match[i]][match[k]]-flowData[match[j]][match[k]]);
				diff += (cityDistance[k][j]-cityDistance[k][i]) * (flowData[match[k]][match[i]]-flowData[match[k]][match[j]]);
				//diff += (cityDistance[j][k]-cityDistance[i][k]) * (flowData[match[i]][match[k]]-flowData[match[j]][match[k]]);
			}
		}
		return diff;
	}
	
	/**
	* Compute the cost difference if elements i and j are swapped.
	* The value of delta[i][j] is supposed to be known before the swap of elements r and s
	*/
	public int different(int[][] delta, int i, int j, int r, int s) {
	    int d = delta[i][j];
	    d += (cityDistance[r][i] - cityDistance[r][j] + cityDistance[s][j] - cityDistance[s][i]) *
	    		(flowData[match[s]][match[i]] - flowData[match[s]][match[j]] + 
	    		flowData[match[r]][match[j]] - flowData[match[r]][match[i]]);
	    
	    d += (cityDistance[i][r] - cityDistance[j][r] + cityDistance[j][s] - cityDistance[i][s]) *
	    		(flowData[match[i]][match[s]] - flowData[match[j]][match[s]] + 
	    		flowData[match[j]][match[r]] - flowData[match[i]][match[r]]);
	    
	    return d;
	}
	
	public Solution mutation(int blockSize) {
		List<Integer> list = new ArrayList<Integer>();
		Collections.shuffle(Solution.cityList);
		for (int i = 0; i <blockSize; i++) {
			list.add(match[Solution.cityList.get(i)]);
		}
		for (int i = 0; i <blockSize; i++) {
			int j = Solution.rand.nextInt(list.size());
			match[Solution.cityList.get(i)] = list.remove(j);
		}
		 this.makespan = this.calcuMakespan(match);
		 return this;
	}
	

	/* (non-Javadoc)
	 * @see java.lang.Comparable#compareTo(java.lang.Object)
	 */
	@Override
	public int compareTo(Solution s) {
		if ( makespan < s.makespan) {
			return 1;
		} else if ( makespan == s.makespan) {
			return 0;
		} else {
			return -1;
		}
	}

	public boolean equals(Solution other) {
		for (int loc = 0; loc < match.length; loc++) {
			if (match[loc] != other.match[loc]) {
				return false;
			}
		}
		return true;
	}
	
	public String toString() {
		String str = "";

		return str;
	}

	public void setFacility(int pos, int facility) { match[pos] = facility; }
	public int getFacility(int pos) { return match[pos]; }
	public int getMakespan() {return makespan;}
	public int getCityNumber() {return cityNumber;}
	public int getNeighborSize() { return neighborSize; }
	public void setLastImproving(int n) { this.lastImproving = n; }
	public int getLastImproving() { return lastImproving;}

	private int[] match;
	private int makespan;
	private int lastImproving = 0; //

	protected int[][] cityDistance;
	protected int[][] flowData;
	protected int[][] pairs;
	protected int cityNumber;
	protected int neighborSize;
	protected int neighborIndex = -1;
	
	public static Random rand;
	public static List<Integer> cityList = null;
	
	public static void main(String[] args) {
		String filePath = (new File("")).getAbsolutePath() + "/../p32/"; 
		String fileName = filePath+"20tai25a.txt";
		Problems.setFileName(fileName);
		Solution s = new Solution(true);
		double d = 0;
		for (int i = 0; i < 100; i++) {
			s.randScheduling();
			System.out.println(s.makespan);
			d += s.makespan;
		}
		System.out.println("Random solution:" + d / 100);
		d = 0;
		for (int i = 0; i < 100; i++) {
			s.greedyScheduling();
			d += s.makespan;
		}
		System.out.println("Greedy Random solution:" + d / 100);
	}

	public static void setSeed(long seed) { Solution.seed = seed; }
	private static long seed = 0;
}
