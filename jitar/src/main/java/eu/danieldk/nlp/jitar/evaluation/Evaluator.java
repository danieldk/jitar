// Copyright 2008, 2009, 2013 Daniel de Kok
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

package eu.danieldk.nlp.jitar.evaluation;

import eu.danieldk.nlp.jitar.corpus.CorpusReader;
import eu.danieldk.nlp.jitar.corpus.TaggedToken;
import eu.danieldk.nlp.jitar.data.Model;
import eu.danieldk.nlp.jitar.tagger.HMMTagger;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * This handler tags the provided sentences using an {@link HMMTagger} and compares the tags
 * against the tags in the test data.
 */
public class Evaluator {
    private final HMMTagger d_tagger;

    private final Model d_model;

    private final Map<String, Map<Integer, Integer>> d_lexicon;

    private int d_knownGood = 0;

    private int d_knownBad = 0;

    private int d_unknownGood = 0;

    private int d_unknownBad = 0;

    public Evaluator(HMMTagger tagger, Model model) {
        d_tagger = tagger;
        d_model = model;
        d_lexicon = model.lexicon();
    }

    public void process(CorpusReader reader) throws IOException {
        List<TaggedToken> sentence;
        while ((sentence = reader.readSentence()) != null) {
            ArrayList<String> sentenceWords = new ArrayList<>(sentence.size());
            for (TaggedToken taggedWord : sentence)
                sentenceWords.add(taggedWord.word());

            List<String> tags = HMMTagger.highestProbabilitySequence(
                    d_tagger.viterbi(sentenceWords), d_model).sequence();

            for (int i = 2; i < tags.size() - 1; ++i) {
                boolean inLexicon = false;
                if (d_lexicon.containsKey(sentenceWords.get(i)) ||
                        d_lexicon.containsKey(sentenceWords.get(i).toLowerCase()))
                    inLexicon = true;

                if (tags.get(i).equals(sentence.get(i).tag())) {
                    if (inLexicon)
                        ++d_knownGood;
                    else
                        ++d_unknownGood;
                } else {
                    if (inLexicon)
                        ++d_knownBad;
                    else
                        ++d_unknownBad;
                }
            }

        }
    }

    public int knownBad() {
        return d_knownBad;
    }

    public int knownGood() {
        return d_knownGood;
    }

    public int unknownBad() {
        return d_unknownBad;
    }

    public int unknownGood() {
        return d_unknownGood;
    }

    public int overallBad() {
        return d_knownBad + d_unknownBad;
    }

    public int overallGood() {
        return d_knownGood + d_unknownGood;
    }
}
