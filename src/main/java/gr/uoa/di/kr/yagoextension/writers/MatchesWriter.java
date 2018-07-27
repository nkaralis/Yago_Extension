package gr.uoa.di.kr.yagoextension.writers;

/**
 * This class is part of the Yago Extension Project
 * Author: Nikos Karalis 
 * kr.di.uoa.gr
 */

import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;

import gr.uoa.di.kr.yagoextension.structures.MatchesStructure;

public class MatchesWriter extends Writer{
	
	private MatchesStructure matches;
	
	public MatchesWriter(String path, MatchesStructure m) {
		super(path);
		matches = m;
	}

	@Override
	public void write() throws FileNotFoundException, UnsupportedEncodingException {
		PrintWriter out = new PrintWriter(outputFile, "UTF-8");
		out.println("@base <http://yago-knowledge.org/resource/> .");
		out.println("@prefix owl: <http://www.w3.org/2002/07/owl#> .");
		out.println("@prefix extension: <http://www.kr.di.uoa.gr/yago_extension/> .");
		for(String x : matches.getKeys()) {
			for(String y : matches.getValueByKey(x))
			out.println("extension:"+x+" owl:sameAs "+y+" .");
		}
		out.close();
	}

}
