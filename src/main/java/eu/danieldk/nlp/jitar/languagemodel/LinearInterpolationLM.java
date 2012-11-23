/*
 * Copyright 2008 Daniel de Kok
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

package eu.danieldk.nlp.jitar.languagemodel;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;

import eu.danieldk.nlp.jitar.data.BiGram;
import eu.danieldk.nlp.jitar.data.TriGram;
import eu.danieldk.nlp.jitar.data.UniGram;

/**
 * This model estimates trigram probabilities using linear interpolation
 * smoothing.
 */
public class LinearInterpolationLM implements LanguageModel {
	private final Map<UniGram, Integer> d_uniGramFreqs;
	private final Map<BiGram, Integer> d_biGramFreqs;
	private final Map<TriGram, Integer> d_triGramFreqs;
	private int d_corpusSize;
	private double d_l1;
	private double d_l2;
	private double d_l3;
	private final Map<TriGram, Double> d_triGramCache;
	
	/**
	 * @param uniGramFreqs 1-gram frequencies
	 * @param biGramFreqs 2-gram frequencies
	 * @param triGramFreqs 3-gram frequencies
	 */
	public LinearInterpolationLM(Map<UniGram, Integer> uniGramFreqs,
			Map<BiGram, Integer> biGramFreqs,
			Map<TriGram, Integer> triGramFreqs) {
		d_uniGramFreqs = new HashMap<UniGram, Integer>(uniGramFreqs);
		d_biGramFreqs = new HashMap<BiGram, Integer>(biGramFreqs);
		d_triGramFreqs = new HashMap<TriGram, Integer>(triGramFreqs);
		d_triGramCache = new ConcurrentHashMap<TriGram, Double>();
		
		calculateCorpusSize();
		calculateLambdas();
	}

	public double triGramProb(TriGram triGram) {
		// If we have cached the likelihood for this trigram, return it.
		if (d_triGramCache.containsKey(triGram))
			return d_triGramCache.get(triGram);
		
		// Unigram likelihood P(t3)
		UniGram t3 = new UniGram(triGram.t3());
		double uniGramProb = d_uniGramFreqs.get(t3) / (double) d_corpusSize;
		
		// Bigram likelihood P(t3|t2).
		BiGram t2t3 = new BiGram(triGram.t2(), triGram.t3());
		UniGram t2 = new UniGram(triGram.t2());
		double biGramProb = 0.0;
		if (d_uniGramFreqs.containsKey(t2) && d_biGramFreqs.containsKey(t2t3))
			biGramProb = d_biGramFreqs.get(t2t3) / (double) d_uniGramFreqs.get(t2);
		
		// Trigram likelihood P(t3|t1,t2).
		BiGram t1t2 = new BiGram(triGram.t1(), triGram.t2());
		double triGramProb = 0.0;
		if (d_biGramFreqs.containsKey(t1t2) && d_triGramFreqs.containsKey(triGram))
			triGramProb = d_triGramFreqs.get(triGram) / (double) d_biGramFreqs.get(t1t2);
		
		double prob = Math.log(d_l1 * uniGramProb + d_l2 * biGramProb +
			d_l3 * triGramProb);
		
		d_triGramCache.put(triGram, prob);
		
		return prob;	
	}
	
	private void calculateCorpusSize() {
		for (Entry<UniGram, Integer> entry: d_uniGramFreqs.entrySet())
			d_corpusSize += entry.getValue();
	}
	
	private void calculateLambdas() {
		int l1f = 0;
		int l2f = 0;
		int l3f = 0;
		
		for (Entry<TriGram, Integer> triGramEntry: d_triGramFreqs.entrySet()) {
			TriGram t1t2t3 = triGramEntry.getKey();
			
			BiGram t1t2 = new BiGram(t1t2t3.t1(), t1t2t3.t2());
			double l3p = 0.0;
			if (d_biGramFreqs.containsKey(t1t2))
				l3p = (triGramEntry.getValue() - 1) /
					(double) (d_biGramFreqs.get(t1t2) - 1);
			
			BiGram t2t3 = new BiGram(t1t2t3.t2(), t1t2t3.t3());
			UniGram t2 = new UniGram(t1t2t3.t2());
			double l2p = 0.0;
			if (d_uniGramFreqs.containsKey(t2) && d_biGramFreqs.containsKey(t2t3))
				l2p = (d_biGramFreqs.get(t2t3) - 1) /
					(double) (d_uniGramFreqs.get(t2) - 1);
			
			UniGram t3 = new UniGram(t1t2t3.t3());
			double l1p = 0.0;
			if (d_uniGramFreqs.containsKey(t3))
				l1p = (d_uniGramFreqs.get(t3) - 1) /
					(double) (d_corpusSize - 1);
			
			if (l1p > l2p && l1p > l3p)
				l1f += triGramEntry.getValue();
			else if (l2p > l1p && l2p > l3p)
				l2f += triGramEntry.getValue();
			else
				l3f += triGramEntry.getValue();
		}
		
		double totalTriGrams = l1f + l2f + l3f;
		
		d_l1 = l1f / totalTriGrams;
		d_l2 = l2f / totalTriGrams;
		d_l3 = l3f / totalTriGrams;
	}
}
