WSE-HW2
=======

Compile code:

javac -cp lib/jsoup-1.8.1.jar src/edu/nyu/cs/cs2580/*.java


Construct Index:

java -cp src:lib/jsoup-1.8.1.jar edu.nyu.cs.cs2580.SearchEngine --mode=index --options=conf/engine.conf
