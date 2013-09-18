package eu.danieldk.nlp.jitar.training;

import eu.danieldk.nlp.jitar.corpus.CorpusSentenceHandler;
import eu.danieldk.nlp.jitar.corpus.TaggedWord;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * This handler will construct a lexicon and n-gram frequency list using the
 * sentences that are provided to the handler.
 */
public class TrainingHandler implements CorpusSentenceHandler<TaggedWord> {
    private final Map<String, Map<String, Integer>> d_lexicon;

    private final Map<String, Integer> d_uniGrams;

    private final Map<String, Integer> d_biGrams;

    private final Map<String, Integer> d_triGrams;

    public TrainingHandler() {
        d_lexicon = new HashMap<String, Map<String, Integer>>();
        d_uniGrams = new HashMap<String, Integer>();
        d_biGrams = new HashMap<String, Integer>();
        d_triGrams = new HashMap<String, Integer>();
    }

    public Map<String, Integer> biGrams() {
        return d_biGrams;
    }

    public void handleSentence(List<TaggedWord> sentence) {
        for (int i = 0; i < sentence.size(); ++i) {
            addLexiconEntry(sentence.get(i));
            addUniGram(sentence, i);
            if (i > 0)
                addBiGram(sentence, i);
            if (i > 1)
                addTriGram(sentence, i);
        }
    }

    public Map<String, Map<String, Integer>> lexicon() {
        return d_lexicon;
    }

    public Map<String, Integer> triGrams() {
        return d_triGrams;
    }

    public Map<String, Integer> uniGrams() {
        return d_uniGrams;
    }

    private void addLexiconEntry(TaggedWord taggedWord) {
        String word = taggedWord.word();
        String tag = taggedWord.tag();

        if (!d_lexicon.containsKey(word))
            d_lexicon.put(word, new HashMap<String, Integer>());

        if (!d_lexicon.get(word).containsKey(tag))
            d_lexicon.get(word).put(tag, 1);
        else
            d_lexicon.get(word).put(tag, d_lexicon.get(word).get(tag) + 1);
    }

    private void addUniGram(List<TaggedWord> sentence, int index) {
        String uniGram = sentence.get(index).tag();

        if (!d_uniGrams.containsKey(uniGram))
            d_uniGrams.put(uniGram, 1);
        else
            d_uniGrams.put(uniGram, d_uniGrams.get(uniGram) + 1);
    }

    private void addBiGram(List<TaggedWord> sentence, int index) {
        String biGram = sentence.get(index - 1).tag() + " " +
                sentence.get(index).tag();

        if (!d_biGrams.containsKey(biGram))
            d_biGrams.put(biGram, 1);
        else
            d_biGrams.put(biGram, d_biGrams.get(biGram) + 1);
    }

    private void addTriGram(List<TaggedWord> sentence, int index) {
        String triGram = sentence.get(index - 2).tag() + " " +
                sentence.get(index - 1).tag() + " " +
                sentence.get(index).tag();

        if (!d_triGrams.containsKey(triGram))
            d_triGrams.put(triGram, 1);
        else
            d_triGrams.put(triGram, d_triGrams.get(triGram) + 1);
    }
}
