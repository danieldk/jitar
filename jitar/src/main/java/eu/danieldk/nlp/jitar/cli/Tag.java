// Copyright 2008, 2008 Daniel de Kok
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

import java.io.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;


import eu.danieldk.nlp.jitar.data.Model;
import eu.danieldk.nlp.jitar.languagemodel.LanguageModel;
import eu.danieldk.nlp.jitar.languagemodel.LinearInterpolationLM;
import eu.danieldk.nlp.jitar.tagger.HMMTagger;
import eu.danieldk.nlp.jitar.wordhandler.KnownWordHandler;
import eu.danieldk.nlp.jitar.wordhandler.SuffixWordHandler;
import eu.danieldk.nlp.jitar.wordhandler.WordHandler;
import org.apache.commons.lang3.StringUtils;

public class Tag {
	public static void main(String[] args) {
		if (args.length != 2) {
			System.out.println("Tag lexicon ngrams");
			System.exit(1);
		}

		// Load the model.
        Model model = null;
        try {
            model = Model.readModel(new File(args[1]));
        } catch (IOException e) {
            System.out.println("Unable to read training data!");
            e.printStackTrace();
            System.exit(1);
        }

		// Set up word handlers. The suffix word handler is used as a fallback of the
		// known word handler.
		SuffixWordHandler swh = new SuffixWordHandler(model.lexicon(), model.uniGrams(),
				2, 2, 8, 4, 10, 10);
		WordHandler wh = new KnownWordHandler(model.lexicon(), model.uniGrams(), swh);
		
		// Create an n-gram language model.
		LanguageModel lm = new LinearInterpolationLM(model.uniGrams(),
				model.biGrams(), model.triGrams());

		// Initialize a tagger with a beam of 1000.0.
		HMMTagger tagger = new HMMTagger(model, wh, lm, 1000.0);
		
		// Read from the standard input, and print tags for the input to the standard
		// output.
		try (BufferedReader reader =  new BufferedReader(new InputStreamReader(System.in))) {
            String line;
			while ((line = reader.readLine()) != null) {
				String tokens[] = line.split("\\s+");
				List<String> tokenList = new ArrayList<>(Arrays.asList(tokens));
				
				// Add start/end markers.
				tokenList.add(0, "<START>");
				tokenList.add(0, "<START>");
				tokenList.add("<END>");
				
				List<String> tags =
					HMMTagger.highestProbabilitySequence(tagger.viterbi(tokenList),
						model).sequence();
				
				System.out.println(StringUtils.join(tags.subList(2, tags.size() - 1), ' '));
			}
		} catch (IOException ignored) {
		}
	}
}
