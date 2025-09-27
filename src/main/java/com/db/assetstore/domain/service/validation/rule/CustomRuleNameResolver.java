package com.db.assetstore.domain.service.validation.rule;

/**
 * Utility for converting custom validation rule class names into the registry
 * identifiers used when looking up {@link CustomValidationRule} beans.
 */
public final class CustomRuleNameResolver {

    private CustomRuleNameResolver() {
    }

    /**
     * Converts a fully-qualified or simple class name into the registry key
     * used by {@link CustomValidationRuleRegistry}. The helper trims whitespace,
     * strips package prefixes and a trailing {@code Rule} suffix, and finally
     * lowercases the first character so a class such as
     * {@code com.example.MyRule} resolves to {@code my}.
     *
     * @param className the class name declared in the schema or database
     *                  constraint definition
     * @return the normalized registry name or {@code null} when the input is
     * blank
     */
    public static String fromClassName(String className) {
        if (className == null) {
            return null;
        }

        var trimmed = className.trim();
        if (trimmed.isEmpty()) {
            return null;
        }

        int lastDot = trimmed.lastIndexOf('.');
        if (lastDot >= 0 && lastDot < trimmed.length() - 1) {
            trimmed = trimmed.substring(lastDot + 1);
        }

        if (trimmed.endsWith("Rule") && trimmed.length() > 4) {
            trimmed = trimmed.substring(0, trimmed.length() - 4);
        }

        if (trimmed.isEmpty()) {
            return null;
        }

        if (trimmed.length() == 1) {
            return trimmed.toLowerCase();
        }

        return Character.toLowerCase(trimmed.charAt(0)) + trimmed.substring(1);
    }
}
