package qap;

import java.io.File;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;

/**
 * 
 * @author yiwen zhong
 *
 */
public class Simulations {
	
	public static void main(String[] args) {
		String filePath = (new File("")).getAbsolutePath() +  "/../pKra30b/";// "/../p32/";//"/../type4/"; //"/../p32/"; 
		if (Simulations.TEST_TYPE == ETestType.SINGLE_INSTANCE_SINGLE_ALGORITHM) {
			String fileName = filePath+"20tai25a.txt";
			testSingleInstance(fileName, Simulations.Q, Simulations.TIMES);
		} else if (Simulations.TEST_TYPE == ETestType.SINGLE_INSTANCE_MULTIPLE_ALGORITHM) {
			String fileName = filePath+"20tai25a.txt";
			for (ESAType t : ESAType.values()) {
				Simulations.saType = t;
				System.out.println("\n"+Simulations.getParaSetting());
				testSingleInstance(fileName, Simulations.Q, Simulations.TIMES);
			}
		}  else if (Simulations.TEST_TYPE == ETestType.MULTIPLE_INSTANCE_SINGLE_ALGORITHM) {
			System.out.println("\n"+Simulations.getParaSetting());
			testPerformance(filePath, Simulations.Q, Simulations.TIMES);
		} else if (Simulations.TEST_TYPE == ETestType.MULTIPLE_INSTANCE_MULTIPLE_ALGORITHM) {
			ESAType[] saTypes = ESAType.values();
			//saTypes = new ESAType[] {ESAType.BASIC_SA, ESAType.CONNOLLY_SA, ESAType.COSINE_SA, ESAType.MODIFIED_SA};
			for (ESAType sat : saTypes) {
				Simulations.saType = sat;
				System.out.println("\n"+Simulations.getParaSetting());
				testPerformance(filePath, Simulations.Q, Simulations.TIMES);
			}
		} else if (Simulations.TEST_TYPE == ETestType.PARAMETER_BEGIN_TEMPERATURE_TUNNING) {
			ESAType[] saTypes;// = new ESAType[] {ESAType.BASIC_SA, ESAType.CONNOLLY_SA, ESAType.COSINE_SA, ESAType.MODIFIED_SA, ESAType.GEOMETRIC_SA};
			saTypes = new ESAType[] {ESAType.BASIC_SA, ESAType.CONNOLLY_SA, ESAType.COSINE_SA, ESAType.MODIFIED_SA};
			saTypes = new ESAType[] {/*ESAType.GEOMETRIC_TA,*/ ESAType.GEOMETRIC_NM};
			for (ESAType sat : saTypes) {
				Simulations.saType = sat;
				parametersBeginTemperatureTunning(filePath);
			}
		} else if (Simulations.TEST_TYPE == ETestType.PARAMETER_FINAL_TEMPERATURE_TUNNING) {
			ESAType[] saTypes;// = new ESAType[] {ESAType.BASIC_SA, ESAType.CONNOLLY_SA, ESAType.COSINE_SA, ESAType.MODIFIED_SA, ESAType.GEOMETRIC_SA};
			saTypes = new ESAType[] {ESAType.BASIC_SA, ESAType.CONNOLLY_SA, ESAType.COSINE_SA, ESAType.MODIFIED_SA};
			//saTypes = new ESAType[] {/*ESAType.GEOMETRIC_TA,*/ ESAType.GEOMETRIC_NM};
			for (ESAType sat : saTypes) {
				Simulations.saType = sat;
				parametersFinalTemperatureTunning(filePath);
			}
		}
	}
	
