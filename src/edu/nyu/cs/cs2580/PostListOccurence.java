package edu.nyu.cs.cs2580;

import java.util.List;
import java.util.TreeMap;

public class PostListOccurence {
	String term;
	TreeMap<Integer, List<Integer>> data;
	PostListOccurence(String term, TreeMap<Integer, List<Integer>> postList){
		this.term = term;
		this.data = postList;
	}
}
