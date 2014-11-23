package edu.nyu.cs.cs2580;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.util.Vector;

/**
 * Representation of a user query.
 * 
 * In HW1: instructors provide this simple implementation.
 * 
 * In HW2: students must implement {@link QueryPhrase} to handle phrases.
 * 
 * @author congyu
 * @auhtor fdiaz
 */
public class Query {
  public String _query = null;
  public List<String> _tokens = new ArrayList<String>();

  public Query(String query) {
    _query = query;
  }

  public void processQuery() {
    if (_query == null) {
      return;
    }
    _tokens = Utility.tokenize2(_query);
  }
}
