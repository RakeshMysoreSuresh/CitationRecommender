import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Set;

//import org.apache.lucene.analysis.CharArrayMap.EntrySet;


public class ICFCalc {
	//Minimum number of occurrences of a word below which a word will be ignored
	private static final int MIN_FREQ = 3;
	HashMap<String, Long> map = new HashMap<>();
//	List<HashMap<String, Integer>> mapList =
//			new ArrayList<HashMap<String, Integer>>();
	
	public ICFCalc() {
		try {
			readDataStruct("VcbFile.ser");
		} catch (ClassNotFoundException | IOException e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * @param args
	 * @throws IOException 
	 * @throws ClassNotFoundException 
	 */
	public static void main(String[] args) throws IOException, ClassNotFoundException {
		
		ICFCalc calc = new ICFCalc();
		calc.words2ID("ContextPTBTokenizedStemmed.txt");
		calc.storeDataStruct();
		calc.readDataStruct("VcbFile.ser");
		calc.storeDataStruct();
	}
	/**
	 * Read each citation context, split up the text into words, every time a
	 * unique word is found, store it in a HashMap with a unique ID and its number of occurrences
	 * across all Citation contexts.  
	 * @param fileName
	 * @throws IOException
	 */
	public void words2ID(String fileName) throws IOException {
		BufferedReader r = new BufferedReader(new FileReader(fileName));
		int count=0;
		String string;
		while((string = r.readLine())!=null){
			String[] words = string.split(" ");
			for(String w : words){
				Long l = map.get(w);				
				if(l==null){
					l = (((long)count)<<32)|1;
					map.put(w, l);
					count++;
				}
				else{
					map.put(w, l+1);
				}
			}				
		}
		r.close();
	}
	
	void storeDataStruct() throws FileNotFoundException, IOException{
		ObjectOutputStream out = new ObjectOutputStream(new FileOutputStream("VcbFile.ser"));
		//out.writeObject(map);
		
		PrintWriter w = new PrintWriter("Vcb.Context");
		Set<String> e = map.keySet();
		for(String s: e){
			w.println(get(s));
		}
		out.close();
	}
	
	void storeScrubbedDatastructure()throws FileNotFoundException, IOException{
		ObjectOutputStream out = new ObjectOutputStream(new FileOutputStream("VcbFile.ser"));
		PrintWriter w = new PrintWriter("Vcb.Context");
		Set<String> e = map.keySet();
		for(String s: e){
			if (getFreq(s) > MIN_FREQ) {
				w.println(get(s));
			}
		}
		out.close();
	}
	
	void readDataStruct(String fileName) throws IOException, IOException, ClassNotFoundException{
		ObjectInputStream in = new ObjectInputStream(new FileInputStream(fileName));
		map =  (HashMap<String,Long>)in.readObject();
		Iterator<String> e = map.keySet().iterator();
		for(int i =0; i<100; i++){
			System.out.println(get(e.next()));
		}		
		in.close();
	}
	
	String get(String word){
		long l = map.get(word);
		int count = (int)(l&(((long)1<<32)-1));
		int id = (int)(l>>32);
		return id + " "+ word + " " + count;
	}
	
	int getFreq(String word){
		long l = map.get(word);
		return (int)(l&(((long)1<<32)-1));
	}
	
	int getID(String word){
		long l = map.get(word);
		int id = (int)(l>>32);
		return id;
	}

}
