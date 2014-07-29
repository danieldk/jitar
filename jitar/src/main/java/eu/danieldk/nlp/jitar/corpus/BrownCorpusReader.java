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

import java.io.BufferedReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * This class implements a corpus reader for Brown-style corpera that are
 * stored in a single file.
 */
public class BrownCorpusReader implements CorpusReader {
    private final BufferedReader reader;

    private final List<TaggedToken> startMarkers;

    private final List<TaggedToken> endMarkers;

    boolean decapitalizeFirstWord;

    public BrownCorpusReader(BufferedReader reader, List<TaggedToken> startMarkers, List<TaggedToken> endMarkers, boolean decapitalizeFirstWord) {
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
        String line;
        while ((line = reader.readLine()) != null) {
            line = line.trim();

            if (line.length() == 0)
                continue;

            List<TaggedToken> sentence = new ArrayList<>(startMarkers);

            String[] lineParts = line.split("\\s+");
            for (int i = 0; i < lineParts.length; ++i) {
                String wordTag = lineParts[i];

                // Get the word and tag.
                int sepIndex = wordTag.lastIndexOf('/');

                if (sepIndex == -1)
                    throw new IOException(String.format("Tag is missing in '%s'", wordTag));

                String word = wordTag.substring(0, sepIndex);
                String tag = wordTag.substring(sepIndex + 1);

                if (word.length() == 0)
                    throw new IOException(String.format("Zero-length word in '%s'", wordTag));

                if (tag.length() == 0)
                    throw new IOException(String.format("Zero-length tag in '%s'", wordTag));

                if (decapitalizeFirstWord && i == 0)
                    word = replaceCharAt(word, 0, Character.toLowerCase(word.charAt(0)));

                sentence.add(new TaggedToken(word, tag));
            }

            sentence.addAll(endMarkers);

            return sentence;
        }

        return null;
    }
}
