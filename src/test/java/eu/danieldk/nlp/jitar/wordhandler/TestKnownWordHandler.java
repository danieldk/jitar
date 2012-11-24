package eu.danieldk.nlp.jitar.wordhandler;

import eu.danieldk.nlp.jitar.data.UniGram;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.HashMap;
import java.util.Map;

public class TestKnownWordHandler {
  @Before
  public void initialize() {
    Map<UniGram, Integer> uniFreqs = new HashMap<UniGram, Integer>();
    uniFreqs.put(new UniGram(0), 2);
    uniFreqs.put(new UniGram(1), 4);

    Map<String, Map<Integer, Integer>> wordFreqs = new HashMap<String, Map<Integer, Integer>>();
    Map<Integer, Integer> freqs = new HashMap<Integer, Integer>();
    freqs.put(0, 1);
    freqs.put(1, 1);
    wordFreqs.put("test", freqs);


    d_wordHandler = new KnownWordHandler(wordFreqs, uniFreqs);
  }

  @Test
  public void testProbability() {
    Map<Integer, Double> probs = d_wordHandler.tagProbs("test");
    Assert.assertEquals("Unexpected probability", Math.log(1./2.), probs.get(0).doubleValue(),
        Math.ulp(Math.log(1./2.)));
    Assert.assertEquals("Unexpected probability", Math.log(1./4.), probs.get(1).doubleValue(),
        Math.ulp(Math.log(1./2.)));
  }

  @Test
  public void testUnknown() {
    Assert.assertEquals("Unknown word should give empty map when there is no fallback",
        new HashMap<Integer, Double>(), d_wordHandler.tagProbs("unknown"));
  }

  WordHandler d_wordHandler;
}
