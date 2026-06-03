package com.neusoft.utils;

import java.util.regex.Pattern;

public class ValidateUtil {
    private static final Pattern MOBILE_PATTERN = Pattern.compile("^1[3-9]\\d{9}$");
    private static final Pattern USERNAME_PATTERN = Pattern.compile("^[A-Za-z0-9_]{3,20}$");

    private ValidateUtil() {
    }

    public static boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }

    public static String trim(String value) {
        return value == null ? null : value.trim();
    }

    public static boolean lengthBetween(String value, int min, int max) {
        if (value == null) {
            return false;
        }
        int length = value.trim().length();
        return length >= min && length <= max;
    }

    public static boolean isPositive(Integer value) {
        return value != null && value > 0;
    }

    public static boolean isMobile(String value) {
        return value != null && MOBILE_PATTERN.matcher(value.trim()).matches();
    }

    public static boolean isUsername(String value) {
        return value != null && USERNAME_PATTERN.matcher(value.trim()).matches();
    }

    public static boolean isPassword(String value) {
        return value != null && value.length() >= 6 && value.length() <= 20;
    }

    public static boolean isGender(String value) {
        String gender = trim(value);
        return "男".equals(gender) || "女".equals(gender);
    }
}
