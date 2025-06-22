package com.educaflow.common.util;

public class TextUtil {
    public static String getLowerCamelCaseFromScreamingSnakeCase(String screamingSnakeCase) {
        if (screamingSnakeCase == null || screamingSnakeCase.isEmpty()) {
            return screamingSnakeCase;
        }
        StringBuilder lowerCamelCase = new StringBuilder();

        String[] parts = screamingSnakeCase.split("_");

        for (String part : parts) {
            lowerCamelCase.append(getLowerFirstLetter(part));
        }

        return lowerCamelCase.toString();
    }

    public static String getHumanCaseFromScreamingSnakeCase(String screamingSnakeCase) {
        if (screamingSnakeCase == null || screamingSnakeCase.isEmpty()) {
            return screamingSnakeCase;
        }
        StringBuilder humanCase = new StringBuilder();

        String[] parts = screamingSnakeCase.split("_");

        for (String part : parts) {
            if (humanCase.length()>0) {
                humanCase.append(" ");
                humanCase.append(part.toLowerCase());
            } else {
                humanCase.append(getLowerFirstLetter(part));
            }
        }

        return humanCase.toString();
    }

    public static String getLowerFirstLetter(String str) {
        if (str == null || str.isEmpty()) {
            return str;
        }
        return str.substring(0, 1).toUpperCase() + str.substring(1).toLowerCase();
    }
}
