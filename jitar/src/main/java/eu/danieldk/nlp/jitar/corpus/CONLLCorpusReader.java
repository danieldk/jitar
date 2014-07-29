// Copyright 2008, 2009, 2013 Daniel de Kok
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

package eu.danieldk.nlp.jitar.corpus;

import org.apache.commons.lang3.StringUtils;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class CONLLCorpusReader implements CorpusReader {
    private final BufferedReader reader;

    private final List<TaggedToken> startMarkers;

    private final List<TaggedToken> endMarkers;

    boolean decapitalizeFirstWord;

    public CONLLCorpusReader(BufferedReader reader, List<TaggedToken> startMarkers, List<TaggedToken> endMarkers, boolean decapitalizeFirstWord) {
        this.reader = reader;
        this.startMarkers = startMarkers;
        this.endMarkers = endMarkers;
        this.decapitalizeFirstWord = decapitalizeFirstWord;
    }

    private static String replaceCharAt(String str, int pos, char c) {
        StringBuilder sb = new StringBuilder();
        sb.append(str.substring(0, pos));
        sb.append(c);
        sb.append(str.substring(pos + 1));
        return sb.toString();
    }

    @Override
    public void close() throws IOException {
        reader.close();
    }

    @Override
    public List<TaggedToken> readSentence() throws IOException {
        List<TaggedToken> sentence = new ArrayList<>(startMarkers);

        String line;
        while ((line = reader.readLine()) != null) {
            String parts[] = StringUtils.split(line.trim(), '\t');

            // We are done with this sentence.
            if (parts.length == 0) {
                sentence.addAll(endMarkers);
                return sentence;
            }

            if (parts.length < 5)
                throw new IOException(String.format("Line has fewer than five columns: %s", line));

            String word = parts[1];
            if (decapitalizeFirstWord && sentence.size() == startMarkers.size())
                word = replaceCharAt(word, 0, Character.toLowerCase(word.charAt(0)));

            sentence.add(new TaggedToken(word, parts[4]));
        }

        // If the the file does not end with a blank line, we have left-overs.
        if (sentence.size() != startMarkers.size()) {
            sentence.addAll(endMarkers);
            return sentence;
        }

        return null;
    }
}
