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
import eu.danieldk.nlp.jitar.data.Model;
import eu.danieldk.nlp.jitar.evaluation.Evaluator;
import eu.danieldk.nlp.jitar.languagemodel.LanguageModel;
import eu.danieldk.nlp.jitar.languagemodel.LinearInterpolationLM;
import eu.danieldk.nlp.jitar.tagger.HMMTagger;
import eu.danieldk.nlp.jitar.wordhandler.KnownWordHandler;
import eu.danieldk.nlp.jitar.wordhandler.SuffixWordHandler;
import eu.danieldk.nlp.jitar.wordhandler.WordHandler;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

public class Evaluate {
    public static void main(String[] args) throws IOException {
        if (args.length != 3) {
            System.out.println("Evaluate [brown/conll] model corpus");
            System.exit(1);
        }

        Model model = null;

        try {
            model = Model.readModel(new File(args[1]));
        } catch (IOException e) {
            System.out.println("Unable to read training data!");
            e.printStackTrace();
            System.exit(1);
        }

        SuffixWordHandler swh = new SuffixWordHandler(model.lexicon(), model.uniGrams(),
                2, 2, 8, 10, 10);
        WordHandler wh = new KnownWordHandler(model.lexicon(), model.uniGrams(), swh);
        LanguageModel lm = new LinearInterpolationLM(model.uniGrams(),
                model.biGrams(), model.triGrams());

        HMMTagger tagger = new HMMTagger(model, wh, lm, 1000.0);

        List<TaggedToken> startMarkers = new ArrayList<TaggedToken>();
        startMarkers.add(new TaggedToken("<START>", "<START>"));
        startMarkers.add(new TaggedToken("<START>", "<START>"));
        List<TaggedToken> endMarkers = new ArrayList<TaggedToken>();
        endMarkers.add(new TaggedToken("<END>", "<END>"));

        BufferedReader reader = null;
        try {
            reader = new BufferedReader(new FileReader(args[2]));
        } catch (FileNotFoundException e) {
            System.err.println(String.format("Could not open corpus for reading:", e.getMessage()));
            System.exit(1);
        }

        Evaluator evaluator = new Evaluator(tagger, model);


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

            evaluator.process(corpusReader);
        } catch (IOException e) {
            System.err.println(String.format("Error reading corpus: %s", e.getMessage()));
            System.exit(1);
        }
        finally {
            if (corpusReader != null)
                corpusReader.close();
        }

        System.out.println(String.format("Overall accuracy: %.2f", (double) evaluator.overallGood() /
                (evaluator.overallGood() + evaluator.overallBad()) * 100.));
        System.out.println(String.format("Known word accuracy: %.2f", (double) evaluator.knownGood() /
                (evaluator.knownBad() + evaluator.knownGood()) * 100.));
        System.out.println(String.format("Unknown word accuracy: %.2f", (double) evaluator.unknownGood() /
                (evaluator.unknownBad() + evaluator.unknownGood()) * 100.));
    }
}
