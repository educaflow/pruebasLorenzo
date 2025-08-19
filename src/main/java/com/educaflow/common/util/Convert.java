package com.educaflow.common.util;

public class Convert {

    public static Long objectToLong(Object obj) {
        if (obj == null) {
            return null;
        } else {
            return ((Number) obj).longValue();
        }
    }

}
