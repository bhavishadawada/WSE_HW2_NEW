package edu.nyu.cs.cs2580;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.Vector;

import edu.nyu.cs.cs2580.SearchEngine.Options;

/**
 * @CS2580: Implement this class for HW2.
 */


public class IndexerInvertedOccurrence extends Indexer {

	// Stores all Document in memory.
	private Vector<Document> _documents = new Vector<Document>();
	private Map<String, Map<String, Map<Integer, List<Integer>>>> _characterMap;
	public IndexerInvertedOccurrence(Options options) {
		super(options);
		_characterMap = new HashMap<String, Map<String, Map<Integer, List<Integer>>>>();
		System.out.println("Using Indexer: " + this.getClass().getSimpleName());
	}
	
	@Override
	public void constructIndex() throws IOException {
		//String corpusFile = _options._corpusPrefix;
		DocProcessor dp = new DocProcessor(_options._corpusPrefix);
	    while(dp.hasNextDoc()){
	  	   dp.nextDoc();
	  	   processDocument(dp.title, dp.body);
	    }

	  /*
		String corpusFile = _options._corpusPrefix + "/testFile.tsv";
		System.out.println("Construct index from: " + corpusFile);

		BufferedReader reader = new BufferedReader(new FileReader(corpusFile));
		try {
			String line = null;
			while ((line = reader.readLine()) != null) {
				processDocument(line);
			}
		}
		finally {
			reader.close();
		}
		*/
	}

	private void processDocument(String content){
		//Document doc = new Document(_documents.size());
		Scanner s = new Scanner(content).useDelimiter("\t");
		int docId = _numDocs;
		DocumentIndexed doc = new DocumentIndexed(docId);
		String title = s.next();	
		doc.setTitle(title);
		buildMapFromTokens(title, docId);
		_documents.add(doc);
		++_numDocs;
		System.out.println(title);
	}
	
	public void buildMapFromTokens(String content,int docId){
		String[] tokens = content.split(" ");
		for (String token : tokens){
			 String start;
			Map<Integer, List<Integer>> docMap = new HashMap<Integer, List<Integer>>();
			List<Integer> occurrencesList = new ArrayList<Integer>();
			
		}
	}
	@Override
	public void loadIndex() throws IOException, ClassNotFoundException {
	}

	@Override
	public Document getDoc(int docid) {
		return null;
	}

	/**
	 * In HW2, you should be using {@link DocumentIndexed}.
	 */
	@Override
	public Document nextDoc(Query query, int docid) {
		return null;
	}

	@Override
	public int corpusDocFrequencyByTerm(String term) {
		return 0;
	}

	@Override
	public int corpusTermFrequency(String term) {
		return 0;
	}

	@Override
	public int documentTermFrequency(String term, String url) {
		SearchEngine.Check(false, "Not implemented!");
		return 0;
	}
}
