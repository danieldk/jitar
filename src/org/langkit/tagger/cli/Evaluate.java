/*
 * Copyright 2008, 2009 Daniël de Kok
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.langkit.tagger.cli;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.langkit.tagger.corpus.BrownCorpusReader;
import org.langkit.tagger.corpus.CorpusReader;
import org.langkit.tagger.corpus.CorpusSentenceHandler;
import org.langkit.tagger.corpus.TaggedWord;
import org.langkit.tagger.data.Model;
import org.langkit.tagger.languagemodel.LanguageModel;
import org.langkit.tagger.languagemodel.LinearInterpolationLM;
import org.langkit.tagger.tagger.HMMTagger;
import org.langkit.tagger.wordhandler.KnownWordHandler;
import org.langkit.tagger.wordhandler.SuffixWordHandler;
import org.langkit.tagger.wordhandler.WordHandler;

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
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		if (args.length != 3) {
			System.out.println("Evaluate lexicon ngrams corpus");
			System.exit(1);
		}
		
		Model model = null;
		
		try {
			model = Model.readModel(new BufferedReader(new FileReader(args[0])),
					new BufferedReader(new FileReader(args[1])));
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
		
		CorpusReader<TaggedWord> corpusReader = new BrownCorpusReader(startMarkers,
				endMarkers, false);
		
		EvalHandler evalHandler = new EvalHandler(tagger, model);
		corpusReader.addHandler(evalHandler);

		try {
			corpusReader.parse(new BufferedReader(new FileReader(args[2])));
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
