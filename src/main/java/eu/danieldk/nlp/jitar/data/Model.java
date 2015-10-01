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

package eu.danieldk.nlp.jitar.data;

import java.io.*;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

public class Model implements Serializable {
    private static final long serialVersionUID = 1L;

    private final Map<String, Map<Integer, Integer>> d_wordTagFreqs;

    private final Map<String, Integer> d_tagNumbers;

    private final Map<Integer, String> d_numberTags;

    private final Map<UniGram, Integer> d_uniGramFreqs;

    private final Map<BiGram, Integer> d_biGramFreqs;

    private final Map<TriGram, Integer> d_triGramFreqs;

    public Model(Map<String, Map<Integer, Integer>> wordTagFreqs, Map<String, Integer> tagNumbers,
                 Map<Integer, String> numberTags, Map<UniGram, Integer> uniGramFreqs,
                 Map<BiGram, Integer> biGramFreqs, Map<TriGram, Integer> triGramFreqs) {
        for (Entry<String, Map<Integer, Integer>> lexiconEntry : wordTagFreqs.entrySet()) {
            wordTagFreqs.put(lexiconEntry.getKey(),
                    Collections.unmodifiableMap(lexiconEntry.getValue()));
        }

        d_wordTagFreqs = Collections.unmodifiableMap(wordTagFreqs);

        d_tagNumbers = Collections.unmodifiableMap(tagNumbers);
        d_numberTags = Collections.unmodifiableMap(numberTags);
        d_uniGramFreqs = Collections.unmodifiableMap(uniGramFreqs);
        d_biGramFreqs = Collections.unmodifiableMap(biGramFreqs);
        d_triGramFreqs = Collections.unmodifiableMap(triGramFreqs);
    }

    /**
     * Read a model. See {@link #readModel(java.io.BufferedReader, java.io.BufferedReader)}.
     *
     * @param lexiconStream A lexicon stream.
     * @param nGramStream   A n-gram frequency stream.
     * @return A model.
     * @throws IOException
     */
    public static Model readModel(InputStream lexiconStream,
                                  InputStream nGramStream) throws IOException {
        return readModel(new BufferedReader(new InputStreamReader(lexiconStream)),
                new BufferedReader(new InputStreamReader(nGramStream)));

    }

    public static Model readModel(File modelFile) throws IOException {
        return readModel(new FileInputStream(modelFile));
    }

    public static Model readModel(InputStream modelStream) throws IOException {
        ObjectInputStream ois = null;
        try {
            ois = new ObjectInputStream(modelStream);
            return (Model) ois.readObject();
        } catch (ClassNotFoundException e) {
            throw new IOException(e);
        } finally {
            if (ois != null)
                ois.close();
        }
    }

    /**
     * Read a model from files, and construct a <i>Model</i> instance. The
     * model should be stored in two text files. The first text file should
     * contain the word/tag frequencies. One line is used per word, each line
     * starts with the word, followed by tags and their frequencies. For
     * example:
     * <p>
     * <tt><pre>
     * advised VBN 13 VBD 11
     * grotesque JJ 5
     * </pre></tt>
     * </p>
     * <p>
     * The second text file contains the uni/bi/trigram frequencies. Each
     * line lists an n-gram, and its frequencies. For example:
     * </p>
     * <p>
     * <tt><pre>
     * DT 6230
     * BEN PN$ 2
     * </pre></tt>
     * </p>
     * <p/>
     * Grams with a different <i>n</i> can be mixed freely.
     *
     * @param lexiconReader The lexicon reader.
     * @param nGramReader   The n-gram frequency reader.
     * @return The constructed <i>Model</i> instance.
     * @throws IOException
     */
    public static Model readModel(BufferedReader lexiconReader,
                                  BufferedReader nGramReader) throws IOException {
        NGrams nGrams = readNGrams(nGramReader);
        Map<String, Map<Integer, Integer>> wordTagFreqs =
                readWordTagFreqs(lexiconReader, nGrams.tagNumbers);

        return new Model(wordTagFreqs, nGrams.tagNumbers, nGrams.numberTags,
                nGrams.uniGramFreqs, nGrams.biGramFreqs, nGrams.triGramFreqs);
    }

