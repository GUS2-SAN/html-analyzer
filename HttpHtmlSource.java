import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Locale;

final class HttpHtmlSource {

    private static final int CONNECT_TIMEOUT_MS = 10_000;
    private static final int READ_TIMEOUT_MS = 10_000;

    private HttpHtmlSource() {
    }

    static BufferedReader openReader(String urlString) throws IOException {
        HttpURLConnection connection = openHttpConnection(urlString);
        Charset charset = charsetFrom(connection.getContentType());
        return new DisconnectingReader(connection, charset);
    }

    private static HttpURLConnection openHttpConnection(String urlString) throws IOException {
        HttpURLConnection conn = (HttpURLConnection) toUrl(urlString).openConnection();
        configure(conn);
        ensureHttpOk(conn);
        return conn;
    }

    private static URL toUrl(String urlString) throws IOException {
        try {
            return URI.create(urlString).toURL();
        } catch (IllegalArgumentException e) {
            throw new IOException("Invalid URL", e);
        }
    }

    private static void configure(HttpURLConnection conn) throws IOException {
        conn.setInstanceFollowRedirects(true);
        conn.setRequestMethod("GET");
        conn.setConnectTimeout(CONNECT_TIMEOUT_MS);
        conn.setReadTimeout(READ_TIMEOUT_MS);
        conn.setRequestProperty("User-Agent", "HtmlAnalyzer/1.0");
    }

    private static void ensureHttpOk(HttpURLConnection conn) throws IOException {
        if (conn.getResponseCode() >= 400) throw new IOException("HTTP error");
    }

    private static Charset charsetFrom(String contentType) {
        String name = extractCharsetName(contentType);
        return resolveCharsetOrUtf8(name);
    }

    private static String extractCharsetName(String contentType) {
        if (contentType == null) return null;
        String lower = contentType.toLowerCase(Locale.ROOT);
        int idx = lower.indexOf("charset=");
        if (idx < 0) return null;
        return sanitizeToken(lower.substring(idx + "charset=".length()));
    }

    private static String sanitizeToken(String raw) {
        String trimmed = raw.trim();
        int semi = trimmed.indexOf(';');
        String token = (semi >= 0) ? trimmed.substring(0, semi) : trimmed;
        return token.replace("\"", "").trim();
    }

    private static Charset resolveCharsetOrUtf8(String name) {
        if (name == null || name.isBlank()) return StandardCharsets.UTF_8;
        try {
            return Charset.forName(name);
        } catch (Exception ignored) {
            return StandardCharsets.UTF_8;
        }
    }

    private static final class DisconnectingReader extends BufferedReader {

        private final HttpURLConnection connection;

        DisconnectingReader(HttpURLConnection conn, Charset charset) throws IOException {
            super(new InputStreamReader(conn.getInputStream(), charset));
            this.connection = conn;
        }

        @Override
        public void close() throws IOException {
            try {
                super.close();
            } finally {
                connection.disconnect();
            }
        }
    }
}
