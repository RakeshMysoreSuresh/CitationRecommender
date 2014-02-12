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


public class CitationsGen extends Config{

	private HashMap<String, Integer> titlesMap;
	private BufferedReader idReader;
	BufferedReader titleReader ;
	private PrintWriter titleWriter;
	private Set<Integer> citedPaperIds = new HashSet<>();
	private ArrayList<String> titleList = new ArrayList<>();
	int[] titleVCBMap = new int[600000];

	CitationsGen(){
		try {
			idReader = new BufferedReader(new FileReader(ID));
			titleReader = new BufferedReader(new FileReader(TITLE));
			
			File titleWriterFile = new File(DATASET_DIR + CITED_PAPERS_FILENAME);
			if (!titleWriterFile.exists())
			{
				titleWriter = new PrintWriter(new FileWriter(titleWriterFile));
				System.out.println("Creating cited papers at:" + DATASET_DIR + CITED_PAPERS_FILENAME);			
				readTitleMap();
				generate();
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
		int numCitation = 0;
		String curPaperId;
		String previousId = "", title;
		int tenPercentLineCount = TOTAL_LINES_IN_DATASET/10;

		System.out.println(idReader.readLine() + " : " + titleReader.readLine()); // read the column name
		//System.out.println(idReader.readLine() + " : " + titleReader.readLine()); // read the column name
		long startTime = 0, endTime = 0;
		startTime = System.currentTimeMillis();
		int percentage=0;
		while ((curPaperId = idReader.readLine()) != null) {
			numCitation++;

			if (0 == numCitation % tenPercentLineCount) {
				endTime = System.currentTimeMillis();
				System.out.println("Time taken for "+(++percentage)+"% = "
						+ ((endTime - startTime) / 1000));
				//break;
			}
			if (curPaperId.equals(previousId)) {
				// Read one citation context and get bag of words
				// Read corresponding paper title, fetch corresponding ID
				title = titleReader.readLine();
				Integer citedPaperId = titlesMap.get(title);
				if(citedPaperId == null){
					System.out.println(title);
				}
				citedPaperIds .add(citedPaperId);
			}
			else {
				if (citedPaperIds.size() !=0) {
					// write bagsOfWords to a file
					// write citedPaperIds to a file
					writeToFile();
				}
				// Initialize Sets to read the new Paper citations
				citedPaperIds.clear();
				// Read corresponding paper title, fetch corresponding ID
				title = titleReader.readLine();
				int citedPaperId = titlesMap.get(title);
				citedPaperIds.add(citedPaperId);
			}
			previousId = curPaperId;
		}

		// Write the last set of values
		if (citedPaperIds !=null) {
			// write bagsOfWords to a file
			// write citedPaperIds to a file
			writeToFile();
		}
		titleWriter.close();

	}

	private void writeToFile() {
		Iterator<Integer> it2 = citedPaperIds.iterator();
		String write= it2.next().toString();
		while(it2.hasNext()){
			Integer nextCited = it2.next();
			write = write +" "+ nextCited;
		}
		titleWriter.println(write);
	}

	@SuppressWarnings("unchecked")
	private void readTitleMap() throws ClassNotFoundException, IOException {

		ObjectInputStream obj;
		File map = new File(TITLE2ID);
		if (!map.exists())
		{
			storeMap(map);
		}
		else{
			obj = new ObjectInputStream(new FileInputStream(map));
			titlesMap = ((HashMap<String, Integer>) obj.readObject());
			obj.close();
		}
	}

	@SuppressWarnings({ "unchecked", "resource" })
	private void storeMap(File mapFile) throws IOException, ClassNotFoundException{
		titlesMap = new HashMap<>(240000);
		String title = null;
		int count = 0;
		BufferedReader br = new BufferedReader(new FileReader(TITLE));
		while((title = br.readLine())!=null){
			if(titlesMap.get(title)==null){
				count++;
				titlesMap.put(title, count);
				titleList.add(title);
			}
		}
		ObjectOutputStream stream = new ObjectOutputStream(new FileOutputStream(mapFile));
		stream.writeObject(titlesMap);
		
		File titleListFile = new File(TITLE_LIST);
		if (!titleListFile.exists())
		{
			stream = new ObjectOutputStream(new FileOutputStream(titleListFile));
			stream.writeObject(titleList);
			stream.close();
		}
		else{
			ObjectInputStream obj = new ObjectInputStream(new FileInputStream(titleListFile));
			titleList = (ArrayList<String>)obj.readObject();
			obj.close();
		}
	}

	void storeTitleVCB(String fileName) throws Exception{
		BufferedReader r = new BufferedReader(new FileReader(fileName));
		String s;
		String[] spl;
		//To remove null
		r.readLine();
		while((s=r.readLine())!=null){
			spl = s.split(" ");
			int index = Integer.parseInt(spl[0]),
					val = Integer.parseInt(spl[1]);
			titleVCBMap[index] = val;
		}
		r.close();
		ObjectOutputStream stream = new ObjectOutputStream(new FileOutputStream(CITED_VCB_PAPER_ID_TO_TITLE_SER_MAP));
		stream.writeObject(titleVCBMap);
		stream.close();
	}
}

/*@SuppressWarnings("unchecked")
private void readTitleList() throws ClassNotFoundException, IOException {

	FileInputStream fin = new FileInputStream(TITLE2ID);
	ObjectInputStream obj;
	File map = new File(TITLE2ID);
	if (!map.exists())
	{
		storeTitleList(map);
	}
	else{
		obj = new ObjectInputStream(new FileInputStream(map));
		titlesMap = ((HashMap<String, Integer>) obj.readObject());
		obj.close();
	}
	fin.close();
}

@SuppressWarnings("unchecked")
private void storeTitleList() throws IOException, ClassNotFoundException
{
	File titleListFile = new File(TITLE_LIST);
	if (!titleListFile.exists())
	{
		ObjectOutputStream stream = new ObjectOutputStream(new FileOutputStream(titleListFile));
		stream.writeObject(titleList);
		stream.close();
	}
	else{
		ObjectInputStream obj = new ObjectInputStream(new FileInputStream(titleListFile));
		titlesMap = ((HashMap<String, Integer>) obj.readObject());
		obj.close();
	}
	
}*/