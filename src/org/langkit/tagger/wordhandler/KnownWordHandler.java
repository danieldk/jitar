/*
 * Copyright 2008, 2009 Daniël de Kok
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

package org.langkit.tagger.wordhandler;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.langkit.tagger.data.UniGram;

/**
 * The <i>KnownWordHandler</i> class estimates the probability of a word given
 * a tag, based on a lexicon. If a word is not known, and a fallback word handler
 * was specified, the word will be offered to the fallback.
 */
public class KnownWordHandler implements WordHandler {	
	/**
	 * Construct a known word handler.
	 * @param wordTagFreqs A map holding tag frequencies per word.
	 * @param uniGramFreqs Frequencies of uni-grams.
	 * @param fallbackWordHandler A fallback word handler to call if a word is
	 * 	not known.
	 */
	public KnownWordHandler(Map<String, Map<Integer, Integer>> wordTagFreqs,
			Map<UniGram, Integer> uniGramFreqs, WordHandler fallbackWordHandler) {
		d_wordTagProbs = new HashMap<String, Map<Integer,Double>>();
		calculateWordTagProbs(wordTagFreqs, uniGramFreqs);
		
		d_fallbackWordHandler = fallbackWordHandler;
	}
	
	public KnownWordHandler(Map<String, Map<Integer, Integer>> wordTagProbs,
			Map<UniGram, Integer> uniGramFreqs) {
		this(wordTagProbs, uniGramFreqs, null);
	}
	
	public Map<Integer, Double> tagProbs(String word) {
		// Lookup the word. If it is known, return P(w|t) probabilities for
		// each tag that the word was seen with in the training data.
		if (d_wordTagProbs.containsKey(word))
				return new HashMap<Integer, Double>(d_wordTagProbs.get(word));

		// If the word could not be found, maybe its lowercase variant can
		// be found (e.g. capitalized words that start a sentence).
		if (Character.isUpperCase(word.charAt(0))) {
			String lcWord = word.toLowerCase();
			if (d_wordTagProbs.containsKey(lcWord))
				return new HashMap<Integer, Double>(d_wordTagProbs.get(lcWord));
		}

		// Try the fallback word handler, if it is available.
		if (d_fallbackWordHandler != null)
			return d_fallbackWordHandler.tagProbs(word);
		else
			return new HashMap<Integer, Double>();
	}
	
	private void calculateWordTagProbs(Map<String, Map<Integer, Integer>> wordTagFreqs,
			Map<UniGram, Integer> uniGramFreqs) {
		for (Entry<String, Map<Integer, Integer>> wordEntry: wordTagFreqs.entrySet()) {
			String word = wordEntry.getKey();
			
			if (!d_wordTagProbs.containsKey(word))
				d_wordTagProbs.put(word, new HashMap<Integer, Double>());
			
			for (Entry<Integer, Integer> tagEntry: wordEntry.getValue().entrySet()) {
				Integer tag = tagEntry.getKey();
				int freq = tagEntry.getValue();

				// P(w|t) = f(w,t) / f(t)
				double p = Math.log(freq / (double) uniGramFreqs.get(new UniGram(tag)));
				d_wordTagProbs.get(word).put(tag, p);
			}
		}
	}
	
	private final Map<String, Map<Integer, Double>> d_wordTagProbs;
	private final WordHandler d_fallbackWordHandler;
}
