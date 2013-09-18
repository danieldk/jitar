// Copyright 2008, 2009 Daniel de Kok
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

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;


import eu.danieldk.nlp.jitar.corpus.*;
import eu.danieldk.nlp.jitar.data.Model;
import eu.danieldk.nlp.jitar.evaluation.EvaluationHandler;
import eu.danieldk.nlp.jitar.languagemodel.LanguageModel;
import eu.danieldk.nlp.jitar.languagemodel.LinearInterpolationLM;
import eu.danieldk.nlp.jitar.tagger.HMMTagger;
import eu.danieldk.nlp.jitar.wordhandler.KnownWordHandler;
import eu.danieldk.nlp.jitar.wordhandler.SuffixWordHandler;
import eu.danieldk.nlp.jitar.wordhandler.WordHandler;

public class Evaluate {
	public static void main(String[] args) {
		if (args.length != 4) {
			System.out.println("Evaluate [brown/conll] lexicon ngrams corpus");
			System.exit(1);
		}

		Model model = null;

		try {
			model = Model.readModel(new BufferedReader(new FileReader(args[1])),
					new BufferedReader(new FileReader(args[2])));
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

		List<TaggedWord> startMarkers = new ArrayList<TaggedWord>();
		startMarkers.add(new TaggedWord("<START>", "<START>"));
		startMarkers.add(new TaggedWord("<START>", "<START>"));
		List<TaggedWord> endMarkers = new ArrayList<TaggedWord>();
		endMarkers.add(new TaggedWord("<END>", "<END>"));

        CorpusReader<TaggedWord> corpusReader = null;
        if (args[0].equals("brown"))
            corpusReader = new BrownCorpusReader(startMarkers, endMarkers, true);
        else if (args[0].equals("conll"))
            corpusReader = new CONLLCorpusReader(startMarkers, endMarkers, true);
        else {
            System.err.println(String.format("Unknown corpus type:", args[0]));
            System.exit(1);
        }

		EvaluationHandler evalHandler = new EvaluationHandler(tagger, model);
		corpusReader.addHandler(evalHandler);

		try {
			corpusReader.parse(new BufferedReader(new FileReader(args[3])));
		} catch (Exception e) {
			System.out.println("Could not read corpus!");
			e.printStackTrace();
			System.exit(1);
		}

        System.out.println(String.format("Overall accuracy: %.2f", (double) evalHandler.overallGood() /
                (evalHandler.overallGood() + evalHandler.overallBad()) * 100.));
		System.out.println(String.format("Known word accuracy: %.2f", (double) evalHandler.knownGood() /
				(evalHandler.knownBad() + evalHandler.knownGood()) * 100.));
		System.out.println(String.format("Unknown word accuracy: %.2f", (double) evalHandler.unknownGood() /
				(evalHandler.unknownBad() + evalHandler.unknownGood()) * 100.));
	}
}
