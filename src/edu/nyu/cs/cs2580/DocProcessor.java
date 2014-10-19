package edu.nyu.cs.cs2580;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.util.Scanner;
import java.util.regex.Pattern;
import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStreamReader;
import java.io.FileReader;
import java.io.File;
import java.io.IOException;



class DocProcessor{
    public String body;
    public String title;
    public int index;
    public File[] file;
    public Scanner sc;
    public boolean simple;


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

    public void nextDoc(){
    	if(simple){
    		if(sc.hasNextLine()){
    			String content = sc.nextLine();
				Scanner s = new Scanner(content).useDelimiter("\t");
    			title = s.next();
    			body = s.next();
                index++;
    		}
            else{
                title = null;
                body = null;
            }
		}
    	else{
            if(index < file.length){
                title = file[index].getName();

                StringBuilder sb= new StringBuilder();
                try{
                    BufferedReader br = new BufferedReader(new FileReader(file[index]));
                    String line = br.readLine();
                    while(line != null){
                        sb.append(line);
                        line = br.readLine();
                    }
                }
                catch(IOException e){
                    e.printStackTrace();
                }

                body = sb.toString();
                body = docProcess(body);

                index++;
            }
            else{
                title = null;
                body = null;
            }
    	}
    	System.out.println(index);
    }

    static public String docProcess(String input){
        Document doc = Jsoup.parse(input);
        String str = doc.text();

        //Pattern nonASCII = Pattern.compile("[^\\x00-\\x7f]");
        //str = nonASCII.matcher(str).replaceAll();

        Pattern ptn = Pattern.compile("([^a-zA-Z0-9])");
        str = ptn.matcher(str).replaceAll(" $1 ");
        return str;
    }
    
    static public void main(String[] args){
    	File f = new File("data/wiki/'03_Bonnie_&_Clyde");
        StringBuilder sb= new StringBuilder();
        try{
            BufferedReader br = new BufferedReader(new FileReader(f));
            String line = br.readLine();
            while(line != null){
                sb.append(line);
                line = br.readLine();
            }
        }
        catch(IOException e){
            e.printStackTrace();
        }

        String output = docProcess(sb.toString());
        System.out.print(output);
    }
}
