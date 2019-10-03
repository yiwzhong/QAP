package qap;

import java.io.File;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.util.PriorityQueue;
import java.util.Random;


/**
 * 
 * @author yiwen zhong
 *
 */
public class SAMethods {

	/**
	 * Zhan, S. H., Lin, J., Zhang, Z. J., & Zhong, Y. W. (2016). 
	 * List-based simulated annealing algorithm for traveling salesman problem. Computational intelligence and neuroscience, 2016, 8.
	 * 
	 * @param solution
	 * @return
	 */
	public static Solution listBasedSA(Solution solution) {
		final int MAX_G = Simulations.MAX_GENERATION; //MAXIMUM GENERATION
		Solution current = new Solution(solution);
		Solution best = new Solution(solution);
		final int neighborSize = current.getNeighborSize();
		final int SCHEDULE_LENGTH = neighborSize * Simulations.Q / MAX_G;
		final int MAXRN = neighborSize/2;
		
		PriorityQueue<Double> tempList= new PriorityQueue<Double>();
		while (tempList.size() < Simulations.LIST_LENGTH) {
			int idx;// = Methods.rand.nextInt(neighborSize);
			if (Simulations.USE_SAME_RANDOM) {
				idx = Solution.rand.nextInt(neighborSize);
			} else {
				idx = SAMethods.rand.nextInt(neighborSize);
			}
			double delta = solution.different(idx);
			if (delta != 0) {
				delta *= Simulations.LIST_WEIGHT;
				if (delta != 0) tempList.offer( (delta >= 0)? -delta : delta);
			}
		}

		double[] temperatures = new double[MAX_G];
		int[] makespans = new int[MAX_G];
		int[] bestMakespans = new int[MAX_G];

		int rejectedCount = 0;
		for (int q = 0; q < MAX_G; q++) {
			double t = -tempList.peek();
			double totalTemp = 0;
			int counter = 0;
			temperatures[q] = t;
			makespans[q] = current.getMakespan();
			bestMakespans[q] = best.getMakespan();
			for (int k = 0; k < SCHEDULE_LENGTH; k++) {
				Neighbor bestNb = current.nextNeighbor();
				double p;// = Methods.rand.nextDouble();
				if (Simulations.USE_SAME_RANDOM) {
					p = Solution.rand.nextDouble();
				} else {
					p = SAMethods.rand.nextDouble();
				}
				if (bestNb.getDelta() < 0 || p < 1.0/Math.exp(Math.abs(bestNb.getDelta())/t)) {
					//accept
					current.swap(bestNb);
					if (current.getMakespan() < best.getMakespan()) {
						best.update(current);
						best.setLastImproving(q);
					}
					if ( bestNb.getDelta()  > 0) {
						totalTemp += Math.abs(bestNb.getDelta() ) / Math.log(1.0/p);
						counter++;
					}
					rejectedCount = 0;
				} else {
					rejectedCount++;
					if (rejectedCount >= MAXRN) {
						current.mutation(Simulations.LIST_MUTATION_STRENGTH);
						rejectedCount = 0;
					}
				}
			}

			//update temperature list
			if ( counter != 0) {
				tempList.remove();
				tempList.offer( - totalTemp/counter);
				//System.out.println("offered temperature:" + totalTemp/counter);
			} 
		}

		if (Simulations.isSavingProcessData()) SAMethods.saveConvergenceData(temperatures, makespans, bestMakespans);
		//System.out.println(lastImprove);
		if (Simulations.USE_LOCAL_SEARCH) best = GreedyMethods.bestImproveLocalSearch(best);
		if (Simulations.USE_TABU_SEARCH) best = TabuMethods.simplifiedTabuSearch(best, best.getCityNumber());
		return best;
	}
	

