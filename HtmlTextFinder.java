import java.io.BufferedReader;
import java.io.IOException;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Optional;

public class HtmlTextFinder {

    private final Deque<String> openTags;
    private int deepestDepth;
    private Optional<String> deepestText;

    public HtmlTextFinder() {
        this.openTags = new ArrayDeque<>();
        this.deepestDepth = -1;
        this.deepestText = Optional.empty();
    }

    public String find(BufferedReader reader) throws IOException {
        Optional<String> line = nextMeaningfulLine(reader);

        while (line.isPresent()) {
            handleLine(line.get());
            line = nextMeaningfulLine(reader);
        }

        if (!openTags.isEmpty()) {
            throw new IllegalArgumentException("Unclosed tags");
        }

        return deepestText.orElseThrow(() -> new IllegalArgumentException("No text found"));
    }

    private Optional<String> nextMeaningfulLine(BufferedReader reader) throws IOException {
        String raw = reader.readLine();

        while (raw != null) {
            String trimmed = raw.trim();
            if (!trimmed.isEmpty()) {
                return Optional.of(trimmed);
            }
            raw = reader.readLine();
        }

        return Optional.empty();
    }

    private void handleLine(String line) {
        Optional<HtmlTag> maybeTag = HtmlTag.tryParse(line);

        if (maybeTag.isPresent()) {
            applyTag(maybeTag.get());
            return;
        }

        if (looksLikeTag(line)) {
            throw new IllegalArgumentException("Malformed tag line");
        }

        acceptText(line);
    }

    private boolean looksLikeTag(String line) {
        return line != null && !line.isEmpty() && line.charAt(0) == '<';
    }

    private void applyTag(HtmlTag tag) {
        if (tag.isOpen()) {
            openTags.push(tag.name());
            return;
        }

        if (openTags.isEmpty()) {
            throw new IllegalArgumentException("Closing without opening");
        }

        String expected = openTags.peek();
        if (!expected.equals(tag.name())) {
            throw new IllegalArgumentException("Mismatched closing tag");
        }

        openTags.pop();
    }

    private void acceptText(String text) {
        int depth = openTags.size();
        if (depth <= deepestDepth) {
            return;
        }

        deepestDepth = depth;
        deepestText = Optional.of(text);
    }
}
