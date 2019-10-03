package qap;

import java.io.File;
import java.util.Random;

public class TabuMethods {
	/**
	 * Misevicius, A. (2003). A modified simulated annealing algorithm for the quadratic assignment problem. Informatica, 14(4), 497-514.
	 * 
	 * @param solution
	 * @param M
	 * @return
	 */
	public static Solution simplifiedTabuSearch(final Solution solution, final int M) {
		Solution current = new Solution(solution);
		Solution best = new Solution(solution);
		int neighborSize = current.getNeighborSize();
		int[] delta = new int[neighborSize];
		boolean[] tabu = new boolean[neighborSize];
		for (int k = 0; k < neighborSize; k++) {
			delta[k] = current.different(k);
			tabu[k] = false;
		}

		for (int m = 0; m < M;  m++) {
			int minDelta = Integer.MAX_VALUE;
			int mink = -1;
			for (int k = 0; k < neighborSize; k++) {
				int d = delta[k];
				if (d < minDelta && (!tabu[k]) || (d+current.getMakespan() < best.getMakespan())) {
					minDelta = d;
					mink = k;
				}
			}
			//swap
			current.swap(mink, minDelta);
			if (current.getMakespan() < best.getMakespan()) {
				best = new Solution(current);
			}
			
			tabu[mink] = true;
			for (int k = 0; k < neighborSize; k++) {
				delta[k] = current.different(k);
			}
		}
		return best;
	}
	
	/**
	 * Taillard, E. (1991). Robust taboo search for the quadratic assignment problem. Parallel computing, 17(4-5), 443-455.
	 * 
	 * @param solution
	 * @param M
	 * @return
	 */
	public static Solution robustTabuSearch(final Solution solution, final int M) {
		Solution current = new Solution(solution);
		Solution best = new Solution(solution);
		int cityNumber = current.getCityNumber();
		int minSize = 9 * cityNumber / 10;
		int maxSize = 11 * cityNumber / 10;
		int aspiration = cityNumber*cityNumber*2;
		
		int[][] delta = new int[cityNumber][cityNumber];
		int[][] tabu = new int[cityNumber][cityNumber];

		for (int i = 0; i < cityNumber; i++) {
			for (int j = 0; j < cityNumber; j++) {
				if ( i < j ) {
					delta[i][j] = current.different(i, j);
				}
				tabu[i][j] = -(cityNumber*i + j);
			}
		}

		for (int m = 0; m < M;  m++) {
			boolean autorized = false;
			boolean aspired = false;
			boolean alreadyAspired = false;
			int iRetained = Integer.MAX_VALUE; //in case all moves are tabu
			int jRetained = Integer.MAX_VALUE;
			int minDelta = Integer.MAX_VALUE;

			for (int i = 0; i < cityNumber; i++) {
				int fi = current.getFacility(i);
				for (int j = i + 1; j < cityNumber; j++) {
					int fj = current.getFacility(j);
					autorized = tabu[fj][i] < m || tabu[fi][j] < m;
					aspired = tabu[fj][i] < m - aspiration ||
							tabu[fi][j] < m - aspiration ||
							current.getMakespan() + delta[i][j] < best.getMakespan();
					
					if ( (aspired && !alreadyAspired) || //first move aspired
							(aspired && alreadyAspired && delta[i][j] < minDelta) ||
							(!aspired && !alreadyAspired && delta[i][j] < minDelta && autorized)) {
						iRetained = i;
						jRetained = j;
						minDelta = delta[i][j];
						if (aspired) alreadyAspired = true;
					}
				}
			}
			
			if (iRetained == Integer.MAX_VALUE) {
				System.out.println("All moves are tabu!");
			} else {
				//swap
				current.swap(iRetained, jRetained, minDelta);
				if (current.getMakespan() < best.getMakespan()) {
					best = new Solution(current);
				}
				tabu[current.getFacility(jRetained)][iRetained] = m + minSize + TabuMethods.rand.nextInt(maxSize-minSize);
				tabu[current.getFacility(iRetained)][jRetained] = m + minSize + TabuMethods.rand.nextInt(maxSize-minSize);
			}

			//update delta matrix
			for (int i = 0; i < cityNumber; i++) {
				for (int j = i + 1; j < cityNumber; j++) {
					if ( i != iRetained && i != jRetained && j != iRetained && j != jRetained) {
						int d = current.different(delta, i, j, iRetained, jRetained);
						delta[i][j] = d;
					} else {
						delta[i][j] = current.different(i, j);
					}
			
				}
			}
		}
		return best;
	}
	
	private static Random rand = new Random();
	
	public static void main(String[] args) {
		String filePath = (new File("")).getAbsolutePath() + "/../p32/"; 
		String fileName = filePath + "20tai25a.txt";//"27tai100a.txt";//"18ste36b.txt";//"20tai25a.txt";
		Problems.setFileName(fileName);
		Solution s; // = new Solution(true);
		final int TIMES = 25;
		final int MAX_ITERATION = 1000000;
		double ave = 0;
		for (int i = 0; i < TIMES; i++) {
			s = new Solution(true);
			s = TabuMethods.robustTabuSearch(s, MAX_ITERATION);
			System.out.println(i + "--" + s.getMakespan());
			ave += s.getMakespan();
		}
		ave /= TIMES;
		int bMakespan = Problems.getProblem().getBestMakespan();
		double apd = Math.round((ave-bMakespan) * (1.0/bMakespan) *100*1000)/1000.0;

		System.out.println("Best Known:" + bMakespan + ", Average solution:" + ave + ", PE:" + apd);
	}
}
