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

import eu.danieldk.nlp.jitar.corpus.CorpusReader;
import eu.danieldk.nlp.jitar.data.Model;
import eu.danieldk.nlp.jitar.evaluation.Evaluator;
import eu.danieldk.nlp.jitar.languagemodel.LanguageModel;
import eu.danieldk.nlp.jitar.languagemodel.LinearInterpolationLM;
import eu.danieldk.nlp.jitar.tagger.HMMTagger;
import eu.danieldk.nlp.jitar.wordhandler.KnownWordHandler;
import eu.danieldk.nlp.jitar.wordhandler.SuffixWordHandler;
import eu.danieldk.nlp.jitar.wordhandler.WordHandler;

import java.io.File;
import java.io.IOException;

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
                2, 2, 8, 6, 10, 10);
        WordHandler wh = new KnownWordHandler(model.lexicon(), model.uniGrams(), swh);
        LanguageModel lm = new LinearInterpolationLM(model.uniGrams(),
                model.biGrams(), model.triGrams());

        HMMTagger tagger = new HMMTagger(model, wh, lm, 1000.0);

        Evaluator evaluator = new Evaluator(tagger, model);

        try (CorpusReader corpusReader = Util.newCorpusReader(args[0], new File(args[2]))) {
            evaluator.process(corpusReader);
        } catch (IOException e) {
            System.err.println(String.format("Error reading corpus: %s", e.getMessage()));
            System.exit(1);
        }

        System.out.println(String.format("Overall accuracy: %.2f", (double) evaluator.overallGood() /
                (evaluator.overallGood() + evaluator.overallBad()) * 100.));
        System.out.println(String.format("Known word accuracy: %.2f", (double) evaluator.knownGood() /
                (evaluator.knownBad() + evaluator.knownGood()) * 100.));
        System.out.println(String.format("Unknown word accuracy: %.2f", (double) evaluator.unknownGood() /
                (evaluator.unknownBad() + evaluator.unknownGood()) * 100.));
    }
}
