package edu.nyu.cs.cs2580;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Vector;

import edu.nyu.cs.cs2580.SearchEngine.Options;

/**
 * @CS2580: Implement this class for HW2.
 */
public class IndexerInvertedCompressed extends Indexer implements Serializable{
	
	private static final long serialVersionUID = -1785477383728439657L;

	// Data structure to maintain unique terms with id
	private Map<String, Integer> _dictionary = new HashMap<String, Integer>();

	// Data structure to store number of times a term occurs in Document
	// term id --> frequency
	private ArrayList<Integer> _documentTermFrequency = new ArrayList<Integer>();

	// Data structure to store number of times a term occurs in the complete Corpus
	// term id --> frequency
	private ArrayList<Integer> _corpusTermFrequency = new ArrayList<Integer>();
	
	private ArrayList<Integer> _termLineNum = new ArrayList<Integer>();
	
	private IndexerInvertedOccurrence _occurIndex;
	
	private ArrayList<PostListCompressed> _postListCompressed; 

	// Data structure to store unique terms in the document
	//private Vector<String> _terms = new Vector<String>();

	// Stores all Document in memory.
	private List<DocumentIndexed> _documents = new ArrayList<DocumentIndexed>();
	
  public IndexerInvertedCompressed() { }
  public IndexerInvertedCompressed(Options options) {
    super(options);
    System.out.println("Using Indexer: " + this.getClass().getSimpleName());
	_occurIndex = new IndexerInvertedOccurrence(options);
  }

  @Override
  public void constructIndex() throws IOException {
	  _occurIndex.constructIndex();
	  this._dictionary = _occurIndex.get_dictionary();
	  this._documentTermFrequency = _occurIndex.get_documentTermFrequency();
	  this._corpusTermFrequency = _occurIndex.get_corpusTermFrequency();
	  this._documents = _occurIndex.get_documents();
	  this._postListCompressed = new ArrayList<PostListCompressed>(this._dictionary.size());

	  for(int i = 0; i < this._dictionary.size(); i++){
		  this._postListCompressed.add(new PostListCompressed());
	  }

	  // compress
	  List<String> files = Utility.getFilesInDirectory(_options._indexPrefix);
	  for (String file : files) {
		  if (file.endsWith(".idx") && !file.equals("corpus.idx")){
			  String fileName = _options._indexPrefix + "/" + file;
			  System.out.println("compress " + file);
			  BufferedReader br;
			  try {
				  br = new BufferedReader(new FileReader(fileName));
				  String line = br.readLine();
				  while(line != null){
					  PostListOccurence postList = _occurIndex.buildPostLs(line);
					  if(_dictionary.containsKey(postList.term)){
						  int termId = _dictionary.get(postList.term);
						  _postListCompressed.set(termId,  new PostListCompressed(postList));
					  }
					  line = br.readLine();
				  }
				  br.close();
			  } catch (FileNotFoundException e) {
				  // TODO Auto-generated catch block
				  e.printStackTrace();
			  } catch (IOException e) {
				  // TODO Auto-generated catch block
				  e.printStackTrace();
			  }
		  }
	  }
	  
		System.out.println("_dictionary size: " + _dictionary.size());
		String indexFile = _options._indexPrefix + "/corpus2.idx";
		System.out.println("Write Indexer to " + indexFile);

	    ObjectOutputStream writer = new ObjectOutputStream(new FileOutputStream(indexFile));
	    writer.writeObject(this);
	    writer.close();
  }
  
  
	
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

  /**
   * @CS2580: Implement this for bonus points.
   */
  @Override
  public int documentTermFrequency(String term, String url) {
    return 0;
  }

  @Override
  public int documentTotalTermFrequency(String url) {
  	// TODO Auto-generated method stub
  	return 0;
  }
}
