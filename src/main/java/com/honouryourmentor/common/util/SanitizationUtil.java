package com.honouryourmentor.common.util;

import java.util.regex.Pattern;

/**
 * Defensive sanitization utility to prevent Stored XSS and injection
 * in user-supplied text fields (names, bios, quotes, titles, comments).
 */
public final class SanitizationUtil {

    private static final Pattern HTML_TAG_PATTERN = Pattern.compile("<[^>]*>", Pattern.DOTALL);
    private static final Pattern CONTROL_CHARS_PATTERN = Pattern.compile("[\\p{Cntrl}&&[^\r\n\t]]");

    private SanitizationUtil() {}

    public static String sanitize(String input) {
        if (input == null || input.isBlank()) {
            return input;
        }

        // Completely strip all HTML tags
        String clean = HTML_TAG_PATTERN.matcher(input).replaceAll("");
        // Strip non-printable control characters
        clean = CONTROL_CHARS_PATTERN.matcher(clean).replaceAll("");

        return clean.trim();
    }
}
