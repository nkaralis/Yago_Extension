package gr.uoa.di.kr.yagoextension.writers;

/**
 * This class is part of the Yago Extension Project
 * Author: Nikos Karalis 
 * kr.di.uoa.gr
 */

import java.io.FileNotFoundException;
import java.io.UnsupportedEncodingException;

public abstract class Writer {
	
	protected String outputFile;
	
	public Writer(String path) {
		outputFile = path;
	}
	
	public abstract void write() throws FileNotFoundException, UnsupportedEncodingException;

}
