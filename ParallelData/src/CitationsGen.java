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
import java.io.OutputStream;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.TreeSet;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;

public class CitationsGen extends Config {

	private BiMap<String, Integer> titlesMap;
	private BufferedReader idReader;
	BufferedReader titleReader;
	private PrintWriter titleWriter;
	private Set<Integer> citedPaperIds = new TreeSet<>();
	private ArrayList<String> titleList = new ArrayList<>();
	int[] titleVCBMap = new int[600000];

	CitationsGen() {
		try {
			idReader = new BufferedReader(new FileReader(ID));
			titleReader = new BufferedReader(new FileReader(TITLE));

			File titleWriterFile = new File(DATASET_DIR + CITED_PAPERS_FILENAME);
			readTitleMap();
			if (!titleWriterFile.exists()) {
				titleWriter = new PrintWriter(new FileWriter(titleWriterFile));
				System.out.println("Creating cited papers at:" + DATASET_DIR
						+ CITED_PAPERS_FILENAME);
			}

		} catch (ClassNotFoundException | IOException e) {
			throw new RuntimeException(e);
		}
	}

	/**
	 * @param args
	 * @throws Exception
	 */
	public static void main(String[] args) throws Exception {
		CitationsGen cit = new CitationsGen();
		cit.generate();

	}

	void generate() throws IOException {
		int numCitation = 1;
		String curPaperId;
		String previousId = "", title;
		int tenPercentLineCount = 2500;//TOTAL_LINES_IN_DATASET / 10;

		long startTime = 0, endTime = 0;
		startTime = System.currentTimeMillis();
		int percentage = 0;
		while ((curPaperId = idReader.readLine()) != null && (title = titleReader.readLine())!=null) {
			numCitation++;
			Integer citedPaperId = titlesMap.get(title);

			if (0 == numCitation % tenPercentLineCount) 
			{
				endTime = System.currentTimeMillis();
				System.out.println("Time taken for " + (++percentage) + "% = " + ((endTime - startTime) / 1000));
				// break;
			}
			if (!curPaperId.equals(previousId) && numCitation>2) 
			{
				writeToFile();
				// Initialize Sets to read the new Paper citations
				citedPaperIds.clear();
			}
			citedPaperIds.add(citedPaperId);
			previousId = curPaperId;
		}

		// Write the last set of values
		if (citedPaperIds != null) {
			// write bagsOfWords to a file
			// write citedPaperIds to a file
			writeToFile();
		}
		if (titleWriter != null) {
			titleWriter.close();
		}
	}

	private void writeToFile() {
		Iterator<Integer> it2 = citedPaperIds.iterator();
		String write = it2.next().toString();
		while (it2.hasNext()) {
			Integer nextCited = it2.next();
			write = write + " " + nextCited;
		}
		if (titleWriter != null) {
			titleWriter.println(write);
		}
	}

	@SuppressWarnings("unchecked")
	private void readTitleMap() throws ClassNotFoundException, IOException {

		ObjectInputStream obj;
		File map = new File(TITLE2ID);
		if (!map.exists()) {
			storeMap(map);
		} else {
			obj = new ObjectInputStream(new FileInputStream(map));
			titlesMap = ((BiMap<String, Integer>) obj.readObject());
			obj.close();
		}
	}

	@SuppressWarnings({ "unchecked", "resource" })
	private void storeMap(File mapFile) throws IOException, ClassNotFoundException {
		PrintWriter writer = new PrintWriter(TITLE2ID_TXT);
		titlesMap = HashBiMap.create(240000);
		String title = null;
		int count = 1;
		BufferedReader br = new BufferedReader(new FileReader(TITLE));
		while ((title = br.readLine()) != null) {
			if (titlesMap.get(title) == null) {
				count++;
				titlesMap.put(title, count);
				writer.println(title + ": " + count);
				titleList.add(title);
			}
		}
		ObjectOutputStream stream = new ObjectOutputStream(new FileOutputStream(mapFile));
		stream.writeObject(titlesMap);

		File titleListFile = new File(TITLE_LIST);
		if (!titleListFile.exists()) {
			stream = new ObjectOutputStream(new FileOutputStream(titleListFile));
			stream.writeObject(titleList);
			stream.close();
		} else {
			ObjectInputStream obj = new ObjectInputStream(new FileInputStream(titleListFile));
			titleList = (ArrayList<String>) obj.readObject();
			obj.close();
		}
	}
}


/*  @SuppressWarnings("unchecked") 
  private void readTitleList() throws  ClassNotFoundException, IOException {
  
	  FileInputStream fin = new FileInputStream(TITLE2ID); ObjectInputStream obj;
	  File map = new File(TITLE2ID); if (!map.exists()) { storeTitleList(map); }
	  else{ obj = new ObjectInputStream(new FileInputStream(map)); titlesMap =
	  ((HashMap<String, Integer>) obj.readObject()); obj.close(); } fin.close(); 
  }
  
  @SuppressWarnings("unchecked") private void storeTitleList() throws
  IOException, ClassNotFoundException {
	  File titleListFile = new File(TITLE_LIST); 
	  if (!titleListFile.exists()) 
	  { 
		  ObjectOutputStream stream = new ObjectOutputStream(new FileOutputStream(titleListFile));
		  stream.writeObject(titleList); stream.close(); 	
	  } 
	  else{ ObjectInputStream obj
	  = new ObjectInputStream(new FileInputStream(titleListFile)); titlesMap =
	  ((HashMap<String, Integer>) obj.readObject()); obj.close(); 
  }
  
  }*/
 