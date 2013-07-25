// Copyright 2008, 2009 Daniel de Kok
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
import java.util.Collections;
import java.util.List;

import eu.danieldk.nlp.jitar.corpus.CorpusReaderException.CorpusReadError;

/**
 * This class implements a corpus reader for Brown-style corpera that are
 * stored in a single file.
 */
public class BrownCorpusReader extends CorpusReader<TaggedWord> {
  public BrownCorpusReader(List<TaggedWord> startMarkers,
      List<TaggedWord> endMarkers, boolean decapitalizeFirstWord) {
    super(startMarkers, endMarkers, decapitalizeFirstWord);
  }
    
  public void parse(BufferedReader reader) throws IOException, CorpusReaderException {
    String line;
    while ((line = reader.readLine()) != null) {
      line = line.trim();
      
      if (line.length() == 0)
        continue;
      
      List<TaggedWord> sentence = new ArrayList<TaggedWord>(d_startMarkers);
      
      String[] lineParts = line.split("\\s+");
      for (int i = 0; i < lineParts.length; ++i) {
        String wordTag = lineParts[i];
      
        // Get the word and tag.
        int sepIndex = wordTag.lastIndexOf('/');

        if (sepIndex == -1)
          throw new CorpusReaderException("Tag is missing in '" +
              wordTag + "'", CorpusReadError.MISSING_TAG);

        String word = wordTag.substring(0, sepIndex);
        String tag = wordTag.substring(sepIndex + 1);

        if (word.length() == 0)
          throw new CorpusReaderException("Zero-length word in '" +
              wordTag + "'", CorpusReadError.ZERO_LENGTH_WORD);

        if (tag.length() == 0)
          throw new CorpusReaderException("Zero-length tag in '" +
              wordTag + "'", CorpusReadError.ZERO_LENGTH_TAG);

        if (d_decapitalizeFirstWord && i == 0)
          word = replaceCharAt(word, 0, Character.toLowerCase(word.charAt(0)));
        
        sentence.add(new TaggedWord(word, tag));        
      }
      
      sentence.addAll(d_endMarkers);

      // We don't want handlers to modify the sentence, so make it
      // immutable;
      sentence = Collections.unmodifiableList(sentence);

      // Call handlers.
      for (CorpusSentenceHandler<TaggedWord> handler: d_sentenceHandlers)
        handler.handleSentence(sentence);
    }
  }
  
  private static String replaceCharAt(String str, int pos, char c) {
    StringBuilder sb = new StringBuilder();
    sb.append(str.substring(0, pos));
    sb.append(c);
    sb.append(str.substring(pos + 1));
    return sb.toString();
  }
}
