package gr.uoa.di.kr.yagoextension.util;

public class LabelProcessing {
	
	public static String processKallikratisLabel(String label) {
		
		if(label.contains("ΔΗΜΟΣ "))
			label = label.replace("ΔHMOΣ ", "");
		else if(label.contains("ΔHMOTIKH ΕNOTHTA "))
			label = label.replace("ΔHMOTIKH ΕNOTHTA ", "");
		else if(label.contains("ΠΕΡΙΦΕΡΕΙΑ "))
			label = label.replace("ΠΕΡΙΦΕΡΕΙΑ ", "");
		
		return label;
	}
	
	public static String processOSLabel(String label) {
		
		return label;
	}
	
	public static String processYagoLabel(String label) {
		
		/** Greek Label (Kallikratis case) */
		label = label.replace('ά', 'α');
		label = label.replace('ό', 'ο');
		label = label.replace('έ', 'ε');
		label = label.replace('ώ', 'ω');
		label = label.replace('ύ', 'υ');
		label = label.replace('ί', 'ι');
		label = label.replace('ή', 'η');
		label = label.replace('Ά', 'Α');
		label = label.replace('Ό', 'Ο');
		label = label.replace('Έ', 'Ε');
		label = label.replace('Ώ', 'Ω');
		label = label.replace('Ύ', 'Υ');
		label = label.replace('Ί', 'Ι');
		label = label.replace('Ή', 'Η');
		return label.toUpperCase();
	}

}
