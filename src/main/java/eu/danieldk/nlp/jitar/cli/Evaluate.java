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
import eu.danieldk.nlp.jitar.languagemodel.LanguageModel;
import eu.danieldk.nlp.jitar.languagemodel.LinearInterpolationLM;
import eu.danieldk.nlp.jitar.tagger.HMMTagger;
import eu.danieldk.nlp.jitar.wordhandler.KnownWordHandler;
import eu.danieldk.nlp.jitar.wordhandler.SuffixWordHandler;
import eu.danieldk.nlp.jitar.wordhandler.WordHandler;

public class Evaluate {
	private static class EvalHandler implements CorpusSentenceHandler<TaggedWord> {
		private final HMMTagger d_tagger;
		private final Model d_model;
		private final Map<String, Map<Integer, Integer>> d_lexicon;
		private int d_knownGood = 0;
		private int d_knownBad = 0;
		private int d_unknownGood = 0;
		private int d_unknownBad = 0;

		public EvalHandler(HMMTagger tagger, Model model) {
			d_tagger = tagger;
			d_model = model;
			d_lexicon = model.lexicon();
		}
		
		public void handleSentence(List<TaggedWord> sentence) {
			ArrayList<String> sentenceWords = new ArrayList<String>(sentence.size());
			for (TaggedWord taggedWord: sentence)
				sentenceWords.add(taggedWord.word());

			List<String> tags = HMMTagger.highestProbabilitySequence(
					d_tagger.viterbi(sentenceWords), d_model).sequence();
			
			for (int i = 2; i < tags.size() - 1; ++i) {
				boolean inLexicon = false;
				if (d_lexicon.containsKey(sentenceWords.get(i)) ||
						d_lexicon.containsKey(sentenceWords.get(i).toLowerCase()))
					inLexicon = true;

				if (tags.get(i).equals(sentence.get(i).tag())) {
					if (inLexicon)
						++d_knownGood;
					else
						++d_unknownGood;
				}
				else {
					if (inLexicon)
						++d_knownBad;
					else
						++d_unknownBad;
				}
			}
		}

		public int knownBad() {
			return d_knownBad;
		}
		
		public int knownGood() {
			return d_knownGood;
		}
		
		public int unknownBad() {
			return d_unknownBad;
		}
		
		public int unknownGood() {
			return d_unknownGood;
		}

        public int overallBad() {
            return d_knownBad + d_unknownBad;
        }

        public int overallGood() {
            return d_knownGood + d_unknownGood;
        }
	}

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
		
		EvalHandler evalHandler = new EvalHandler(tagger, model);
		corpusReader.addHandler(evalHandler);

		try {
			corpusReader.parse(new BufferedReader(new FileReader(args[3])));
		} catch (Exception e) {
			System.out.println("Could not read corpus!");
			e.printStackTrace();
			System.exit(1);
		}
		
		System.out.println("Known word accuracy: " + ((double) evalHandler.knownGood() /
				(evalHandler.knownBad() + evalHandler.knownGood())));
		System.out.println("Unknown word accuracy: " + ((double) evalHandler.unknownGood() /
				(evalHandler.unknownBad() + evalHandler.unknownGood())));
	}
}
