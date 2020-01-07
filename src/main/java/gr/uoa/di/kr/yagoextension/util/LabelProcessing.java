package gr.uoa.di.kr.yagoextension.util;

/**
 * This class is part of the YAGO Extension Project
 * Author: Nikos Karalis, Giorgos Mandilaras(GiorgosMandi)
 * kr.di.uoa.gr
 */

public class LabelProcessing {

    public static String processDataSourceLabel(String label, String source) {
        switch (source) {
            case "kallikratis":
                return processKallikratisLabel(label);
            case "kapodistrias":
                return processKapodistriasLabel(label);
            case "os":
                return processOsLabel(label);
            case "osi":
                return processOsiLabel(label);
            default:
                return label;
        }
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


    private static String processKallikratisLabel(String label) {

        if (label.contains("ΔΗΜΟΣ "))
            label = label.replace("ΔΗΜΟΣ ", "");
        else if (label.contains("ΔHMOTIKH ΕNOTHTA "))
            label = label.replace("ΔHMOTIKH ΕNOTHTA ", "");
        else if (label.contains("ΠΕΡΙΦΕΡΕΙΑ "))
            label = label.replace("ΠΕΡΙΦΕΡΕΙΑ ", "");

        return label;
    }

    private static String processKapodistriasLabel(String label) {
        label = processYagoLabel(label);
        if (label.contains("ΔΗΜΟΤΙΚΟ ΔΙΑΜΕΡΙΣΜΑ "))
            label = label.replace("ΔΗΜΟΤΙΚΟ ΔΙΑΜΕΡΙΣΜΑ ", "");
        if (label.contains("ΚΟΙΝΟΤΙΚΟ ΔΙΑΜΕΡΙΣΜΑ "))
            label = label.replace("ΚΟΙΝΟΤΙΚΟ ΔΙΑΜΕΡΙΣΜΑ ", "");
        if (label.contains("ΚΟΙΝΟΤΗΤΑ "))
            label = label.replace("ΚΟΙΝΟΤΗΤΑ ", "");
        if (label.contains("ΔΗΜΟΣ "))
            label = label.replace("ΔΗΜΟΣ ", "");
        else if (label.contains("ΝΟΜΟΣ "))
            label = label.replace("ΝΟΜΟΣ ", "");
        if (label.contains("ΝΟΜΑΡΧΙΑ "))
            label = label.replace("ΝΟΜΑΡΧΙΑ ", "");
        else if (label.contains("ΠΕΡΙΦΕΡΕΙΑ "))
            label = label.replace("ΠΕΡΙΦΕΡΕΙΑ ", "");

        return label;
    }

    private static String processOsLabel(String label) {

        if (label.contains("District"))
            label = label.replace("District", "");
        if (label.contains("County"))
            label = label.replace(" County", "");
        if (label.contains("(B)"))
            label = label.replace("(B)", "");
        if (label.contains("CP"))
            label = label.replace("CP", "");
        if (label.contains("District"))
            label = label.replace("District", "");
        if (label.contains("City of"))
            label = label.replace("City of", "");
        if (label.contains("Community"))
            label = label.replace("Community", "");
        if (label.contains("London Boro"))
            label = label.replace("London Boro", "");
        if (label.contains("Central Ward"))
            label = label.replace("Central Ward", "");
        else if (label.contains("Ward"))
            label = label.replace("Ward", "");
        if (label.contains(" - "))
            label = label.split(" - ")[1];

        return label.toUpperCase().trim();
    }

    private static String processOsiLabel(String label) {

        if (label.contains("MUNICIPAL DISTRICT OF "))
            label = label.replace("MUNICIPAL DISTRICT OF ", "");
        if (label.contains(" RURAL AREA"))
            label = label.replace(" RURAL AREA", "");
        if (label.contains(" COUNTY COUNCIL"))
            label = label.replace(" COUNTY COUNCIL", "");
        if (label.contains(" COUNTY"))
            label = label.replace(" COUNTY", "");
        if (label.contains(" COUNCIL"))
            label = label.replace(" COUNCIL", "");
        if (label.contains(" \\(ED"))
            label = label.split(" \\(ED")[0];
        if (label.equals("\n"))
            label = label.split("\n")[0];

        return label.toUpperCase().trim();
    }

}
