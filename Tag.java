import java.util.Locale;
import java.util.Optional;

public record Tag(String name, boolean open) {

    public boolean isOpen() {
        return open;
    }

    public static Optional<Tag> tryParse(String line) {
        if (line == null) {
            return Optional.empty();
        }

        String s = line.trim();
        if (s.isEmpty() || s.charAt(0) != '<') {
            return Optional.empty();
        }

        if (s.length() < 3 || s.charAt(s.length() - 1) != '>') {
            return Optional.empty();
        }

        boolean closing = s.startsWith("</");
        int start = closing ? 2 : 1;
        int end = s.length() - 1;

        if (end <= start) {
            return Optional.empty();
        }

        String token = s.substring(start, end).trim();
        if (!isValidNameToken(token)) {
            return Optional.empty();
        }

        return Optional.of(new Tag(normalize(token), !closing));
    }

    private static boolean isValidNameToken(String token) {
        if (token.isEmpty()) {
            return false;
        }

        if (token.endsWith("/")) {
            return false; // self-closing => malformed (fora das premissas)
        }

        if (containsWhitespace(token)) {
            return false; // atributos/espacos nao permitidos
        }

        return isAsciiTagName(token);
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
