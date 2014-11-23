package edu.nyu.cs.cs2580;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import java.util.Scanner;
import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.File;
import java.io.IOException;
import org.apache.commons.io.FileUtils;



class DocProcessor{
	public String body;
	public String title;
	public int index;
	public File[] file;
	public Scanner sc;
	public boolean simple;
	private BufferedReader br;


	public DocProcessor(String path) throws FileNotFoundException{
		if(path.equals("data/simple")){
			String  corpusFile = path + "/corpus.tsv";
			file = new File[0];
			sc = new Scanner(new FileInputStream(corpusFile));
			simple = true;
		}
		else{
			File dir = new File(path);
			file = dir.listFiles();
			simple = false;
		}
		index = 0;
	}

	public Boolean hasNextDoc(){
		if(simple){
			return sc.hasNextLine();
		}
		else{
			return index < file.length;
		}
	}

	public void nextDoc() throws IOException{
		if(simple){
			if(sc.hasNextLine()){
				String content = sc.nextLine();
				Scanner s = new Scanner(content).useDelimiter("\t");
				title = s.next();
				body = s.next();
				index++;
			}
		}
		else{
			if(index < file.length){
				title = file[index].getName();
				body =  Jsoup.parse(FileUtils.readFileToString(file[index])).text();
				index++;
			}
		}
	}
}
