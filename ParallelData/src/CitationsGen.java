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


public class CitationsGen {

	private HashMap<String, Integer> titlesMap;
	private BufferedReader idReader;
	BufferedReader titleReader ;
	private PrintWriter titleWriter;
	private Set<Integer> citedPaperIds = new HashSet<>();
	private ArrayList<String> titleList = new ArrayList<>();
	int[] titleVCBMap = new int[600000];

	@SuppressWarnings("unchecked")
	CitationsGen() throws IOException, ClassNotFoundException{
		idReader = new BufferedReader(new FileReader("ID"));
		titleReader = new BufferedReader(new FileReader("Title"));
		titleWriter = new PrintWriter(new FileWriter("CitedPapers"));
		ObjectInputStream obj;
		File map = new File("Map.ser");
		if (!map.exists())
		{
			storeMap(map);
		}
		obj = new ObjectInputStream(new FileInputStream(map));
		titlesMap = ((HashMap<String, Integer>) obj.readObject());
		obj.close();
	}

	/**
	 * @param args
	 * @throws Exception
	 */
	public static void main(String[] args) throws Exception {
		CitationsGen cit = new CitationsGen();
		//cit.storeTitleVCB("CitedPapers.vcb");
		cit.generate();

	}

	void generate() throws IOException {
		int numCitation = 0;
		String curPaperId;
		String previousId = "", title;

		curPaperId = idReader.readLine(); // read the column name
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
				// Read corresponding paper title, fetch correspoding ID
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

	private void readTitleMap() throws ClassNotFoundException, IOException {

		FileInputStream fin = new FileInputStream("Map.ser");
//		ObjectInputStream ois = new ObjectInputStream(fin);
//		titlesMap = (HashMap<String, Integer>) ois.readObject();

	}

	private void storeMap(File mapFile) throws IOException{
		HashMap<String, Integer> map = new HashMap<>();
		String title = null;
		int count = 0;
		while((title = titleReader.readLine())!=null){
			if(map.get(title)==null){
				count++;
				map.put(title, count);
				titleList.add(title);
			}
		}
		ObjectOutputStream stream = new ObjectOutputStream(new FileOutputStream(mapFile));
		stream.writeObject(map);
		//stream = new ObjectOutputStream(new FileOutputStream("TitleList.ser"));
		//stream.writeObject(titleList);
		stream.close();
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
		ObjectOutputStream stream = new ObjectOutputStream(new FileOutputStream("VCBTitleBridge.ser"));
		stream.writeObject(titleVCBMap);
		stream.close();

	}
}