	public static Solution geometricSA(Solution solution) {
		Solution current = new Solution(solution);
		Solution best = new Solution(solution);
		final int neighborSize = current.getNeighborSize();
		final int MAX_G = Simulations.MAX_GENERATION; //MAXIMUM GENERATION
		final int SCHEDULE_LENGTH = neighborSize * Simulations.Q / MAX_G;
		final int MAXRN = neighborSize/2;
		//System.out.println(neighborSize+","+SCHEDULE_LENGTH);
		//double[] cps = produceCoolingParas(); //using cosine_SA's strategy to produce t0 and tf
		double[] cps = SAMethods.produceCoolingParas4Modified(current, neighborSize); //using modified_SA's strategy to produce t0 and tf
		final double t0 =cps[0]/Simulations.GEOMETRIC_BEGIN_TEMPERATURE_SCALE_FACTOR;
		final double tf = cps[1]/Simulations.GEOMETRIC_FINAL_TEMPERATURE_SCALE_FACTOR;
		double alpha = Math.pow( tf/t0, 1.0/MAX_G);
		double finalT = tf;
		
		double[] temperatures = new double[MAX_G];
		int[] makespans = new int[MAX_G];
		int[] bestMakespans = new int[MAX_G];
		
		double t = t0;
		int rejectedCount = 0;
		boolean isConstant = false;
		for (int q = 0; q < MAX_G; q++) {
			temperatures[q] = t;
			makespans[q] =current.getMakespan();
			bestMakespans[q] = best.getMakespan();
			for (int k = 0; k < SCHEDULE_LENGTH; k++) {
				Neighbor bestNb = current.nextNeighbor();
				double p;// = Methods.rand.nextDouble();
				if (Simulations.USE_SAME_RANDOM) {
					p = Solution.rand.nextDouble();
				} else {
					p = SAMethods.rand.nextDouble();
				}
				if (bestNb.getDelta() < 0 || p < 1.0/Math.exp(Math.abs(bestNb.getDelta())/t)) {
					//accept
					current.swap(bestNb);
					if (current.getMakespan() < best.getMakespan()) {
						best.update(current);
						best.setLastImproving(q);
						finalT = t;
					}
					rejectedCount = 0;
				} else {
					rejectedCount++;
				}
				if (!isConstant) {
					if (rejectedCount >= MAXRN) {
						isConstant = true;
						current.mutation(Simulations.GEOMETRIC_MUTATION_STRENGTH);
						rejectedCount = 0;
						t = finalT;
						alpha = 1;
					}
				}
			}
			t *= alpha;
		}

		if (Simulations.isSavingProcessData()) SAMethods.saveConvergenceData(temperatures, makespans, bestMakespans);
		if (Simulations.USE_LOCAL_SEARCH) best = GreedyMethods.bestImproveLocalSearch(best);
		if (Simulations.USE_TABU_SEARCH) best = TabuMethods.simplifiedTabuSearch(best, best.getCityNumber());
		return best;
	}

	
	public static Solution basicSA(Solution solution) {
		Solution current = new Solution(solution);
		Solution best = new Solution(solution);
		final int neighborSize = current.getNeighborSize();
		int L0 = Simulations.Q * neighborSize;
		int L = L0;
		
		double[] cps;
		cps = SAMethods.produceCoolingParas4Modified(current, neighborSize);
		double t0 = cps[0] / Simulations.BASIC_BEGIN_TEMPERATURE_SCALE_FACTOR;
		double tf = cps[1] / Simulations.BASIC_FINAL_TEMPERATURE_SCALE_FACTOR;
		double beta = (t0-tf)/(L*t0*tf);
	
		double[] temperatures = new double[L0];
		int[] makespans = new int[L0];
		int[] bestMakespans = new int[L0];
		
		double t = t0;
		for (int k = 0; k < L0; k++) {
			temperatures[k] = t;
			makespans[k] = current.getMakespan();
			bestMakespans[k] = best.getMakespan();
			int d = SAMethods.accept(current, current.nextNeighbor(), t);
			if ( d != Integer.MIN_VALUE) {
				if ( current.getMakespan() < best.getMakespan()) {
					best.update(current);
					best.setLastImproving(k);
				}
			}
			t /= (1+beta*t);
		}

		if (Simulations.isSavingProcessData()) SAMethods.saveConvergenceData(temperatures, makespans, bestMakespans);
		if (Simulations.USE_LOCAL_SEARCH) best = GreedyMethods.bestImproveLocalSearch(best);
		if (Simulations.USE_TABU_SEARCH) best = TabuMethods.simplifiedTabuSearch(best, best.getCityNumber());
		return best;
	}
	
	/**
	 * Connolly, D.T. (1990). An improved annealing scheme for the QAP. European J. of Operational Research, 46, 93¨C100.
	 * 
	 * @param solution
	 * @return
	 */
	
