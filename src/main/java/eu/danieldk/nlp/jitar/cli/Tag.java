/*
 * Copyright 2008, 2009 Daniel de Kok
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

package eu.danieldk.nlp.jitar.cli;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;


import eu.danieldk.nlp.jitar.data.Model;
import eu.danieldk.nlp.jitar.languagemodel.LanguageModel;
import eu.danieldk.nlp.jitar.languagemodel.LinearInterpolationLM;
import eu.danieldk.nlp.jitar.tagger.HMMTagger;
import eu.danieldk.nlp.jitar.wordhandler.KnownWordHandler;
import eu.danieldk.nlp.jitar.wordhandler.SuffixWordHandler;
import eu.danieldk.nlp.jitar.wordhandler.WordHandler;

public class Tag {
	private static String join(Collection<String> strings, String delimiter) {
		StringBuilder sb = new StringBuilder();
		
		Iterator<String> iter = strings.iterator();
		while (iter.hasNext()) {
			sb.append(iter.next());
			if (iter.hasNext())
				sb.append(delimiter);
		}
		
		return sb.toString();
	}

	public static void main(String[] args) {
		if (args.length != 2) {
			System.out.println("Tag lexicon ngrams");
			System.exit(1);
		}

		// Load the model.
		Model model = null;
		try {
			model = Model.readModel(new BufferedReader(new FileReader(args[0])),
					new BufferedReader(new FileReader(args[1])));
		} catch (IOException e) {
			System.out.println("Unable to read the model!");
			e.printStackTrace();
			System.exit(1);
		}

		// Set up word handlers. The suffix word handler is used as a fallback of the
		// known word handler.
		SuffixWordHandler swh = new SuffixWordHandler(model.lexicon(), model.uniGrams(),
				2, 5, 10, 10, 10);
		WordHandler wh = new KnownWordHandler(model.lexicon(), model.uniGrams(), swh);
		
		// Create an n-gram language model.
		LanguageModel lm = new LinearInterpolationLM(model.uniGrams(),
				model.biGrams(), model.triGrams());

		// Initialize a tagger with a beam of 1000.0.
		HMMTagger tagger = new HMMTagger(model, wh, lm, 1000.0);
		
		// Read from the standard input, and print tags for the input to the standard
		// output.
		BufferedReader reader =  new BufferedReader(new InputStreamReader(System.in));
		String line;
		try {
			while ((line = reader.readLine()) != null) {
				String tokens[] = line.split("\\s+");
				List<String> tokenList = new ArrayList<String>(Arrays.asList(tokens));
				
				// Add start/end markers.
				tokenList.add(0, "<START>");
				tokenList.add(0, "<START>");
				tokenList.add("<END>");
				
				List<String> tags =
					HMMTagger.highestProbabilitySequence(tagger.viterbi(tokenList),
						model).sequence();
				
				System.out.println(join(tags.subList(2, tags.size() - 1), " "));
			}
		} catch (IOException e) {
		}
	}
}
