package edu.nyu.cs.cs2580;

import java.util.ArrayList;
import java.util.List;

public class PostListCompressed {
	String term;
	ArrayList<Integer> delta;
	PostListCompressed(String term, ArrayList<Integer> delta){
		this.term = term;
		this.delta = delta;
	}

	PostListCompressed(PostListOccurence postLs){
		this.term = postLs.term;
		TreeMap<Integer, List<Integer>> data;
	}
}
