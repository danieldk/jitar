package eu.danieldk.nlp.jitar.corpus;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Some commons corpus utilities.
 *
 * @author Daniël de Kok <me@danieldk.eu>
 */
public class Common {
    public static List<TaggedToken> DEFAULT_END_MARKERS = Collections.unmodifiableList(getEndMarkers());

    public static List<String> DEFAULT_END_MARKER_TOKENS = Collections.unmodifiableList(getEndMarkerTokens());

    public static List<TaggedToken> DEFAULT_START_MARKERS = Collections.unmodifiableList(getStartMarkers());

    public static List<String> DEFAULT_START_MARKER_TOKENS = Collections.unmodifiableList(getStartMarkerTokens());

    private static List<TaggedToken> getEndMarkers() {
        List<TaggedToken> endMarkers = new ArrayList<>();
        endMarkers.add(new TaggedToken("<END>", "<END>"));
        return endMarkers;
    }

    private static List<String> getEndMarkerTokens() {
        List<String> endMarkers = new ArrayList<>();
        endMarkers.add("<END>");
        return endMarkers;
    }

    private static List<TaggedToken> getStartMarkers() {
        List<TaggedToken> startMarkers = new ArrayList<>();
        startMarkers.add(new TaggedToken("<START>", "<START>"));
        startMarkers.add(new TaggedToken("<START>", "<START>"));
        return startMarkers;
    }

    private static List<String> getStartMarkerTokens() {
        List<String> startMarkers = new ArrayList<>();
        startMarkers.add("<START>");
        startMarkers.add("<START>");
        return startMarkers;
    }

    private Common() {}
}
