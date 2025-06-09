package org.aau.util;

public final class StringUtil {

    private StringUtil() {
        throw new UnsupportedOperationException("This is a utility class and cannot be instantiated");
    }

    public static boolean isEmpty(String str) {
        return str == null || str.trim().isEmpty();
    }
}
