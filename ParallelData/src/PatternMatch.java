import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringReader;
import java.util.List;

import com.ibm.icu.text.Normalizer;

import edu.stanford.nlp.ling.CoreLabel;
import edu.stanford.nlp.ling.HasWord;
import edu.stanford.nlp.process.CoreLabelTokenFactory;
import edu.stanford.nlp.process.DocumentPreprocessor;
import edu.stanford.nlp.process.PTBTokenizer;
import edu.stanford.nlp.process.TokenizerFactory;
import edu.stanford.nlp.tagger.maxent.MaxentTagger;

/**
 * @author Rakesh_BCKUP
 * 
 * This class uses Regular expressions to match common error patterns and other common sources of noise. Once matched they can be 
 * eliminated. The PTBTokenizer in the Core NLP package from Stanford University is used to clean and tokenize the words in the 
 * citation context. The Maximum Entropy(Maxent) tagger is used to tag the words with their parts of speech.
 *
 */
public class PatternMatch extends Config{
	
	//Options for the Penn Tree Bank tokenizer
	private static final String OPTIONS = "invertible = true," +
										  "americanize = true," +
										  "normalizeAmpersandEntity = true," +
										  "ptb3Ellipsis = true," +
										  "asciiQuotes  = true,"+
										  "tokenizeNLs  = false,"+
										  "untokenizable = noneDelete,"+
										  "normalizeOtherBrackets = true";
	private static final String EXCLUDEPATTERN = "(.*[\\\\][/].*)|((\\-.+\\-))";
	private static final String EMAILPATTERN = "([A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,4})";
	//Space followed by zero or more spl char followed by a set of diacritics
	private static final String DIACRITICPATTERN = "((?<=\\s)[\\^\\-=]*[^\u0000-\u0080a-zA-Z0-9]+[\\^\\-=]*)|([\\^\\-=]*[^\u0000-\u0080a-zA-Z0-9]+[\\^\\-=]*(?=\\s))";
	private static final String MULTSPLCHARPATTERN = "([^a-zA-Z0-9\\s][^a-zA-Z0-9\\s]+)";
	private static final String TRAILSPLCHARPATTERN = "((?<=\\s)[^a-zA-Z0-9\\s]+)|([^a-zA-Z0-9\\s]+(?=\\s))";
	private static final String MATHVARPATTERN = "((?<=\\s)[A-Za-z][ijkabclmnxz0-9](?=\\s))";
	private static final String SPLCHARPATTERN = MULTSPLCHARPATTERN+"|"+TRAILSPLCHARPATTERN;
	private static final String REMOVEPATTERN = DIACRITICPATTERN+"|"+EMAILPATTERN+"|"+SPLCHARPATTERN+"|"+MATHVARPATTERN;
	//private static final String PROPERNOUNPATTERN = "((?<=\\s)
	private static final MaxentTagger tagger = new MaxentTagger(POS_TAG_MODELS_DIR + POS_TAG_MODEL);
	
	private TokenizerFactory<CoreLabel> ptbTokenizerFactory = PTBTokenizer.factory(new CoreLabelTokenFactory(), OPTIONS);
	StringBuffer otherPOSBuffer, properNounBuffer;

	/**
	 *  
	 * @throws FileNotFoundException
	 * @throws IOException
	 */
	public static void clean(String otherPoSFile, String properNounFile) throws FileNotFoundException, IOException {
		TokenizerFactory<CoreLabel> ptbTokenizerFactory = 
		        PTBTokenizer.factory(new CoreLabelTokenFactory(), OPTIONS);
		BufferedReader r = new BufferedReader(new FileReader(CONTEXT));
		PrintWriter pw = new PrintWriter(new FileWriter(otherPoSFile));
		PrintWriter propNounWriter = new PrintWriter(new FileWriter(properNounFile));
	    String s;
	    s = r.readLine();
		while ((s=r.readLine())!=null) {
			process(ptbTokenizerFactory, s, pw, propNounWriter);
			pw.println("");
			propNounWriter.println("");
		}
		r.close();
	}

