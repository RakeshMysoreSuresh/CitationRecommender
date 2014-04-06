import java.io.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;
import java.util.TreeSet;

import org.apache.lucene.search.TotalHitCountCollector;

/**
 * 
 */

/**
 * @author Rakesh_BCKUP
 *
 */
public class Evaluation extends Config{
	
	double[] precision, recall, fMeasure, bpRef, reciprocalRank, mRR;
	double[] avgPrecision, avgRecall, avgFMeasure, avgBpRef, avgReciprocalRank, avgMRR;
	int totalRuns = 0;
	double correctRec;
	public static final int FIVE=0,
							TEN =1,
							TWENTY=2,
							FORTY=3,
							EIGHTY=4;
	public static final int[] LOOKUP = new int[]{5, 10, 20, 40, 80};
	int totalQueries = 0;
	private Set<Integer> rec, actual;
	private int[] recArray;
	Integer[] actualArray;
	//Temporary fields
	HashMap<Integer, Integer> index = new HashMap<>();
	int firstRecIndex = -1;
	int[] lastIndexes = new int[]{4,9,19,39,79};		
	RefRec ref = new RefRec();;
	
	public Evaluation() {
		precision = new double[5];
		recall = new double[5];
		fMeasure = new double[5];
		bpRef = new double[5];
		mRR = new double[5];
		reciprocalRank = new double[5];
		
		avgPrecision = new double[5];
		avgRecall = new double[5];
		avgFMeasure = new double[5];
		avgBpRef = new double[5];
		avgMRR = new double[5];
		avgReciprocalRank = new double[5]; 
		
	}
	/**
	 * @param args
	 * @throws IOException 
	 * @throws ClassNotFoundException 
	 */
	public static void main(String[] args) throws ClassNotFoundException, IOException {
		
		Evaluation ev = new Evaluation();
		BufferedReader idReader = new BufferedReader(new FileReader(ID));
		BufferedReader	contextReader = new BufferedReader(new FileReader(CONTEXT));
		BufferedReader	actualRefReader = new BufferedReader(new FileReader(DATASET_DIR + CITED_PAPERS_FILENAME));
		BufferedReader	bowReader = new BufferedReader(new FileReader(DATASET_DIR + BAG_OF_WORDS_FILENAME));
		// Sample citation context
		//"myopia affects approximately 25% of adult Americans[2]. Ethnic diversity appears to distinguish different groups with regard to prevalence. Caucasians have a higher prevalence than African Americans=-=[3]-=-. Asian populations have the highest prevalence rates with reports ranging from 50-90%[1, 4-5]. Jewish Caucasians, one of the target populations of the present study, have consistently demonstrated a ";
		//---------------------------------------------
			 
		String curPaperId, context,actualRef=null;
		String previousId = "";	
		String bagOfWords = null;
		int i = 0;
		int citedPaperIndex = 0;
		
		//Read File Line By Line
		while (((curPaperId = idReader.readLine()) != null) &&   ((context = contextReader.readLine()) != null) && citedPaperIndex < 7000) {
			if (!curPaperId.equals(previousId)) // TODO i
			{
				actualRef = actualRefReader.readLine();
				bagOfWords = bowReader.readLine();
				citedPaperIndex++;
			} 
			int percent = 0;
			if (i % 1000 == 0) 
			{
				try 
				{
					int[] recoIDS;
					String[] arr = actualRef.split(" ");
					if (arr.length > 5) {
						Integer[] actualRefId = new Integer[arr.length];
						for (int j = 0; j < arr.length; j++) {
							actualRefId[j] = Integer.parseInt(arr[j]);
						}
						recoIDS = ev.ref.rankPapers(context, arr);
						if(recoIDS != null)
						{
							ev.evaluate(recoIDS, actualRefId);
							ev.updateMetrics();
							ev.reportMetrics();
						}
						else
						{
							System.out.println("Ignoring query due low confidence:\n\t\t"+context);
						}
					}
				} catch (IOException e) {	e.printStackTrace(); }
				//System.out.println("Completed "+(percent++)+"%");
			}
			previousId = curPaperId;
			i++;
		}
		ev.reportAvgMetrics();
		RefRec.service.shutdown();
	//-------------------------------
	}
	
	public void updateMetrics() {
		for(int k=0; k<5; k++){
			avgPrecision[k] += precision[k];
			avgRecall[k] += recall[k]; 
			avgFMeasure[k] += fMeasure[k]; 
			avgBpRef[k] += bpRef[k]; 
			avgReciprocalRank[k] += reciprocalRank[k]; 
			avgMRR[k] += mRR[k];
		}
		totalRuns++;
	}
	
	public void reportMetrics() throws IOException {	
//		ref.printer.println("precision for "+LOOKUP[0]+" recommendations: "+precision[0]);
		ref.printer.println("recall for "+LOOKUP[1]+" recommendations: "+recall[0]);
		ref.printer.println("fmeasure for "+LOOKUP[1]+" recommendations: "+fMeasure[0]);
//		ref.printer.println("avgBpRef for "+LOOKUP[0]+" recommendations: "+bpRef[0]);
//		ref.printer.println("avgReciprocalRank for "+LOOKUP[0]+" recommendations: "+reciprocalRank[0]);
//		ref.printer.println("avgMRR for "+LOOKUP[0]+" recommendations: "+mRR[0]);
		
		//System.out.println("precision for "+LOOKUP[0]+" recommendations: "+precision[0]);
		//System.out.println("recall for "+LOOKUP[0]+" recommendations: "+recall[0]);
		System.out.println("fmeasure for "+LOOKUP[1]+" recommendations: "+fMeasure[0]);
		//System.out.println("avgBpRef for "+LOOKUP[0]+" recommendations: "+bpRef[0]);
		//System.out.println("avgReciprocalRank for "+LOOKUP[0]+" recommendations: "+reciprocalRank[0]);
		//System.out.println("avgMRR for "+LOOKUP[0]+" recommendations: "+mRR[0]);
	}
	
