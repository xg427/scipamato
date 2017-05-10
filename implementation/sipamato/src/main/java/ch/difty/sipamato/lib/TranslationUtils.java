package ch.difty.sipamato.lib;

public final class TranslationUtils {

    public static final String NOT_TRANSL = "not translated";

    private TranslationUtils() {
    }

    /**
     * Convert an extended language code (e.g. de_CH) into the main one (de)
     * @param languageCode
     * @return trimmed languageCode
     */
    public static String trimLanguageCode(String languageCode) {
        String lc = AssertAs.notNull(languageCode, "languageCode");
        if (lc.length() > 2)
            return lc.substring(0, 2);
        return lc;
    }

    /**
     * Converts a camel cased string into an underscored one, e.g. {@code fooBar} {@literal ->} {@code foo_bar}
     * @param original
     * @return de-camel-cased string (or null if original is null)
     */
    public static String deCamelCase(String original) {
        if (original == null)
            return null;
        if (original.isEmpty())
            return original;
        return original.replaceAll("(.)(\\p{Upper})", "$1_$2").toLowerCase();
    }
}
