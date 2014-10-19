import java.io.*;
import org.apache.lucene.analysis.core.*;
import org.apache.lucene.analysis.*;
import org.apache.lucene.analysis.ngram.NGramTokenFilter;
import org.apache.lucene.analysis.shingle.ShingleFilter;
import org.apache.lucene.analysis.standard.*;
import org.apache.lucene.util.*;
import org.apache.lucene.analysis.util.*;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;

class TestAnalyzer extends Analyzer {

TestAnalyzer() {
    super();
}

protected TokenStreamComponents createComponents( String fieldName, Reader reader ) {
    String token;
    TokenStream result = null;

    Tokenizer source = new StandardTokenizer( Version.LUCENE_CURRENT, reader );
    result = new ShingleFilter(source, 2, 3);

    return new TokenStreamComponents( source, result );
}
}

public class LuceneTest {

public static void main(String[] args) {

    TestAnalyzer analyzer = new TestAnalyzer();

    try {
        TokenStream stream = analyzer.tokenStream("field", new StringReader("This is a test."));
        CharTermAttribute termAtt = stream.addAttribute(CharTermAttribute.class);

        stream.reset();

        // print all tokens until stream is exhausted
        while (stream.incrementToken()) {
            System.out.println(termAtt.toString());
        }

        stream.end();
        stream.close();
     }
     catch (Exception ex) {
         ex.printStackTrace();
     }
}