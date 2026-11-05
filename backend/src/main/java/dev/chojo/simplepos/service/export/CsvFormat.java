package dev.chojo.simplepos.service.export;

/**
 * How a spreadsheet in a given language expects CSV: German Excel splits fields at {@code ;} and reads a decimal
 * comma, English Excel splits at {@code ,} and reads a decimal point.
 */
public enum CsvFormat {
    GERMAN(';', ','),
    ENGLISH(',', '.');

    private final char separator;
    private final char decimal;

    CsvFormat(char separator, char decimal) {
        this.separator = separator;
        this.decimal = decimal;
    }

    /**
     * The format for a UI language: German for {@code de}, English for anything else.
     */
    public static CsvFormat of(String language) {
        return "de".equalsIgnoreCase(language) ? GERMAN : ENGLISH;
    }

    public char separator() {
        return separator;
    }

    public char decimal() {
        return decimal;
    }
}
