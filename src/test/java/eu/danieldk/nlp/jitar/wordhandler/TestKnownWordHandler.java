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

package eu.danieldk.nlp.jitar.wordhandler;

import com.google.common.collect.ImmutableMap;
import eu.danieldk.nlp.jitar.corpus.Common;
import eu.danieldk.nlp.jitar.data.BiGram;
import eu.danieldk.nlp.jitar.data.Model;
import eu.danieldk.nlp.jitar.data.TriGram;
import eu.danieldk.nlp.jitar.data.UniGram;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.HashMap;
import java.util.Map;

public class TestKnownWordHandler {
    @Before
    public void initialize() {
        Map<UniGram, Integer> uniFreqs = new HashMap<>();
        uniFreqs.put(new UniGram(0), 2);
        uniFreqs.put(new UniGram(1), 4);

        Map<String, Map<Integer, Integer>> wordFreqs = new HashMap<>();
        Map<Integer, Integer> freqs = new HashMap<>();
        freqs.put(0, 1);
        freqs.put(1, 1);
        wordFreqs.put("test", freqs);

        Model model = new Model(wordFreqs,
                ImmutableMap.of(
                        Common.START_TOKEN, 42,
                        Common.END_TOKEN, 42
                ),
                ImmutableMap.<Integer, String>of(), uniFreqs,
                ImmutableMap.<BiGram, Integer>of(),
                ImmutableMap.<TriGram, Integer>of());


        d_wordHandler = new LexiconWordHandler(wordFreqs, uniFreqs);
        d_wordHandlerWithFallback = new LexiconWordHandler(wordFreqs, uniFreqs,
                new SuffixWordHandler(model, 2, 5, 5, 10, 5, 5));
    }

    @Test
    public void testProbability() {
        Map<Integer, Double> probs = d_wordHandler.tagProbs("test");
        Assert.assertEquals("Unexpected probability", Math.log(1. / 2.), probs.get(0),
                Math.ulp(Math.log(1. / 2.)));
        Assert.assertEquals("Unexpected probability", Math.log(1. / 4.), probs.get(1),
                Math.ulp(Math.log(1. / 2.)));
    }

    @Test
    public void testUnknown() {
        Assert.assertEquals("Unknown word should give empty map when there is no fallback",
                new HashMap<Integer, Double>(), d_wordHandler.tagProbs("unknown"));
    }

    @Test
    public void testUnknownWithFallback() {
        Assert.assertNotEquals("Unknown word should give non-empty map with SuffixWordHandler fallback",
                new HashMap<Integer, Double>(), d_wordHandlerWithFallback.tagProbs("unknown"));
    }

    WordHandler d_wordHandler;
    WordHandler d_wordHandlerWithFallback;
}
