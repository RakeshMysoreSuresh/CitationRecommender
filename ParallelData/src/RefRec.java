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
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.en.EnglishAnalyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import org.apache.lucene.util.Version;

/**
 * 
 */

/**
 * @author rakesh
 *
 */

public class RefRec extends Config{
	
	static final int numContexts = 1200000;
	ContextFreq[] contextFreq;
	final TTable table;
	double[] prob = new double[1200000];
	HashMap<String, Integer> queryTokens = new HashMap<>();
	HashMap<String,Integer> wordsToID = new HashMap<>();	
	ArrayList<ArrayList<Integer>> citingLikelihood = new ArrayList<ArrayList<Integer>>();
	ArrayList<String> titleList;
	double[] buckets = new double[]{1,0.1,0.001};//,0.0001,0.00001};
	int[] recommendedIds = new int[MAX_RECOM];
	String[] recommendations = new String[MAX_RECOM];
	public static final int MAX_RECOM = 100;
	int[] titleVCBMap;
	PatternMatch preprocessor = new PatternMatch();
	
	static final ExecutorService service  = Executors.newFixedThreadPool(2);
	static final int totalWords = TTable.findNum(BAG_OF_WORDS_VCB); 
	static final int totalTitles = TTable.findNum(CITED_PAPERS_VCB);
			
	public RefRec(){
		try {
			//testQueryTokenization();
			Future<TTable> ttableFuture = readTTable();
			File cf = new File(ICF_ARRAY);
			if (cf.exists()) {
				contextFreq = readICF();
				wordsToID = readWordsToID();
			}
			else{
				storeDataStruct(BAG_OF_WORDS_VCB);
			}
			for (int i = 0; i < buckets.length; i++) {
				citingLikelihood.add(new ArrayList<Integer>());
			}
			//Thread ttableReader = new Thread(new Runn)
			
			titleList= readTitleList();
			titleVCBMap = readVCBTitleBridge();
			table = ttableFuture.get(); 
		} catch (IOException | ClassNotFoundException | InterruptedException | ExecutionException e) {
			throw new RuntimeException(e);
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
		System.out.println("Recommending papers");
		PrintWriter printer = new PrintWriter(new FileWriter(DATASET_DIR + "Citation Recommendation report.txt"));
		preprocessor.preProcessQuery(query);
		Analyzer analyzer = new StandardAnalyzer(Version.LUCENE_36);
		TokenStream tokenStream = analyzer.tokenStream("query", new StringReader(preprocessor.otherPOSBuffer.toString()));
		CharTermAttribute token = tokenStream.getAttribute(CharTermAttribute.class);
		List<String> tokenList = new ArrayList<>();
		tokenList.addAll(Arrays.asList(preprocessor.properNounBuffer.toString().split(" ")));
		while(tokenStream.incrementToken()){
			tokenList.add(token.toString());
		}
		
		for (String word : tokenList) {
			Integer occurrences;
			if ((occurrences = queryTokens.get(word)) == null) {
				queryTokens.put(word, 1);
			} else {
				queryTokens.put(word, occurrences + 1);
			}
		}
		
		Long currTime = System.currentTimeMillis();
		Set<String> queryTokenSet = queryTokens.keySet();
		for(int paper : table.titles){
			for(String word : queryTokenSet){
				Integer wordID = wordsToID.get(word);
				if(wordID != null){
					double tValue = table.getTValue(wordID, paper);
					int termFreq = queryTokens.get(word);
					double icf = (contextFreq[wordID].icf);
					double TF_ICF = termFreq*icf;
					prob[paper] += tValue*TF_ICF;
					//System.out.println("TF-ICF of " + word + " = "+TF_ICF);
				}
			}
			bucket(paper, prob[paper]);			
		}
		 int[] recoIDS1 = new int[MAX_RECOM];
		recommend();
		
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
		printer.close();		
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
				recommendations[numRec] = titleList.get(titleVCBMap[curList.get(j)]);
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
	void storeDataStruct(String fileName) throws IOException{
		contextFreq = new ContextFreq[totalWords + 100];
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
			if ((count%(totalWords/10)) == 0) {
				percent=count/6000;
				System.out.println("Calculating ICF: " + percent+"%");
			}
			/*if(percent > 96)
				System.out.println(count);*/
			count++;

		}
		ObjectOutputStream out = new ObjectOutputStream(new FileOutputStream(ICF_ARRAY));
		out.writeObject(contextFreq);
		out.close();
		out = new ObjectOutputStream(new FileOutputStream(WORDS2ID_LIST));
		out.writeObject(wordsToID);
		out.close();
		r.close();
	}
	
	ContextFreq[] readICF() throws ClassNotFoundException, IOException{
		System.out.println("Deserializing ICF Array, Time: " + (System.currentTimeMillis() - START_TIME));
		return (ContextFreq[])new ObjectInputStream(new FileInputStream(ICF_ARRAY)).readObject();
	}
	
	HashMap<String, Integer> readWordsToID() throws ClassNotFoundException, IOException{
		System.out.println("Deserializing words2Id, Time: " + (System.currentTimeMillis() - START_TIME));
		return (HashMap<String, Integer>)new ObjectInputStream(new FileInputStream(WORDS2ID_LIST)).readObject();
	}
	
	Future<TTable> readTTable() throws ClassNotFoundException, IOException{
		System.out.println("Deserializing translation table, Time: " + (System.currentTimeMillis() - START_TIME));
		return service.submit(
				new Callable<TTable>() 
				{
					@Override
					public TTable call() throws Exception {
						return (TTable)new ObjectInputStream(new FileInputStream(TRANSLATION_TABLE_SER)).readObject();
				}
		});
		
	}
	
	ArrayList<String> readTitleList() throws ClassNotFoundException, IOException{
		System.out.println("Deserializing title list, Time: " + (System.currentTimeMillis() - START_TIME));
		return (ArrayList<String>)new ObjectInputStream(new FileInputStream(TITLE_LIST)).readObject();
	}
	
	int[] readVCBTitleBridge() throws ClassNotFoundException, IOException{
		
		File f = new File(CITED_PAPERS_VCB);
		if (!f.exists()) {
			BufferedReader r = new BufferedReader(new FileReader(CITED_PAPERS_VCB.trim()));
			String s;
			String[] spl;
			//To remove null
			r.readLine();
			titleVCBMap = new int[totalTitles + 100];
			while ((s = r.readLine()) != null) {
				spl = s.split(" ");
				int index = Integer.parseInt(spl[0]), val = Integer
						.parseInt(spl[1]);
				titleVCBMap[index] = val;
			}
			r.close();
			ObjectOutputStream stream = new ObjectOutputStream(
					new FileOutputStream(CITED_VCB_PAPER_ID_TO_TITLE_SER_MAP));
			stream.writeObject(titleVCBMap);
			stream.close();
			return titleVCBMap;
		}
		else
		{
			System.out.println("Deserializing vcb-title bridge");
			return (int[])new ObjectInputStream(new FileInputStream(CITED_VCB_PAPER_ID_TO_TITLE_SER_MAP)).readObject();
		}
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