	/**
	 * 
	 * @param ptbTokenizerFactory
	 * @param s
	 * @param pw
	 * @param propNounWriter
	 * @throws IOException
	 */
	public static void process(TokenizerFactory<CoreLabel> ptbTokenizerFactory,
			String s, PrintWriter pw, PrintWriter propNounWriter) throws IOException {
		
		DocumentPreprocessor documentPreprocessor = new DocumentPreprocessor(new StringReader(s));
		documentPreprocessor.setTokenizerFactory(ptbTokenizerFactory);
		for (List<HasWord> sentence : documentPreprocessor) {
			StringBuilder builder = new StringBuilder(200);
			int count = 0;
			for (HasWord word : sentence) {
				String w = word.toString();
				if (w.length() > 1 || w.matches("[A-Z]+")) {
					if (!w.matches(EXCLUDEPATTERN)) {
						builder.append(word);
						builder.append(" ");
					}
				}
			}
			String ptbTokenizedString = builder.toString();
			String regexCleaned = ptbTokenizedString.replaceAll(
					REMOVEPATTERN, "");
			//pw.print(regexCleaned);
			//System.out.println(regexCleaned);
			String tagged = tagger.tagString(regexCleaned);
			for(String word : tagged.split(" ")){
				if(word.endsWith("_NNP")||word.endsWith("_NNPS")){
					propNounWriter.print((word.split("_"))[0]);;
					propNounWriter.print(" ");
				}
				else{
					pw.print((word.split("_"))[0]);
					pw.print(" ");
				}
			}
		}
	}
	
	/**
	 * 
	 * @param ptbTokenizerFactory
	 * @param s
	 * @param pw
	 * @param propNounWriter
	 * @throws IOException
	 */
	public void preProcessQuery(String s) throws IOException {
		otherPOSBuffer = new StringBuffer(s.length());
		properNounBuffer = new StringBuffer(s.length() >> 1);
		DocumentPreprocessor documentPreprocessor = new DocumentPreprocessor(new StringReader(s));
		documentPreprocessor.setTokenizerFactory(ptbTokenizerFactory);
		for (List<HasWord> sentence : documentPreprocessor) {
			StringBuilder builder = new StringBuilder(200);
			for (HasWord word : sentence) {
				String w = word.toString();
				if (w.length() > 1 || w.matches("[A-Z]+")) {
					if (!w.matches(EXCLUDEPATTERN)) {
						builder.append(word);
						builder.append(" ");
					}
				}
			}
			String ptbTokenizedString = builder.toString();
			String regexCleaned = ptbTokenizedString.replaceAll(
					REMOVEPATTERN, "");
			//pw.print(regexCleaned);
			//System.out.println(regexCleaned);
			String tagged = tagger.tagString(regexCleaned);
			for(String word : tagged.split(" ")){
				if (word != null) {
					if (word.endsWith("_NNP") || word.endsWith("_NNPS")) {
						properNounBuffer.append((word.split("_"))[0]);
						;
						properNounBuffer.append(" ");
					} else {
						otherPOSBuffer.append((word.split("_"))[0]);
						otherPOSBuffer.append(" ");
					}
				}
			}
		}
	}
	
	static void regexTest(String input, String pattern){
		System.out.println(input.replaceAll(pattern, ""));
	}
	
	public static String removeAccents(String text) { 
		return Normalizer.decompose(text, false, 0) .replaceAll("\\p{InCombiningDiacriticalMarks}+", "");
	}
	
