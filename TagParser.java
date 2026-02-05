import java.util.Locale;

final class TagParser {

    private TagParser() {
    }

    static Tag parse(String line) throws MalformedHtmlException {
        ensureTagEnvelope(line);
        TagKind kind = parseKind(line);
        String rawName = extractName(line, kind);
        validateTagName(rawName);
        return new Tag(kind, normalize(rawName));
    }

    private static void ensureTagEnvelope(String line) throws MalformedHtmlException {
        if (line.length() < 3) throw new MalformedHtmlException();
        if (!line.startsWith("<") || !line.endsWith(">")) throw new MalformedHtmlException();
    }

    private static TagKind parseKind(String line) {
        return line.startsWith("</") ? TagKind.CLOSE : TagKind.OPEN;
    }

    private static String extractName(String line, TagKind kind) throws MalformedHtmlException {
        int start = (kind == TagKind.CLOSE) ? 2 : 1;
        int end = line.length() - 1;
        if (end <= start) throw new MalformedHtmlException();
        return line.substring(start, end);
    }

    private static void validateTagName(String name) throws MalformedHtmlException {
        if (name.isEmpty()) throw new MalformedHtmlException();
        if (name.endsWith("/")) throw new MalformedHtmlException();
        if (!isValidTagName(name)) throw new MalformedHtmlException();
    }

    private static boolean isValidTagName(String name) {
        if (!isAsciiLetter(name.charAt(0))) return false;
        for (int i = 1; i < name.length(); i++) if (!isAsciiLetterOrDigit(name.charAt(i))) return false;
        return true;
    }

    private static boolean isAsciiLetterOrDigit(char c) {
        return isAsciiLetter(c) || isAsciiDigit(c);
    }

    private static boolean isAsciiLetter(char c) {
        return (c >= 'A' && c <= 'Z') || (c >= 'a' && c <= 'z');
    }

    private static boolean isAsciiDigit(char c) {
        return c >= '0' && c <= '9';
    }

    private static String normalize(String tagName) {
        return tagName.toLowerCase(Locale.ROOT);
    }

    enum TagKind { OPEN, CLOSE }

    static final class Tag {
        final TagKind kind;
        final String name;

        Tag(TagKind kind, String name) {
            this.kind = kind;
            this.name = name;
        }
    }

    static final class MalformedHtmlException extends Exception {
        private static final long serialVersionUID = 1L;
    }
}