    private static NGrams readNGrams(BufferedReader reader) throws IOException {
        Map<String, Integer> tagNumbers = new HashMap<>();
        Map<Integer, String> numberTags = new HashMap<>();
        Map<UniGram, Integer> uniGramFreqs = new HashMap<>();
        Map<BiGram, Integer> biGramFreqs = new HashMap<>();
        Map<TriGram, Integer> triGramFreqs = new HashMap<>();

        int tagNumber = 0;
        String line;
        while ((line = reader.readLine()) != null) {
            String[] lineParts = line.split("\\s+");

            int freq = Integer.parseInt(lineParts[lineParts.length - 1]);

            if (lineParts.length == 2) {
                tagNumbers.put(lineParts[0], tagNumber);
                numberTags.put(tagNumber, lineParts[0]);
                uniGramFreqs.put(new UniGram(tagNumber), freq);
                ++tagNumber;
            } else if (lineParts.length == 3)
                biGramFreqs.put(new BiGram(tagNumbers.get(lineParts[0]),
                        tagNumbers.get(lineParts[1])), freq);
            else if (lineParts.length == 4)
                triGramFreqs.put(new TriGram(tagNumbers.get(lineParts[0]),
                        tagNumbers.get(lineParts[1]), tagNumbers.get(lineParts[2])), freq);

        }

        return new NGrams(tagNumbers, numberTags, uniGramFreqs, biGramFreqs, triGramFreqs);
    }

    private static Map<String, Map<Integer, Integer>> readWordTagFreqs(BufferedReader reader,
                                                                       Map<String, Integer> tagNumbers)
            throws IOException {
        Map<String, Map<Integer, Integer>> wordTagFreqs = new HashMap<>();

        String line;
        while ((line = reader.readLine()) != null) {
            String[] lineParts = line.split("\\s+");
            String word = lineParts[0];

            // Make a lexicon entry for this word represented by this line.
            wordTagFreqs.put(word, new HashMap<Integer, Integer>());

            for (int i = 1; i < lineParts.length; i += 2) {
                int p = Integer.parseInt(lineParts[i + 1]);
                wordTagFreqs.get(word).put(tagNumbers.get(lineParts[i]), p);
            }
        }

        return wordTagFreqs;
    }

    /**
     * Returns the model bigram frequencies.
     *
     * @return Bigram frequencies.
     */
    public Map<BiGram, Integer> biGrams() {
        return d_biGramFreqs;
    }

    /**
     * Returns the model word/tag frequencies.
     *
     * @return Word/tag frequencies.
     */
    public Map<String, Map<Integer, Integer>> lexicon() {
        return d_wordTagFreqs;
    }

    public Map<Integer, String> numberTags() {
        return d_numberTags;
    }

    public Map<String, Integer> tagNumbers() {
        return d_tagNumbers;
    }

    /**
     * Returns the model trigram frequencies.
     *
     * @return Trigram frequencies.
     */
    public Map<TriGram, Integer> triGrams() {
        return d_triGramFreqs;
    }

    /**
     * Returns the model unigram frequencies.
     *
     * @return Unigram (tag) frequencies.
     */
    public Map<UniGram, Integer> uniGrams() {
        return d_uniGramFreqs;
    }

    private static class NGrams {
        private final Map<String, Integer> tagNumbers;

        private final Map<Integer, String> numberTags;

        private final Map<UniGram, Integer> uniGramFreqs;

        private final Map<BiGram, Integer> biGramFreqs;

        private final Map<TriGram, Integer> triGramFreqs;

        public NGrams(Map<String, Integer> tagNumbers, Map<Integer, String> numberTags,
                      Map<UniGram, Integer> uniGramFreqs, Map<BiGram, Integer> biGramFreqs,
                      Map<TriGram, Integer> triGramFreqs) {
            this.tagNumbers = tagNumbers;
            this.numberTags = numberTags;
            this.uniGramFreqs = uniGramFreqs;
            this.biGramFreqs = biGramFreqs;
            this.triGramFreqs = triGramFreqs;
        }
    }
}
