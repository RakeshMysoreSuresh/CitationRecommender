import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

/**
 * 
 */

/**
 * @author Rakesh_BCKUP
 *
 */
public class Evaluation {
	
	double[] precision, recall, fMeasure, bpRef, reciprocalRank, mRR;
	double correctRec;
	public static final int FIVE=0,
							TEN =1,
							TWENTY=2,
							FORTY=3,
							EIGHTY=4;
	int totalQueries = 0;
	private Set<Integer> rec, actual;
	private int[] recArray;
	Integer[] actualArray;
	//Temporary fields
	HashMap<Integer, Integer> index = new HashMap<>();
	int firstRecIndex = -1;
	int[] lastIndexes = new int[]{4,9,19,39,79};			
	
	public Evaluation() {
		precision = new double[5];
		recall = new double[5];
		fMeasure = new double[5];
		bpRef = new double[5];
		mRR = new double[5];
		reciprocalRank = new double[5]; 
	}
	/**
	 * @param args
	 * @throws IOException 
	 * @throws ClassNotFoundException 
	 */
	public static void main(String[] args) throws ClassNotFoundException, IOException {
		Evaluation ev = new Evaluation();
		RefRec ref = new RefRec();
		String query = "myopia affects approximately 25% of adult Americans[2]. Ethnic diversity appears to distinguish different groups with regard to prevalence. Caucasians have a higher prevalence than African Americans=-=[3]-=-. Asian populations have the highest prevalence rates with reports ranging from 50-90%[1, 4-5]. Jewish Caucasians, one of the target populations of the present study, have consistently demonstrated a ";
		ref.rankPapers(query);
		System.exit(0);
		//Run evaluation
		ev.evaluate(new int[]{3904,9522,2931,9679,3491,12,3623,796,4196,10340,3424,3645,11,8295,4088,2886,12346,6296,3597,8290,449,8085,6884,547,2586,12350,2055,2,10,546,4912,4501,3596,50,5307,3605,466,2199,1098,2930,3636,8288,8660,3487,11470,9697,2059,7196,3558,3606,5025,1192,9652,12348,325,3614,1443,10632,13,2054,54,195,8314,134,1157,7213,2584,9695,3588,8296,2652,140,8661,3433,80,3141,7455,135,7190,3,4,5,6,7,8,9,48,1819,3345,1378,7195,12349,3555,1170,7429,11876,3616,3607,138,7430}
				, new Integer[]{2,3,4,5,6,7,8,9,10,11,12});
		ev.evaluate(new int[]{186,480,249,2189,977,923,5,443,10,558,165,465,504,648,320,7,5036,24089,9,5894,5884,940,116,3,935,463,442,11,434,677,469,441,475,755,533,866,852,479,460,723,726,727,647,721,653,646,654,949,947,420,2,21991,525,314,9438,458,890,309,770,642,9036,9177,17071,497,13,1129,760,1126,1125,1149,1167,929,467,351,350,372,371,379,352,361,363,118,456,103,515,310,316,291,221,696,1127,8,21985,21988,21983,21989,21986,21987,21990,248}
		, new Integer[]{2,3,4,5,6,7,8,9,10,11,12});
		ev.calcMRR();
		System.out.println("Done");		
	}
	
	private void calcMRR() {
		for (int i = 0; i < mRR.length; i++) {
			mRR[i] = reciprocalRank[i] / totalQueries;
		}
	}
	public void evaluate(int[] recommRef, Integer[] actualRef){
		rec = new HashSet<Integer>();
		actual = new HashSet<Integer>(Arrays.asList(actualRef));
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
		double tempPrec;
		double tempRec;
		bpRef[numRec] = bPref(rec, actual);
		if (firstRecIndex < lastIndexes[numRec]){
			reciprocalRank[numRec] += 1/(double)firstRecIndex;
		} 
		rec.retainAll(actual);
		correctRec = rec.size();
		tempPrec = correctRec/i;
		tempRec = correctRec/actual.size();
		precision[numRec] = tempPrec;
		recall[numRec] = tempRec;
		if (tempPrec + tempRec >0) {
			fMeasure[numRec] = (2.0 * tempPrec * tempRec)
					/ (tempPrec + tempRec);
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
			
			int diff = index.get(ref) - count;
			count++;
			currBPRef = (1- diff/recommRef.size());
			if(currBPRef<0){
				currBPRef = 0;
			}
			sum += currBPRef;
		}
		sum = sum/actualRef.size();
		return sum;
	}
	
	


}
