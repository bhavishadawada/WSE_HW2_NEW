package edu.nyu.cs.cs2580;

import java.io.File;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.Vector;
import java.util.regex.Pattern;

/**
 * This class contains all utility methods
 * @author bdawada
 *
 */
public class Utility {
	// This can then help removing stop words
	public static Set<String> tokenize(String document){
		Set<String> uniqueTermSet = new HashSet<String>();
		
		String str;
        Pattern ptn = Pattern.compile("([^a-zA-Z0-9])");
        str = ptn.matcher(document).replaceAll(" $1 ");

		StringTokenizer st = new StringTokenizer(str);
		while(st.hasMoreTokens()){
			String token = st.nextToken().toLowerCase().trim();
			if(token.length() > 0){
				uniqueTermSet.add(token);
			}
		}
		return uniqueTermSet;
	}

	public static List<String> tokenize2(String document){
		List<String> tokenList = new ArrayList<String>();
		
		String str;
        Pattern ptn = Pattern.compile("([^a-zA-Z0-9])");
        str = ptn.matcher(document).replaceAll(" $1 ");

		StringTokenizer st = new StringTokenizer(str);
		while(st.hasMoreTokens()){
			String token = st.nextToken().toLowerCase().trim();
			if(token.length() > 0){
				tokenList.add(token);
			}
		}
		return tokenList;
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
	
	private static String compressIntegerToBinary(Integer input) {
		String binary = Integer.toBinaryString(input);
		String st = "";
		int counter = 1;
		boolean flag = true;
		for (int i = binary.length() - 1; i >= 0; i--) {
			if (counter == 8) {
				if (flag) {
					st += "1";
					st += binary.charAt(i);
					flag = false;
				} else {
					st += "0";
					st += binary.charAt(i);
				}
				counter = 1;
			} else {
				st += binary.charAt(i);
			}
			counter++;
		}
		int size = st.length();
		String temp = "";
		flag = true;
		while (size % 8 != 0) {
			if (size < 8) {
				if (flag) {
					temp += "1";
					flag = false;
				} else {
					temp += "0";
				}
			} else {
				temp += "0";
			}
			size++;
		}
		String finalSt = "";
		for (int i = st.length() - 1; i >= 0; i--) {
			finalSt += st.charAt(i);
		}
		finalSt = temp + finalSt;
		//System.out.println(finalSt);
		//int i = Integer.parseInt(finalSt, 2);
		//Byte bytes = new Byte((byte) i);
		return Long.toHexString(Long.parseLong(finalSt, 2));
	}
	
	private static String compressBinaryToHex(String input){
		return Long.toHexString(Long.parseLong(input, 2));
	}
	
	// This implements delta encoding also
	// i/p InnerMap: (1,[2,4]), (2,[7,18,23]), (3,[2,6]), (4,[3,13])
	public static List<String> createCompressedList(Map<Integer, List<Integer>> map){
		List<Integer> returnList = new ArrayList<Integer>();
		Integer nextKey = 0;
		for (Map.Entry<Integer, List<Integer>> entry : map.entrySet()) {
			returnList.add(entry.getKey() - nextKey);
			nextKey = entry.getKey();
			returnList.add(entry.getValue().size());
			List<Integer> tempList = entry.getValue();
			returnList.add(tempList.get(0));
			for (int i = 1; i < tempList.size(); i++) {
				returnList.add(tempList.get(i) - tempList.get(i - 1));
			}
		}
		
		// returnList :  (1,2,[2,2]), (1,3,[7,11,5]), (1,2,[2,4]), (1,2,[3,10])
	    System.out.println(returnList);
		List<String> compressList = new ArrayList<String>();
		for (Integer s : returnList) {
			String hexString = compressIntegerToBinary(s);
			compressList.add(hexString + " ");
		}
		// compressList is list of string 
		//81 82 82 82, 81 83 87 8B 85, 81 82 82 84, 81 82 83 8A
		return compressList;
	}
	
	private static Integer decompress(Integer num) {
		String i1 = String.valueOf(num);
		int counter = 1;
		String ft = "";
		for (int x = i1.length() - 1; x >= 0; x--) {
			if (counter != 8) {
				ft += i1.charAt(x);
			} else {
				counter = 0;
			}
			counter++;
		}
		String gt = "";
		for (int x = ft.length() - 1; x >= 0; x--) {
			gt += ft.charAt(x);
		}
		Integer i5 = Integer.parseInt(gt, 2);
		return i5;
	}
	
	// while reading from file do an integer conversion and pass to this method
	public static Map<Integer, List<Integer>> createDecompressedMap(
			List<String> list1) {
		int size = 0;
		Map<Integer, List<Integer>> map = new LinkedHashMap<Integer, List<Integer>>();
		Integer st = 0;
		Integer lastSt = 0;
		List<String> list = new ArrayList<String>();
		for (String b : list1) {
			String binaryNum = new BigInteger(b , 16).toString(2);
			list.add(binaryNum);
		}
		while (size < list.size()) {
		
			// write logic to create the map here
			
		}
		return map;
	}
	
	public static void main(String[] args) {
		//String doc = "This is to test set to";
		//tokenize(doc);
		/*Map<Integer, List<Integer>> map = new TreeMap<Integer, List<Integer>>();
		map.put(1,Arrays.asList(2,4));
		map.put(2, Arrays.asList(7,18,23));
		map.put(3, Arrays.asList(2,6));
		map.put(4, Arrays.asList(3,13));
		List<String> resultList = createCompressedList(map);
		System.out.println(resultList);*/
		
		//Integer dcomp  = decompress(81);
		String num = "011CA0";
		String bin = new BigInteger(num, 16).toString(2);
		System.out.println(bin);
	}
}

