package edu.nyu.cs.cs2580;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;
import java.util.Scanner;
import java.util.TreeMap;
import java.util.Vector;

import edu.nyu.cs.cs2580.SearchEngine.Options;

/**
 * @CS2580: Implement this class for HW2.
 */


public class IndexerInvertedOccurrence extends Indexer  implements Serializable{

	// Stores all Document in memory.
	private static final long serialVersionUID = -3852089003709472813L;

	final int BULK_DOC_PROCESSING_SIZE = 300;

	// Data structure to maintain unique terms with id
	Map<String, Integer> _dictionary = new HashMap<String, Integer>();

	// Data structure to store number of times a term occurs in Document
	// term id --> frequency
	ArrayList<Integer> _documentTermFrequency = new ArrayList<Integer>();

	// Data structure to store number of times a term occurs in the complete Corpus
	// term id --> frequency
	ArrayList<Integer> _corpusTermFrequency = new ArrayList<Integer>();
	
	private ArrayList<Integer> _termLineNum = new ArrayList<Integer>();
	
	List<DocumentIndexed> _documents = new ArrayList<DocumentIndexed>();

	private Map<Character, Map<String, Map<Integer, List<Integer>>>> _characterMap = 
			new HashMap<Character, Map<String, Map<Integer, List<Integer>>>>();
	
	// use buffer of post list to reduce file IO
	private HashMap<String, PostListOccurence> _postListBuf = 
			new HashMap<String, PostListOccurence>();
	int _postListBufSize = 1000;
	
	

	// Provided for serialization
	public IndexerInvertedOccurrence(){ }

