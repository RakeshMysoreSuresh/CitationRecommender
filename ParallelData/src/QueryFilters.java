import java.io.BufferedReader;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringReader;
import java.util.HashSet;
import java.util.Set;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.LowerCaseTokenizer;
import org.apache.lucene.analysis.PorterStemFilter;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import org.apache.lucene.util.Version;

/**
 * 
 */

/**
 * @author Rakesh_BCKUP
 *
 */
public class QueryFilters {
	
	private Set<String> bagOfWords = new HashSet<String>();
	
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub

	}	
	
	void stemTerm (String path) throws IOException {
		
		Version matchVersion = Version.LUCENE_35; // Substitute desired Lucene
			// version for XY
		Analyzer analyzer = new StandardAnalyzer(matchVersion); // or any other
	    PrintWriter writer = new PrintWriter(new FileWriter(path+"_stemmed"));
			// analyzer
		BufferedReader contextReader = new BufferedReader(new FileReader(path));
		String s;
		while((s = contextReader.readLine()) !=null ){
			bagOfWords.clear();
//			TokenStream tokenStream = analyzer.tokenStream("test",
//					new StringReader(s));
			
		    TokenStream stemFilter = new PorterStemFilter(
		    		new LowerCaseTokenizer(matchVersion, new StringReader(s)));
		 // retrieve the remaining tokens
			CharTermAttribute token = stemFilter
					.getAttribute(CharTermAttribute.class);
			while (stemFilter.incrementToken()) {
				bagOfWords.add(token.toString());
			}
		    writer.println(stemFilter.incrementToken());
		}
	}
	
	private void removeLongEntries() {
		
	}
}
