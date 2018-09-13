package gr.uoa.di.kr.yagoextension.util;

public class LabelProcessing {
	
	public static String processDataSourceLabel(String label, String source) {
		
		if(source.equals("kallikratis"))
			return processKallikratisLabel(label);
		else if(source.equals("os"))
			return processOSLabel(label);
		else if(source.equals("osni"))
			return processOSNILabel(label);
			return label;
		
	}
	
	private static String processKallikratisLabel(String label) {
		
		if(label.contains("ΔΗΜΟΣ "))
			label = label.replace("ΔΗΜΟΣ ", "");
		else if(label.contains("ΔHMOTIKH ΕNOTHTA "))
			label = label.replace("ΔHMOTIKH ΕNOTHTA ", "");
		else if(label.contains("ΠΕΡΙΦΕΡΕΙΑ "))
			label = label.replace("ΠΕΡΙΦΕΡΕΙΑ ", "");
		return label;
	}
	
	public static String processOSLabel(String label) {
		if(label.contains("District"))
			label = label.replace("District", "");
		if(label.contains("County"))
			label = label.replace(" County", "");
		if(label.contains("(B)"))
			label = label.replace("(B)", "");
		if(label.contains("CP"))
			label = label.replace("CP", "");
		if(label.contains("District"))
			label = label.replace("District", "");
		if(label.contains("City of"))
			label = label.replace("City of", "");
		if(label.contains("Community"))
			label = label.replace("Community", "");
		if(label.contains("London Boro"))
			label = label.replace("London Boro", "");
		if(label.contains("Central Ward"))
			label = label.replace("Central Ward", "");
		else if(label.contains("Ward"))
			label = label.replace("Ward", "");
		if(label.contains(" - "))
			label = label.split(" - ")[1];
		return label.toUpperCase().trim();
	}
	
	private static String processOSNILabel(String label) {
		
		
		
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
		/** English label (OS case) */
		label = label.replace("London Borough of", "");
		label = label.replace("Borough of", "");
		
		return label.toUpperCase();
	}

}
