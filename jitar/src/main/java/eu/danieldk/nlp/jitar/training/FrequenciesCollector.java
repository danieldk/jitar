package eu.danieldk.nlp.jitar.training;

import eu.danieldk.nlp.jitar.corpus.CorpusReader;
import eu.danieldk.nlp.jitar.corpus.TaggedToken;
import eu.danieldk.nlp.jitar.data.BiGram;
import eu.danieldk.nlp.jitar.data.Model;
import eu.danieldk.nlp.jitar.data.TriGram;
import eu.danieldk.nlp.jitar.data.UniGram;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * This handler will construct a lexicon and n-gram frequency list using the
 * sentences that are provided to the handler.
 */
public class FrequenciesCollector {
    private final Map<Integer, String> d_numberTags;

    private final Map<String, Integer> d_tagNumbers;

    private final Map<String, Map<Integer, Integer>> d_lexicon;

    private final Map<UniGram, Integer> d_uniGrams;

    private final Map<BiGram, Integer> d_biGrams;

    private final Map<TriGram, Integer> d_triGrams;

    public FrequenciesCollector() {
        d_numberTags = new HashMap<Integer, String>();
        d_tagNumbers = new HashMap<String, Integer>();
        d_lexicon = new HashMap<String, Map<Integer, Integer>>();
        d_uniGrams = new HashMap<UniGram, Integer>();
        d_biGrams = new HashMap<BiGram, Integer>();
        d_triGrams = new HashMap<TriGram, Integer>();
    }

    public Model model()
    {
        return new Model(d_lexicon, d_tagNumbers, d_numberTags, d_uniGrams, d_biGrams, d_triGrams);
    }

    public void process(CorpusReader reader) throws IOException {
        List<TaggedToken> sentence;
        while ((sentence = reader.readSentence()) != null) {
            for (int i = 0; i < sentence.size(); ++i) {
                addLexiconEntry(sentence.get(i));
                addUniGram(sentence, i);
                if (i > 0)
                    addBiGram(sentence, i);
                if (i > 1)
                    addTriGram(sentence, i);
            }
        }
    }

    private Integer lookupTag(String tag) {
        Integer tagNumber = d_tagNumbers.get(tag);
        if (tagNumber == null) {
            tagNumber = d_tagNumbers.size();
            d_tagNumbers.put(tag, tagNumber);
            d_numberTags.put(tagNumber, tag);
        }

        return tagNumber;
    }

    private void addLexiconEntry(TaggedToken taggedWord) {
        String word = taggedWord.word();
        String tag = taggedWord.tag();

        Map<Integer, Integer> tagFreqs = d_lexicon.get(word);
        if (tagFreqs == null) {
            tagFreqs = new HashMap<Integer, Integer>();
            d_lexicon.put(word, tagFreqs);
        }

        Integer tagNum = lookupTag(tag);

        Integer f = tagFreqs.get(tagNum);
        if (f == null)
            f = 0;

        tagFreqs.put(tagNum, ++f);
    }

    private void addUniGram(List<TaggedToken> sentence, int index) {
        UniGram uniGram = new UniGram(lookupTag(sentence.get(index).tag()));

        if (!d_uniGrams.containsKey(uniGram))
            d_uniGrams.put(uniGram, 1);
        else
            d_uniGrams.put(uniGram, d_uniGrams.get(uniGram) + 1);
    }

    private void addBiGram(List<TaggedToken> sentence, int index) {
        BiGram biGram = new BiGram(lookupTag(sentence.get(index - 1).tag()), lookupTag(sentence.get(index).tag()));

        if (!d_biGrams.containsKey(biGram))
            d_biGrams.put(biGram, 1);
        else
            d_biGrams.put(biGram, d_biGrams.get(biGram) + 1);
    }

    private void addTriGram(List<TaggedToken> sentence, int index) {
        TriGram triGram = new TriGram(lookupTag(sentence.get(index - 2).tag()),
                lookupTag(sentence.get(index - 1).tag()),
                lookupTag(sentence.get(index).tag()));

        if (!d_triGrams.containsKey(triGram))
            d_triGrams.put(triGram, 1);
        else
            d_triGrams.put(triGram, d_triGrams.get(triGram) + 1);
    }
}
