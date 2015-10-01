//
// Copyright 2008, 2014 Daniël de Kok
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//     http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.
//

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
    public static final String START_TOKEN = "<START>";

    public static final String END_TOKEN = "<END>";

    public static List<TaggedToken> DEFAULT_END_MARKERS = Collections.unmodifiableList(getEndMarkers());

    public static List<String> DEFAULT_END_MARKER_TOKENS = Collections.unmodifiableList(getEndMarkerTokens());

    public static List<TaggedToken> DEFAULT_START_MARKERS = Collections.unmodifiableList(getStartMarkers());

    public static List<String> DEFAULT_START_MARKER_TOKENS = Collections.unmodifiableList(getStartMarkerTokens());

    private static List<TaggedToken> getEndMarkers() {
        List<TaggedToken> endMarkers = new ArrayList<>();
        endMarkers.add(new TaggedToken(END_TOKEN, END_TOKEN));
        return endMarkers;
    }

    private static List<String> getEndMarkerTokens() {
        List<String> endMarkers = new ArrayList<>();
        endMarkers.add(END_TOKEN);
        return endMarkers;
    }

    private static List<TaggedToken> getStartMarkers() {
        List<TaggedToken> startMarkers = new ArrayList<>();
        startMarkers.add(new TaggedToken(START_TOKEN, START_TOKEN));
        startMarkers.add(new TaggedToken(START_TOKEN, START_TOKEN));
        return startMarkers;
    }

    private static List<String> getStartMarkerTokens() {
        List<String> startMarkers = new ArrayList<>();
        startMarkers.add(START_TOKEN);
        startMarkers.add(START_TOKEN);
        return startMarkers;
    }

    private Common() {}
}
