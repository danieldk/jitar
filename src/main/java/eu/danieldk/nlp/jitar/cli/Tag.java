//
// Copyright 2008, 2014 Daniël de Kok
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
//

package eu.danieldk.nlp.jitar.cli;

import com.google.common.base.Joiner;
import eu.danieldk.nlp.jitar.data.Model;
import eu.danieldk.nlp.jitar.languagemodel.LanguageModel;
import eu.danieldk.nlp.jitar.languagemodel.LinearInterpolationLM;
import eu.danieldk.nlp.jitar.tagger.HMMTagger;
import eu.danieldk.nlp.jitar.wordhandler.LexiconWordHandler;
import eu.danieldk.nlp.jitar.wordhandler.SuffixWordHandler;
import eu.danieldk.nlp.jitar.wordhandler.WordHandler;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Arrays;
import java.util.List;

public class Tag {
    public static void main(String[] args) {
        if (args.length != 1) {
            System.out.println("tag model");
            System.exit(1);
        }

        // Load the model.
        Model model = null;
        try {
            model = Model.readModel(new File(args[0]));
        } catch (IOException e) {
            System.out.println("Unable to read training data!");
            e.printStackTrace();
            System.exit(1);
        }

        // Set up word handlers. The suffix word handler is used as a fallback of the
        // known word handler.
        SuffixWordHandler swh = new SuffixWordHandler(model.lexicon(), model.uniGrams(),
                2, 2, 8, 4, 10, 10);
        WordHandler wh = new LexiconWordHandler(model.lexicon(), model.uniGrams(), swh);

        // Create an n-gram language model.
        LanguageModel lm = new LinearInterpolationLM(model.uniGrams(),
                model.biGrams(), model.triGrams());

        // Initialize a tagger with a beam of 1000.0.
        HMMTagger tagger = new HMMTagger(model, wh, lm, 1000.0);

        // Read from the standard input, and print tags for the input to the standard
        // output.
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(System.in))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String tokens[] = line.split("\\s+");

                List<String> tags =
                        HMMTagger.highestProbabilitySequence(tagger.tag(Arrays.asList(tokens)),
                                model).sequence();

                System.out.println(Joiner.on(' ').join(tags));
            }
        } catch (IOException ignored) {
        }
    }
}
