package qap;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class GreedyMethods {
	/**
	 * Misevicius, A. (2003). A modified simulated annealing algorithm for the quadratic assignment problem. Informatica, 14(4), 497-514.
	 * 
	 * @param solution
	 * @return
	 */
	public static Solution bestImproveLocalSearch(final Solution solution) {
		Solution s = new Solution(solution);
		final int neighborSize = s.getNeighborSize();
		boolean improved = true;
		while (improved) {
			improved = false;
			Neighbor bestNeighbor = null;
			for (int k = 0; k < neighborSize; k++) {
				Neighbor neighbor = s.nextNeighbor(k);
				if (bestNeighbor==null || neighbor.getDelta() < bestNeighbor.getDelta()) {
					bestNeighbor = neighbor;
				}
			}
			if (bestNeighbor.getDelta() < 0) {
				s.swap(bestNeighbor);
				improved = true;
			}
		}
		return s;
	}
	
	/**
	 * Gambardella, L. M., Taillard, E. D., & Dorigo, M. (1999). 
	 * Ant colonies for the quadratic assignment problem. Journal of the operational research society, 50(2), 167-176..
	 * 
	 * @param solution
	 * @param TIMES
	 * @return
	 */
	public static Solution firstImproveLocalSearch(Solution solution, final int TIMES) {
		Solution s = new Solution(solution);
		List<Integer> listLoc1 = new ArrayList<Integer>();
		List<Integer> listLoc2 = new ArrayList<Integer>();
		int cityNumber = s.getCityNumber();
		for (int i = 0; i < cityNumber; i++) {
			listLoc1.add(i);
			listLoc2.add(i);
		}
		for (int t = 0; t < TIMES; t++) {
			Collections.shuffle(listLoc1);
			for (int i = 0; i < cityNumber; i++) {
				int x = listLoc1.get(i);
				Collections.shuffle(listLoc2);
				for (int j = 0; j < cityNumber; j++) {
					int y = listLoc2.get(j);
					Neighbor neighbor = s.nextNeighbor(x, y);
					if ( neighbor.getDelta() < 0) {
						s.swap(neighbor);
					}
				}
			}
		}
		return s;
	}
}