	public void reportAvgMetrics() throws IOException {	
		PrintWriter writer = new PrintWriter(new FileWriter(DATASET_DIR + CITATION_RECOMMENDATION_REPORT, true));;
		writer.println("~~~~~~~~~~~~~~~~~~~~~~~~~~ Average metrics ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~");
		for(int k=0; k<5; k++){
			System.out.println("Avg precision for "+LOOKUP[k]+" recommendations: "+avgPrecision[k]/totalQueries);
			System.out.println("Avg recall for "+LOOKUP[k]+" recommendations: "+avgRecall[k]/totalQueries);
			System.out.println("Avg fmeasure for "+LOOKUP[k]+" recommendations: "+avgFMeasure[k]/totalQueries);
			System.out.println("Avg avgBpRef for "+LOOKUP[k]+" recommendations: "+avgBpRef[k]/totalQueries);
			System.out.println("Avg avgReciprocalRank for "+LOOKUP[k]+" recommendations: "+avgReciprocalRank[k]/totalQueries);
			System.out.println("Avg avgMRR for "+LOOKUP[k]+" recommendations: "+avgMRR[k]/totalQueries);
			
			
			writer.println("Avg precision for "+LOOKUP[k]+" recommendations: "+avgPrecision[k]/totalQueries);
			writer.println("Avg recall for "+LOOKUP[k]+" recommendations: "+avgRecall[k]/totalQueries);
			writer.println("Avg fmeasure for "+LOOKUP[k]+" recommendations: "+avgFMeasure[k]/totalQueries);
			writer.println("Avg avgBpRef for "+LOOKUP[k]+" recommendations: "+avgBpRef[k]/totalQueries);
			writer.println("Avg avgReciprocalRank for "+LOOKUP[k]+" recommendations: "+avgReciprocalRank[k]/totalQueries);
			writer.println("Avg avgMRR for "+LOOKUP[k]+" recommendations: "+avgMRR[k]/totalQueries);
			
		}
	}
	
	private void calcMRR() {
		for (int i = 0; i < mRR.length; i++) {
			mRR[i] = reciprocalRank[i] / totalQueries;
		}
	}
	public void evaluate(int[] recommRef, Integer[] actualRef){
		rec = new TreeSet<Integer>();
		actual = new TreeSet<Integer>(Arrays.asList(actualRef));
		//recArray = new int[recommRef.size()];
		recArray = recommRef; actualArray = actualRef;
		createLookupHash();	
		int correctRec, tempPrec, tempRec;
		for (int i = 0; i < recommRef.length; i++) {
			rec.add(recommRef[i]);
			switch (i) {
			case 4:
				calculate(i, FIVE);
				break;
			case 9:
				calculate(i, TEN);
				break;
			case 19:
				calculate(i, TWENTY);
				break;
			case 39:
				calculate(i, FORTY);
				break;
			case 79:
				calculate(i, EIGHTY);
				break;
			default:
				break;
			}			
		}
		rec.clear();
		actual.clear();
		totalQueries++;
		firstRecIndex = -1;
	}
	
	private void createLookupHash() {
		for (int i = 0; i < recArray.length; i++) {
			index.put(recArray[i], i);
			if(!(firstRecIndex>-1) && actual.contains(recArray[i])){
				firstRecIndex = i;
			}
		}
	}
	public void calculate(int i, final int numRec) {
		//TODO bug : rec clears after calculated, BPref problem
		double tempPrec;
		double tempRec;
		bpRef[numRec] = bPref(rec, actual);
		if (firstRecIndex < lastIndexes[numRec]){
			reciprocalRank[numRec] += 1/(double)firstRecIndex;
		}
		rec.retainAll(actual);
		correctRec = rec.size();
		tempPrec = correctRec/(i+1);
		tempRec = correctRec/Math.min(i+1, actual.size());
		precision[numRec] = tempPrec;
		recall[numRec] = tempRec;
		if (tempPrec + tempRec > 0) {
			fMeasure[numRec] = (2.0 * tempPrec * tempRec) / (tempPrec + tempRec);
		}	
		else{
			fMeasure[numRec] = 0;
		}
	}
	
	private double bPref(Set<Integer> recommRef, Set<Integer> actualRef){
		
		double sum = 0;
		int count = 0;
		double currBPRef;
		for(int ref : actualRef){
			Integer refIndex = index.get(ref);
			if (refIndex != null) {
				int diff = refIndex - count;
				count++;
				currBPRef = (1 - diff / recommRef.size());
				if (currBPRef < 0) {
					currBPRef = 0;
				}
				sum += currBPRef;
			}
		}
		sum = sum/actualRef.size();
		return sum;
	}
	
	


}
