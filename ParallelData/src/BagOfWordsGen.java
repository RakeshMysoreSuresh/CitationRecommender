/**
 * 
 */
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.PrintWriter;
import java.io.StringReader;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.StopFilter;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.en.EnglishAnalyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import org.apache.lucene.util.Version;

/**
 * @author Rakesh_BCKUP
 * 
 */
public class BagOfWordsGen {

	private static BufferedReader idReader;
	private static BufferedReader contextReader;
	private static PrintWriter wordsWriter;
	private Set<String> bagOfWords;

	/**
	 * @param args
	 * @throws IOException
	 * @throws ClassNotFoundException
	 */

	public static void main(String[] args) throws IOException,
			ClassNotFoundException {

		BagOfWordsGen parallel = new BagOfWordsGen();
		parallel.generate();

	}

	public void generate() throws IOException {
		int numCitation = 0;
		String curPaperId, context;
		String previousId = "";

		curPaperId = idReader.readLine(); // read the column name
		context = contextReader.readLine(); // read the column name
		System.out.println(curPaperId);
		long startTime = 0, endTime = 0;
		startTime = System.currentTimeMillis();
		int percentage=0;
		while ((curPaperId = idReader.readLine()) != null) {
			numCitation++;
			
			if (0 == numCitation%12000) {
				endTime = System.currentTimeMillis();
				System.out.println("Time taken for "+(++percentage)+"% = "
						+ ((endTime - startTime) / 1000));
				//break;
			}
			if (curPaperId.equals(previousId)) {

				// Read one citation context and get bag of words
				context = contextReader.readLine();
				removeStopWords(context);

			} else {
				if (bagOfWords.size() != 0) {
					// write bagsOfWords to a file
					// write citedPaperIds to a file
					writeToFile(bagOfWords);
				}
				// Initialize Sets to read the new Paper citations
				bagOfWords.clear();

				// Read one citation context and get bag of words
				context = contextReader.readLine();
				removeStopWords(context);

			}

			previousId = curPaperId;

		}

		// Write the last set of values
		if (bagOfWords != null) {
			// write bagsOfWords to a file
			// write citedPaperIds to a file
			writeToFile(bagOfWords);
		}

		wordsWriter.close();
	}
	
	

	public BagOfWordsGen() throws IOException {

		// Buffered Readers
		idReader = new BufferedReader(new FileReader("ID.txt"));
		contextReader = new BufferedReader(new FileReader("Context.txt"));

		// Buffered Writers
		wordsWriter = new PrintWriter(new BufferedWriter(new FileWriter(
				"BagOfWordsEC2_v2.txt")));

		bagOfWords = new HashSet<String>();
	}

	private static void writeToFile(Set<String> bagOfWords) {

		Iterator<String> it = bagOfWords.iterator();
		String write = "";
		while (it.hasNext()) {
			write = write + " " + it.next();
		}
		wordsWriter.println(write);
	}

	public void removeStopWords(String input) throws IOException {
		// input string
		Version matchVersion = Version.LUCENE_35; // Substitute desired Lucene
													// version for XY
		Analyzer analyzer = new StandardAnalyzer(matchVersion); // or any other
																// analyzer
		TokenStream tokenStream = analyzer.tokenStream("test",
				new StringReader(input));
		// remove stop words
		tokenStream = new StopFilter(Version.LUCENE_35, tokenStream,
				EnglishAnalyzer.getDefaultStopSet());

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

}
