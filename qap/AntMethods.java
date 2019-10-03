package qap;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

public class AntMethods {

	/**
	 * Taillard E D, Gambardella L. Adaptive memories for the Quadratic Assignment Problems[J]. Journal of Operational Research, 1997.
	 * 
	 * @param solution
	 * @param MAX_G
	 * @return
	 */
	public static Solution fastAntSystem(final Solution solution, final int MAX_G) {
		Solution current = new Solution(solution);
		Solution best = new Solution(solution);
		int cityNumber = current.getCityNumber();
		double pherStep = 1.0;
		double pherBest = 4.0;
		double pherCurrent = pherStep;
		final double alpha = 1.0;
		final double beta = 0;
				
		double[][] pher = new double[cityNumber][cityNumber];
		double[][] heur = new double[cityNumber][cityNumber];
		pher = AntMethods.setupInitialPheromone(pher, cityNumber, pherStep);
		
		for (int g = 0; g < MAX_G; g++) {
			current = AntMethods.buildNewSolution(current, pher, alpha, heur, beta);
			
			//improve current with local search method
			if (Simulations.USE_LOCAL_SEARCH) current = GreedyMethods.firstImproveLocalSearch(current, 2);
			
			if ( current.getMakespan() < best.getMakespan() || current.equals(best) ) {
				if (current.getMakespan() < best.getMakespan()) {
					best = new Solution(current);
					pherCurrent = pherStep;
				} else  {
					pherCurrent += pherStep;
				}
				AntMethods.setupInitialPheromone(pher, cityNumber, pherCurrent);
			} else {
				depositPheromone(pher, current, pherCurrent, Integer.MAX_VALUE);
				depositPheromone(pher, best, pherBest, Integer.MAX_VALUE);
			}
		}
		
		return best;
	}
	
	/**
	 * Gambardella, L. M., Taillard, E. D., & Dorigo, M. (1999). 
	 * Ant colonies for the quadratic assignment problem. Journal of the operational research society, 50(2), 167-176..
	 * 
	 * @param solution
	 * @param MAX_G
	 * @return
	 */
	public static Solution hybridAntSystem(final int POP_SIZE, final int MAX_G) {
		int cityNumber = Problems.getProblem().getCityNumber();
		Solution[] currents = new Solution[POP_SIZE];
		int bestIdx = 0;
		for (int i = 0; i < currents.length; i++) {
			currents[i] = new Solution(true);
			//improve current with local search method
			if (Simulations.USE_LOCAL_SEARCH) currents[i] = GreedyMethods.firstImproveLocalSearch(currents[i], 2);;
			if (currents[i].getMakespan() < currents[bestIdx].getMakespan()) {
				bestIdx = i;
			}
		}
		Solution best = new Solution(currents[bestIdx]);
		
		final double alpha1 = 0.1; 	//for pheromone evaporate
		final double alpha2 = 0.1;	//for pheromone deposit
		final double greedyProb = 0.9;
		final int SWAP_TIMES = cityNumber/3; //R
		final double Q = 100.0;
		final int MAX_UNIMPROVED_ITERATIONS = cityNumber / 2; //S
		
		//create pheromone matrix and heuristic matrix
		double[][] pher = new double[cityNumber][cityNumber];
		pher = AntMethods.setupInitialPheromone(pher, cityNumber, 1.0/(best.getMakespan()*Q));
		boolean isIntensification = true;
		int unimprovedIterations = 0;
		
		for (int g = 0; g < MAX_G; g++) {
			Solution[] candidates = new Solution[POP_SIZE];
			boolean changed = false;
			for (int popIdx = 0; popIdx < currents.length; popIdx++) {
				candidates[popIdx] = AntMethods.modifySolution(currents[popIdx], pher, greedyProb, SWAP_TIMES);
				//improve current with local search method
				if (Simulations.USE_LOCAL_SEARCH) candidates[popIdx] = GreedyMethods.firstImproveLocalSearch(candidates[popIdx], 2);
			}
			
			bestIdx = 0;
			for (int popIdx = 0; popIdx < currents.length; popIdx++) {
				if ( isIntensification ) {
					if (candidates[popIdx].getMakespan() < currents[popIdx].getMakespan()) {
						currents[popIdx] = candidates[popIdx];
						changed = true;
					}
				} else {
					currents[popIdx] = candidates[popIdx];
				}
				if (currents[popIdx].getMakespan() < currents[bestIdx].getMakespan()) bestIdx = popIdx;
			}
			
			if (!changed) isIntensification = false;
			
			if (currents[bestIdx].getMakespan() < best.getMakespan()) {
				best = new Solution(currents[bestIdx]);
				isIntensification = true;
				unimprovedIterations = 0;
			} else {
				unimprovedIterations++;
			}
			
			//update pheromone
			evaporatePheromone(pher, alpha1, 0); //no minimum pheromone is used
			depositPheromone(pher, best, alpha2/best.getMakespan(), Integer.MAX_VALUE); //no maximum pheromone is used

			//diversification
			if (unimprovedIterations >= MAX_UNIMPROVED_ITERATIONS) {
				for (int i = 0; i < currents.length-1; i++) {
					currents[i] = new Solution(true);
					//improve current with local search method
					if (Simulations.USE_LOCAL_SEARCH) currents[i] = GreedyMethods.firstImproveLocalSearch(currents[i], 2);
				}
				currents[currents.length-1] = new Solution(best);
				pher = AntMethods.setupInitialPheromone(pher, cityNumber, 1.0/(best.getMakespan()*Q));
			}
			
		}
		
		return best;
	}
	
