import java.io.BufferedReader;
import java.io.IOException;
import java.util.ArrayDeque;
import java.util.Deque;

final class DeepestTextScanner {

    private DeepestTextScanner() {
    }

    static String findDeepestText(BufferedReader reader) throws IOException, TagParser.MalformedHtmlException {
        ScanState state = new ScanState();
        for (String line; (line = nextMeaningfulLine(reader)) != null; ) state.consume(line);
        state.ensureAllTagsClosed();
        return state.bestText;
    }

    private static String nextMeaningfulLine(BufferedReader reader) throws IOException {
        for (String raw; (raw = reader.readLine()) != null; ) {
            String trimmed = raw.trim();
            if (!trimmed.isEmpty()) return trimmed;
        }
        return null;
    }

    private static final class ScanState {

        private final Deque<String> openTags = new ArrayDeque<>();
        private int bestDepth = -1;
        private String bestText = "";

        void consume(String line) throws TagParser.MalformedHtmlException {
            if (isTagLine(line)) consumeTagLine(line);
            else acceptText(line);
        }

        void ensureAllTagsClosed() throws TagParser.MalformedHtmlException {
            if (!openTags.isEmpty()) throw new TagParser.MalformedHtmlException();
        }

        private void acceptText(String text) {
            int depth = openTags.size();
            if (depth <= bestDepth) return;
            bestDepth = depth;
            bestText = text;
        }

        private void consumeTagLine(String line) throws TagParser.MalformedHtmlException {
            TagParser.Tag tag = TagParser.parse(line);
            if (tag.kind == TagParser.TagKind.OPEN) openTags.push(tag.name);
            else closeExpected(tag.name);
        }

        private void closeExpected(String closingTag) throws TagParser.MalformedHtmlException {
            if (openTags.isEmpty()) throw new TagParser.MalformedHtmlException();
            if (!openTags.peek().equals(closingTag)) throw new TagParser.MalformedHtmlException();
            openTags.pop();
        }
    }

    private static boolean isTagLine(String line) {
        return !line.isEmpty() && line.charAt(0) == '<';
    }
}
