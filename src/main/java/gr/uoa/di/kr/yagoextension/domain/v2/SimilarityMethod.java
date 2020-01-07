package gr.uoa.di.kr.yagoextension.domain.v2;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.text.similarity.JaroWinklerDistance;
import org.apache.commons.text.similarity.LevenshteinDistance;

import java.util.function.BiFunction;

public enum SimilarityMethod {

    JAROWINKLER(SimilarityMethod::jaroWinklerSim),
    SUBSTRING(SimilarityMethod::substringSim),
    LEVENSHTEIN(SimilarityMethod::levenshteinSim);

    public static final String JAROWINKLER_NAME = "jarowinkler";

    public static final String SUBSTRING_NAME = "substring";
    private static final double SUBSTRING_P_FACTOR = 0.6D;

    private static final double WINKLER_IMPR_P_FACTOR = 0.1D;

    private final BiFunction<String, String, Double> similarityMethod;

    SimilarityMethod(BiFunction<String, String, Double> similarityMethod) {
        this.similarityMethod = similarityMethod;
    }


    public static Double calculateSim(String label1, String label2, String similarityStr) {
        switch (similarityStr.toLowerCase()) {
            case JAROWINKLER_NAME:
                return JAROWINKLER.similarityMethod.apply(label1, label2);
            case SUBSTRING_NAME:
                return SUBSTRING.similarityMethod.apply(label1, label2);
            default:
                return LEVENSHTEIN.similarityMethod.apply(label1, label2);

        }
    }

    private static Double jaroWinklerSim(String str1, String str2) {
        return new JaroWinklerDistance().apply(str1, str2);
    }

    private static double levenshteinSim(String label1, String label2) {
        LevenshteinDistance levenshteinDistance = new LevenshteinDistance();
        int distance = levenshteinDistance.apply(label1, label2);
        int maxLength = Math.max(label1.length(), label2.length());
        return 1 - ((double) distance / maxLength);
    }

    private static double substringSim(String str1, String str2) {
        /* sim(s1, s1) = comm(s1, s2) - diff(s1, s2) + winkler(s1, s2) */
        String label1 = str1;
        String label2 = str2;
        if (label2.length() > label1.length()) {
            String temp = label2;
            label2 = label1;
            label1 = temp;
        }
        int len1 = label1.length();
        int len2 = label2.length();
        int lengthSum = 0;
        String lcs;
        /* comm(s1, s2) */
        do {
            /* find common longest substrings. at each step remove current lcs and search for the next lcs */
            lcs = "";
            int[][] dpa = new int[label1.length()][label2.length()];
            int size = 0;
            for (int i = 0; i < label1.length(); i++) {
                for (int j = 0; j < label2.length(); j++) {
                    if (label1.charAt(i) == label2.charAt(j)) {
                        if (i == 0 || j == 0)
                            dpa[i][j] = 1;
                        else
                            dpa[i][j] = dpa[i - 1][j - 1] + 1;
                        if (dpa[i][j] > size) {
                            size = dpa[i][j];
                            lcs = label1.substring(i - size + 1, i + 1);
                        }
                    } else {
                        dpa[i][j] = 0;
                    }
                }
            }
            /* update labels by removing lcs and update sum */
            lengthSum = lengthSum + lcs.length();
            label1 = label1.replace(lcs, "");
            label2 = label2.replace(lcs, "");
            if (label1.length() == 0 || label2.length() == 0) break;
        } while (!lcs.equals(""));
        double comm = (2 * (double) lengthSum) / (len1 + len2);
        /* diff(s1, s2) */
        double uLen1 = (double) label1.length() / str1.length();
        double uLen2 = (double) label2.length() / str2.length();
        double diff = (uLen1 * uLen2) / (SUBSTRING_P_FACTOR + (1 - SUBSTRING_P_FACTOR) * (uLen1 + uLen2 - uLen1 * uLen2));
        /* winkler improvement */
        String commonPrefix = StringUtils.getCommonPrefix(str1, str2);
        int L = Math.min(commonPrefix.length(), 4);
        double wrinkler = L * WINKLER_IMPR_P_FACTOR * (1 - comm);
        return comm - diff + wrinkler;
    }
}