	public IndexerInvertedOccurrence(Options options) {
		super(options);
		_characterMap = new HashMap<Character, Map<String, Map<Integer, List<Integer>>>>();
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
				writeFile(_characterMap, false);
				_characterMap.clear();
				//writeFrequency(_corpusTermFrequency);
				//_corpusTermFrequency.clear();
			}
		}

		if (!_characterMap.isEmpty()) {
			writeFile(_characterMap, false);
			_characterMap.clear();
		}
		mergeAll();
		
		System.out.println("_dictionary size: " + get_dictionary().size());
		String indexFile = _options._indexPrefix + "/corpus.idx";
		System.out.println("Write Indexer to " + indexFile);

	    ObjectOutputStream writer = new ObjectOutputStream(new FileOutputStream(indexFile));
	    writer.writeObject(this);
	    writer.close();
	}

	private void processDocument(String title, String body){
		//Document doc = new Document(_documents.size());
		int docId = _numDocs;
		++_numDocs;
		List<String> bodyTermVector = Utility.tokenize2(body);
		// check if this works correctly
		buildMapFromTokens(bodyTermVector, docId);
		
		Set<String> uniqueTermSetBody = Utility.tokenize(body);

		//build _dictionary
		for(String token:uniqueTermSetBody){
	        if(!get_dictionary().containsKey(token)){
	        	get_dictionary().put(token, get_corpusTermFrequency().size());
	        	get_corpusTermFrequency().add(0);
	        	get_documentTermFrequency().add(0);
	        	_termLineNum.add(0);
	        }
	        int id = get_dictionary().get(token);
	        get_documentTermFrequency().set(id, get_documentTermFrequency().get(id) + 1);
		}
		
		for(String token : bodyTermVector){
	        if(get_dictionary().containsKey(token)){
	        	int id = get_dictionary().get(token);
				get_corpusTermFrequency().set(id, get_corpusTermFrequency().get(id) + 1);
	        }
	        else{
	        	System.out.println(token + " is not in _dictionary in processDocument()");
	        }
		}

		DocumentIndexed doc = new DocumentIndexed(docId);
		doc.setTitle(title);
		doc._termNum = bodyTermVector.size();
		doc.setUrl(Integer.toString(docId));

		get_documents().add(doc);
	}

	public void buildMapFromTokens(List<String> tokens,int docId){
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

	private void writeFile(Map<Character, Map<String, Map<Integer, List<Integer>>>> characterMap, Boolean record)
					throws IOException {
		int lineNum = 0;
		for (Entry<Character, Map<String, Map<Integer, List<Integer>>>> entry : characterMap.entrySet()) {
			String path = _options._indexPrefix + "/" + entry.getKey() + ".idx";
			File file = new File(path);
			BufferedWriter write = new BufferedWriter(new FileWriter(file, true));
			Map<String, Map<Integer, List<Integer>>> tempMap = entry.getValue();
			for (Entry<String, Map<Integer, List<Integer>>> entry1 : tempMap.entrySet()) {
				String wordName = entry1.getKey();
				Map<Integer, List<Integer>> innerMostMap = entry1.getValue();
				write.write(wordName + "::");
				StringBuffer sb = new StringBuffer();
				for (Entry<Integer, List<Integer>> innerEntry : innerMostMap.entrySet()) {
					sb.append(innerEntry.getKey()).append(":").append(innerEntry.getValue()).append("  ");
				}
				write.write(sb.toString());
				write.write("\n");
				lineNum++;
				if(record){
					if(get_dictionary().containsKey(wordName)){
						//System.out.println("word: " + wordName + " " + lineNum);
						int id = get_dictionary().get(wordName);
						_termLineNum.set(id, lineNum);
					}
					else{
						System.out.println(wordName + " is not in _dictionary");
					}
				}
			}
			write.close();
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

	private void mergeAll() throws IOException{
		List<String> files = Utility.getFilesInDirectory(_options._indexPrefix);
		for (String file : files) {
			if (file.endsWith(".idx")) {
				System.out.println("merging files " + file);
				Map<Character, Map<String, Map<Integer, List<Integer>>>> CharacterMap = readAll(file);
				String fileName = _options._indexPrefix + "/" + file;
				File charFile = new File(fileName);
				charFile.delete();
				writeFile(CharacterMap, true);
			}
		}
	}

	private Map<Character, Map<String, Map<Integer, List<Integer>>>> readAll(String fileName) throws FileNotFoundException{
		Map<Character, Map<String, Map<Integer, List<Integer>>>> CharacterMap
		= new HashMap<Character, Map<String,Map<Integer,List<Integer>>>>();
		Map<String, Map<Integer, List<Integer>>> tempMap = new HashMap<String, Map<Integer, List<Integer>>>();

		String file = _options._indexPrefix + "/" + fileName;
		BufferedReader reader = new BufferedReader(new FileReader(file));
		try {
		      String line = null;
		      while ((line = reader.readLine()) != null) {
		    	  String lineArray[] = line.split("::");
		    	  if(!lineArray[0].equals("")){
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
		      }
		} catch (NumberFormatException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} finally {
		      try {
				reader.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
	    }
		
		CharacterMap.put(fileName.charAt(0), tempMap);
		return CharacterMap;


	}
	@Override
	public void loadIndex() throws IOException, ClassNotFoundException {
	    String indexFile = _options._indexPrefix + "/corpus.idx";
	    System.out.println("Load index from: " + indexFile);

	    ObjectInputStream reader = new ObjectInputStream(new FileInputStream(indexFile));

	    IndexerInvertedOccurrence loaded = (IndexerInvertedOccurrence) reader.readObject();

	    this.set_documents(loaded.get_documents());
	    this.set_dictionary(loaded.get_dictionary());
	    this._numDocs = get_documents().size();
	    this.set_corpusTermFrequency(loaded.get_corpusTermFrequency());
	    this.set_documentTermFrequency(loaded.get_documentTermFrequency());
	    this._termLineNum = loaded._termLineNum;
	    for (Integer freq : loaded.get_corpusTermFrequency()) {
	        this._totalTermFrequency += freq;
	    }
	    System.out.println(Integer.toString(_numDocs) + " documents loaded " +
	        "with " + Long.toString(_totalTermFrequency) + " terms!");
	    reader.close();
	    
	    System.out.println("dic size: " + get_dictionary().size());
	    /*
	    Query query = new Query("Alfred Matthew");
	    Document doc = nextDoc(query, -1);
	    System.out.println(doc.getTitle());
	    doc = nextDoc(query, doc._docid);
	    */
	}
	
	public PostListOccurence buildPostLs(String line){
		String lineArray[] = line.split("::");
		TreeMap<Integer, ArrayList<Integer>> innerMap = new TreeMap<Integer, ArrayList<Integer>>();
		if(!lineArray[0].equals("")){
		    String word = lineArray[0];

		    String[] docIDList = lineArray[1].split("  ");
		
		    for(String docEntry : docIDList){
		    	ArrayList<Integer> occurenceList = new ArrayList<Integer>();
		    	String[] tempDocIdList = docEntry.split(":");
		    	Integer docId = Integer.parseInt(tempDocIdList[0]);
		    	String rstr = tempDocIdList[1].replaceAll("[\\[, \\]]", " ");
		    	String[] occurence = rstr.trim().split("\\s+");
		    	//String[] occurence = tempDocIdList[1].replaceAll("[", " ").split(",");
		    	for(int i = 0; i < occurence.length; i++){
		    		occurenceList.add(Integer.parseInt(occurence[i]));
		    	}		
		    	innerMap.put(docId, occurenceList);
		    }

		    return new PostListOccurence(word, innerMap);
		}
		else{
			return null;
		}
	}


	@Override
	public DocumentIndexed getDoc(int docid) {
		if(docid < _documents.size()){
			return _documents.get(docid);
		}
		else{
			return null;
		}
	}

	/**
	 * In HW2, you should be using {@link DocumentIndexed}
	 */
	
	public PostListOccurence getPostList(String term){
		if(_postListBuf.size() > _postListBufSize){
			_postListBuf.clear();
		}
		if(_postListBuf.containsKey(term)){
			return _postListBuf.get(term);
		}
		if(get_dictionary().containsKey(term)){

			int lineNum = _termLineNum.get(get_dictionary().get(term));
			String fileName = _options._indexPrefix + "/"+ term.charAt(0) + ".idx";

			//System.out.println("queryTerm " + term);
			//System.out.println("Search in " + fileName);
			//System.out.println("lineNum " + lineNum);

			
			// build post list
			BufferedReader br;
			try {
				br = new BufferedReader(new FileReader(fileName));
			    String line = "";
			    int li = 0;
			    while(line != null && li < lineNum){
			    	li++;
			    	line = br.readLine();
			    }
			    if(li == lineNum){
			    	PostListOccurence postLs = buildPostLs(line);

			    	//buffer the post list to reduce file IO
			    	_postListBuf.put(term, postLs);

			    	return postLs;
			    }
			    else{
			    	System.out.println("error lineNum: " + li);
			    	return null;
			    }
			} catch (FileNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		return null;
	}

	//TODO: This is to be implemented as discussed in class?????
	@Override
	public DocumentIndexed nextDoc(QueryPhrase query, int docid) {
		ArrayList<ArrayList<Integer>> postLsArr = new ArrayList<ArrayList<Integer>>();
		ArrayList<Integer> cache = new ArrayList<Integer>();

		//query.processQuery();
		List<String> queryVector = query._tokens;
		for (String search : queryVector) {
			// build post list
 	
			PostListOccurence temp = getPostList(search);
			if(temp == null){
				return null;
			}
			else{
				ArrayList<Integer>  postLs =  new ArrayList<Integer>(temp.data.navigableKeySet());
				postLsArr.add(postLs);
				cache.add(0);
			}
		}
		
		if(postLsArr.size() > 0){
			Boolean hasNextDocId = true;
		    while(hasNextDocId){
		    	int nextDocId = -1;
		    	int cnt = 0;
		    	for(int i = 0; i < postLsArr.size(); i++){
		    		int c = cache.get(i);
		    		ArrayList<Integer> postLs = postLsArr.get(i);
		    		c = postLsNext(postLs, c, docid);
		    		cache.set(i, c);
		    		if(c == -1){
		    			hasNextDocId = false;
		    			break;
		    		}
		    		else{
		    			int currDocId = postLs.get(c);
		    			if(nextDocId == -1){
		    				nextDocId = currDocId;
		    				cnt++;
		    			}
		    			else{
		    				if(nextDocId == currDocId){
		    					cnt++;
		    				}
		    				else{
		    					nextDocId = Math.max(nextDocId, currDocId);
		    				}
		    			}
		    		}
		    	}
		    	if(cnt == postLsArr.size()){
		    		//System.out.println("document found " + nextDocId);
		    		//check phrase here
		    		boolean ret = true;
		    		for(List<String> phrase : query._phraseTokens){
		    			ret = ret & checkPhrase(phrase, nextDocId);
		    		}
		    		if(ret){
		    			return _documents.get(nextDocId);
		    		}
		    		else{
		    			docid = nextDocId;
		    		}
		    	}
		    	else{
		    		docid = nextDocId - 1;
		    	}
		    }
		    return null;
		}
		else{
			return null;
		}
	}
	
	public boolean checkPhrase(List<String> phrase, int docid){
		ArrayList<ArrayList<Integer>> occurLsArr = new ArrayList<ArrayList<Integer>>();
		ArrayList<Integer> cache = new ArrayList<Integer>();
		for(String term : phrase){
			occurLsArr.add(getPostList(term).data.get(docid));
			cache.add(0);
		}
		Boolean hasNextDocId = true;
		int pos = -1;
		while(hasNextDocId){
			int cnt = 0;
			for(int i = 0; i < occurLsArr.size(); i++){
				int c = cache.get(i);
				ArrayList<Integer> occurLs = occurLsArr.get(i);
				c = occurNext(occurLs, c, pos);
				cache.set(i, c);
				if(c == -1){
					hasNextDocId = false;
					break;
				}
				else{
					if(cnt == 0){
						pos = occurLs.get(c);
						cnt++;
					}
					else{
						if(pos+1 == occurLs.get(c)){
							cnt++;
						}
						pos = Math.max(pos, occurLs.get(c));
						//System.out.println("pos: " + pos + "c: " + c);
					}
				}
			}
			if(cnt == phrase.size()){
				return true;
			}
		}
		return false;
	}
	
	public int occurNext(ArrayList<Integer> occurLs, int cache, int pos){
		int last = occurLs.size() - 1;
		if(cache<0){
			return -1;
		}
		else if(occurLs.get(last) <= pos){
			return -1;
		}

		while(cache < occurLs.size() && occurLs.get(cache) <= pos){
			cache++;
		}

		if(cache == occurLs.size()){
			return -1;
		}
		else{
			return cache;
		}
	}
	
	// return a pos such that posLs.get(pos)> docid
	public int postLsNext(ArrayList<Integer> postLs, int cache, int docid){
		int last = postLs.size() - 1;
		if(cache < 0){
			return -1;
		}
		else if(cache > last){
			return -1;
		}
		else if(postLs.get(last) <= docid){
			return -1;
		}
		while(cache < postLs.size() && postLs.get(cache) <= docid){
			cache++;
		}

		if(postLs.get(cache) > docid){
			return cache;
		}
		else{
			return -1;
		}
	}
	
	// number of documents the term occurs in
	@Override
	public int corpusDocFrequencyByTerm(String term) {
		return get_dictionary().containsKey(term) ?
				get_documentTermFrequency().get(get_dictionary().get(term)) : 0;
	}

	//number of times a term appears in corpus
	@Override
	public int corpusTermFrequency(String term) {
		return get_dictionary().containsKey(term) ?
				get_corpusTermFrequency().get(get_dictionary().get(term)) : 0;
	}

	// number of times a term occurs in document
	@Override
	public int documentTermFrequency(String term, String url) {
		int docid = Integer.parseInt(url);
		if(get_dictionary().containsKey(term)){
			PostListOccurence postLs = getPostList(term);
			return postLs.data.get(docid).size();
		}
		else{
			return 0;
		}
	}

	@Override
	public int documentTotalTermFrequency(String url) {
		int docid = Integer.parseInt(url);
		if(docid < get_documents().size()){
			return get_documents().get(docid)._termNum;
		}
		else{
			return 0;
		}
	}

	public Map<String, Integer> get_dictionary() {
		return _dictionary;
	}

	public void set_dictionary(Map<String, Integer> _dictionary) {
		this._dictionary = _dictionary;
	}

	public ArrayList<Integer> get_documentTermFrequency() {
		return _documentTermFrequency;
	}

	public void set_documentTermFrequency(ArrayList<Integer> _documentTermFrequency) {
		this._documentTermFrequency = _documentTermFrequency;
	}

	public ArrayList<Integer> get_corpusTermFrequency() {
		return _corpusTermFrequency;
	}

	public void set_corpusTermFrequency(ArrayList<Integer> _corpusTermFrequency) {
		this._corpusTermFrequency = _corpusTermFrequency;
	}

	public List<DocumentIndexed> get_documents() {
		return _documents;
	}

	public void set_documents(List<DocumentIndexed> _documents) {
		this._documents = _documents;
	}
}