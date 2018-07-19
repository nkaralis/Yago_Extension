package gr.uoa.di.kr.yagoextension.readers;

import gr.uoa.di.kr.yagoextension.structures.LabelStructure;

public abstract class Reader {
	
	protected String inputFile;
	protected String type;
	protected LabelStructure entLabelPairs;
	
	Reader(String path) {
		inputFile = path;
		entLabelPairs = new LabelStructure();
	}
	
	public String getType() {
		return type;
	}
	
	public LabelStructure getLabels() {
		return entLabelPairs;
	}

	abstract public void read();
	
}
