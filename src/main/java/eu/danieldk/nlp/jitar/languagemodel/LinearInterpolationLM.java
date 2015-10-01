//
// Copyright 2008, 2015 Daniël de Kok
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
//

package eu.danieldk.nlp.jitar.languagemodel;

import java.util.Map;
import java.util.Map.Entry;

import com.google.common.collect.ImmutableMap;
import eu.danieldk.nlp.jitar.data.BiGram;
import eu.danieldk.nlp.jitar.data.TriGram;
import eu.danieldk.nlp.jitar.data.UniGram;

/**
 * This model estimates trigram probabilities using linear interpolation
 * smoothing.
 */
public class LinearInterpolationLM implements LanguageModel {
	private final Map<UniGram, Double> d_uniGramProbs;
	private final Map<BiGram, Double> d_biGramProbs;
	private final Map<TriGram, Double> d_triGramProbs;

	/**
	 * @param uniGramFreqs 1-gram frequencies
	 * @param biGramFreqs 2-gram frequencies
	 * @param triGramFreqs 3-gram frequencies
	 */
	public LinearInterpolationLM(Map<UniGram, Integer> uniGramFreqs,
			Map<BiGram, Integer> biGramFreqs,
			Map<TriGram, Integer> triGramFreqs) {

		int corpusSize = calculateCorpusSize(uniGramFreqs);
        SmoothingParameters smoothingParameters = calculateLambdas(corpusSize, uniGramFreqs, biGramFreqs, triGramFreqs);
        d_uniGramProbs = unigramProbs(corpusSize, smoothingParameters, uniGramFreqs);
        d_biGramProbs = bigramProbs(corpusSize, smoothingParameters, uniGramFreqs, biGramFreqs);
        d_triGramProbs = trigramProbs(corpusSize, smoothingParameters, uniGramFreqs, biGramFreqs, triGramFreqs);
	}

    private Map<UniGram, Double> unigramProbs(int corpusSize, SmoothingParameters smoothingParameters, Map<UniGram, Integer> uniGramFreqs) {
        ImmutableMap.Builder<UniGram, Double> builder = ImmutableMap.builder();

        for (Entry<UniGram, Integer> entry: uniGramFreqs.entrySet()) {
            double uniGramProb = entry.getValue() / (double) corpusSize;
            double prob = Math.log(smoothingParameters.l1() * uniGramProb);
            builder.put(entry.getKey(), prob);
        }

        return builder.build();
    }


    private Map<BiGram, Double> bigramProbs(int corpusSize, SmoothingParameters smoothingParameters, Map<UniGram, Integer> uniGramFreqs, Map<BiGram, Integer> biGramFreqs) {
        ImmutableMap.Builder<BiGram, Double> builder = ImmutableMap.builder();

        for (Entry<BiGram, Integer> entry: biGramFreqs.entrySet()) {
            BiGram biGram = entry.getKey();

            // Unigram likelihood P(t2)
            UniGram t2 = new UniGram(biGram.t2());
            double uniGramProb = uniGramFreqs.get(t2) / (double) corpusSize;

            // Bigram likelihood P(t2|t1).
            UniGram t1 = new UniGram(biGram.t1());
            Integer t1Freq = uniGramFreqs.get(t1);
            double biGramProb = 0.0;
            if (t1 != null)
                biGramProb = entry.getValue() / (double) t1Freq;

            double prob = Math.log(smoothingParameters.l1() * uniGramProb + smoothingParameters.l2() * biGramProb);

            builder.put(biGram, prob);
        }

        return builder.build();
    }

