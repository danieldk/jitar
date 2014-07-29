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

package eu.danieldk.nlp.jitar.wordhandler;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.Map.Entry;
import java.util.regex.Pattern;


import eu.danieldk.nlp.jitar.data.UniGram;
import eu.danieldk.nlp.jitar.data.util.ProbEntryComparator;

/**
 * The <i>SuffixWordHandler</i> class that tries to estimate the probability
 * of a word given a tag, based on the suffix of the word. This handler can
 * be used when a word could not be found in a lexicon such as the
 * <i>KnownWordHandler</i>. This handler has no fallback handler, because it
 * will always given a result, even when the shortest suffix is unknown.
 */
public class SuffixWordHandler implements WordHandler {	
	/**
	 * Construct a suffix word handler from a lexicon and the over unigram
	 * frequency list for the corpus.
	 * @param lexicon The lexicon.
	 * @param uniGrams Unigram tag frequencies.
	 * @param upperMaxFreq Uppercase words with a frequency lower than or equal
	 * 	to this value will be used for suffix training.
	 * @param lowerMaxFreq Lowercase words with a frequency lower than or equal
	 * 	to this value will be used for suffix training.
	 * @param cardinalMaxFreq Cardinals with a frequency lower than or equal
	 * 	to this value will be used for suffix training.
	 */
	public SuffixWordHandler(Map<String, Map<Integer, Integer>> lexicon,
			Map<UniGram, Integer> uniGrams, int maxSuffixLength, int upperMaxFreq,
			int lowerMaxFreq, int cardinalMaxFreq, int maxTags) {
		double theta = WordSuffixTree.calculateTheta(uniGrams);
		
		d_upperSuffixTrie = new WordSuffixTree(uniGrams, theta, maxSuffixLength);
		d_lowerSuffixTrie = new WordSuffixTree(uniGrams, theta, maxSuffixLength);
		d_cardinalSuffixTrie = new WordSuffixTree(uniGrams, theta, maxSuffixLength);
		d_maxTags = maxTags;

		for (Entry<String, Map<Integer, Integer>> wordEntry: lexicon.entrySet()) {
			String word = wordEntry.getKey();
			
			// Incorrect lexicon entry.
			if (word.length() == 0)
				continue;
			
			int wordFreq = 0;
			for (Entry<Integer, Integer> tagEntry: wordEntry.getValue().entrySet())
				wordFreq += tagEntry.getValue();

			// Select the correct tree.
			WordSuffixTree suffixTree;
			if (s_cardinalPattern.matcher(word).matches()) {
				if (wordFreq > cardinalMaxFreq)
					continue;

				suffixTree = d_cardinalSuffixTrie;
			}
			else {
				boolean isUpper = Character.isUpperCase(word.charAt(0));
				
				if (!isUpper && wordFreq > lowerMaxFreq)
					continue;
				if (isUpper && wordFreq > upperMaxFreq)
					continue;
				
				suffixTree = isUpper ? d_upperSuffixTrie : d_lowerSuffixTrie;
			}
			
			suffixTree.addWord(word, wordEntry.getValue());
		}
	}
	
	public Map<Integer, Double> tagProbs(String word) {
		WordSuffixTree suffixTree;
		if (s_cardinalPattern.matcher(word).matches())
			suffixTree = d_cardinalSuffixTrie;
		else {
			boolean isUpper = Character.isUpperCase(word.charAt(0));
			suffixTree = isUpper ? d_upperSuffixTrie : d_lowerSuffixTrie;
		}
		
		Set<Entry<Integer, Double>> orderedTags =
			new TreeSet<>(new ProbEntryComparator());
		orderedTags.addAll(suffixTree.suffixTagProbs(word).entrySet());
		
		// Get first N results, ordered by descending probability.
		Map<Integer, Double> results = new HashMap<>();
		Iterator<Entry<Integer, Double>> iter = orderedTags.iterator();
		for (int i = 0; i < d_maxTags && iter.hasNext(); ++i) {
			Entry<Integer, Double> entry = iter.next();
			results.put(entry.getKey(), Math.log(entry.getValue()));
		}
		
		return results;
	}
	
	private final static Pattern s_cardinalPattern =
		Pattern.compile("^([0-9]+)|([0-9]+\\.)|([0-9.,:-]+[0-9]+)|([0-9]+[a-zA-Z]{1,3})$");
	private final WordSuffixTree d_upperSuffixTrie;
	private final WordSuffixTree d_lowerSuffixTrie;
	private final WordSuffixTree d_cardinalSuffixTrie;
	private final int d_maxTags;
}
