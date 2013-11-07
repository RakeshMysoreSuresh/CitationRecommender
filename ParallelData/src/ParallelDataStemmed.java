/**
 * 
 */
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.LowerCaseTokenizer;
import org.apache.lucene.analysis.StopFilter;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.en.EnglishAnalyzer;
import org.apache.lucene.analysis.en.EnglishMinimalStemFilter;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import org.apache.lucene.util.Version;

/**
 * @author Rakesh_BCKUP
 * 
 */
public class ParallelDataStemmed {

	private static BufferedReader idReader;
	// private static BufferedReader titleReader;
	private static BufferedReader contextReader;
	private static BufferedReader properNounReader;
	private static PrintWriter wordsWriter;
	private static PrintWriter titleWriter;
	private Set<String> bagOfWords;
	private String stoppedWriter;
	private String stemmedWriter;
	private BufferedReader titleReader;

	// private static PrintWriter titleWriter;
	// private static HashMap<String, Integer> titlesMap;
	/**
	 * @param args
	 * @throws IOException
	 * @throws ClassNotFoundException
	 */

	public static void main(String[] args) throws IOException,
			ClassNotFoundException {

		ParallelDataStemmed parallel = new ParallelDataStemmed(new String[]
				{"ID.txt", "Title.txt", "Context_OtherPOS.txt", "BagOfWordsPOSTag.txt", 
				"CitedPapersPOSTag.txt"}, "Context_ProperNouns.txt");

		parallel.generate();
		parallel.stemTerm(parallel.stoppedWriter);

	}

	public void generate() throws IOException {
		int numCitation = 0;
		String curPaperId, context, title, noun;
		String previousId = "";

		curPaperId = idReader.readLine(); // read the column name
		context = contextReader.readLine(); // read the column name
		// title = titleReader.readLine(); // read the column name
		if(properNounReader!=null){
			noun = properNounReader.readLine();//Dummy read
		}
		System.out.println(curPaperId);
		long startTime = 0, endTime = 0;
		startTime = System.currentTimeMillis();
		int percentage = 0;
		while ((curPaperId = idReader.readLine()) != null) {
			numCitation++;

			if (0 == numCitation % 12000) {
				endTime = System.currentTimeMillis();
				System.out.println("Time taken for " + (++percentage) + "% = "
						+ ((endTime - startTime) / 1000));
				// break;
			}
			if (curPaperId.equals(previousId)) {

				// Read one citation context and get bag of words
				context = contextReader.readLine();
				removeStopWords(context);

			} else {
				if (bagOfWords.size() != 0) {
					// write bagsOfWords to a file
					// write citedPaperIds to a file
					writeToFile(bagOfWords, wordsWriter);
				}
				// Initialize Sets to read the new Paper citations
				bagOfWords.clear();
				context = context.substring(context.indexOf(' '), context.lastIndexOf(' '));
				removeStopWordsStem(context);
				if(properNounReader!=null){
					noun = properNounReader.readLine();
					bagOfWords.addAll(Arrays.asList(noun.split(" ")));
				}	
			}
			previousId = curPaperId;
		}

		// Write the last set of values
		if (bagOfWords != null) {
			// write bagsOfWords to a file
			// write citedPaperIds to a file
			writeToFile(bagOfWords, wordsWriter);
		}
		wordsWriter.close();
	}

	public ParallelDataStemmed(String[] args, String propNounFile)
			throws IOException {

		// Buffered Readers
		idReader = new BufferedReader(new FileReader(args[0]));
		titleReader = new BufferedReader(new FileReader(args[1]));
		contextReader = new BufferedReader(new FileReader(args[2]));
		properNounReader = new BufferedReader(new FileReader(propNounFile));
		// Buffered Writers
		wordsWriter = new PrintWriter(new FileWriter(args[3]));
		titleWriter = new PrintWriter(new FileWriter(args[4]));

		bagOfWords = new HashSet<String>();
	}

