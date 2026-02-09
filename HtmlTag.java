import java.util.Locale;
import java.util.Optional;

public record HtmlTag(String name, boolean isOpen) {

    public static Optional<HtmlTag> tryParse(String line) {
        String s = trimToNull(line);
        Optional<HtmlTag> result = Optional.empty();

        if (s != null && isAngleBracketedTag(s)) {
            boolean closing = isClosingTagLine(s);
            Optional<String> token = extractNameToken(s, closing);

            if (token.isPresent()) {
                result = Optional.of(new HtmlTag(normalize(token.get()), !closing));
            }
        }

        return result;
    }

    private static String trimToNull(String line) {
        if (line == null) {
            return null;
        }

        String s = line.trim();
        return s.isEmpty() ? null : s;
    }

    private static boolean isAngleBracketedTag(String s) {
        return s.length() >= 3 && s.charAt(0) == '<' && s.charAt(s.length() - 1) == '>';
    }

    private static boolean isClosingTagLine(String s) {
        return s.startsWith("</");
    }

    private static Optional<String> extractNameToken(String s, boolean closing) {
        int start = closing ? 2 : 1;
        int end = s.length() - 1;

        Optional<String> token = Optional.empty();
        if (end > start) {
            String candidate = s.substring(start, end).trim();
            if (isValidNameToken(candidate)) {
                token = Optional.of(candidate);
            }
        }

        return token;
    }

    private static boolean isValidNameToken(String token) {
        return !token.isEmpty()
                && !token.endsWith("/")
                && !containsWhitespace(token)
                && isAsciiTagName(token);
    }

    private static boolean containsWhitespace(String s) {
        for (int i = 0; i < s.length(); i++) {
            if (Character.isWhitespace(s.charAt(i))) {
                return true;
            }
        }
        return false;
    }

    private static boolean isAsciiTagName(String name) {
        if (!isAsciiLetter(name.charAt(0))) {
            return false;
        }

        for (int i = 1; i < name.length(); i++) {
            char c = name.charAt(i);
            if (!isAsciiLetter(c) && !isAsciiDigit(c)) {
                return false;
            }
        }

        return true;
    }

    private static boolean isAsciiLetter(char c) {
        return (c >= 'A' && c <= 'Z') || (c >= 'a' && c <= 'z');
    }

    private static boolean isAsciiDigit(char c) {
        return c >= '0' && c <= '9';
    }

    private static String normalize(String name) {
        return name.toLowerCase(Locale.ROOT);
    }
}
