package edu.nyu.cs.cs2580;

import java.io.File;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.StringTokenizer;

/**
 * This class contains all utility methods
 * @author bdawada
 *
 */
public class Utility {
	// This can then help removing stop words
	public static Set<String> tokenize(String document){
		Set<String> uniqueTermSet = new HashSet<String>();
		StringTokenizer st = new StringTokenizer(document);
		while(st.hasMoreTokens()){
			String token = st.nextToken().toLowerCase().trim();
			if(token.length() > 0){
				uniqueTermSet.add(token);
			}
		}
		return uniqueTermSet;
	}
	
	

	public static List<String> getFilesInDirectory(String directory) {
		File folder = new File(directory);
		List<String> files = new ArrayList<String>();
		for (final File fileEntry : folder.listFiles()) {
			files.add(fileEntry.getName());
		}
		System.out.println(files.size());
		return files;
	}
	
	public static void main(String[] args) {
		//String doc = "This is to test set to";
		//tokenize(doc);
		getFilesInDirectory("data/simple");
	}
}

