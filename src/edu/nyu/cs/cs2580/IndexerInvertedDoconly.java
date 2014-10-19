package edu.nyu.cs.cs2580;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Scanner;
import java.util.Set;
import java.util.Vector;

import edu.nyu.cs.cs2580.SearchEngine.Options;

/**
 * @CS2580: Implement this class for HW2.
 */

// check if this should implement serializable
public class IndexerInvertedDoconly extends Indexer {
	final int BULK_DOC_PROCESSING_SIZE = 2;

	// Data structure to maintain unique terms with id
	private Map<String, Integer> _dictionary;

	// Data structure to store number of times a term occurs in Document
	private Map<String,Integer> _documentTermFrequency;
	// Data structure to store number of times a term occurs in the complete Corpus
	private Map<String, Integer> _corpusTermFrequency;
	// Data structure to store unique terms in the document
	//private Vector<String> _terms = new Vector<String>();

	// Stores all Document in memory.
	private List<Document> _documents;
	private Map<Character, Map<String, List<Integer>>> _characterMap;


	public IndexerInvertedDoconly(Options options) {
		super(options);
		_documentTermFrequency = new HashMap<String,Integer>();
		_corpusTermFrequency = new HashMap<String,Integer>();
		_documents = new ArrayList<Document>();
		_characterMap = new HashMap<Character, Map<String, List<Integer>>>();
		System.out.println("Using Indexer: " + this.getClass().getSimpleName());
	}

	@Override
	public void constructIndex() throws IOException {
			String corpusFile = _options._corpusPrefix;
			System.out.println("Construct index from: " + corpusFile);

			//delete everything in index before constructIndex
			deleteFile();
			
			DocProcessor dp = new DocProcessor(_options._corpusPrefix);
			while (dp.hasNextDoc()) {
				// The problem is this will include num_views also
				dp.nextDoc();
				processDocument(dp.title, dp.body);

				if(_numDocs % BULK_DOC_PROCESSING_SIZE == 0){
					writeFile(_characterMap);
					_characterMap.clear();
					//writeFrequency(_corpusTermFrequency);
					//_corpusTermFrequency.clear();
				}
			}

			// if the documents are  < BULK_DOC_PROCESSING_SIZE
			if (!_characterMap.isEmpty()) {
				writeFile(_characterMap);
				_characterMap.clear();
			}
			if (!_documents.isEmpty()) {
				// store documents to disk
				_documents.clear();
			}
			if (!_corpusTermFrequency.isEmpty()) {
				writeFrequency(_corpusTermFrequency);
				_corpusTermFrequency.clear();
			}

			mergeAll();
			_documents.clear();
		}

		private void processDocument(String title, String body) {
			int docId = _numDocs;
			DocumentIndexed doc = new DocumentIndexed(docId);
			Set<String> uniqueTermSetTitle = Utility.tokenize(title);
			buildMapFromTokens(uniqueTermSetTitle,docId);
			doc.setTitle(title);
			// set the url here
			Set<String> uniqueTermSetBody = Utility.tokenize(body);
			buildMapFromTokens(uniqueTermSetBody,docId);
			// think if we need to store the term vector along with the doc object
			_documents.add(doc);
			++ _numDocs;
		}

		private void buildMapFromTokens(Set<String> uniqueTermSet, int docId){
			for(String token: uniqueTermSet){
				if(_corpusTermFrequency.containsKey(token)){
					int value = _corpusTermFrequency.get(token) + 1;
					_corpusTermFrequency.put(token, value);
				}
				else{
					_corpusTermFrequency.put(token, 1);
				}
				// check how to do document frequency here
				char start = token.charAt(0);
				if (_characterMap.containsKey(start)) {
					Map<String, List<Integer>> wordMap = _characterMap.get(start);
					if (wordMap.containsKey(token)) {
						List<Integer> docList = wordMap.get(token);
						if(!docList.contains(docId)){
							docList.add(docId);
						}
					}
					else{
						List<Integer> tempDocList = new ArrayList<Integer>();
						tempDocList.add(docId);
						wordMap.put(token, tempDocList);
					}
				}else{
					// else for if not characterMap
					Map<String, List<Integer>> tempMap = new HashMap<String, List<Integer>>();
					List<Integer> tempList = new ArrayList<Integer>();
					tempList.add(docId);
					tempMap.put(token,tempList);
					_characterMap.put(start,tempMap);		
				}
			}
		}

