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