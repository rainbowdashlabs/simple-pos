package dev.chojo.simplepos.service.export;

import java.io.IOException;
import java.io.Writer;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

/**
 * Writes CSV in a {@link CsvFormat}, so a spreadsheet in that language opens it with a double click: UTF-8 with a
 * byte order mark, the format's separator and decimal mark, and date-times in the business time zone.
 * <p>
 * Text fields that a spreadsheet would read as a formula, because they start with {@code =}, {@code +}, {@code -},
 * {@code @}, a tab or a carriage return, get a leading apostrophe, so a note typed into the till cannot run anything
 * when the file is opened. The apostrophe stays visible in the opened file, e.g. a note "-5 Pfand" shows as
 * "'-5 Pfand"; that is the price of the protection, not a bug.
 */
public class CsvWriter {
    private static final char BOM = '\uFEFF';
    private static final DateTimeFormatter DATE_TIME = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private final Writer out;
    private final ZoneId zone;
    private final CsvFormat format;
    private final List<String> row = new ArrayList<>();

    public CsvWriter(Writer out, ZoneId zone, CsvFormat format) {
        this.out = out;
        this.zone = zone;
        this.format = format;
    }

    /**
     * Starts the file with the byte order mark and the header row.
     */
    public void header(String... names) throws IOException {
        out.write(BOM);
        for (String name : names) text(name);
        endRow();
    }

    public CsvWriter text(String value) {
        row.add(quote(defuse(value == null ? "" : value)));
        return this;
    }

    public CsvWriter number(BigDecimal value) {
        row.add(value == null ? "" : value.toPlainString().replace('.', format.decimal()));
        return this;
    }

    public CsvWriter number(long value) {
        row.add(Long.toString(value));
        return this;
    }

    public CsvWriter dateTime(Instant value) {
        row.add(value == null ? "" : DATE_TIME.format(value.atZone(zone)));
        return this;
    }

    public void endRow() throws IOException {
        out.write(String.join(String.valueOf(format.separator()), row));
        out.write("\r\n");
        row.clear();
    }

    private static String defuse(String value) {
        if (value.isEmpty()) return value;
        char first = value.charAt(0);
        return first == '=' || first == '+' || first == '-' || first == '@' || first == '\t' || first == '\r'
                ? "'" + value
                : value;
    }

    private String quote(String value) {
        boolean needsQuotes = value.indexOf(format.separator()) >= 0 || value.indexOf('"') >= 0
                || value.indexOf('\n') >= 0 || value.indexOf('\r') >= 0;
        return needsQuotes ? '"' + value.replace("\"", "\"\"") + '"' : value;
    }
}
