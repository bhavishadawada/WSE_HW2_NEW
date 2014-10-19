package edu.nyu.cs.cs2580;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.util.Scanner;
import java.util.regex.Pattern;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.FileReader;
import java.io.File;
import java.io.IOException;



class DocProcessor{
    public String body;
    public String title;
    public int index;
    public File[] file;

    public DocProcessor(String path){
        File dir = new File(path);
        file = dir.listFiles();
        index = 0;
    }

    public Boolean hasNextDoc(){
        return index < file.length;
    }

    public void nextDoc(){
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

    public String docProcess(String input){
        Document doc = Jsoup.parse(input);
        String str = doc.text();

        //Pattern nonASCII = Pattern.compile("[^\\x00-\\x7f]");
        //str = nonASCII.matcher(str).replaceAll();

        //Pattern ptn = Pattern.compile("[^a-zA-Z0-9]");
        //str = ptn.matcher(str).replaceAll(" ");
        return str;
    }
}
