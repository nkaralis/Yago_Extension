package gr.uoa.di.kr.yagoextension.util;

/**
 * This class is part of the YAGO Extension Project
 * Author: Nikos Karalis 
 * kr.di.uoa.gr
 */

import org.apache.commons.text.similarity.JaroWinklerDistance;
import org.apache.commons.text.similarity.LevenshteinDistance;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * String Similarity between two labels
 * Uses implementations from external libraries
 */

public class StringSimilarity {

	/** p factor of substring */
	private static double p = 0.6;
	/** P factor of winklerImpr */
	private static double P = 0.1;
	
	public static double similarity(String label1, String label2, String method) {
		
		if(method.toLowerCase().equals("jarowinkler"))
			return JaroWinkler(label1, label2);
		else if(method.toLowerCase().equals("substring"))
			return SubstringSimilarity(label1, label2);
		else
			return Levenshtein(label1, label2);
		
	}
	
	
	private static double JaroWinkler(String label1, String label2) {
		JaroWinklerDistance jr = new JaroWinklerDistance();
		return jr.apply(label1, label2);
	}
	
	private static double Levenshtein(String label1, String label2) {
		LevenshteinDistance lv = new LevenshteinDistance();
		int dist = lv.apply(label1, label2);
		/** similarity ratio */
		int maxLen = Math.max(label1.length(), label2.length());
		return 1-((double)dist/maxLen);
	}
	
	/*
	 * Proposed by Giorgos Stoilos, Giorgos Stamou, and Stefanos Kollias
	 * A String Metric for Ontology Alignment
	 */
	private static double SubstringSimilarity(String str1, String str2) {
		/** sim(s1, s1) = comm(s1, s2) - diff(s1, s2) + winkler(s1, s2) */
		String label1 = str1;
		String label2 = str2;
		if(label2.length() > label1.length()) {
			String temp = label2;
			label2 = label1;
			label1 = temp;
		}
		int len1 = label1.length();
		int len2 = label2.length();
		int lengthSum = 0;
		String lcs;
		/** comm(s1, s2) */
		do {
			/** find common longest substrings. at each step remove current lcs and search for the next lcs */
			lcs = "";
			int[][] dpa = new int[label1.length()][label2.length()];
			int size = 0;
			for(int i = 0; i < label1.length(); i ++) {
				for(int j = 0; j < label2.length(); j ++) {
					if(label1.charAt(i) == label2.charAt(j)) {
						if(i == 0 || j == 0)
							dpa[i][j] = 1;
						else
							dpa[i][j] = dpa[i-1][j-1] + 1;
						if(dpa[i][j] > size) {
							size = dpa[i][j];
							lcs = label1.substring(i-size+1, i+1);
						}
					}
					else {
						dpa[i][j] = 0;
					}
				}
			}
			/** update labels by removing lcs and update sum */
			lengthSum = lengthSum + lcs.length();
			label1 = label1.replace(lcs, "");
			label2 = label2.replace(lcs, "");
			if(label1.length() == 0 || label2.length() == 0) break;
		} while(!lcs.equals(""));
		double comm = (2*(double)lengthSum) / (len1 + len2);
		/** diff(s1, s2) */
		double uLen1 = (double)label1.length() / str1.length();
		double uLen2 = (double)label2.length() / str2.length();
		double diff = (uLen1*uLen2) / (p+(1-p)*(uLen1 + uLen2 - uLen1*uLen2));
		/** winkler improvement */
		String commonPrefix = StringUtils.getCommonPrefix(str1, str2);
		int L = Math.min(commonPrefix.length(), 4);
		double wrinkler = L*P*(1-comm);
		return comm-diff+wrinkler;
	}
}