	public static boolean sentenceDetector(String text){
		return false;
	}
/****************************Rough work*************************
 * 
 * 

public static void clean(String otherPoSFile, String properNounFile) throws FileNotFoundException, IOException {
		TokenizerFactory<CoreLabel> ptbTokenizerFactory = 
		        PTBTokenizer.factory(new CoreLabelTokenFactory(), OPTIONS);
		BufferedReader r = new BufferedReader(new FileReader(Config.CONTEXT));
		//StringReader r = new StringReader("ï¿½ï¿½Â¤Â£ and ï¿½ï¿½ï¿½ï¿½ï¿½ &quot;play$%&&ï¿½ï¿½ï¿½ï¿½ï¿½ï¿½ï¿½ï¿½ï¿½ï¿½ï¿½[sgsds]&quot; C# 1-R are positive constants-=-=. Hence user benefit is an increasing function of the number of contributors, but with diminishing returnsâa form widely accepted in this context (see, e.g., [2], [3], [15]). Thus, the performance of the system, denoted by ï¿½ï¿½ï¿½ï¿½ï¿½ï¿½ï¿½ï¿½ï¿½ï¿½ï¿½ï¿½ï¿½ , is defined as the difference between the average benefit received by all users (including both contributors and free-riders) an");
	    //PrintWriter pw = new PrintWriter(new OutputStreamWriter(System.out, "utf-8"));
		//PrintWriter pw = new PrintWriter(new FileWriter("Context_PTBtokenized(NoisyCitationRemoved).text"));
		PrintWriter pw = new PrintWriter(new FileWriter(otherPoSFile));
		PrintWriter propNounWriter = new PrintWriter(new FileWriter(properNounFile));
	    String s;
	    s = r.readLine();
		while ((s=r.readLine())!=null) {
			process(ptbTokenizerFactory, s, pw, propNounWriter);
			pw.println("");
			propNounWriter.println("");
		}
		r.close();
	}


	public static void test() throws IOException {
		TokenizerFactory<CoreLabel> ptbTokenizerFactory = 
		        PTBTokenizer.factory(new CoreLabelTokenFactory(), OPTIONS);
//		PrintWriter pw = new PrintWriter(new FileWriter("Context_OtherPOS.txt"));
//		PrintWriter propNounWriter = new PrintWriter(new FileWriter("Context_ProperNouns.txt"));
		PrintWriter pw = new PrintWriter(System.out);
		String s = "al representation a, which is equivalent to a solution of the overdetermined equation. Qa = x (5) and according to our goals we have a projection method, since y may be expressed as y = Q(QTQ) �1 QT=-=x (6) a-=-nd the combination of the Q matrices P = Q(Q T Q) �1 Q T features the property P 2 = P, and thus forms a projector matrix. In short, we have designed a relaxing dynamical equation that utilises thre";	
		process(ptbTokenizerFactory, s, pw, pw);
		pw.close();
		//propNounWriter.close();
	}

	public static void main(String[] args) throws Exception {

		clean();	 
        //System.out.println(REMOVEPATTERN);
		test();
	}
	
	
	public static void cleanAndContexts() throws FileNotFoundException, IOException {
		clean(, "Context_ProperNouns.txt");
	}


//		System.out.println(new String("1\\/2").matches("(.*[\\\\][/].*)|((\\-...\\-))"));
//		System.out.println(new String("-LLR-").matches("([0-9][\\\\][/][0-9])|((\\-.+\\-))"));
//		System.out.println(new String("&quot; C# 1-R are positive constants=--.").replaceAll("([^a-zA-Z0-9\\s][^a-zA-Z0-9\\s]+)", ""));
		//regexTest("coœmmœœœ ^œœœ^ œœcoœ ", DIACRITICPATTERN);
		//regexTest("Xj = i + 1 + 2 ai", MATHVARPATTERN);
		//regexTest("(NNP Mr.) (NNP Morton) (VBD said) (CD 2) (NN %) (JJ annual) (NN growth)", )
		//regexTest("coœmmœœœ ^œœœ^ œœcoœ ", "((?<=\\s)[\\^\\-=]*[^\u0000-\u0080a-zA-Z0-9]+[\\^\\-=]*)");
		//regexTest("rms@rit.edu coœmmœœœ ^œœœ^ coœ  =--RR-=---B- ", REMOVEPATTERN);
		//regexTest(" =--RR-=---B- ", SPLCHARPATTERN);
		//System.out.println("PR_NNP".endsWith("_NNP"));
		//System.out.println(new String("rms@rit.edu").replaceAll(EMAILPATTERN, " "));
		// The sample string
       // String sample = "PageRank algorithm was debveloped buy Larry Page which is based  the random surfer model similar to Markov chains D K11 D K22 D K21 K11 D D As for K  D and are similarly partitioned as 11 12 21 22 D11 D12 D D21 D22  11 12";
        
 
        // The tagged string
        //String tagged = tagger.tagString(sample);
 
        // Output the result
        //System.out.println(tagged);
       //- See more at: http://www.galalaly.me/index.php/2011/05/tagging-text-with-stanford-pos-tagger-in-java-applications/#sthash.6J5FrHb7.dpuf
		
		//BufferedReader contextReader = new BufferedReader(new FileReader("Context.txt"));
 */
	
	/************************************************************/
	//public static 
	
//	String s=null; int count = 0;
//	while((s=contextReader.readLine())!=null){ 
//		count++;
//		if(count>100){
//		String resultString = s.replaceAll("[^A-Z0-9a-z \\.\\[\\]][^A-Z0-9a-z \\.\\[\\]]+ [ \\[", "");
//		//wordsWriter.println(resultString);
//	}
//	contextReader.close();
//	wordsWriter.close();
	
	/*//		PrintWriter wordsWriter = new PrintWriter(new BufferedWriter(new FileWriter("ContextCleaned_v1.txt")));
	System.out.println(removeAccents("play$%&&ï¿½ï¿½ï¿½ï¿½ï"));
	regexTest("&quot;play$%&&ï¿½ï¿½ï¿½ï¿½ï¿½ï¿½ï¿½ï¿½ï¿½ï¿½ï¿½[sgsds]&quot;",
			"[^\\p{L}\\p{N} .\\[\\]][^\\p{L}\\p{N} .\\[\\]]+");
	int unicode = "play$%&&ï¿½ï¿½ï¿½ï¿½ï¿½ï¿½ï¿½ï¿½ï¿½ï¿½ï¿½[sgsds]".charAt(15);
	System.out.println(unicode);
	System.out.println("play$%&&ï¿½ï¿½ï¿½ï¿½ï¿½ï¿½ï¿½ï¿½ï¿½ï¿½ï¿½[sgsds]".charAt(15));*/	


}
