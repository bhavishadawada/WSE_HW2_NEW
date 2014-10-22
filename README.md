WSE-HW2
=======

Compile code:

javac -cp lib/jsoup-1.8.1.jar src/edu/nyu/cs/cs2580/*.java


Construct Index:

java -cp src:lib/jsoup-1.8.1.jar edu.nyu.cs.cs2580.SearchEngine --mode=index --options=conf/engine.conf

Load Index:
java -cp src:lib/jsoup-1.8.1.jar -Xmx256m edu.nyu.cs.cs2580.SearchEngine --mode=serve --options=conf/engine.conf --port=25811
