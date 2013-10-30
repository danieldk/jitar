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
import eu.danieldk.nlp.jitar.data.BiGram;
import eu.danieldk.nlp.jitar.data.Model;
import eu.danieldk.nlp.jitar.data.TriGram;
import eu.danieldk.nlp.jitar.data.UniGram;
import eu.danieldk.nlp.jitar.training.FrequenciesCollector;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

public class Train {
    public static void main(String[] args) throws IOException {
        if (args.length != 3) {
            System.out.println("Train [brown/conll] corpus model");
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

        Model model = frequenciesCollector.model();

        ObjectOutputStream oos = null;
        try {
            oos = new ObjectOutputStream(new FileOutputStream(args[2]));
            oos.writeObject(model);
        } catch (IOException e) {
            System.out.println("Could not write model!");
            e.printStackTrace();
            System.exit(1);
        } finally {
            oos.close();
        }
    }


}