	public static Solution connollySA(Solution solution) {
		Solution current = new Solution(solution);
		Solution best = new Solution(solution);
		final int neighborSize = current.getNeighborSize();
		final int MAXRN = neighborSize/2;
		int L0 = Simulations.Q * neighborSize;
		int L = L0;
		
		double[] cps;// = Methods.produceCoolingParas4Connolly(current, neighborSize);
		cps = SAMethods.produceCoolingParas4Modified(current, neighborSize);
		double t0 = cps[0] / Simulations.CONNOLLY_BEGIN_TEMPERATURE_SCALE_FACTOR;
		double tf = cps[1] / Simulations.CONNOLLY_FINAL_TEMPERATURE_SCALE_FACTOR;
		double beta = (t0-tf)/(L*t0*tf);
	
		double finalT = 0; //the temperature where the last improving move happened
		double[] temperatures = new double[L0];
		int[] makespans = new int[L0];
		int[] bestMakespans = new int[L0];
		
		double t = t0;
		int rejectedCount = 0;
		boolean isConstant = false;

		for (int k = 0; k < L0; k++) {
			temperatures[k] = t;
			makespans[k] = current.getMakespan();
			bestMakespans[k] = best.getMakespan();
			int d = SAMethods.accept(current, current.nextNeighbor(), t);
			if ( d != Integer.MIN_VALUE) {
				if ( current.getMakespan() < best.getMakespan()) {
					best.update(current);
					best.setLastImproving(k);
					finalT = t;
				}
				rejectedCount = 0;
			} else {
				rejectedCount++;
			}

			//oscillation
			if (!isConstant) {
				if (rejectedCount >= MAXRN) {
					isConstant = true;
					t = finalT;
					beta = 0;
					//the next swap is accepted
					k++;
					temperatures[k] = t;
					makespans[k] = current.getMakespan();
					bestMakespans[k] = best.getMakespan();
					Neighbor nb = current.nextNeighbor();
					current.swap(nb);
					if ( current.getMakespan() < best.getMakespan()) {
						best.update(current);
						best.setLastImproving(k);
					}
				} 
			} 

			t /= (1+beta*t);
		}

		if (Simulations.isSavingProcessData()) SAMethods.saveConvergenceData(temperatures, makespans, bestMakespans);
		if (Simulations.USE_LOCAL_SEARCH) best = GreedyMethods.bestImproveLocalSearch(best);
		if (Simulations.USE_TABU_SEARCH) best = TabuMethods.simplifiedTabuSearch(best, best.getCityNumber());
		return best;
	}
	
	/**
	 * Optimizing simulated annealing schedules with genetic programming. European J. of Operational Research, 92, 402-416.
	 *  
	 * The following implementation is based on the version described in:
	 *  A modified simulated annealing algorithm for the quadratic assignment problem[J]. Informatica, 2003, 14(4): 497-514.
	 * 
	 * @param solution
	 * @return
	 */
	public static Solution consineSA(Solution solution) {
		Solution current = new Solution(solution);
		Solution best = new Solution(solution);
		final int neighborSize = current.getNeighborSize();
		final int MAXRN = neighborSize/2;
		int L0 = Simulations.Q * neighborSize;
		int L = L0;
		
		double[] cps;
		//cps = Methods.produceCoolingParas4Cosine(current);
		cps = SAMethods.produceCoolingParas4Modified(current, neighborSize);
		double t0 = cps[0]/Simulations.COSINE_BEGIN_TEMPERATURE_SCALE_FACTOR;
		double tf = cps[1]/Simulations.COSINE_FINAL_TEMPERATURE_SCALE_FACTOR;
		double beta = (t0-tf)/(L*t0*tf);
	
		double finalT = 0;
		int finalL = L;
		double[] temperatures = new double[L0];
		int[] makespans = new int[L0];
		int[] bestMakespans = new int[L0];
		
		double t = t0;
		int rejectedCount = 0;
		boolean isOscillation = false;
		double omega = 16.0 * Math.PI / (25*current.getCityNumber()*(current.getCityNumber()-1));

		for (int k = 0; k < L0; k++) {
			temperatures[k] = t;
			makespans[k] = current.getMakespan();
			bestMakespans[k] = best.getMakespan();
			int d = SAMethods.accept(current, current.nextNeighbor(), t);
			if ( d != Integer.MIN_VALUE) {
				if ( current.getMakespan() < best.getMakespan()) {
					best.update(current);
					best.setLastImproving(k);
				}
				rejectedCount = 0;
			} else {
				rejectedCount++;
			}

			//oscillation
			if (!isOscillation) {
				if (rejectedCount >= MAXRN) {
					isOscillation = true;
					finalT = t;
					finalL = k;
				} 
			} 

			if (!isOscillation) {
				t /= (1+beta*t);
			} else {
				t = finalT + 0.5 * finalT * Math.cos(omega*(k-finalL)); //0.5
			}

		}

		if (Simulations.isSavingProcessData()) SAMethods.saveConvergenceData(temperatures, makespans, bestMakespans);
		if (Simulations.USE_LOCAL_SEARCH) best = GreedyMethods.bestImproveLocalSearch(best);
		if (Simulations.USE_TABU_SEARCH) best = TabuMethods.simplifiedTabuSearch(best, best.getCityNumber());
		return best;
	}
	
