package gr.uoa.di.kr.yagoextension.readers;

/**
 * This class is part of the Yago Extension Project
 * Author: Nikos Karalis 
 * kr.di.uoa.gr
 */

public class TSVReader extends Reader {

	private String delim;
	
	public TSVReader(String path) {
		super(path);
		String extension = path.split(".")[1];
		if(extension.equals(".tsv")) {
			type = "TSV File";
			delim = "\t";
		}
		else if(extension.equals(".csv")) {
			type = "CSV File";
			delim = ",";
		}
	}

	@Override
	public void read() {
		// TODO Auto-generated method stub
		
	}

}
