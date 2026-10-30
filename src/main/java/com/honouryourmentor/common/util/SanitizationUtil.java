package com.honouryourmentor.common.util;

import java.util.regex.Pattern;

public final class SanitizationUtil {

    private static final Pattern SCRIPT_PATTERN = Pattern.compile("<script>(.*?)</script>", Pattern.CASE_INSENSITIVE);
    private static final Pattern SRC_PATTERN = Pattern.compile("src[\r\n]*=[\r\n]*\\\'(.*?)\\\'", Pattern.CASE_INSENSITIVE | Pattern.MULTILINE | Pattern.DOTALL);
    private static final Pattern LONELY_SCRIPT_PATTERN = Pattern.compile("</script>", Pattern.CASE_INSENSITIVE);
    private static final Pattern SCRIPT_START_PATTERN = Pattern.compile("<script(.*?)>", Pattern.CASE_INSENSITIVE | Pattern.MULTILINE | Pattern.DOTALL);
    private static final Pattern EVAL_PATTERN = Pattern.compile("eval\\((.*?)\\)", Pattern.CASE_INSENSITIVE | Pattern.MULTILINE | Pattern.DOTALL);
    private static final Pattern EXPRESSION_PATTERN = Pattern.compile("expression\\((.*?)\\)", Pattern.CASE_INSENSITIVE | Pattern.MULTILINE | Pattern.DOTALL);
    private static final Pattern JAVASCRIPT_PATTERN = Pattern.compile("javascript:", Pattern.CASE_INSENSITIVE);
    private static final Pattern VBSCRIPT_PATTERN = Pattern.compile("vbscript:", Pattern.CASE_INSENSITIVE);
    private static final Pattern ONLOAD_PATTERN = Pattern.compile("onload(.*?)=", Pattern.CASE_INSENSITIVE | Pattern.MULTILINE | Pattern.DOTALL);
    private static final Pattern ON_EVENT_PATTERN = Pattern.compile("on[a-zA-Z]+[\r\n]*=", Pattern.CASE_INSENSITIVE | Pattern.MULTILINE | Pattern.DOTALL);
    private static final Pattern IFRAME_PATTERN = Pattern.compile("<iframe(.*?)>", Pattern.CASE_INSENSITIVE | Pattern.MULTILINE | Pattern.DOTALL);

    private SanitizationUtil() {}

    public static String sanitize(String input) {
        if (input == null || input.isBlank()) {
            return input;
        }

        String clean = input;
        clean = SCRIPT_PATTERN.matcher(clean).replaceAll("");
        clean = SCRIPT_START_PATTERN.matcher(clean).replaceAll("");
        clean = LONELY_SCRIPT_PATTERN.matcher(clean).replaceAll("");
        clean = SRC_PATTERN.matcher(clean).replaceAll("");
        clean = EVAL_PATTERN.matcher(clean).replaceAll("");
        clean = EXPRESSION_PATTERN.matcher(clean).replaceAll("");
        clean = JAVASCRIPT_PATTERN.matcher(clean).replaceAll("");
        clean = VBSCRIPT_PATTERN.matcher(clean).replaceAll("");
        clean = ONLOAD_PATTERN.matcher(clean).replaceAll("");
        clean = ON_EVENT_PATTERN.matcher(clean).replaceAll("");
        clean = IFRAME_PATTERN.matcher(clean).replaceAll("");

        return clean.trim();
    }
}