	/**
	 * Misevicius, A. (2003). A modified simulated annealing algorithm for the quadratic assignment problem. Informatica, 14(4), 497-514.
	 * 
	 * @param solution
	 * @return
	 */
	
	public static Solution modifiedSA(Solution solution) {
		Solution current = new Solution(solution);
		Solution best = new Solution(solution);
		final int neighborSize = current.getNeighborSize();
		final int MAXRN = neighborSize/2;
		int L0 = Simulations.Q * neighborSize; 	//total iteration times
		int L = L0; 	//iteration times in each oscillation
		
		double[] cps = SAMethods.produceCoolingParas4Modified(current, neighborSize);
		double t0 = cps[0]/= Simulations.MODIFIED_BEGIN_TEMPERATURE_SCALE_FACTOR;
		double tf = cps[1]/= Simulations.MODIFIED_FINAL_TEMPERATURE_SCALE_FACTOR;
		double beta = (t0-tf)/(L0*t0*tf);
		
		double finalT = 0; 	//when MAXRN consecutive moves are rejected
		int finalL = L;		//when MAXRN consecutive moves are rejected
		double[] temperatures = new double[L0];
		int[] makespans = new int[L0];
		int[] bestMakespans = new int[L0];
		
		double t = t0;
		int kk = Integer.MAX_VALUE; //counter when algorithm is in oscillation
		int rejectedCount = 0;
		boolean isOscillation = false;
		boolean bestImproved = false;
		int oscillationTimes = 0;
		for (int k = 0; k < L0; k++) {
			temperatures[k] = t;
			makespans[k] = current.getMakespan();
			bestMakespans[k] = best.getMakespan();
			int d = SAMethods.accept(current, current.nextNeighbor(), t);
			if ( d != Integer.MIN_VALUE) {
				if ( current.getMakespan() < best.getMakespan()) {
					best.update(current);
					best.setLastImproving(k);
					bestImproved = true;
				}
				if ( d != 0 ) rejectedCount = 0;
			} else {
				rejectedCount++;
			}

			//oscillation
			if (!isOscillation) {
				if (rejectedCount >= MAXRN) {
					isOscillation = true;
					oscillationTimes++;
					finalT = t;
					finalL = k;

					L = Math.min(finalL, L0 - oscillationTimes*finalL);
					t0 = (L<current.getCityNumber())?finalT:(1+1.0/3)*finalT;
					tf = (L<current.getCityNumber())?finalT:(1-1.0/3)*finalT;
					beta = (t0-tf)/(L*t0*tf);
					t = t0;
					kk = 0;
//					if (bestImproved && Simulations.USE_LOCAL_SEARCH) {
//						best = Methods.bestImproveLocalSearch(best);
//						bestImproved = false;
//					}
				}
			} else {//in oscillation
				kk++;
				if (kk >= L) {
					oscillationTimes++;
					if (L0 - k < finalL) {//update L, to, tf, beta
						L = Math.min(finalL, L0 - oscillationTimes*finalL);
						t0 = (L<current.getCityNumber())?finalT:(1+1.0/3)*finalT;
						tf = (L<current.getCityNumber())?finalT:(1-1.0/3)*finalT;
						beta = (t0-tf)/(L*t0*tf);
					}
					t = t0;
					kk = 0;
//					if (bestImproved && Simulations.USE_LOCAL_SEARCH) {
//						best = Methods.bestImproveLocalSearch(best);
//						bestImproved = false;
//					}
				}
			}
			if ( kk > 0 ) t /= (1+beta*t);
		}

		if (Simulations.isSavingProcessData()) SAMethods.saveConvergenceData(temperatures, makespans, bestMakespans);
		if (Simulations.USE_LOCAL_SEARCH) best = GreedyMethods.bestImproveLocalSearch(best);
		if (Simulations.USE_TABU_SEARCH) best = TabuMethods.simplifiedTabuSearch(best, best.getCityNumber());
		return best;
	}
	