	private static void parametersBeginTemperatureTunning(String filePath) {
		String pathName = filePath.substring(filePath.lastIndexOf("/", filePath.length()-2)).substring(1);
		pathName = pathName.substring(0, pathName.length()-1);
		List<double[]> resultsList = new ArrayList<>();
		List<Double> scalesList = new ArrayList<>();
		for (int i = 0; i <= 10; i++) {
			double scale = 0.2 + i*0.2;
			if (Simulations.saType == ESAType.GEOMETRIC_SA || Simulations.saType == ESAType.GEOMETRIC_TA || Simulations.saType == ESAType.GEOMETRIC_NM ) {

			}
			scalesList.add(scale);
			switch (Simulations.saType) {
			case BASIC_SA: Simulations.BASIC_BEGIN_TEMPERATURE_SCALE_FACTOR = scale; break;
			case CONNOLLY_SA: Simulations.CONNOLLY_BEGIN_TEMPERATURE_SCALE_FACTOR = scale; break;
			case COSINE_SA: Simulations.COSINE_BEGIN_TEMPERATURE_SCALE_FACTOR = scale; break;
			case MODIFIED_SA: Simulations.MODIFIED_BEGIN_TEMPERATURE_SCALE_FACTOR = scale; break;
			case GEOMETRIC_SA: Simulations.GEOMETRIC_BEGIN_TEMPERATURE_SCALE_FACTOR = scale; break;
			case GEOMETRIC_TA: Simulations.GEOMETRIC_TA_BEGIN_THRESHOLD_SCALE_FACTOR = scale; break;
			case GEOMETRIC_NM: Simulations.GEOMETRIC_NM_BEGIN_THRESHOLD_SCALE_FACTOR = scale; break;
			default: System.out.println("Error SA type: " + Simulations.saType); return;
			}
			System.out.println("\n"+Simulations.getParaSetting());
			double[] results = testPerformance(filePath, Simulations.Q, Simulations.TIMES);
			resultsList.add(results);

			if (Simulations.SAVING_PARA_TUNNING) {
				try {
					String fileName = (new File("")).getAbsolutePath() + "\\results\\Parameters\\";
					fileName += pathName + "-" + Simulations.saType + "-" + Simulations.USE_LOCAL_SEARCH + "-" + Simulations.USE_TABU_SEARCH + "-" + USE_SAME_RANDOM;
					fileName += "-Q=" + Simulations.Q + "-S=" + Simulations.INITIAL_SEED + " para t0 tunning results.csv";
					PrintWriter printWriter = new PrintWriter(new FileWriter(fileName));
					for (int idx = 0; idx < resultsList.size(); idx++) {
						double[] rs = resultsList.get(idx);
						printWriter.println();
						printWriter.print(scalesList.get(idx));
						for (int j = 0; j < rs.length; j++) {
							printWriter.print(","+rs[j]);
						}
					}

					printWriter.close();
				} catch (Exception ex) {
					ex.printStackTrace();
				}
			}
		}
	}
	
	private static void parametersFinalTemperatureTunning(String filePath) {
		String pathName = filePath.substring(filePath.lastIndexOf("/", filePath.length()-2)).substring(1);
		pathName = pathName.substring(0, pathName.length()-1);
		List<double[]> resultsList = new ArrayList<>();
		List<Double> scalesList = new ArrayList<>();
		for (int i = 0; i <= 20; i++) {
			double scale = 1.0 + i*0.1;
			if (Simulations.saType == ESAType.GEOMETRIC_SA || Simulations.saType == ESAType.GEOMETRIC_TA || Simulations.saType == ESAType.GEOMETRIC_NM ) {
				;
			}
			scalesList.add(scale);
			switch (Simulations.saType) {
			case BASIC_SA: Simulations.BASIC_FINAL_TEMPERATURE_SCALE_FACTOR = scale; break;
			case CONNOLLY_SA: Simulations.CONNOLLY_FINAL_TEMPERATURE_SCALE_FACTOR = scale; break;
			case COSINE_SA: Simulations.COSINE_FINAL_TEMPERATURE_SCALE_FACTOR = scale; break;
			case MODIFIED_SA: Simulations.MODIFIED_FINAL_TEMPERATURE_SCALE_FACTOR = scale; break;
			case GEOMETRIC_SA: Simulations.GEOMETRIC_FINAL_TEMPERATURE_SCALE_FACTOR = scale; break;
			case GEOMETRIC_TA: Simulations.GEOMETRIC_TA_FINAL_THRESHOLD_SCALE_FACTOR = scale; break;
			case GEOMETRIC_NM: Simulations.GEOMETRIC_NM_FINAL_THRESHOLD_SCALE_FACTOR = scale; break;
			default: System.out.println("Error SA type: " + Simulations.saType); return;
			}
			System.out.println("\n"+Simulations.getParaSetting());
			double[] results = testPerformance(filePath, Simulations.Q, Simulations.TIMES);
			resultsList.add(results);

			if (Simulations.SAVING_PARA_TUNNING) {
				try {
					String fileName = (new File("")).getAbsolutePath() + "\\results\\Parameters\\";
					fileName += pathName + "-" + Simulations.saType + "-" + Simulations.USE_LOCAL_SEARCH + "-" + Simulations.USE_TABU_SEARCH + "-" + USE_SAME_RANDOM;
					fileName += "-Q=" + Simulations.Q + "-S=" + Simulations.INITIAL_SEED + " para tf tunning results.csv";
					PrintWriter printWriter = new PrintWriter(new FileWriter(fileName));
					for (int idx = 0; idx < resultsList.size(); idx++) {
						double[] rs = resultsList.get(idx);
						printWriter.println();
						printWriter.print(scalesList.get(idx));
						for (int j = 0; j < rs.length; j++) {
							printWriter.print(","+rs[j]);
						}
					}

					printWriter.close();
				} catch (Exception ex) {
					ex.printStackTrace();
				}
			}
		}
	}

