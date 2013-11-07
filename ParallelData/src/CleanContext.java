import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.Reader;
import java.io.StringReader;
import java.util.HashSet;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.Tokenizer;
import org.apache.lucene.analysis.snowball.SnowballAnalyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import org.apache.lucene.util.Version;


public class CleanContext {
	
	HashSet<String> bagOfWords=null;
	
	/**
	 * @param args
	 * @throws IOException 
	 */
	public static void main(String[] args) throws IOException {
		
		//remStop();
		
	}

	public static void remStop() throws FileNotFoundException, IOException {
		BufferedReader contextReader = new BufferedReader(new FileReader("Context.txt"));
		PrintWriter wordsWriter = new PrintWriter(new BufferedWriter(new FileWriter(
				"ContextStop.txt")));
		Analyzer analyzer = new StandardAnalyzer(Version.LUCENE_36);
		
		String s;
		int lines;
		while ((s=contextReader.readLine())!=null) {			
			TokenStream tokenStream = analyzer.tokenStream("ContextCleaner",
					new StringReader(s));
			CharTermAttribute token = tokenStream
					.getAttribute(CharTermAttribute.class);
			StringBuilder builder = new StringBuilder(400);
			while (tokenStream.incrementToken()) {
				builder.append(token.toString());
				builder.append(" ");
			}
			String cleanString = builder.toString();
			//System.out.println(cleanString);
			wordsWriter.println(cleanString);
		}
	}
	
	public static void filterMore() throws FileNotFoundException, IOException {
		BufferedReader contextReader = new BufferedReader(new FileReader("Context.txt"));
		PrintWriter wordsWriter = new PrintWriter(new BufferedWriter(new FileWriter(
				"ContextStop.txt")));
		Analyzer analyzer = new StandardAnalyzer(Version.LUCENE_36);		
		String s;
		int lines;
		while ((s=contextReader.readLine())!=null) {			
			TokenStream tokenStream = analyzer.tokenStream("ContextCleaner",
					new StringReader(s));
			CharTermAttribute token = tokenStream
					.getAttribute(CharTermAttribute.class);
			StringBuilder builder = new StringBuilder(400);
			while (tokenStream.incrementToken()) {
				builder.append(token.toString());
				builder.append(" ");
			}
			String cleanString = builder.toString();
			//System.out.println(cleanString);
			wordsWriter.println(cleanString);
		}
	}
	
	/*public void removeStopWords(String input) throws IOException {
		// input string
		Version matchVersion = Version.LUCENE_35; // Substitute desired Lucene
													// version for XY
		Analyzer analyzer = new StandardAnalyzer(matchVersion); // or any other
																// analyzer
		TokenStream tokenStream = analyzer.tokenStream("test",
				new StringReader(input));

		// tokenStream = new EnglishMinimalStemFilter(tokenStream);
		// remove stop words
		
		 * tokenStream = new StopFilter(Version.LUCENE_35, tokenStream,
		 * EnglishAnalyzer.getDefaultStopSet());
		 

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
	
	void remStopfrmFile(String fileName) throws IOException{
		BufferedReader r = new BufferedReader(new FileReader(fileName));
		PrintWriter w = new PrintWriter(new FileWriter(fileName+"Stop"));
		String s;		
		while((s=r.readLine())!=null){
			StringBuilder builder = new StringBuilder(100);
			removeStopWords(s);
			for(String str: bagOfWords){
				builder.append(str);
				builder.append(" ");
			}			
			w.println(builder.toString());
		}
		
	}*/

}
