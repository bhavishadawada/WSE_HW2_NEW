package edu.nyu.cs.cs2580;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Scanner;
import java.util.TreeMap;
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
			if (!_characterMap.isEmpty()) {
				writeFile(_characterMap);
				_characterMap.clear();
			}
			if (!_documents.isEmpty()) {
				// store documents to disk
				_documents.clear();
			}
			/*if (!_corpusTermFrequency.isEmpty()) {
					writeFrequency(_corpusTermFrequency);
					_corpusTermFrequency.clear();
				}*/

			mergeAll();
			_documents.clear();
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
		String[] tokens = content.toLowerCase().split("\\s+");
		int tokenIndex = 0;
		for (String token : tokens){
			// improve this if it runs slow. 
			Character start = token.charAt(0);
			if (_characterMap.containsKey(start)) {
				Map<String, Map<Integer, List<Integer>>> wordMap = _characterMap.get(start);
				if (wordMap.containsKey(token)) {
					Map<Integer, List<Integer>> docMap = wordMap.get(token);
					if (docMap.containsKey(docId)) {
						List<Integer> occurenceList = docMap.get(docId);
						occurenceList.add(tokenIndex);
						docMap.put(docId, occurenceList);
					}
					else{
						List<Integer> occurrencesList = new ArrayList<Integer>();
						occurrencesList.add(tokenIndex);
						docMap.put(docId, occurrencesList);
						wordMap.put(token, docMap);
					}
				}
				// if the word map does not have the string
				else{
					Map<Integer, List<Integer>> tempInnerMap = new TreeMap<Integer, List<Integer>>();
					List<Integer> occurrencesList = new ArrayList<Integer>();
					occurrencesList.add(tokenIndex);
					tempInnerMap.put(docId,occurrencesList);
					wordMap.put(token, tempInnerMap);
				}
			}
			else{
				Map<String,Map<Integer,List<Integer>>> tempMap = new HashMap<String, Map<Integer,List<Integer>>>();
				List<Integer> occurrencesList = new ArrayList<Integer>();
				occurrencesList.add(tokenIndex);
				Map<Integer, List<Integer>> tempInnerMap = new TreeMap<Integer, List<Integer>>();
				tempInnerMap.put(docId,occurrencesList);
				tempMap.put(token, tempInnerMap);
				_characterMap.put(start, tempMap);
			}
			tokenIndex ++ ;
		}
	}

	private void writeFile(
			Map<Character, Map<String, Map<Integer, List<Integer>>>> characterMap)
					throws IOException {
		for (Entry<Character, Map<String, Map<Integer, List<Integer>>>> entry : characterMap
				.entrySet()) {
			String path = _options._indexPrefix + "/" + entry.getKey() + ".idx";
			File file = new File(path);
			BufferedWriter write = new BufferedWriter(
					new FileWriter(file, true));
			Map<String, Map<Integer, List<Integer>>> tempMap = entry.getValue();
			for (Entry<String, Map<Integer, List<Integer>>> entry1 : tempMap
					.entrySet()) {
				String wordName = entry1.getKey() + "::";
				Map<Integer, List<Integer>> innerMostMap = entry1.getValue();
				write.write(wordName);
				StringBuffer sb = new StringBuffer();
				for (Entry<Integer, List<Integer>> innerEntry : innerMostMap
						.entrySet()) {
					sb.append(innerEntry.getKey()).append(":").append(	innerEntry.getValue()).append("  ");
				}
				write.write(sb.toString());
				write.write("\n");

			}
			write.close();
		}
	}

	private void mergeAll() throws IOException{
		List<String> files = Utility.getFilesInDirectory(_options._indexPrefix);
		for (String file : files) {
			if (file.endsWith(".idx")) {
				System.out.println("merging files " + file);
				Map<Character, Map<String, Map<Integer, List<Integer>>>> CharacterMap = readAll(file);
				String fileName = _options._indexPrefix + "/" + file;
				File charFile = new File(fileName);
				charFile.delete();
				writeFile(CharacterMap);
			}
		}
	}

	private Map<Character, Map<String, Map<Integer, List<Integer>>>> readAll(String fileName) throws FileNotFoundException{
		String file = _options._indexPrefix + "/" + fileName;
		Scanner scan = new Scanner(new File(file));
		Map<Character, Map<String, Map<Integer, List<Integer>>>> CharacterMap
		= new HashMap<Character, Map<String,Map<Integer,List<Integer>>>>();
		Map<String, Map<Integer, List<Integer>>> tempMap = new HashMap<String, Map<Integer, List<Integer>>>();
		
		while(scan.hasNextLine()){
			String line = scan.nextLine();
			String lineArray[] = line.split("::");
			String word = lineArray[0];
			Map<Integer, List<Integer>> innerMap = null;
			if (tempMap.containsKey(word)){
				innerMap = tempMap.get(word);
			}
			else{
				innerMap = new TreeMap<Integer, List<Integer>>();
			}
			String[] docIDList = lineArray[1].split("  ");
			
			for(String docEntry : docIDList){
				List<Integer> occurenceList = new ArrayList<Integer>();
				String[] tempDocIdList = docEntry.split(":");
				Integer docId = Integer.parseInt(tempDocIdList[0]);
				String rstr = tempDocIdList[1].replaceAll("[\\[, \\]]", " ");
				String[] occurence = rstr.trim().split("\\s+");
				//String[] occurence = tempDocIdList[1].replaceAll("[", " ").split(",");
				for(int i = 0; i < occurence.length; i++){
					occurenceList.add(Integer.parseInt(occurence[i]));
				}		
				innerMap.put(docId, occurenceList);
				tempMap.put(word,innerMap);
			}
		}
		
		CharacterMap.put(fileName.charAt(0), tempMap);
		scan.close();
		return CharacterMap;


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