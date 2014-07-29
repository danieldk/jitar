// Copyright 2013 Daniel de Kok
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

import eu.danieldk.nlp.jitar.corpus.*;
import eu.danieldk.nlp.jitar.data.Model;
import eu.danieldk.nlp.jitar.evaluation.Evaluator;
import eu.danieldk.nlp.jitar.languagemodel.LanguageModel;
import eu.danieldk.nlp.jitar.languagemodel.LinearInterpolationLM;
import eu.danieldk.nlp.jitar.tagger.HMMTagger;
import eu.danieldk.nlp.jitar.training.FrequenciesCollector;
import eu.danieldk.nlp.jitar.wordhandler.KnownWordHandler;
import eu.danieldk.nlp.jitar.wordhandler.SuffixWordHandler;
import eu.danieldk.nlp.jitar.wordhandler.WordHandler;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class CrossValidation {
    private static final int N_FOLDS = 10;

    public static void main(String[] args) throws IOException {
        if (args.length != 2) {
            System.out.println("Evaluate [brown/conll] corpus");
            System.exit(1);
        }

        List<TaggedToken> startMarkers = getStartMarkers();
        List<TaggedToken> endMarkers = getEndMarkers();

        String corpusType = args[0];
        String corpusFilename = args[1];

        List<Double> overallPrecs = new ArrayList<>();

        // Todo: make the number of folds a command-line argument.
        for (int evalFold = 0; evalFold < N_FOLDS; ++evalFold) {
            // Train on everything but the i-th fold.
            Set<Integer> trainingFolds = getTrainingFolds(evalFold);

            Model model;
            try (CorpusReader corpusReader = new SplittingCorpusReader(
                    Util.newCorpusReader(corpusType, new File(corpusFilename), startMarkers, endMarkers), N_FOLDS, trainingFolds)) {
                FrequenciesCollector collector = new FrequenciesCollector();
                collector.process(corpusReader);
                model = collector.model();
            }

            Set<Integer> evalFolds = new HashSet<>();
            evalFolds.add(evalFold);
            try (CorpusReader evalCorpusReader = new SplittingCorpusReader(
                    Util.newCorpusReader(corpusType, new File(corpusFilename), startMarkers, endMarkers), N_FOLDS, evalFolds)) {

                SuffixWordHandler swh = new SuffixWordHandler(model.lexicon(), model.uniGrams(),
                        2, 2, 8, 4, 10, 10);
                WordHandler wh = new KnownWordHandler(model.lexicon(), model.uniGrams(), swh);
                LanguageModel lm = new LinearInterpolationLM(model.uniGrams(),
                        model.biGrams(), model.triGrams());
                HMMTagger tagger = new HMMTagger(model, wh, lm, 1000.0);

                Evaluator evaluator = new Evaluator(tagger, model);
                evaluator.process(evalCorpusReader);

                double overallPrec = (double) evaluator.overallGood() /
                        (evaluator.overallGood() + evaluator.overallBad()) * 100.;
                double unknownPrec = (double) evaluator.unknownGood() /
                        (evaluator.unknownBad() + evaluator.unknownGood()) * 100.;

                System.out.println(String.format("Fold %d accuracy: %.2f (unknown: %.2f)", evalFold, overallPrec, unknownPrec));

                overallPrecs.add(overallPrec);
            }
        }

        double sum = 0.;
        for (Double prec : overallPrecs)
            sum += prec;

        System.out.println(String.format("\nOverall accuracy: %.2f", sum / overallPrecs.size()));

    }

    private static Set<Integer> getTrainingFolds(int trainFold) {
        Set<Integer> trainingFolds = new HashSet<>();
        for (int fold = 0; fold < N_FOLDS; ++fold)
            if (fold != trainFold)
                trainingFolds.add(fold);
        return trainingFolds;
    }

    private static List<TaggedToken> getEndMarkers() {
        List<TaggedToken> endMarkers = new ArrayList<>();
        endMarkers.add(new TaggedToken("<END>", "<END>"));
        return endMarkers;
    }

    private static List<TaggedToken> getStartMarkers() {
        List<TaggedToken> startMarkers = new ArrayList<>();
        startMarkers.add(new TaggedToken("<START>", "<START>"));
        startMarkers.add(new TaggedToken("<START>", "<START>"));
        return startMarkers;
    }
}