	/**
	 * XIA, X., & ZHOU, Y. (2018). Performance Analysis of ACO on the Quadratic Assignment Problem. Chinese Journal of Electronics, 27(1).
	 * 
	 * @param solution
	 * @param MAX_G
	 * @return
	 */
	public static Solution onePlusOneAntAlgorithm(final Solution solution, final int MAX_G) {
		Solution current = new Solution(solution);
		Solution best = new Solution(solution);
		int cityNumber = current.getCityNumber();
		
		double pherMin = 1.0/(cityNumber*cityNumber);
		double pherMax = 1.0/cityNumber;
		final double alpha = 1.0;
		final double beta = 0;
		final double rho = 1.0;
		final double value = pherMax;
		
		//create pheromone matrix and heuristic matrix
		double[][] pher = new double[cityNumber][cityNumber];
		double[][] heur = new double[cityNumber][cityNumber];
		pher = AntMethods.setupInitialPheromone(pher, cityNumber, pherMax);
		
		
		for (int g = 0; g < MAX_G; g++) {
			current = AntMethods.buildNewSolution(current, pher, alpha, heur, beta);
			
			//improve current with local search method
			if (Simulations.USE_LOCAL_SEARCH) current = GreedyMethods.firstImproveLocalSearch(current, 2);
			
			if ( current.getMakespan() < best.getMakespan()) {
				best = new Solution(current);
			}
			
			//update pheromone
			evaporatePheromone(pher, rho, pherMin);
			depositPheromone(pher, best, value, pherMax);
			//System.out.println(best.getMakespan());
		}
		
		return best;
	}
	
	private static double[][] setupInitialPheromone(double[][] pher, final int cityNumber, final double p) {
		//double[][] pher = new double[cityNumber][cityNumber];
		for (int i = 0; i < pher.length; i++) {
			for (int j = 0; j < pher[i].length; j++) {
				pher[i][j] = p;
			}
		}
		return pher;
	}
	
	private static void evaporatePheromone(double[][] pher, final double rho, final double pherMin) {
		for (int loc = 0; loc < pher.length; loc++) {
			for (int fac = 0; fac < pher[loc].length; fac++) {
				pher[loc][fac] = (1 - rho) * pher[loc][fac];
				if (pher[loc][fac] < pherMin) pher[loc][fac] = pherMin;
			}
		}
	}
	
