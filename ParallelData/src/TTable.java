import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.PrintWriter;
import java.io.RandomAccessFile;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

import org.omg.CORBA.OMGVMCID;


public class TTable extends Config implements Serializable{

	/**
	 * 
	 */
	private static final long serialVersionUID = -9002172753555230584L;
	ArrayList<HashMap<Integer, Double>> table;
	HashSet<Integer> titles = new HashSet<>();

	public TTable()
	{
		try {
			
			File titleListFile = new File(TRANSLATION_TABLE_SER);
			if (!titleListFile.exists())
			{
				storeTT();
			}
			storeTT();
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}
	
	public TTable readTransTable(String fileName) throws IOException, ClassNotFoundException {
		BufferedReader r = new BufferedReader(new FileReader(fileName));
		ObjectInputStream in = new ObjectInputStream(new FileInputStream(TRANSLATION_TABLE_SER));
		return (TTable)in.readObject();
	}
	
	public static int findNum(String file){
		try {
			BufferedReader r = new BufferedReader(new InputStreamReader(Runtime.getRuntime().exec
					("wc -l " + file). getInputStream()));
			String output = r.readLine();
			int numWords = Integer.parseInt(output.split(" ")[0]);
			System.out.println("Number of words =  " + numWords);
			return numWords;
		} catch (NumberFormatException | IOException e) {
			throw new RuntimeException(e);
		}
		
	}
	
	public void storeTT() throws IOException {
		
		File ttFile = new File(TRANSLATION_TABLE_SER);
		if(!ttFile.exists())
		{
			TTable.compressTTable(TRANSLATION_TABLE_TXT);
			String fileName = TRANSLATION_TABLE_TXT + TTABLE_COMPRESSED_SUFFIX;
			
			BufferedReader r = new BufferedReader(new FileReader(fileName));
			int numWords = findNum(BAG_OF_WORDS_VCB);
			table = new ArrayList<HashMap<Integer, Double>>(numWords);
			for(int i=0; i<numWords+100; i++){
				table.add(new HashMap<Integer, Double>(30));
			}
			String s;
			String[] spl;
			HashMap<Integer, Double> currWordMap = null;
			int prevWordId = -1;
			while((s = r.readLine())!=null){
				spl = s.split(" ");
				int wordId = Integer.parseInt(spl[0]);
				Integer titleId = Integer.parseInt(spl[1]);
				Double tValue = Double.parseDouble(spl[2]);
				if(wordId != prevWordId){
					currWordMap = table.get(wordId);			
				}
				titles.add(titleId);
				currWordMap.put(titleId, tValue);
				prevWordId = wordId;
			}
			ObjectOutputStream out = new ObjectOutputStream(new FileOutputStream(TRANSLATION_TABLE_SER));
			out.writeObject(this);
			System.out.println("Finished serializing translation table onto disk");
			r.close();
			out.close();
		}
	}
	
	double getTValue(int wordId, int paperId){
		Double val = table.get(wordId).get(paperId);
		if(val == null){
			return 0.0;
		}
		return val;
	}
	
	static void compressTTable(String path) throws NumberFormatException, IOException{
		BufferedReader r =  new BufferedReader(new FileReader(path));
		PrintWriter w = new PrintWriter(new File(path+"_compressed"));
		String entry;
		while((entry = r.readLine())!=null){
			double value = Double.parseDouble(entry.split(" ")[2]); 
			if(value > 0.001){
				w.println(entry);
			}
		}
		w.close();
		r.close();
	}
}
