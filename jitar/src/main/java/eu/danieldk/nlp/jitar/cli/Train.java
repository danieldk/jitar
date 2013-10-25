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

package eu.danieldk.nlp.jitar.cli;

import eu.danieldk.nlp.jitar.corpus.BrownCorpusReader;
import eu.danieldk.nlp.jitar.corpus.CONLLCorpusReader;
import eu.danieldk.nlp.jitar.corpus.CorpusReader;
import eu.danieldk.nlp.jitar.corpus.TaggedToken;
import eu.danieldk.nlp.jitar.training.FrequenciesCollector;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

public class Train {
    private static void writeNGrams(Map<String, Integer> uniGrams,
                                    Map<String, Integer> biGrams, Map<String, Integer> triGrams,
                                    BufferedWriter writer) throws IOException {
        for (Entry<String, Integer> entry : uniGrams.entrySet())
            writer.write(entry.getKey() + " " + entry.getValue() + "\n");

        for (Entry<String, Integer> entry : biGrams.entrySet())
            writer.write(entry.getKey() + " " + entry.getValue() + "\n");

        for (Entry<String, Integer> entry : triGrams.entrySet())
            writer.write(entry.getKey() + " " + entry.getValue() + "\n");

        writer.flush();
    }

    private static void writeLexicon(Map<String, Map<String, Integer>> lexicon,
                                     BufferedWriter writer) throws IOException {
        for (Entry<String, Map<String, Integer>> wordEntry : lexicon.entrySet()) {
            String word = wordEntry.getKey();

            writer.write(word);

            for (Entry<String, Integer> tagEntry : lexicon.get(word).entrySet()) {
                writer.write(" ");
                writer.write(tagEntry.getKey());
                writer.write(" ");
                writer.write(tagEntry.getValue().toString());
            }

            writer.newLine();
        }

        writer.flush();
    }

    public static void main(String[] args) throws IOException {
        if (args.length != 4) {
            System.out.println("Train [brown/conll] corpus lexicon ngrams");
            System.exit(1);
        }

        List<TaggedToken> startMarkers = new ArrayList<TaggedToken>();
        startMarkers.add(new TaggedToken("<START>", "<START>"));
        startMarkers.add(new TaggedToken("<START>", "<START>"));
        List<TaggedToken> endMarkers = new ArrayList<TaggedToken>();
        endMarkers.add(new TaggedToken("<END>", "<END>"));

        BufferedReader reader = null;
        try {
            reader = new BufferedReader(new FileReader(args[1]));
        } catch (FileNotFoundException e) {
            System.err.println(String.format("Could not open corpus for reading:", e.getMessage()));
            System.exit(1);
        }

        FrequenciesCollector frequenciesCollector = new FrequenciesCollector();
        CorpusReader corpusReader = null;
        try {
            if (args[0].equals("brown"))
                corpusReader = new BrownCorpusReader(reader, startMarkers, endMarkers, true);
            else if (args[0].equals("conll"))
                corpusReader = new CONLLCorpusReader(reader, startMarkers, endMarkers, true);
            else {
                System.err.println(String.format("Unknown corpus type:", args[0]));
                System.exit(1);
            }

            frequenciesCollector.process(corpusReader);
        } catch (IOException e) {
            System.err.println(String.format("Error reading corpus: %s", e.getMessage()));
            System.exit(1);
        } finally {
            if (corpusReader != null)
                corpusReader.close();
        }

        try {
            writeLexicon(frequenciesCollector.lexicon(), new BufferedWriter(new FileWriter(args[2])));
            writeNGrams(frequenciesCollector.uniGrams(), frequenciesCollector.biGrams(),
                    frequenciesCollector.triGrams(), new BufferedWriter(new FileWriter(args[3])));
        } catch (IOException e) {
            System.out.println("Could not write training data!");
            e.printStackTrace();
            System.exit(1);
        }
    }


}
