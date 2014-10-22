package edu.nyu.cs.cs2580;

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
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.Vector;

import edu.nyu.cs.cs2580.SearchEngine.Options;

/**
 * @CS2580: Implement this class for HW2.
 */

// check if this should implement serializable
public class IndexerInvertedDoconly extends Indexer implements Serializable{
	private static final long serialVersionUID = 3361289105007800861L;

	final int BULK_DOC_PROCESSING_SIZE = 300;

	// Data structure to maintain unique terms with id
	private Map<String, Integer> _dictionary = new HashMap<String, Integer>();

	// Data structure to store number of times a term occurs in Document
	// term id --> frequency
	private ArrayList<Integer> _documentTermFrequency = new ArrayList<Integer>();

	// Data structure to store number of times a term occurs in the complete Corpus
	// term id --> frequency
	private ArrayList<Integer> _corpusTermFrequency = new ArrayList<Integer>();
	
	// Data structure to store unique terms in the document
	//private Vector<String> _terms = new Vector<String>();

	// Stores all Document in memory.
	private List<DocumentIndexed> _documents = new ArrayList<DocumentIndexed>();

	private ArrayList<ArrayList<Integer>> _doconly = new ArrayList<ArrayList<Integer>>();

	// Provided for serialization
	public IndexerInvertedDoconly(){ }

	public IndexerInvertedDoconly(Options options) {
		super(options);
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
			}

			System.out.println("_dictionary size: " + _dictionary.size());
			String indexFile = _options._indexPrefix + "/corpus.idx";
			System.out.println("Write Indexer to " + indexFile);

		    ObjectOutputStream writer = new ObjectOutputStream(new FileOutputStream(indexFile));
		    writer.writeObject(this);
		    writer.close();

		}

		private void processDocument(String title, String body) {
			int docId = _numDocs;
			++ _numDocs;
			Set<String> uniqueTermSetBody = Utility.tokenize(body);

			//build _dictionary
			for(String token:uniqueTermSetBody){
		        if(!_dictionary.containsKey(token)){
		        	_dictionary.put(token, _corpusTermFrequency.size());
		        	_corpusTermFrequency.add(0);
		        	_documentTermFrequency.add(0);
		        	_doconly.add(new ArrayList<Integer>());
		        }
		        int id = _dictionary.get(token);
		        _documentTermFrequency.set(id, _documentTermFrequency.get(id) + 1);
				_doconly.get(id).add(docId);
			}
			
			Vector<String> bodyTermVector = Utility.tokenize2(body);
			for(String token : bodyTermVector){
		        int id = _dictionary.get(token);
				_corpusTermFrequency.set(id, _corpusTermFrequency.get(id) + 1);
			}

			DocumentIndexed doc = new DocumentIndexed(docId);
			doc.setTitle(title);
			doc._termNum = bodyTermVector.size();
			doc.setUrl(Integer.toString(docId));

			_documents.add(doc);
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


		// This is used when the SearchEngine is called with the serve option
		@Override
		public void loadIndex() throws IOException, ClassNotFoundException {
		    String indexFile = _options._indexPrefix + "/corpus.idx";
		    System.out.println("Load index from: " + indexFile);

		    ObjectInputStream reader = new ObjectInputStream(new FileInputStream(indexFile));

		    IndexerInvertedDoconly loaded = (IndexerInvertedDoconly) reader.readObject();

		    this._documents = loaded._documents;
		    this._dictionary = loaded._dictionary;
		    this._numDocs = _documents.size();
		    this._corpusTermFrequency = loaded._corpusTermFrequency;
		    this._documentTermFrequency = loaded._documentTermFrequency;
		    this._doconly = loaded._doconly;
		    for (Integer freq : loaded._corpusTermFrequency) {
		        this._totalTermFrequency += freq;
		    }
		    System.out.println(Integer.toString(_numDocs) + " documents loaded " +
		        "with " + Long.toString(_totalTermFrequency) + " terms!");
		    reader.close();
		    
		    /*
		    Query query = new Query("Alfred Matthew");
		    Document doc = nextDoc(query, -1);
		    System.out.println(doc.getTitle());
		    doc = nextDoc(query, doc._docid);
		    System.out.println(doc.getTitle());
		    */
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

		//TODO: This is to be implemented as discussed in class?????
		@Override
		public DocumentIndexed nextDoc(QueryPhrase query, int docid) {
			ArrayList<ArrayList<Integer>> postLsArr = new ArrayList<ArrayList<Integer>>();
			ArrayList<Integer> cache = new ArrayList<Integer>();

			//query.processQuery();
			List<String> queryVector = query._tokens;
			for (String search : queryVector) {
				ArrayList<Integer> postLs = getPostList(search);
				if(postLs == null){
					return null;
				}
				postLsArr.add(postLs);
				cache.add(0);
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
			    		System.out.println("document found " + nextDocId);
			    		System.out.println(_documents.get(nextDocId).getTitle());
			    		return _documents.get(nextDocId);
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
	
		public ArrayList<Integer> getPostList(String term){
			if(_dictionary.containsKey(term)){
				//System.out.println("queryTerm " + term);
				int termid = _dictionary.get(term);
				return _doconly.get(termid);
			}
			return null;
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
			int docid = Integer.parseInt(url);
			//System.out.println("get docid: " + docid);
			if(_dictionary.containsKey(term)){
				int cache = -1;
				ArrayList<Integer> postLs = getPostList(term);
				cache = postLsNext(postLs, cache, docid-1);
				if(cache >= 0 && postLs.get(cache) == docid){
					return 1;
				}
				else{
					return 0;
				}
			}
			else{
				return 0;
			}
		}

		@Override
		public int documentTotalTermFrequency(String url) {
			int docid = Integer.parseInt(url);
			if(docid < _documents.size()){
				return 1;
			}
			else{
				return 0;
			}
		}
	}
