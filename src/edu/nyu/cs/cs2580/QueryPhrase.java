package edu.nyu.cs.cs2580;

import java.util.ArrayList;
import java.util.Vector;

/**
 * @CS2580: implement this class for HW2 to handle phrase. If the raw query is
 * ["new york city"], the presence of the phrase "new york city" must be
 * recorded here and be used in indexing and ranking.
 */
public class QueryPhrase extends Query {
  public ArrayList<Vector<String>> _phraseTokens = new ArrayList<Vector<String>>();

  public QueryPhrase(String query) {
    super(query);
  }

  @Override
  public void processQuery() {
	  String[] phrases = _query.split("\"");
	  _tokens = new Vector<String>();
	  for(int i = 0; i < phrases.length; i++){
		  if(i%2 == 0){
			  //non phrase
			  _tokens.addAll(Utility.tokenize2(phrases[i]));
		  }
		  else{
			  //phrase
			  _phraseTokens.add(Utility.tokenize2(phrases[i]));
		  }
	  }
	  //System.out.println(_tokens);
	  //System.out.println(_phraseTokens);
  }
  
  public static void main(String[] args){
	  QueryPhrase q = new QueryPhrase("I would \"surely\" like to \"go to school\".haha");
	  q.processQuery();
  }
}