	private static void testSingleInstance(String fileName, final int Q, final int TIMES) {
		double[] results = runSA(fileName, Q, TIMES);
		for (double d : results) {
			System.out.print(d + "\t");
		}
		System.out.println();			 
	}

	private static double[] testPerformance(String filePath, final int Q, final int TIMES) {
		java.io.File dir = new java.io.File(filePath);
		java.io.File[] files = dir.listFiles();
		String pathName = filePath.substring(filePath.lastIndexOf("/", filePath.length()-2)).substring(1);
		pathName = pathName.substring(0, pathName.length()-1);
		System.out.println(pathName);
		double[][] results = new double[files.length][];
		int i = 0;
		for (java.io.File file : files) {
			results[i] = runSA(file.getAbsolutePath(), Q, TIMES);
			System.out.println();
			System.out.print(file.getName()+"\t");
			for (double d : results[i]) {
				System.out.print(d+"\t");
			}
			i++;
		}
		double[] totals = new double[results[0].length];
		for (i = 0; i < files.length; i++) {
			System.out.println();
			System.out.print(files[i].getName()+"\t");
			for (int j = 0; j < results[i].length; j++) {
				System.out.print(results[i][j]+"\t");
				totals[j] += results[i][j];
			}
		}
		System.out.println("\t");
		for (int j = 0; j < totals.length; j++) {
			totals[j] = Math.round(totals[j]/files.length*1000)/1000.0;
			System.out.print(totals[j]+"\t");
		}
		
		if (Simulations.SAVING_FINAL_RESULTS) {
			try {
				String fileName = (new File("")).getAbsolutePath() + "\\results\\Performance\\" + pathName + "-";
				fileName += Simulations.getParaSetting();
				fileName += " results.csv";
				PrintWriter printWriter = new PrintWriter(new FileWriter(fileName));
				for (i = 0; i < files.length; i++) {
					printWriter.println();
					printWriter.print(files[i].getName());
					for (int j = 0; j < results[i].length; j++) {
						printWriter.print(","+results[i][j]);
					}
				}
				printWriter.println();
				printWriter.print("Average ");
				for (int j = 0; j < totals.length; j++) {
					printWriter.print(","+totals[j]);
				}
				printWriter.close();
			} catch (Exception ex) {
				ex.printStackTrace();
			}
		}
		return totals; //average data for all files
	}

