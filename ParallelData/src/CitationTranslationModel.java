import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.util.Scanner;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * 
 */

/**
 * @author rakesh
 * 
 */
public class CitationTranslationModel extends Config{
	
	/**
	 * @param args
	 */
	public static void main(String[] args) throws Exception{
		
		
		PrintWriter timeTakenWriter = new PrintWriter(new File(DATASET_DIR + "timetaken.log"));
		logger = new PrintWriter(new File(DATASET_DIR + "log"));

		long absoluteStartTime = System.currentTimeMillis();
		/********************************************************************** citationsGen = new CitationsGen();**/
		runShellCommand("mkdir " +DATASET_DIR + "serialized");
		//runShellCommand("mv " + DATASET_DIR + "TitleList.ser " + DATASET_DIR + "serialized/");
		
		if(DENOISING)
		{
			if(POS_TAGGING)
			{
				PatternMatch.cleanWithTagging(CONTEXT_OTHER_POS, CONTEXT_PROPERNOUNS);
				bagOfWordsGen = new BagOfWordsGen(new String[] { ID, CONTEXT_OTHER_POS, DATASET_DIR + BAG_OF_WORDS_FILENAME }, CONTEXT_PROPERNOUNS);
			}
			else
			{
				PatternMatch.tokenize();
				bagOfWordsGen = new BagOfWordsGen(new String[] { ID, CONTEXT_PTB_TOKENIZED, DATASET_DIR + BAG_OF_WORDS_FILENAME });
			}
		}
		else
		{
			bagOfWordsGen = new BagOfWordsGen(new String[] { ID, CONTEXT, DATASET_DIR +BAG_OF_WORDS_FILENAME });
		}
		bagOfWordsGen.generate();
		
		
		citationsGen = new CitationsGen();
		citationsGen.generate();
		runShellCommand(PLAIN2SNT);// error exists NPE at 165
		
		if(NOISY_WORD_REMOVAL)
		{
			identifyNoisyWords();
			BagOfWordsGen.removeNoisyWords();
			runShellCommand("mv " + DATASET_DIR+BAG_OF_WORDS_FILENAME + " " + DATASET_DIR + "BagOfWordsOriginal");
			runShellCommand("mv " + BAG_OF_WORDS_DENOISED + " " + DATASET_DIR + BAG_OF_WORDS_FILENAME);
			runShellCommand(PLAIN2SNT);
		}
		runShellCommand(MKCLS_BOW);
		timeTakenWriter.println("Time after mkcls bag of words" +(System.currentTimeMillis() - absoluteStartTime));
		runShellCommand(MKCLS_CP);
		timeTakenWriter.println("Time after mkcls cited papers" +(System.currentTimeMillis() - absoluteStartTime));

		runShellCommand(SNT2COOC);
		timeTakenWriter.println("Time after snt2cooc" +(System.currentTimeMillis() - absoluteStartTime));
		//runShellCommand("");
		timeTakenWriter.println("Time after giza" +(System.currentTimeMillis() - absoluteStartTime));
		TTable tt = new TTable();
		System.out.println("here");
		RefRec ref = new RefRec();
		String query = "myopia affect approximate adult america ethnic diversity appear distinguish different group regard prevalence caucasian have higher prevalence than africa america asia population have highest prevalence rate report range from jewish caucasian one target population present study have consistent demonstrate";
		ref.rankPapers(query);
				
		timeTakenWriter.close();logger.close();
	}

	/**
	 * @throws IOException 
	 * 
	 */
/*	private static void runGiza() {
		runShellCommand(PLAIN2SNT);
		runShellCommand(MKCLS_BOW);
		runShellCommand(MKCLS_CP);
		runShellCommand(SNT2COOC);
		runShellCommand(GIZAPP);
	}*/
	
	//To to called after running plain2snt, vcb files must be generated
	private static void identifyNoisyWords() throws IOException {
		System.out.println("Execute this command on terminal: " + IDENTIFY_NOISY_WORDS + " > " + NOISY_WORDS);
		System.out.println("press any key to continue..");
		new Scanner(System.in).next();
		/*String s;
		PrintWriter pw = new PrintWriter(NOISY_WORDS);
		Process p;
		try {
			System.out.println("Executing" + IDENTIFY_NOISY_WORDS);
			p = Runtime.getRuntime().exec(IDENTIFY_NOISY_WORDS);
			BufferedReader br = new BufferedReader(new InputStreamReader(
					p.getInputStream()));

			while ((s = br.readLine()) != null)
				pw.println("line: " + s);
			p.waitFor();
			logger.println("exit: " + p.exitValue());
			System.out.println("exit: " + p.exitValue());
			p.destroy();
		} catch (Exception e) { throw new RuntimeException("Exception occurred when running command "+IDENTIFY_NOISY_WORDS, e); }*/
		
		/*ProcessBuilder builder = new ProcessBuilder(IDENTIFY_NOISY_WORDS);
		builder.redirectOutput(new File(NOISY_WORDS));
		builder.start();*/
	}
	