    private Map<TriGram, Double> trigramProbs(int corpusSize, SmoothingParameters smoothingParameters, Map<UniGram, Integer> uniGramFreqs, Map<BiGram, Integer> biGramFreqs,
                                              Map<TriGram, Integer> triGramFreqs) {
        ImmutableMap.Builder<TriGram, Double> builder = ImmutableMap.builder();

        for (TriGram triGram: triGramFreqs.keySet()) {
            // Unigram likelihood P(t3)
            UniGram t3 = new UniGram(triGram.t3());
            double uniGramProb = uniGramFreqs.get(t3) / (double) corpusSize;

            // Bigram likelihood P(t3|t2).
            BiGram t2t3 = new BiGram(triGram.t2(), triGram.t3());
            UniGram t2 = new UniGram(triGram.t2());
            Integer t2Freq = uniGramFreqs.get(t2);
            Integer t2t3Freq = biGramFreqs.get(t2t3);
            double biGramProb = 0.0;
            if (t2Freq != null && t2t3Freq != null)
                biGramProb = t2t3Freq / (double) t2Freq;

            // Trigram likelihood P(t3|t1,t2).
            BiGram t1t2 = new BiGram(triGram.t1(), triGram.t2());
            Integer t1t2Freq = biGramFreqs.get(t1t2);
            Integer triGramFreq = triGramFreqs.get(triGram);
            double triGramProb = 0.0;
            if (t1t2Freq != null && triGramFreq != null)
                triGramProb = triGramFreq / (double) t1t2Freq;

            double prob = Math.log(smoothingParameters.l1() * uniGramProb +
                    smoothingParameters.l2() * biGramProb +
                    smoothingParameters.l3() * triGramProb);

            builder.put(triGram, prob);
        }

        return builder.build();
    }

	public double triGramProb(TriGram triGram) {
        Double prob = d_triGramProbs.get(triGram);
        if (prob != null)
            return prob;

        BiGram biGram = new BiGram(triGram.t2(), triGram.t3());
        prob = d_biGramProbs.get(biGram);
        if (prob != null)
            return prob;

        UniGram uniGram = new UniGram(triGram.t3());
        prob = d_uniGramProbs.get(uniGram);
        if (prob != null)
            return prob;

        throw new RuntimeException(String.format("Unknown tag: %d", uniGram.t1()));
	}
	
	private int calculateCorpusSize(Map<UniGram, Integer> uniGramFreqs) {
        int corpusSize = 0;

        for (Integer freq : uniGramFreqs.values()) {
            corpusSize += freq;
        }

        return corpusSize;
	}
	
	private SmoothingParameters calculateLambdas(int corpusSize, Map<UniGram, Integer> uniGramFreqs,
                                                 Map<BiGram, Integer> biGramFreqs,
                                                 Map<TriGram, Integer> triGramFreqs) {
		int l1f = 0;
		int l2f = 0;
		int l3f = 0;
		
		for (Entry<TriGram, Integer> triGramEntry: triGramFreqs.entrySet()) {
			TriGram t1t2t3 = triGramEntry.getKey();
			
			BiGram t1t2 = new BiGram(t1t2t3.t1(), t1t2t3.t2());
            Integer t1t2Freq = biGramFreqs.get(t1t2);
			double l3p = 0.0;
			if (t1t2Freq != null)
				l3p = (triGramEntry.getValue() - 1) / (double) (t1t2Freq - 1);
			
			BiGram t2t3 = new BiGram(t1t2t3.t2(), t1t2t3.t3());
			UniGram t2 = new UniGram(t1t2t3.t2());
            Integer t2t3Freq = biGramFreqs.get(t2t3);
            Integer t2Freq = uniGramFreqs.get(t2);
			double l2p = 0.0;
			if (t2t3Freq != null && t2Freq != null)
				l2p = (biGramFreqs.get(t2t3) - 1) / (double) (uniGramFreqs.get(t2) - 1);
			
			UniGram t3 = new UniGram(t1t2t3.t3());
            Integer t3Freq = uniGramFreqs.get(t3);
			double l1p = 0.0;
			if (t3Freq != null)
				l1p = (t3Freq - 1) / (double) (corpusSize - 1);
			
			if (l1p > l2p && l1p > l3p)
				l1f += triGramEntry.getValue();
			else if (l2p > l1p && l2p > l3p)
				l2f += triGramEntry.getValue();
			else
				l3f += triGramEntry.getValue();
		}
		
		double totalTriGrams = l1f + l2f + l3f;
		
		return new SmoothingParameters(l1f / totalTriGrams, l2f / totalTriGrams, l3f / totalTriGrams);
	}

    private class SmoothingParameters {
        private final double l1;
        private final double l2;
        private final double l3;

        private SmoothingParameters(double l1, double l2, double l3) {
            this.l1 = l1;
            this.l2 = l2;
            this.l3 = l3;
        }

        public double l1() {
            return l1;
        }

        public double l2() {
            return l2;
        }

        public double l3() {
            return l3;
        }
    }
}
