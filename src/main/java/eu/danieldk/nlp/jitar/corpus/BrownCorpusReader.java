/*
 * Copyright 2008, 2009 Daniel de Kok
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

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
    String line = null;
    
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
