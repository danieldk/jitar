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
import java.util.List;

/**
 * All corpus readers should inherit from the <i>CorpusReader</i> abstract
 * class. Child classes have to implement the <i>parse</i> method, and
 * should take care that:
 * 
 * <ul>
 * <li>Start/end markers are added.</li>
 * <li>The first word is decapitalized when requested.</li>
 * </ul>
 */
public abstract class CorpusReader<WordType> {	
	/**
	 * @param sentenceHandlers Handlers that corpus sentences should be passed to.
	 * @param startMarkers Start markers to add to sentences.
	 * @param endMarkers End markers to add to sentences.
	 */
	public CorpusReader(List<WordType> startMarkers,
			List<WordType> endMarkers, boolean decapitalizeFirstWord) {
		d_startMarkers = new ArrayList<WordType>(startMarkers);
		d_endMarkers = new ArrayList<WordType>(endMarkers);
		d_decapitalizeFirstWord = decapitalizeFirstWord;
	}
		
	/**
	 * Parse a corpus, passing sentences to registered corpus handlers.
	 * @param reader Reader providing the data to parse.
	 * @throws IOException
	 */
	abstract public void parse(BufferedReader reader) throws IOException, CorpusReaderException;
	
	/**
	 * This method adds a sentence handler to the corpus reader.
	 * @param sentenceHandler The handler to add.
	 */
	public void addHandler(CorpusSentenceHandler<WordType> sentenceHandler) {
		d_sentenceHandlers.add(sentenceHandler);
	}
	
	/**
	 * This method adds a sentence handler from the corpus reader.
	 * @param sentenceHandler The handler to remove.
	 */
	public void removeHandler(CorpusSentenceHandler<WordType> sentenceHandler) {
		d_sentenceHandlers.remove(sentenceHandler);
	}
	
	protected final List<CorpusSentenceHandler<WordType>> d_sentenceHandlers =
		new ArrayList<CorpusSentenceHandler<WordType>>();
	protected final List<WordType> d_startMarkers;
	protected final List<WordType> d_endMarkers;
	protected final boolean d_decapitalizeFirstWord;
}