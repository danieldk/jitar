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
 * <t>WordSuffixTree</t> is used to store words in a suffix tree.
 */
class WordSuffixTree {
	private class TreeNode {
		private final Map<Character, TreeNode> d_children;
		private final Map<Integer, Integer> d_tagFreqs;
		private int d_tagFreq;
		
		public TreeNode() {
			d_children = new HashMap<Character, TreeNode>();
			d_tagFreqs = new HashMap<Integer, Integer>();
			d_tagFreq = 0;
		}
		
		public void addSuffix(String reverseSuffix, Map<Integer, Integer> tagFreqs) {
			// Add the tag frequencies to the current state/node.
			if (this != d_root)
				for (Entry<Integer, Integer> entry: tagFreqs.entrySet()) {
					Integer tag = entry.getKey();
					int tagFreq = entry.getValue();
				
					if (!d_tagFreqs.containsKey(tag))
						d_tagFreqs.put(tag, tagFreq);
					else
						d_tagFreqs.put(tag, d_tagFreqs.get(tag) + tagFreq);

					d_tagFreq += tagFreq;
				}
			
			// If the suffix is fully processed, we reached the final state for
			// this suffix.
			if (reverseSuffix.length() == 0)
				return;

			// Add transition.
			Character transitionChar = reverseSuffix.charAt(0);
			if (!d_children.containsKey(transitionChar))
				d_children.put(transitionChar, new TreeNode());
			d_children.get(transitionChar).addSuffix(reverseSuffix.substring(1), tagFreqs);
		}
		
		public Map<Integer, Double> suffixTagProbs(String reverseSuffix,
				Map<Integer, Double> tagProbs) {
			for (Integer tag: d_root.d_tagFreqs.keySet()) {
				double p = 0.0;
				if (d_tagFreqs.containsKey(tag))
					// P(t|reverseSuffix).
					p = d_tagFreqs.get(tag) / (double) d_tagFreq;
				
				if (this != d_root) {
					// Add weighted probability of the shorter suffixes.
					p += d_theta * tagProbs.get(tag);
					
					// Normalize.
					p /= d_theta + 1.0;
				}
				
				tagProbs.put(tag, p);
			}
			
			// If the remaining suffix length is zero, we reached the final
			// state for this suffix.
			if (reverseSuffix.length() == 0)
				return bayesianInversion(tagProbs);

			// Transition on the next suffix character.
			Character transitionChar = reverseSuffix.charAt(0);
			if (!d_children.containsKey(transitionChar))
				return bayesianInversion(tagProbs);
			
			return d_children.get(transitionChar).suffixTagProbs(reverseSuffix.substring(1),
					tagProbs);			
		}
	}
	
	private final Map<Integer, Double> bayesianInversion(Map<Integer, Double> tagProbs) {
		Map<Integer, Double> inverseTagProbs = new HashMap<Integer, Double>();
		
		for (Entry<Integer, Double> tagProb: tagProbs.entrySet()) {
			Integer tag = tagProb.getKey();
			Double value = tagProb.getValue();

			inverseTagProbs.put(tag, value / d_uniGrams.get(new UniGram(tag)));
		}
		
		return inverseTagProbs;
	}
	
	private final Map<UniGram, Integer> d_uniGrams;
	private final TreeNode d_root;
	private final int d_maxLength;
	private double d_theta;
	
	public WordSuffixTree(Map<UniGram, Integer> uniGrams, double theta,
			int maxLength) {
		d_uniGrams = new HashMap<UniGram, Integer>(uniGrams);
		d_theta = theta;
		d_maxLength = maxLength;
		d_root = new TreeNode();
		
		for (Entry<UniGram, Integer> uniGramFreq: d_uniGrams.entrySet())
		{
			d_root.d_tagFreqs.put(uniGramFreq.getKey().t1(),
					uniGramFreq.getValue());
			d_root.d_tagFreq += uniGramFreq.getValue();
		}
	}
	
	public WordSuffixTree(Map<UniGram, Integer> uniGrams, double theta) {
		this(uniGrams, theta, 10);
	}
	
	public void addWord(String word, Map<Integer, Integer> tagFreqs) {
		String reverseWord = reverse(word);
	
		if (reverseWord.length() > d_maxLength)
			reverseWord = reverseWord.substring(0, d_maxLength);
		
		d_root.addSuffix(reverseWord, tagFreqs);
	}
			
	public Map<Integer, Double> suffixTagProbs(String word) {
		String reverseWord = reverse(word);
		
		if (reverseWord.length() > d_maxLength)
			reverseWord = reverseWord.substring(0, d_maxLength);

		return d_root.suffixTagProbs(reverseWord, new HashMap<Integer, Double>());
	}
	
	public static double calculateTheta(Map<UniGram, Integer> uniGrams) {
		double pAvg = 1.0 / uniGrams.size();
		
		int freqSum = 0;
		for (Entry<UniGram, Integer> entry: uniGrams.entrySet())
			freqSum += entry.getValue();
		
		double stdDevSum = 0.0;
		for (Entry<UniGram, Integer> entry: uniGrams.entrySet()) {
			// P(t)
			double p = entry.getValue() / (double) freqSum;
			stdDevSum += Math.pow(p - pAvg, 2.0);
		}
		
		return Math.sqrt(stdDevSum / (uniGrams.size() - 1));
	}
	
	private String reverse(String str) {
		char[] reverseChars = new char[str.length()];
		
		for (int i = 0; i < str.length(); ++i)
			reverseChars[i] = str.charAt(str.length() - (1 + i));
		
		return new String(reverseChars);
	}	
}