package edu.nyu.cs.cs2580;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
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
	private Map<Character, Map<String, Map<Integer, List<Integer>>>> _characterMap;
	final int BULK_DOC_PROCESSING_SIZE = 2;
	
	
	
	public IndexerInvertedOccurrence(Options options) {
		super(options);
		_characterMap = new HashMap<Character, Map<String, Map<Integer, List<Integer>>>>();
		System.out.println("Using Indexer: " + this.getClass().getSimpleName());
	}
	
	@Override
	public void constructIndex() throws IOException {
		//String corpusFile = _options._corpusPrefix;
		/*DocProcessor dp = new DocProcessor(corpusFile);
	  while(dp.hasNextDoc()){
		  dp.nextDoc();
		  processDocument(dp.title, dp.body);
	  }*/

		String corpusFile = _options._corpusPrefix + "/testFile.tsv";
		System.out.println("Construct index from: " + corpusFile);

		BufferedReader reader = new BufferedReader(new FileReader(corpusFile));
		try {
			String line = null;
			while ((line = reader.readLine()) != null) {
				processDocument(line);
				if (_numDocs % BULK_DOC_PROCESSING_SIZE == 0) {
					writeFile(_characterMap);
					_characterMap.clear();
					//writeFrequency(_corpusTermFrequency);
					//_corpusTermFrequency.clear();
				}
			}
		}
		finally {
			reader.close();
		}
	}

	private void processDocument(String content){
		//Document doc = new Document(_documents.size());
		Scanner s = new Scanner(content).useDelimiter("\t");
		int docId = _numDocs;
		DocumentIndexed doc = new DocumentIndexed(docId);
		String title = s.next();	
		doc.setTitle(title);
		// check if this works correctly
		buildMapFromTokens(content, docId);
		_documents.add(doc);
		++_numDocs;
		//System.out.println(title);
	}
	
	public void buildMapFromTokens(String content,int docId){
		String[] tokens = content.split(" ");
		int tokenIndex = 0;
		for (String token : tokens){
			 Map<Integer, List<Integer>> newdocMap = new HashMap<Integer, List<Integer>>();
			List<Integer> occurrencesList = new ArrayList<Integer>();
			Character start = token.charAt(0);
			if (_characterMap.containsKey(start)) {
			    Map<String, Map<Integer, List<Integer>>> wordMap = _characterMap.get(start);
				if (wordMap.containsKey(token)) {
					Map<Integer, List<Integer>> docMap = wordMap.get(token);
					if (docMap.containsKey(docId)) {
						docMap.get(docId).put(tokenIndex);
					} else {
						occurrencesList.add(tokenIndex);
						docMap.put(docId, occurrencesList);
					}	
				}
				else{
					occurrencesList.add(tokenIndex);
					newdocMap.put(docId, occurrencesList);
					wordMap.put(token, newdocMap);
				}
			}else{
				Map<String, Map<Integer, Integer>> tempMap = new HashMap<String, Map<Integer, Integer>>();
				Map<Integer, Integer> tempInnerMap = new TreeMap<Integer, Integer>();
				wordMap.put(token, newdocMap);
				_characterMap.put(start, wordMap);
			}
			tokenIndex ++;		
		}
	}
	
	private void writeFile(
			Map<Character, Map<String, Map<Integer, Integer>>> characterMap)
			throws IOException {
		for (Map.Entry<Character, Map<String, Map<Integer, Integer>>> entry : characterMap
				.entrySet()) {
			String path = _options._indexPrefix + "/" + entry.getKey() + ".idx";
			File file = new File(path);
			BufferedWriter write = new BufferedWriter(
					new FileWriter(file, true));
			Map<String, Map<Integer, Integer>> tempMap = entry.getValue();
			for (Map.Entry<String, Map<Integer, Integer>> entry1 : tempMap
					.entrySet()) {
				String wordName = entry1.getKey() + ":";
				Map<Integer, Integer> innerMostMap = entry1.getValue();
				write.write(wordName);
				StringBuffer sb = new StringBuffer();
				for (Map.Entry<Integer, Integer> innerEntry : innerMostMap
						.entrySet()) {
					sb.append(innerEntry.getKey()).append(",").append(innerEntry.getValue());
				}
				write.write(sb.toString());
				write.write("\n");
				
			}
			write.close();
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