	private static double[] produceCoolingParas4Connolly(Solution s, int K) {
		double maxDelta = Integer.MIN_VALUE;
		double minDelta = Integer.MAX_VALUE;
		for (int k = 0; k < K; k++) {
			Neighbor nb = s.randNeighbor();
			int delta = Math.abs(nb.getDelta());
			if ( delta < minDelta && delta != 0) {
				minDelta = delta;
			}
			if ( delta > maxDelta) {
				maxDelta = delta;
			}
		}
		double t0 = minDelta + (maxDelta - minDelta)/10;
		double tf = minDelta;		
		return new double[]{t0, tf};
	}

	private static double[] produceCoolingParas4Modified(Solution s, int K) {
		double minDelta = Integer.MAX_VALUE;
		double avrDelta = 0;
		for (int k = 0; k < K; k++) {
			Neighbor nb = s.randNeighbor();
			int delta = Math.abs(nb.getDelta());
			if ( delta < minDelta && delta != 0 ) {
				minDelta = delta;
			}
			avrDelta += delta;
		}
		avrDelta /= K;

		double t0 = (1-Simulations.MODIFIED_LAMDA1)*minDelta + Simulations.MODIFIED_LAMDA1*avrDelta;
		double tf = (1-Simulations.MODIFIED_LAMDA2)*minDelta + Simulations.MODIFIED_LAMDA2*avrDelta;		
		return new double[]{t0, tf};
	}

	
	private static int accept(Solution s, Neighbor nb, double t) {
		double p;// = Methods.rand.nextDouble();
		if (Simulations.USE_SAME_RANDOM) {
			p = Solution.rand.nextDouble();
		} else {
			p = SAMethods.rand.nextDouble();
		}
		if (nb.getDelta() < 0 || p < 1.0/Math.exp(Math.abs(nb.getDelta())/t)) {
			//accept
			s.swap(nb);
			return nb.getDelta();
		} else {
			return Integer.MIN_VALUE;
		}
	}
	
	
	private static double[] produceCoolingParas4Cosine(Solution s) {
		int[][] cityDistance = s.cityDistance;
		int[][] flowData = s.flowData;
		//find max distance and max flow
		double maxD = -1;
		double maxF = -1;
		for (int i = 0; i < cityDistance.length; i++) {
			for (int j = 0; j < cityDistance[i].length; j++) {
				if (cityDistance[i][j] > maxD) {
					maxD = cityDistance[i][j];
				}
				if (flowData[i][j] > maxF) {
					maxF = flowData[i][j];
				}
			}
		}
		//parameters
		double t0 = 10.0 * maxD * maxF / 25;
		double tf = 2.0 * maxD * maxF / 25;	
		return new double[]{t0, tf};
	}
	
