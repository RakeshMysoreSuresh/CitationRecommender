import java.io.Serializable;


public class ContextFreq implements Serializable {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	String word;
	int freq;
	double icf;
	
	public ContextFreq(String s, int n, double icf) {
		word = s;
		freq = n;
		this.icf = icf;
	}
}