	private static double[] runSA(String fileName, final int Q, final int TIMES) {
		double duration = (new java.util.Date()).getTime();
		Problems.setFileName(fileName);
		int bMakespan = Problems.getProblem().getBestMakespan();
		Solution s;
		int[] makespans = new int[Simulations.TIMES];
		int[] iterations = new int[Simulations.TIMES]; //last improving iteration
		Solution.setSeed(Simulations.INITIAL_SEED); //to have same initial solution
		for (int i = 0; i < Simulations.TIMES; i++) {
			s = new Solution(true);
			if (Simulations.saType == ESAType.BASIC_SA) {
				s = SAMethods.basicSA(s);
			}  else if (Simulations.saType == ESAType.CONNOLLY_SA){
				s = SAMethods.connollySA(s);
			}  else if (Simulations.saType == ESAType.COSINE_SA){
				s = SAMethods.consineSA(s);
			} else if (Simulations.saType == ESAType.MODIFIED_SA) {
				s = SAMethods.modifiedSA(s);
			} else if (Simulations.saType == ESAType.GEOMETRIC_SA ) {
				s = SAMethods.geometricSA(s); 
			} else if (Simulations.saType == ESAType.LIST_BASED_SA) {
				s = SAMethods.listBasedSA(s);
			} else if (Simulations.saType == ESAType.GEOMETRIC_TA) {
				s = SAMethods.geometricTA(s);
			}  else if (Simulations.saType == ESAType.GEOMETRIC_NM) {
				s = SAMethods.geometricNM(s);
			} else if (Simulations.saType == ESAType.GREAT_DELUGE) {
				s = SAMethods.geometricGD(s);
			} else if (Simulations.saType == ESAType.HYBRID_ANT_SYSTEM) {
				s = AntMethods.hybridAntSystem(10, 100);
			}  else if (Simulations.saType == ESAType.ROBUST_TABOO_SEARCH) {
				s = TabuMethods.robustTabuSearch(s, Simulations.Q);
			}  else {
				System.out.println("Cannot reach here!");;
			}
			makespans[i] = s.getMakespan();
			iterations[i] = s.getLastImproving();
		}
		duration = (new java.util.Date()).getTime()-duration;
		duration /= TIMES;
		duration = Math.round(duration/1000*1000)/1000.0;

		//Adjust according to population size;
		int times = TIMES;
		if (Simulations.POPULATION_SIZE > 1) {
			times = TIMES / Simulations.POPULATION_SIZE;
			for (int t = 0; t < times; t++) {
				for (int idx = 0; idx < Simulations.POPULATION_SIZE; idx++) {
					int i = t * Simulations.POPULATION_SIZE + idx;
					if ( idx == 0) {
						makespans[t] = makespans[i];
					} else if (makespans[i] < makespans[t]) {
						makespans[t] = makespans[i];
					}
				}
			}
		}
		int min = Integer.MAX_VALUE, max = Integer.MIN_VALUE, count = 0;
		double total = 0;
		double totalIterations = 0;
		for (int i = 0; i < times; i++) {
			int mk = makespans[i];
			total += mk;
			if ( (mk-bMakespan) * (1.0/bMakespan) *100 < 1) {
				count++;
			}
			if ( mk < min) {
				min = mk;
			}
			if (mk > max) {
				max = mk;
			}
			totalIterations += iterations[i];
		}
		double ave = total / times;
		double bpd = Math.round((min-bMakespan) * (1.0/bMakespan) *100*1000)/1000.0;
		double wpd = Math.round((max-bMakespan) * (1.0/bMakespan) *100*1000)/1000.0;
		double apd = Math.round((ave-bMakespan) * (1.0/bMakespan) *100*1000)/1000.0;
		double itr = Math.round(totalIterations/iterations.length*10)/10; //average last improving iteration
		return new double[] {bMakespan, min, max, ave, bpd, wpd, apd, count, itr, duration};
	}
	
	public static ESAType getSaType() { return Simulations.saType;}
	public static boolean isSavingFinalResults() { return Simulations.SAVING_FINAL_RESULTS;}
	public static boolean isSavingProcessData() { return Simulations.SAVING_PROCESS_DATA;}
	public static String getParaSetting() {
		String str = saType + "-" + USE_LOCAL_SEARCH +"-" + USE_TABU_SEARCH + "-" + USE_SAME_RANDOM + "-";
		if (saType == ESAType.BASIC_SA) {
			str += "Scale=" + BASIC_BEGIN_TEMPERATURE_SCALE_FACTOR + "-" + BASIC_FINAL_TEMPERATURE_SCALE_FACTOR;
		} else if (saType == ESAType.CONNOLLY_SA) {
			str += "Scale=" + CONNOLLY_BEGIN_TEMPERATURE_SCALE_FACTOR + "-" + CONNOLLY_FINAL_TEMPERATURE_SCALE_FACTOR;
		} else if (saType == ESAType.COSINE_SA) {
			str += "Scale=" + COSINE_BEGIN_TEMPERATURE_SCALE_FACTOR + "-" + COSINE_FINAL_TEMPERATURE_SCALE_FACTOR;
		} else if (saType == ESAType.MODIFIED_SA) {
			str += MODIFIED_LAMDA1 + "-" + MODIFIED_LAMDA2 + "-Scale=" + MODIFIED_BEGIN_TEMPERATURE_SCALE_FACTOR + "-" + MODIFIED_FINAL_TEMPERATURE_SCALE_FACTOR;
		} else if (saType == ESAType.GEOMETRIC_SA) {
			str += " Scale=" + GEOMETRIC_BEGIN_TEMPERATURE_SCALE_FACTOR + "-" + GEOMETRIC_FINAL_TEMPERATURE_SCALE_FACTOR ;
			str += " G=" + Simulations.MAX_GENERATION;
		} else if (saType == ESAType.GEOMETRIC_TA) {
			str += " Scale=" + GEOMETRIC_TA_BEGIN_THRESHOLD_SCALE_FACTOR + "-" + GEOMETRIC_TA_FINAL_THRESHOLD_SCALE_FACTOR ;
			str += " G=" + Simulations.MAX_GENERATION;
		} else if (saType == ESAType.GEOMETRIC_NM) {
			str += " Scale=" + GEOMETRIC_NM_BEGIN_THRESHOLD_SCALE_FACTOR + "-" + GEOMETRIC_NM_FINAL_THRESHOLD_SCALE_FACTOR ;
			str += " G=" + Simulations.MAX_GENERATION;
		} else if (saType == ESAType.LIST_BASED_SA) {
			str += " LS=" + LIST_LENGTH + " Weight=" + LIST_WEIGHT ;
			str += " G=" + Simulations.MAX_GENERATION;
		} 
		
		return str + " Q=" + Simulations.Q;
	}
	