	/**
	 * Pepper, J. W., Golden, B. L., & Wasil, E. A. (2002). Solving the traveling salesman problem with annealing-based heuristics: a computational study.
	 * IEEE Transactions on Systems, Man, and Cybernetics-Part A: Systems and Humans,32(1),72-77.
	 * 
	 * @param solution
	 * @return
	 */
	public static Solution geometricTA(Solution solution) {
		Solution current = new Solution(solution);
		Solution best = new Solution(solution);
		final int neighborSize = current.getNeighborSize();
		final int MAX_G = Simulations.MAX_GENERATION; //MAXIMUM GENERATION
		final int SCHEDULE_LENGTH = neighborSize * Simulations.Q / MAX_G;
		final int MAXRN = neighborSize/2;
		//System.out.println(neighborSize+","+SCHEDULE_LENGTH);
		//double[] cps = produceCoolingParas(); //using cosine_SA's strategy to produce t0 and tf
		double[] cps = SAMethods.produceCoolingParas4Modified(current, neighborSize); //using modified_SA's strategy to produce t0 and tf
		final double t0 =cps[0]/Simulations.GEOMETRIC_TA_BEGIN_THRESHOLD_SCALE_FACTOR;
		final double tf = cps[1]/Simulations.GEOMETRIC_TA_FINAL_THRESHOLD_SCALE_FACTOR;
		double alpha = Math.pow( tf/t0, 1.0/MAX_G);
		double finalT = tf;
		
		double[] temperatures = new double[MAX_G];
		int[] makespans = new int[MAX_G];
		int[] bestMakespans = new int[MAX_G];
		
		double t = t0;
		int rejectedCount = 0;
		boolean isConstant = false;
		for (int q = 0; q < MAX_G; q++) {
			temperatures[q] = t;
			makespans[q] =current.getMakespan();
			bestMakespans[q] = best.getMakespan();
			for (int k = 0; k < SCHEDULE_LENGTH; k++) {
				Neighbor bestNb = current.nextNeighbor();
				if (bestNb.getDelta() < t ) {
					//accept
					current.swap(bestNb);
					if (current.getMakespan() < best.getMakespan()) {
						best.update(current);
						best.setLastImproving(q);
						finalT = t;
					}
					rejectedCount = 0;
				} else {
					rejectedCount++;
				}
				if (!isConstant) {
					if (rejectedCount >= MAXRN) {
						isConstant = true;
						current.mutation(Simulations.GEOMETRIC_TA_MUTATION_STRENGTH);
						rejectedCount = 0;
						t = finalT;
						alpha = 1;
					}
				}
			}
			t *= alpha;
		}

		if (Simulations.isSavingProcessData()) SAMethods.saveConvergenceData(temperatures, makespans, bestMakespans);
		if (Simulations.USE_LOCAL_SEARCH) best = GreedyMethods.bestImproveLocalSearch(best);
		if (Simulations.USE_TABU_SEARCH) best = TabuMethods.simplifiedTabuSearch(best, best.getCityNumber());
		return best;
	}
	
