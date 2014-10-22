WSE-HW2
=======

I test it on energon1 server and it works well. 

  Compile code:

    javac -cp lib/jsoup-1.8.1.jar src/edu/nyu/cs/cs2580/*.java


  Construct Index:

    java -cp src:lib/jsoup-1.8.1.jar edu.nyu.cs.cs2580.SearchEngine --mode=index --options=conf/engine.conf

  Load Index:

    java -cp src:lib/jsoup-1.8.1.jar -Xmx512m edu.nyu.cs.cs2580.SearchEngine --mode=index --options=conf/engine.conf --port=25811
    
  
  Indexer which supports Phrase Query:

    IndexerInvertedOccurrence.java
    IndexerInvertedCompressed.java
    curl "http://localhost:25811/search?query="%22subsidiary%20Garmin%20International%22"&ranker=favorite"


