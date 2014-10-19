package edu.nyu.cs.cs2580;

import java.io.IOException;
import java.io.StringReader;
import java.util.HashSet;

import org.apache.lucene.analysis.PorterStemFilter;
import org.apache.lucene.analysis.StopFilter;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.analysis.standard.StandardTokenizer;
import org.apache.lucene.analysis.tokenattributes.TermAttribute;
import org.apache.lucene.util.Version;

/**
* A class provide the document normalization utilities, including removing the stopwords and stemming
*/

public class NormalizationUtil {

	/**
	 * Normalize the text, remove its stopwords and get the stemming words
	 * @param input
	 * @return
	 */
    @SuppressWarnings("deprecation")
	public static String normalize(String input, boolean removeStopWords, boolean stemming) {
    	TokenStream tokenStream = new StandardTokenizer(Version.LUCENE_36, new StringReader(input));
        if (removeStopWords) {
        	tokenStream = new StopFilter(true, tokenStream, StandardAnalyzer.STOP_WORDS_SET);
        } else {
        	tokenStream = new StopFilter(true, tokenStream, new HashSet<String>());
        }
        if (stemming) {
        	tokenStream = new PorterStemFilter(tokenStream);
        }
        StringBuilder builder = new StringBuilder();
        TermAttribute termAttr = tokenStream.getAttribute(TermAttribute.class);
        try {
			while (tokenStream.incrementToken()) {
			    if (builder.length() > 0) {
			        builder.append(" ");
			    }
			    builder.append(termAttr.term());
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
        return builder.toString();
    }
}