	/**
	 * Charon, I., & Hudry, O. (2001). 
	 * The noising methods: A generalization of some metaheuristics. European Journal of Operational Research, 135(1), 86-101.
	 * 
	 * @param solution
	 * @return
	 */
	public static Solution geometricNM(Solution solution) {
		Solution current = new Solution(solution);
		Solution best = new Solution(solution);
		final int neighborSize = current.getNeighborSize();
		final int MAX_G = Simulations.MAX_GENERATION; //MAXIMUM GENERATION
		final int SCHEDULE_LENGTH = neighborSize * Simulations.Q / MAX_G;
		final int MAXRN = neighborSize/2;
		//System.out.println(neighborSize+","+SCHEDULE_LENGTH);
		//double[] cps = produceCoolingParas(); //using cosine_SA's strategy to produce t0 and tf
		double[] cps = SAMethods.produceCoolingParas4Modified(current, neighborSize); //using modified_SA's strategy to produce t0 and tf
		final double t0 =cps[0]/Simulations.GEOMETRIC_NM_BEGIN_THRESHOLD_SCALE_FACTOR;
		final double tf = cps[1]/Simulations.GEOMETRIC_NM_FINAL_THRESHOLD_SCALE_FACTOR;
		double alpha = Math.pow( tf/t0, 1.0/MAX_G);
		double finalT = tf;
		
		double[] temperatures = new double[MAX_G];
		int[] makespans = new int[MAX_G];
		int[] bestMakespans = new int[MAX_G];
		
		double t = t0; //rate
		int rejectedCount = 0;
		boolean isConstant = false;
		for (int q = 0; q < MAX_G; q++) {
			temperatures[q] = t;
			makespans[q] =current.getMakespan();
			bestMakespans[q] = best.getMakespan();
			for (int k = 0; k < SCHEDULE_LENGTH; k++) {
				Neighbor bestNb = current.nextNeighbor();
				double p = SAMethods.rand.nextDouble();
				//if (SAMethods.rand.nextDouble() < 0.5) p = -p; //do we need this?
				double nosing = t * p;
				if (bestNb.getDelta() < nosing ) {
					//accept
					current.swap(bestNb);
					if (current.getMakespan() < best.getMakespan()) {
						best.update(current);
						best.setLastImproving(q);
						finalT = t;
					}
					rejectedCount = 0;
				} else {
					rejectedCount++;
				}
				if (!isConstant) {
					if (rejectedCount >= MAXRN) {
						isConstant = true;
						current.mutation(Simulations.GEOMETRIC_NM_MUTATION_STRENGTH);
						rejectedCount = 0;
						t = finalT;
						alpha = 1;
					}
				}
			}
			t *= alpha;
		}

		if (Simulations.isSavingProcessData()) SAMethods.saveConvergenceData(temperatures, makespans, bestMakespans);
		if (Simulations.USE_LOCAL_SEARCH) best = GreedyMethods.bestImproveLocalSearch(best);
		if (Simulations.USE_TABU_SEARCH) best = TabuMethods.simplifiedTabuSearch(best, best.getCityNumber());
		return best;
	}
	
	
	/**
	 * Dueck G. New Optimization Heuristics : The Great Deluge Algorithm and the Record-to-Record Travel[J]. 
	 * Journal of Computational Physics, 1993, 104(1):86-92.
	 * 
	 * @param solution
	 * @return
	 */
	public static Solution geometricGD(Solution solution) {
		Solution current = new Solution(solution);
		Solution best = new Solution(solution);
		final int neighborSize = current.getNeighborSize();
		final int MAX_G = Simulations.MAX_GENERATION; //MAXIMUM GENERATION
		final int SCHEDULE_LENGTH = neighborSize * Simulations.Q / MAX_G;
		final int MAXRN = neighborSize/2;
		//System.out.println(neighborSize+","+SCHEDULE_LENGTH);

		double waterLevel = current.getMakespan();
		double[] temperatures = new double[MAX_G];
		int[] makespans = new int[MAX_G];
		int[] bestMakespans = new int[MAX_G];
		
		int rejectedCount = 0;
		for (int q = 0; q < MAX_G; q++) {
			temperatures[q] = waterLevel;
			makespans[q] =current.getMakespan();
			bestMakespans[q] = best.getMakespan();
			double down = 0;
			int count = 0;
			for (int k = 0; k < SCHEDULE_LENGTH; k++) {
				Neighbor bestNb = current.nextNeighbor();
				down += Math.abs(current.getMakespan() - waterLevel);
				count++;
				if (bestNb.getDelta() + current.getMakespan() < waterLevel ) {
					//accept
					current.swap(bestNb);
					if (current.getMakespan() < best.getMakespan()) {
						best.update(current);
						best.setLastImproving(q);
					}
					rejectedCount = 0;
				} else {
					rejectedCount++;
				}

				if (rejectedCount >= MAXRN) {
					rejectedCount = 0;
					waterLevel = (int)(1.1*waterLevel);
				}
			}
			if ( count != 0) {
				waterLevel -=  0.3 * down / count;
			}
		}

		if (Simulations.isSavingProcessData()) SAMethods.saveConvergenceData(temperatures, makespans, bestMakespans);
		if (Simulations.USE_LOCAL_SEARCH) best = GreedyMethods.bestImproveLocalSearch(best);
		if (Simulations.USE_TABU_SEARCH) best = TabuMethods.simplifiedTabuSearch(best, best.getCityNumber());
		return best;
	}
	

	private static void saveConvergenceData( double[] ts, int[] vs, int[] bs) {
		try {
			String f = Problems.fileName;
			f += " " + Simulations.getParaSetting() + " for qap results.csv";
			f = (new File("")).getAbsolutePath() + "\\results\\Convergence\\" + f.substring(f.lastIndexOf("/")+1);
			System.out.println(f);
			PrintWriter printWriter = new PrintWriter(new FileWriter(f));
			for (int idx=0; idx<ts.length; idx++) {
				printWriter.println(ts[idx] + "," + vs[idx] + "," + bs[idx]);
			}
			printWriter.close();
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}
	
	private static Random rand = new Random(); //
	
	
	public static void main(String[] args) {
		String filePath = (new File("")).getAbsolutePath() + "/../p32/"; 
		String fileName = filePath + "20tai25a.txt";//"27tai100a.txt";//"18ste36b.txt";//"20tai25a.txt";
		Problems.setFileName(fileName);
		Solution s; // = new Solution(true);
		final int TIMES = 25;
		double ave = 0;
		for (int i = 0; i < TIMES; i++) {
			s = new Solution(true);
			s = SAMethods.geometricTA(s);
			System.out.println(i + "--" + s.getMakespan());
			ave += s.getMakespan();
		}
		ave /= TIMES;
		int bMakespan = Problems.getProblem().getBestMakespan();
		double apd = Math.round((ave-bMakespan) * (1.0/bMakespan) *100*1000)/1000.0;

		System.out.println("Best Known:" + bMakespan + ", Average solution:" + ave + ", PE:" + apd);
	}
}
