import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Locale;
import java.util.Optional;

public class HtmlSource implements AutoCloseable {

    private static final int CONNECT_TIMEOUT_MS = 10_000;
    private static final int READ_TIMEOUT_MS = 10_000;

    private final HttpURLConnection connection;
    private final BufferedReader reader;

    public HtmlSource(String urlString) throws IOException {
        this.connection = openHttpConnection(urlString);
        this.reader = new BufferedReader(new InputStreamReader(connection.getInputStream(), resolveCharset(connection)));
    }

    public BufferedReader reader() {
        return reader;
    }

    @Override
    public void close() throws IOException {
        try {
            reader.close();
        } finally {
            connection.disconnect();
        }
    }

    private static HttpURLConnection openHttpConnection(String urlString) throws IOException {
        URL url = toUrl(urlString);
        var raw = url.openConnection();

        if (!(raw instanceof HttpURLConnection http)) {
            throw new IOException("Only HTTP/HTTPS supported");
        }

        http.setInstanceFollowRedirects(true);
        http.setRequestMethod("GET");
        http.setConnectTimeout(CONNECT_TIMEOUT_MS);
        http.setReadTimeout(READ_TIMEOUT_MS);
        http.setRequestProperty("User-Agent", "HtmlAnalyzer/1.0");

        int code = http.getResponseCode();
        if (code >= 400) {
            throw new IOException("HTTP error");
        }

        return http;
    }

    private static URL toUrl(String urlString) throws IOException {
        try {
            return URI.create(urlString).toURL();
        } catch (IllegalArgumentException e) {
            throw new IOException("Invalid URL", e);
        }
    }

    private static Charset resolveCharset(HttpURLConnection conn) {
        return parseCharsetName(conn.getContentType())
                .flatMap(HtmlSource::safeCharset)
                .orElse(StandardCharsets.UTF_8);
    }

    private static Optional<String> parseCharsetName(String contentType) {
        if (contentType == null) {
            return Optional.empty();
        }

        String lower = contentType.toLowerCase(Locale.ROOT);
        int idx = lower.indexOf("charset=");
        if (idx < 0) {
            return Optional.empty();
        }

        String after = lower.substring(idx + "charset=".length()).trim();
        int semi = after.indexOf(';');
        String token = (semi >= 0) ? after.substring(0, semi).trim() : after.trim();

        String cleaned = token.replace("\"", "").trim();
        return cleaned.isEmpty() ? Optional.empty() : Optional.of(cleaned);
    }

    private static Optional<Charset> safeCharset(String name) {
        try {
            return Optional.of(Charset.forName(name));
        } catch (Exception ignored) {
            return Optional.empty();
        }
    }
}