	private static void writeToFile(Set<String> bagOfWords, PrintWriter writer) {

		Iterator<String> it = bagOfWords.iterator();
		String write = "";
		while (it.hasNext()) {
			write = write + " " + it.next();
		}
		writer.println(write);
	}

	public void removeStopWords(String input) throws IOException {
		// input string
		Version matchVersion = Version.LUCENE_35; // Substitute desired Lucene
													// version for XY
		Analyzer analyzer = new StandardAnalyzer(matchVersion); // or any other
																// analyzer
		TokenStream tokenStream = analyzer.tokenStream("test",
				new StringReader(input));

		// tokenStream = new EnglishMinimalStemFilter(tokenStream);
		// remove stop words
		/*
		 * tokenStream = new StopFilter(Version.LUCENE_35, tokenStream,
		 * EnglishAnalyzer.getDefaultStopSet());
		 */

		// retrieve the remaining tokens
		CharTermAttribute token = tokenStream
				.getAttribute(CharTermAttribute.class);

		while (tokenStream.incrementToken()) {
			bagOfWords.add(token.toString());
		}

		tokenStream.end();
		tokenStream.close();
		analyzer.close();

	}

	void stemTerm(String path) throws IOException {

		long startTime = 0, endTime = 0;
		startTime = System.currentTimeMillis();
		Version matchVersion = Version.LUCENE_35; // Substitute desired Lucene
		// version for XY
		Analyzer analyzer = new StandardAnalyzer(matchVersion); // or any other
		// analyzer
		BufferedReader contextReader = new BufferedReader(new FileReader(path));
		String s;
		int count=0,percentage=0;
		while ((s = contextReader.readLine()) != null) {
			bagOfWords.clear();
			s = s.substring(s.indexOf(' '), s.lastIndexOf(' '));
			// TokenStream tokenStream = analyzer.tokenStream("test",
			// new StringReader(s));

			if (0 == count % 500) {
				endTime = System.currentTimeMillis();
				System.out.println("Stemming- Time taken for " + (++percentage) + "% = "
						+ ((endTime - startTime) / 1000));
				// break;
			}
			TokenStream tokenStream = analyzer.tokenStream("test",
					new StringReader(s));
			// remove stop words
			// tokenStream = new StopFilter(Version.LUCENE_35, tokenStream,
			// EnglishAnalyzer.getDefaultStopSet());

			tokenStream = new EnglishMinimalStemFilter(new LowerCaseTokenizer(
					matchVersion, new StringReader(s)));
			// retrieve the remaining tokens
			CharTermAttribute token = tokenStream
					.getAttribute(CharTermAttribute.class);

			while (tokenStream.incrementToken()) {
				// System.out.println(token.toString());
				if(token.toString().length()>1){
					bagOfWords.add(token.toString());
				}				
			}

			writeToFile(bagOfWords, wordsWriter);
			tokenStream.end();
			tokenStream.close();
			count++;
			
		}
		contextReader.close();
		analyzer.close();
		wordsWriter.close();

	}
	
	public void removeStopWordsStem(String input) throws IOException {
		// input string
		Version matchVersion = Version.LUCENE_35; // Substitute desired Lucene
													// version for XY
		Analyzer analyzer = new StandardAnalyzer(matchVersion); // or any other
																// analyzer
		TokenStream tokenStream = analyzer.tokenStream("test",
				new StringReader(input));
		// remove stop words
		tokenStream = new EnglishMinimalStemFilter(tokenStream);

		// retrieve the remaining tokens
		CharTermAttribute token = tokenStream
				.getAttribute(CharTermAttribute.class);
		if(token==null){
			@SuppressWarnings("unused")
			int i=0;
		}
		while (tokenStream.incrementToken()) {
			bagOfWords.add(token.toString());
		}
		
		tokenStream.end();
		tokenStream.close();
		analyzer.close();
	}
	
	

}
