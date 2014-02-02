import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.PrintWriter;
import java.io.StringReader;
import java.sql.Ref;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Set;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.en.EnglishAnalyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
//import org.apache.lucene.search.QueryTermVector;
import org.apache.lucene.util.Version;

/**
 * 
 */

/**
 * @author rakesh
 *
 */

public class RefRec {
	
	static final int numContexts = 1200000;
	ContextFreq[] contextFreq;
	TTable table;
	double[] prob = new double[1200000];
	HashMap<Integer, Integer> queryTokens = new HashMap<Integer, Integer>();
	HashMap<String,Integer> wordsToID = new HashMap<>();	
	ArrayList<ArrayList<Integer>> citingLikelihood = new ArrayList<ArrayList<Integer>>();
	ArrayList<String> titleList;
	double[] buckets = new double[]{1,0.1,0.001};//,0.0001,0.00001};
	int[] recommendedIds = new int[MAX_RECOM];
	String[] recommendations = new String[MAX_RECOM];
	public static final int MAX_RECOM = 100;
	int[] titleVCBMap;
	
			
	public RefRec(){
		try {
			//testQueryTokenization();
			table = readTTable();
			contextFreq = readICF();
			wordsToID = readWordsToID();
			for (int i = 0; i < buckets.length; i++) {
				citingLikelihood.add(new ArrayList<Integer>());
			}
			titleList= readTitleList();
			titleVCBMap = readVCBTitleBridge();
			
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
	}
	/**
	 * @param args
	 * @throws IOException 
	 */
	public static void main(String[] args) throws Exception {
		RefRec ref = new RefRec();
		String query = "myopia affects approximately 25% of adult Americans[2]. Ethnic diversity appears to distinguish different groups with regard to prevalence. Caucasians have a higher prevalence than African Americans=-=[3]-=-. Asian populations have the highest prevalence rates with reports ranging from 50-90%[1, 4-5]. Jewish Caucasians, one of the target populations of the present study, have consistently demonstrated a ";
		ref.rankPapers(query);
	}

	public int[] rankPapers(String query) throws ClassNotFoundException, IOException {
		//storeDataStruct("", "ContextStop.vcb");	
		Analyzer analyzer = new StandardAnalyzer(Version.LUCENE_36);
		TokenStream tokenStream = analyzer.tokenStream("query", new StringReader(query));
		CharTermAttribute token = tokenStream
				.getAttribute(CharTermAttribute.class);
		while(tokenStream.incrementToken()){
			String word = token.toString(); 
			Integer temp = wordsToID.get(word);
			if (temp!=null) {
				Integer occrces;
				if ((occrces = queryTokens.get(temp)) == null) {
					queryTokens.put(temp, 1);
				} else {
					queryTokens.put(temp, occrces + 1);
				}
			}
		}
		Long currTime = System.currentTimeMillis();
		Set<Integer> queryTokenSet = queryTokens.keySet();
		for(int paper : table.titles){
			for(Integer wordID : queryTokenSet){	
				if(wordID != null){
					double tValue = table.getTValue(wordID, paper);
					int termFreq = queryTokens.get(wordID);
					double icf = (contextFreq[wordID].icf);
					prob[paper] += tValue*termFreq*icf;
				}
			}
			bucket(paper, prob[paper]);			
		}
		 int[] recoIDS1 = new int[MAX_RECOM];
		recommend();
		PrintWriter printer = new PrintWriter(new FileWriter("Citation Recommendation report.txt"));
		printer.println("Time for "+table.titles.size()+" paper= "+
		(System.currentTimeMillis()-currTime));
		
		printer.println("Recommendations: IDs");
		for (int i = 0; i < MAX_RECOM; i++) {
			recoIDS1[i] = recommendedIds[i];
			printer.print(recommendedIds[i] + " ");
		}
		printer.println("");
		for (int i = 0; i < MAX_RECOM; i++) {
			printer.println(i + ". " + recommendations[i]+": "+prob[recommendedIds[i]]);
		}
				
		return recoIDS1;
	}
	
	private void recommend() {
		
		Comparator<Integer> paperComparator = new Comparator<Integer>() {
			@Override
			public int compare(Integer o1, Integer o2) {
				return  (int)Math.round((prob[o2]-prob[o1])/buckets[buckets.length - 1]);
			}
		};
		int numRec = 0;
		for (int i = 0; i < buckets.length; i++) {
			ArrayList<Integer > curList = citingLikelihood.get(i);
			//Sort in descending order
			Collections.sort(curList,paperComparator);
			for(int j =0;j<curList.size(); j++){
				recommendedIds[numRec] = curList.get(j);
				recommendations[numRec] = titleList.get(
						titleVCBMap[curList.get(j)]);
				numRec++;
				if(numRec>MAX_RECOM-1)
					return;
			}
		}
		System.out.println("Done");
	}
	
	private void bucket(int paper, double d) {
		for (int i = 0; i < buckets.length; i++) {
			if (d> buckets[i]) {
				citingLikelihood.get(i).add(paper);
				return;
			}
		}
	}
	void storeDataStruct(String path, String fileName) throws IOException{
		contextFreq = new ContextFreq[TTable.findNumWords(fileName)];
		String[]arr;
		String temp;
		int index, freq; 
		int count = 0, percent =0;
		double icf;
		BufferedReader r = new BufferedReader(new FileReader(fileName));
		while ((temp = r.readLine())!=null) {
			arr = temp.split(" ");
			index = Integer.parseInt(arr[0]);
			String word = arr[1];
			freq =  Integer.parseInt(arr[2]);
			if(wordsToID.get(word)==null){
				wordsToID.put(word, index);
			}
			icf = Math.log(((double)numContexts)/freq);
			contextFreq[index] = new ContextFreq(word, freq, icf);
			if ((count%6000) == 0) {
				percent=count/6000;
				System.out.println(percent+"%");
			}
			if(percent > 96)
				System.out.println(count);
			count++;

		}
		ObjectOutputStream out = new ObjectOutputStream(new FileOutputStream("ICFArray.ser"));
		out.writeObject(contextFreq);
		out.close();
		out = new ObjectOutputStream(new FileOutputStream("WordsToID.ser"));
		out.writeObject(wordsToID);
		out.close();
		r.close();
	}
	
	ContextFreq[] readICF() throws ClassNotFoundException, IOException{
		return (ContextFreq[])new ObjectInputStream(new FileInputStream("ICFArray.ser")).readObject();
	}
	
	HashMap<String, Integer> readWordsToID() throws ClassNotFoundException, IOException{
		return (HashMap<String, Integer>)new ObjectInputStream(new FileInputStream("WordsToID.ser")).readObject();
	}
	
	TTable readTTable() throws ClassNotFoundException, IOException{
		return (TTable)new ObjectInputStream(new FileInputStream("TTable.ser2")).readObject();
	}
	
	ArrayList<String> readTitleList() throws ClassNotFoundException, IOException{
		return (ArrayList<String>)new ObjectInputStream(new FileInputStream("TitleList.ser")).readObject();
	}
	
	int[] readVCBTitleBridge() throws ClassNotFoundException, IOException{
		return (int[])new ObjectInputStream(new FileInputStream("VCBTitleBridge.ser")).readObject();
	}
	
	void testQueryTokenization() throws IOException{
		String query = "myopia affects approximately 25% of adult Americans[2]. Ethnic div-=-ersity appears to distinguish different groups with regard to prevalence. Caucasians have a higher prevalence than African Americans=-=[3]-=-. Asian populations have the highest prevalence rates with reports ranging from 50-90%[1, 4-5]. Jewish Caucasians, one of the target populations of the present study, have consistently demonstrated a ";
		Analyzer analyzer = new StandardAnalyzer(Version.LUCENE_36);
		TokenStream tokenStream = analyzer.tokenStream("query", new StringReader(query));
		CharTermAttribute token = tokenStream
				.getAttribute(CharTermAttribute.class);
		while(tokenStream.incrementToken()){
			System.out.println(token.toString());
		}
	}	
}