	private static void runShellCommand(String cmd) {
		String s;
		System.out.println("Running command:\n" + cmd);
		logger.println("Running command:" + cmd);
		Process p;
		try {
			p = Runtime.getRuntime().exec(cmd);
			BufferedReader br = new BufferedReader(new InputStreamReader(
					p.getInputStream()));

			while ((s = br.readLine()) != null)
				System.out.println("line: " + s);
			p.waitFor();
			logger.println("exit: " + p.exitValue());
			System.out.println("exit: " + p.exitValue());
			p.destroy();
		} catch (Exception e) { throw new RuntimeException("Exception occurred when running command "+cmd, e); }
	}
	
	
	//Advanced params. No need to change
	static final String COOCCURRENCE_FILE = DATASET_DIR + BAG_OF_WORDS_FILENAME + "_" + CITED_PAPERS_FILENAME + ".cooc ";
	static final String GIZA_LOG_FILE = BAG_OF_WORDS_FILENAME + "_" + CITED_PAPERS_FILENAME + ".log ";
	static final String SENTENCE_FILE = DATASET_DIR + BAG_OF_WORDS_FILENAME + "_" + CITED_PAPERS_FILENAME + ".snt ";

	// MGIZA commands
/*	static final String PLAIN2SNT = MGIZA_DIR + "plain2snt " + DATASET_DIR + BAG_OF_WORDS_FILENAME + " " + DATASET_DIR + CITED_PAPERS_FILENAME;
	static final String MKCLS_BOW = MGIZA_DIR + "mkcls " + "-p" + DATASET_DIR + BAG_OF_WORDS_FILENAME  + " " + "-V" + DATASET_DIR + BAG_OF_WORDS_FILENAME + ".vcb.classes";
	static final String MKCLS_CP = MGIZA_DIR + "mkcls " + "-p" + DATASET_DIR + CITED_PAPERS_FILENAME  + " " + "-V" + DATASET_DIR + CITED_PAPERS_FILENAME + ".vcb.classes"; 
	static final String SNT2COOC = MGIZA_DIR + "snt2cooc " + COOCCURRENCE_FILE + BAG_OF_WORDS_VCB + CITED_PAPERS_VCB + SENTENCE_FILE;*/
	
	//GIZA commands
	static final String PLAIN2SNT = GIZA_DIR + "plain2snt.out " + DATASET_DIR + BAG_OF_WORDS_FILENAME + " " + DATASET_DIR + CITED_PAPERS_FILENAME;
	static final String MKCLS_BOW = MKCLS_DIR + "mkcls " + "-p" + DATASET_DIR + BAG_OF_WORDS_FILENAME  + " " + "-V" + DATASET_DIR + BAG_OF_WORDS_FILENAME + ".vcb.classes";
	static final String MKCLS_CP = MKCLS_DIR + "mkcls " + "-p" + DATASET_DIR + CITED_PAPERS_FILENAME  + " " + "-V" + DATASET_DIR + CITED_PAPERS_FILENAME + ".vcb.classes"; 
	static final String SNT2COOC = GIZA_DIR + "snt2cooc.out " + BAG_OF_WORDS_VCB + CITED_PAPERS_VCB + SENTENCE_FILE + " > " + COOCCURRENCE_FILE;
	static final String GIZAPP = GIZA_DIR + "GIZA++ " + "-s " + BAG_OF_WORDS_VCB + "-t " + CITED_PAPERS_VCB + " -c  " + SENTENCE_FILE + " -CoocurrenceFile " + COOCCURRENCE_FILE + " > " + "GizappLogaa";
	
	static final String PLAIN2SNT_DENOISED = GIZA_DIR + "plain2snt.out " + BAG_OF_WORDS_DENOISED + " " + DATASET_DIR + CITED_PAPERS_FILENAME; 
	
	
	private static BagOfWordsGen bagOfWordsGen;
	private static CitationsGen citationsGen;
	private static PrintWriter logger;
			
	/*	mkcls -pCitedPapersNOT -VCitedPapersNOT.vcb.classes

	mkcls -pBagOfWordsNOT -VBagOfWordsNOT.vcb.classes

	snt2cooc.out BagOfWordsNOT.vcb CitedPapersNOT.vcb BagOfWordsNOT_CitedPapersNOT.snt > bag_cited_NOT.cooc
	snt2cooc.out BagOfWords.vcb CitedPapers.vcb BagOfWords_CitedPapers.snt > bag_cited.cooc

	GIZA++ -s BagOfWordsNOT.vcb -t CitedPapersNOT.vcb -c BagOfWordsNOT_CitedPapersNOT.snt -CoocurrenceFile bag_cited_NOT.cooc > bag_cited_NOT_log.txt*/
}