	private static void depositPheromone(double[][] pher, final Solution s, final double value, final double pherMax) {
		for (int loc = 0; loc < pher.length; loc++) {
			int fac = s.getFacility(loc);
			pher[loc][fac] += value;
			if (pher[loc][fac] > pherMax) pher[loc][fac] = pherMax;
		}
	}
	
	
	private static Solution buildNewSolution(Solution solution, double[][] pher, double alpha, double[][] heur, double beta) {
		List<Integer> listLoc = new ArrayList<Integer>();
		List<Integer> listFac = new ArrayList<Integer>();
		int cityNumber = pher.length;
		for (int i = 0; i < cityNumber; i++) {
			listLoc.add(i);
			listFac.add(i);
		}
		Collections.shuffle(listLoc);
		Collections.shuffle(listFac);
		
		while (!listLoc.isEmpty()) {
			int loc = listLoc.remove(0);
			double totalProb = 0;
			List<Double> probs = new ArrayList<Double>();
			for (int fac : listFac) {
				double p = Math.pow(pher[loc][fac], alpha) * Math.pow(heur[loc][fac], beta);
				totalProb += p;
				probs.add(p);
			}
			double p = AntMethods.rand.nextDouble()*totalProb;
			int facIdx = 0;
			double sum = probs.get(facIdx);
			while ( sum < p) {
				facIdx++;
				sum += probs.get(facIdx);;
			}
			solution.setFacility(loc, listFac.remove(facIdx));
		}
		solution.caluMakespan();
		return solution;
	}
	
	
	private static Solution modifySolution(Solution solution, double[][] pher, final double greedyProb, final int TIMES) {
		int locNumber = pher.length;
		int times = TIMES;
		Solution s = new Solution(solution);
		while ( times-- > 0) {
			int loc1 = AntMethods.rand.nextInt(locNumber);
			int fac1 = s.getFacility(loc1);
			int loc2 = loc1;
			
			double[] probs = new double[locNumber];
			int bestLoc2 = 0;
			for (int i = 0; i < probs.length; i++) {
				if ( i == loc1 ) continue;
				probs[i] = pher[loc1][s.getFacility(i)] + pher[i][fac1];
				if (probs[i] > probs[bestLoc2]) bestLoc2 = i;
			}
			
			double p =  AntMethods.rand.nextDouble(); 
			if (p < greedyProb) {
				loc2 = bestLoc2;
			} else {
				for (int i = 1; i < probs.length; i++) {
					probs[i] += probs[i-1];
				}
				p = AntMethods.rand.nextDouble()*probs[probs.length-1];
				loc2 = 0;
				while ( probs[loc2] < p && loc2 < probs.length - 1) {
					loc2++;
				}
			}
			s.swap(s.nextNeighbor(loc1, loc2));
		}
		//solution.caluMakespan();
		return s;
	}
	

	
	private static Random rand = new Random();
	
	public static void main(String[] args) {
		String filePath = (new File("")).getAbsolutePath() + "/../p32/"; 
		String fileName = filePath + "02kra30b.txt";//"27tai100a.txt";//"18ste36b.txt";//"20tai25a.txt";
		Problems.setFileName(fileName);
		Solution s; // = new Solution(true);
		final int TIMES = 10;
		final int MAX_ITERATION = 100;
		double ave = 0;
		for (int i = 0; i < TIMES; i++) {
			s = new Solution(true);
			//s = AntInspiredMethods.onePlusOneAntAlgorithm(s, MAX_ITERATION);
			//s = AntInspiredMethods.fastAntSystem(s, MAX_ITERATION);
			s = AntMethods.hybridAntSystem(10, MAX_ITERATION);
			System.out.println(i + "--" + s.getMakespan());
			ave += s.getMakespan();
		}
		ave /= TIMES;
		int bMakespan = Problems.getProblem().getBestMakespan();
		double apd = Math.round((ave-bMakespan) * (1.0/bMakespan) *100*1000)/1000.0;

		System.out.println("Best Known:" + bMakespan + ", Average solution:" + ave + ", PE:" + apd);
	}
}
