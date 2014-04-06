/**
 * 
 */

/**
 * @author rakesh
 *
 */
public class Config {

	public static final boolean DEBUG = true;
	// Modify these params suitably (Dont forget the trailing slash for directories)
	//static final String DATASET_DIR = "/media/Project/Rakesh/March5/";
	//static final String DATASET_DIR = "/media/Project/IndexCorrected/Small/";
	//static final String DATASET_DIR = "/media/Project/Rakesh/Denoised/DatasetNew/";
	//static final String DATASET_DIR = "/media/Project/IndexCorrected/BugsThrash/";
	//static final String DATASET_DIR = "/media/Project/Corrected/";
	//static final String DATASET_DIR = "/media/Project/Third/";
	//static final String DATASET_DIR = "/media/Project/TenThousand/";
	//static final String DATASET_DIR = "/media/Project/SevenThousand/";
	static final String DATASET_DIR = "/media/Project/BareBone/Fifth/";
	
	static final String BAG_OF_WORDS_FILENAME = "BagOfWords";
	static final String CITED_PAPERS_FILENAME = "CitedPapers";
	static final String BAG_OF_WORDS_VCB = DATASET_DIR + BAG_OF_WORDS_FILENAME +".vcb ";
	static final String CITED_PAPERS_VCB = DATASET_DIR + CITED_PAPERS_FILENAME +".vcb ";
	static final String NOISY_WORDS = DATASET_DIR + "NoisyWords";
	
	static final String IDENTIFY_NOISY_WORDS = "grep -o -P '\\ .*(?=\\s1$)' "+ BAG_OF_WORDS_VCB.trim();
	
	static final String MGIZA_DIR = "/home/rakesh/Desktop/mgizapp/bin/";
	static final String GIZA_DIR = "/home/rakesh/Desktop/Giza/giza-pp/GIZA++-v2/";
	static final String MKCLS_DIR = "/home/rakesh/Desktop/Giza/giza-pp/mkcls-v2/";
	static final int TOTAL_LINES_IN_DATASET = 1200000;
	static final int TOTAL_LINES_IN_DATASET_TENPERCENT = 120000;
	public static final String CITATION_RECOMMENDATION_REPORT = "Citation Recommendation report.txt";
	public static final int MAX_RECOM = 100;
	
	public static final boolean NOISY_WORD_REMOVAL = false;
	
	static final boolean DENOISING = false;
	static final boolean AGGRESSIVE_STEMMING = false;
	static final boolean MINIMAL_STEMMING = false;
	static final boolean POS_TAGGING = false;
	static final boolean REMOVE_NOISY_WORDS = false;
	static final String CONTEXT = DATASET_DIR + "Context";
	static final String ID = DATASET_DIR + "ID";
	static final String TITLE = DATASET_DIR + "Title";
	
	static final String CONTEXT_OTHER_POS = DATASET_DIR + "Context_OtherPOS";
	static final String CONTEXT_PROPERNOUNS = DATASET_DIR + "Context_ProperNouns";
	static final String POS_TAG_MODELS_DIR = "Tools/stanford-postagger-2013-04-04/models/";
	static final String POS_TAG_MODEL = "english-left3words-distsim.tagger";
	
	/*
	 * Serialized object names
	 */
	
	static final String BAG_OF_WORDS_DENOISED = DATASET_DIR + "BagOfwordsDenoised";
	static final String CONTEXT_PTB_TOKENIZED = DATASET_DIR + "Context_PTBTokenized";
	//Map that gives the title given cited paper ID
	static final String CITED_VCB_PAPER_ID_TO_TITLE_SER_MAP = DATASET_DIR + "serialized/VCBTitleBridge.ser";
	static final String TITLE_LIST = DATASET_DIR + "serialized/TitleList.ser";
	static final String TRANSLATION_TABLE_SER = DATASET_DIR + "serialized/TTable.ser";
	static final String TRANSLATION_TABLE_TXT = DATASET_DIR + "114-03-29.170452.rakesh.t3.final";
	static final boolean TTABLE_COMPRESSION = true;
	static final String TTABLE_COMPRESSED_SUFFIX = "_compressed";
	static final double TVALUE_COMPRESSION_THRESHOLD = 0.001;
	static final double MIN_CONFIDENCE = 0.1;
	static final String WORDS2ID_LIST = DATASET_DIR + "serialized/WordsToID.ser";
	//Indexed by word ids
	static final String ICF_ARRAY = DATASET_DIR + "serialized/ICFArray.ser";
	static final String TITLE2ID = DATASET_DIR + "serialized/Map.ser";
	static final String TITLE2ID_TXT = DATASET_DIR + "Title2ID_Map.txt";
	
	static final long START_TIME = System.currentTimeMillis();
	//static final String  = ;
}
