import java.io.IOException;
import java.util.Optional;

public class HtmlAnalyzer {

    private static final String OUT_MALFORMED = "malformed HTML";
    private static final String OUT_URL_ERROR = "URL connection error";

    public static void main(String[] args) {
        System.out.println(run(args));
    }

    public static String run(String[] args) {
        Optional<String> url = singleUrlArg(args);
        if (url.isEmpty()) {
            return OUT_URL_ERROR;
        }

        try (HtmlSource source = new HtmlSource(url.get())) {
            return new HtmlTextFinder().find(source.reader());
        } catch (IOException e) {
            return OUT_URL_ERROR;
        } catch (RuntimeException e) {
            return OUT_MALFORMED;
        }
    }

    private static Optional<String> singleUrlArg(String[] args) {
        if (args == null || args.length != 1)
            return Optional.empty();

        String raw = args[0];
        if (raw == null || raw.isBlank())
            return Optional.empty();

        return Optional.of(raw.trim());
    }

}
