//
// Copyright 2008, 2014 Daniël de Kok
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

package eu.danieldk.nlp.jitar.tagger;

import eu.danieldk.nlp.jitar.corpus.Common;
import eu.danieldk.nlp.jitar.data.Model;
import eu.danieldk.nlp.jitar.data.TriGram;
import eu.danieldk.nlp.jitar.languagemodel.LanguageModel;
import eu.danieldk.nlp.jitar.wordhandler.WordHandler;

import java.util.*;
import java.util.Map.Entry;

/**
 * Instances of this class can be used to tag sequences using a Hidden
 * Markov Model (HMM).
 */
public class HMMTagger {
    private final Model d_model;

    private final WordHandler d_wordHandler;

    private final LanguageModel d_languageModel;

    private final double d_beamFactor;

    private static class TrellisState {
        public int tag;

        public Map<TrellisState, Double> probabilities =
                new HashMap<>();

        public Map<TrellisState, TrellisState> backPointers =
                new HashMap<>();

        public TrellisState(int tag) {
            this.tag = tag;
        }
    }

    /**
     * Wrapper class for a sequence, and its associated probability.
     */
    public static class Sequence {
        public Sequence(List<Integer> sequence, double logProb, Model model) {
            d_sequence = sequence;
            d_logProb = logProb;
            d_numberTags = model.numberTags();
        }

        /**
         * Return the sequence.
         *
         * @return Sequence of part-of-speech tags.
         */
        public List<String> sequence() {
            List<String> tagSequence = new ArrayList<>(d_sequence.size() - 3);

            for (int i = Common.DEFAULT_START_MARKER_TOKENS.size();
                 i < d_sequence.size() - Common.DEFAULT_END_MARKER_TOKENS.size(); ++i) {
                Integer tagNumber = d_sequence.get(i);

                // Get the tag, remove capitalization information.
                String tag = d_numberTags.get(tagNumber).substring(2);
                tagSequence.add(tag);

            }

            return tagSequence;
        }

        /**
         * Return the probability of the sequence in log space.
         *
         * @return log(p(w1..n))
         */
        public double logProb() {
            return d_logProb;
        }

        private final List<Integer> d_sequence;

        private final double d_logProb;

        private final Map<Integer, String> d_numberTags;
    }

    /**
     * Construct an <i>HMMTagger</i> instance.
     *
     * @param wordHandler   The handler to be used for retrieving the probabilities
     *                      of a word given a tag.
     * @param languageModel The language model.
     * @param beamFactor    A beam factor, states with a probability lower than the
     *                      most probably state divided by this factor will be discarded.
     */
    public HMMTagger(Model model, WordHandler wordHandler, LanguageModel languageModel,
                     double beamFactor) {
        d_wordHandler = wordHandler;
        d_model = model;
        d_languageModel = languageModel;
        d_beamFactor = beamFactor;
    }

    /**
     * Extract the most probable sequence from a tag matrix.
     *
     * @param trellis The Viterbi trellis.
     * @return The tag sequence with the highest probability.
     */
    public static Sequence highestProbabilitySequence(List<TrellisState> trellis,
                                                      Model model) {
        // Find the most probably final state.
        double highestProb = Double.NEGATIVE_INFINITY;
        TrellisState tail = null;
        TrellisState beforeTail = null;

        // Find the most probable state in the last column.
        for (TrellisState entry : trellis) {
            for (Map.Entry<TrellisState, Double> probEntry : entry.probabilities.entrySet()) {
                if (probEntry.getValue() > highestProb) {
                    highestProb = probEntry.getValue();
                    tail = entry;
                    beforeTail = probEntry.getKey();
                }
            }
        }

        // We should always have a final state with some probability.
        assert tail != null;

        List<Integer> tagSequence = new ArrayList<>(trellis.size());

        while (true) {
            tagSequence.add(tail.tag);

            if (beforeTail == null)
                break;

            TrellisState tmp = tail.backPointers.get(beforeTail);
            tail = beforeTail;
            beforeTail = tmp;
        }

        Collections.reverse(tagSequence);

        return new Sequence(tagSequence, highestProb, model);
    }

    /**
     * Return the model used by the tagger.
     */
    public Model model() {
        return d_model;
    }

    /**
     * Tag a sentence.
     *
     * @return The trellis.
     */
    public List<TrellisState> tag(List<String> sentence) {
        List<String> tokens = new ArrayList<>(sentence);
        tokens.addAll(0, Common.DEFAULT_START_MARKER_TOKENS);
        tokens.addAll(Common.DEFAULT_END_MARKER_TOKENS);
        return viterbi(tokens);
    }

    /**
     * Tag a sentence.
     *
     * @param sentence The actual sentence with two start markers, and preferably
     *                 one end marker.
     * @return The Viterbi trellis.
     */
    private List<TrellisState> viterbi(List<String> sentence) {
        List<TrellisState> trellis = new ArrayList<>();

        int startTag = d_model.tagNumbers().get(sentence.get(0));

        // Prepare initial trellis states;
        TrellisState initialState = new TrellisState(startTag);

        TrellisState initialState2 = new TrellisState(startTag);
        initialState2.probabilities.put(initialState, 0.0);
        initialState2.backPointers.put(initialState, null);

        trellis.add(initialState2);

        double beam = 0.0;

        // Loop through the tokens.
        for (int i = 2; i < sentence.size(); ++i) {
            double columnHighestProb = Double.NEGATIVE_INFINITY;

            List<TrellisState> newTrellis = new ArrayList<>();

            for (Entry<Integer, Double> tagEntry :
                    d_wordHandler.tagProbs(sentence.get(i)).entrySet()) {
                TrellisState newEntry = new TrellisState(tagEntry.getKey());

                // Loop over all possible trigrams
                for (TrellisState t2 : trellis) {
                    double highestProb = Double.NEGATIVE_INFINITY;
                    TrellisState highestProbBp = null;

                    for (Map.Entry<TrellisState, Double> t1Entry : t2.probabilities.entrySet()) {
                        if (t1Entry.getValue() < beam)
                            continue;

                        TriGram curTriGram = new TriGram(t1Entry.getKey().tag, t2.tag,
                                tagEntry.getKey());

                        double triGramProb = d_languageModel.triGramProb(curTriGram);
                        double prob = triGramProb + tagEntry.getValue() + t1Entry.getValue();

                        if (prob > highestProb) {
                            highestProb = prob;
                            highestProbBp = t1Entry.getKey();
                        }
                    }

                    newEntry.probabilities.put(t2, highestProb);
                    newEntry.backPointers.put(t2, highestProbBp);

                    if (highestProb > columnHighestProb)
                        columnHighestProb = highestProb;
                }

                newTrellis.add(newEntry);
            }

            trellis = newTrellis;
            beam = columnHighestProb - d_beamFactor;
        }

        return trellis;
    }
}
