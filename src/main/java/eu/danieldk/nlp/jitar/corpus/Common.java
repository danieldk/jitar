//
// Copyright 2008, 2015 Daniël de Kok
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

import com.google.common.collect.ImmutableList;

import java.util.List;

/**
 * Some commons corpus utilities.
 *
 * @author Daniël de Kok &lt;me@danieldk.eu&gt;
 */
public final class Common {
    public static final String START_TOKEN = "<START>";

    public static final String END_TOKEN = "<END>";

    public static List<TaggedToken> DEFAULT_END_MARKERS = ImmutableList.of(new TaggedToken(END_TOKEN, END_TOKEN));

    public static List<String> DEFAULT_END_MARKER_TOKENS = ImmutableList.of(END_TOKEN);

    public static List<TaggedToken> DEFAULT_START_MARKERS = ImmutableList.of(
            new TaggedToken(START_TOKEN, START_TOKEN),
            new TaggedToken(START_TOKEN, START_TOKEN)
    );

    public static List<String> DEFAULT_START_MARKER_TOKENS = ImmutableList.of(START_TOKEN, START_TOKEN);

    private Common() {
    }
}