	private static ESAType saType = ESAType.GREAT_DELUGE;
	
	private static final int INITIAL_SEED = 2018;//2018;
	public static final boolean USE_SAME_RANDOM = true; //to use same random in Methods and Solution, so same seed will always has same result
	public static final int Q = 50;
	public static final int TIMES = 100;
	public static final int POPULATION_SIZE = 1;
		
	public static final boolean SAVING_PROCESS_DATA = false;
	public static final boolean SAVING_FINAL_RESULTS = false;
	public static final boolean SAVING_PARA_TUNNING = true;
	public static final ETestType TEST_TYPE = ETestType.PARAMETER_FINAL_TEMPERATURE_TUNNING;
	
	public static final boolean USE_LOCAL_SEARCH = false;
	public static final boolean USE_TABU_SEARCH = false;
	
	//parameters for homogeneous SA
	public static final int MAX_GENERATION = 1000; //1000
	
	//parameters for list-based SA algorithm 
	public static final int LIST_MUTATION_STRENGTH = 1;
	public static final int LIST_LENGTH = 200; //200
	public static final double LIST_WEIGHT = 0.3;
	
	//parameters for geometric SA algorithm  
	public static final int GEOMETRIC_MUTATION_STRENGTH = 1;
	public static double GEOMETRIC_BEGIN_TEMPERATURE_SCALE_FACTOR = 1;
	public static double GEOMETRIC_FINAL_TEMPERATURE_SCALE_FACTOR = 5;
	
	//parameters for basic SA algorithm
	public static double BASIC_BEGIN_TEMPERATURE_SCALE_FACTOR = 1.0;
	public static double BASIC_FINAL_TEMPERATURE_SCALE_FACTOR = 1.8;//p32=1.8; type1 = 0.5; type2 = 1.6; type3=2.7;type4 = 1.2
	
	//parameters for connolly SA algorithm
	public static double CONNOLLY_BEGIN_TEMPERATURE_SCALE_FACTOR = 1.0;
	public static double CONNOLLY_FINAL_TEMPERATURE_SCALE_FACTOR = 1.9;//p32=1.8; type1 = 1.8; type2 = 1.6; type3=3; type4 = 1.2
	
	//parameters for cosine SA algorithm
	public static double COSINE_BEGIN_TEMPERATURE_SCALE_FACTOR = 1.0;
	public static double COSINE_FINAL_TEMPERATURE_SCALE_FACTOR = 2.0;//p32=1.8; typ1 =1.4; type2 = 1.6;  type3=2.8; type4 = 1.2
	
	//parameters for modified SA algorithm
	public static double MODIFIED_BEGIN_TEMPERATURE_SCALE_FACTOR = 1.0;
	public static double MODIFIED_FINAL_TEMPERATURE_SCALE_FACTOR = 1.8;//p32=1.8; typ1=0.8; type2 = 1.6; type3 = 2.7; type4 = 1.2
	public static final double MODIFIED_LAMDA1 = 0.5;
	public static final double MODIFIED_LAMDA2 = 0.05;
	
	//parameters for geometric TA algorithm  
	public static final int GEOMETRIC_TA_MUTATION_STRENGTH = 1;
	public static double GEOMETRIC_TA_BEGIN_THRESHOLD_SCALE_FACTOR = 1;
	public static double GEOMETRIC_TA_FINAL_THRESHOLD_SCALE_FACTOR = 5;	
	
	//parameters for geometric NM algorithm  
	public static final int GEOMETRIC_NM_MUTATION_STRENGTH = 1;
	public static double GEOMETRIC_NM_BEGIN_THRESHOLD_SCALE_FACTOR = 0.8;
	public static double GEOMETRIC_NM_FINAL_THRESHOLD_SCALE_FACTOR = 1;	
}
