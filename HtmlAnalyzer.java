import java.io.BufferedReader;
import java.io.IOException;

public final class HtmlAnalyzer {

    private static final String OUT_MALFORMED = "malformed HTML";
    private static final String OUT_URL_ERROR = "URL connection error";

    private HtmlAnalyzer() {
    }

    public static void main(String[] args) {
        System.out.println(run(args));
    }

    private static String run(String[] args) {
        String url = singleUrlOrNull(args);
        if (url == null) return OUT_URL_ERROR;

        try (BufferedReader reader = HttpHtmlSource.openReader(url)) {
            return DeepestTextScanner.findDeepestText(reader);
        } catch (TagParser.MalformedHtmlException e) {
            return OUT_MALFORMED;
        } catch (IOException | RuntimeException e) {
            return OUT_URL_ERROR;
        }
    }

    private static String singleUrlOrNull(String[] args) {
        if (args == null || args.length != 1) return null;
        String value = args[0];
        if (value == null || value.isBlank()) return null;
        return value.trim();
    }
}