		private void writeFile( Map<Character, Map<String, List<Integer>>> _characterMap) throws IOException{
			for(Entry<Character, Map<String, List<Integer>>> entry : _characterMap.entrySet()){
				String path = _options._indexPrefix + "/" + entry.getKey() + ".idx";
				File file = new File(path);
				BufferedWriter writer = new BufferedWriter(new FileWriter(file, true));
				Map<String, List<Integer>> docMap = entry.getValue();
				for(Entry<String, List<Integer>> entry1 : docMap.entrySet()){
					String wordName = entry1.getKey();
					List<Integer> docList = entry1.getValue();
					writer.write(wordName + ":");
					StringBuffer sb = new StringBuffer();
					for(Integer docId : docList){
						sb.append(docId).append(" ");
					}
					writer.write(sb.toString());
					writer.write("\n");
				} 

				writer.close();
			}

		}

		private void deleteFile(){
			String path = _options._indexPrefix + "/";
    		File dir = new File(path);
			File[] fileLs = dir.listFiles();
			for(File file : fileLs){
				//if(file.getName().endsWith(".idx")){
					file.delete();
				//}
			}
		}

		private void writeFrequency(Map<String, Integer> frequency) throws IOException{
			String path = _options._indexPrefix + "/" + _numDocs + ".freq";
			File file = new File(path);
			OutputStream out = new FileOutputStream(file, true);
			for (Map.Entry<String, Integer> entry : frequency.entrySet()) {
				out.write(entry.getKey().getBytes());
				out.write(" ".getBytes());
				out.write(entry.getValue().toString().getBytes());
				out.write("\n".getBytes());
			}
			out.close();
		}

		private void mergeAll() throws IOException{
			List<String> files = Utility.getFilesInDirectory(_options._indexPrefix);
			for (String file : files) {
				if (file.endsWith(".idx")) {
					System.out.println("merging files " + file);
					Map<Character, Map<String,List<Integer>>> charMap = readAll(file);
					String fileName = _options._indexPrefix + "/" + file;
					File charFile = new File(fileName);
					charFile.delete();
					writeFile(charMap);
				}
			}
		}

		private Map<Character, Map<String, List<Integer>>> readAll(String fileName) throws FileNotFoundException{
			String file = _options._indexPrefix + "/" + fileName;
			Scanner scan = new Scanner(new File(file));
			Map<Character, Map<String, List<Integer>>> CharacterMap = new HashMap<Character, Map<String, List<Integer>>>();
			Map<String, List<Integer>> tempMap = new HashMap<String, List<Integer>>();
			while(scan.hasNextLine()){
				String line = scan.nextLine();
				String lineArray[] = line.split(":");
				if(lineArray.length == 2){
					String word = lineArray[0];
					String[] docIDList = lineArray[1].split(" ");
					List<Integer> docList = new ArrayList<Integer>();
					for(int i = 0; i < docIDList.length; i++){
						Integer docId = Integer.parseInt(docIDList[i].trim());
						docList.add(docId);	
					}
					if(tempMap.containsKey(word)){
						List<Integer> tempList = tempMap.get(word);
						tempList.addAll(docList);
						tempMap.put(word,tempList);
					}
					else{
						tempMap.put(word, docList);
					}
				}
			}

			CharacterMap.put(fileName.charAt(0),tempMap);
			scan.close();
			return CharacterMap;

		}


		// This is used when the SearchEngine is called with the serve option
		@Override
		public void loadIndex() throws IOException, ClassNotFoundException {
		}

		@Override
		public Document getDoc(int docid) {
			return null;
		}

		/**
		 * In HW2, you should be using {@link DocumentIndexed}
		 */

		//TODO: This is to be implemented as discussed in class?????
		@Override
		public Document nextDoc(Query query, int docid) {
			query.processQuery();
			List<String> queryVector = query._tokens;
			for (String search : queryVector) {
				String fileName = _options._indexPrefix + "/"
						+ search.charAt(0) + ".idx";
				System.out.println("Search in" + fileName);
				grepFile(search,
						fileName);
			}
			return null;
		}

		private void grepFile(String search, String fileName){
			String cmd = "grep '\\<" + search + "\\>' " + fileName;
			System.out.println(cmd);
		}

		// number of documents the term occurs in
		@Override
		public int corpusDocFrequencyByTerm(String term) {
			return _dictionary.containsKey(term) ?
					_documentTermFrequency.get(_dictionary.get(term)) : 0;
		}

		//number of times a term appears in corpus
		@Override
		public int corpusTermFrequency(String term) {
			return _dictionary.containsKey(term) ?
					_corpusTermFrequency.get(_dictionary.get(term)) : 0;
		}

		// number of times a term occurs in document
		@Override
		public int documentTermFrequency(String term, String url) {
			SearchEngine.Check(false, "Not implemented!");
			return 0;
		}
	}
