//
// Copyright 2008, 2015 Daniël de Kok
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
import com.google.common.base.Optional;
import com.google.common.collect.ImmutableList;
import eu.danieldk.nlp.conllx.CONLLToken;
import eu.danieldk.nlp.conllx.Sentence;
import eu.danieldk.nlp.conllx.SimpleSentence;
import eu.danieldk.nlp.conllx.Token;
import eu.danieldk.nlp.conllx.reader.CONLLReader;
import eu.danieldk.nlp.conllx.writer.CONLLWriter;
import eu.danieldk.nlp.jitar.data.Model;
import eu.danieldk.nlp.jitar.languagemodel.LanguageModel;
import eu.danieldk.nlp.jitar.languagemodel.LinearInterpolationLM;
import eu.danieldk.nlp.jitar.tagger.HMMTagger;
import eu.danieldk.nlp.jitar.wordhandler.LexiconWordHandler;
import eu.danieldk.nlp.jitar.wordhandler.SuffixWordHandler;
import eu.danieldk.nlp.jitar.wordhandler.WordHandler;

import java.io.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class TagConllX {
    public static void main(String[] args) throws IOException {
        if (args.length != 1) {
            System.out.println("jitar-tag-conlx model");
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
        SuffixWordHandler swh = new SuffixWordHandler(model, 2, 2, 8, 4, 10, 10);
        WordHandler wh = new LexiconWordHandler(model.lexicon(), model.uniGrams(), swh);

        // Create an n-gram language model.
        LanguageModel lm = new LinearInterpolationLM(model.uniGrams(),
                model.biGrams(), model.triGrams());

        // Initialize a tagger with a beam of 1000.0.
        HMMTagger tagger = new HMMTagger(model, wh, lm, 1000.0);

        try (CONLLReader reader = new CONLLReader(new BufferedReader(new InputStreamReader(System.in)))) {
            try (CONLLWriter writer = new CONLLWriter(new BufferedWriter(new OutputStreamWriter(System.out)))) {
                Sentence sentence;
                while ((sentence = reader.readSentence()) != null) {
                    List<String> tokens = new ArrayList<>(sentence.getTokens().size());
                    for (Token token : sentence.getTokens()) {
                        tokens.add(token.getForm().or("_"));
                    }

                    List<String> tags = HMMTagger.highestProbabilitySequence(tagger.tag(tokens), model).sequence();

                    ImmutableList.Builder<Token> taggedtokens = ImmutableList.builder();

                    for (int i = 0; i < tags.size(); i++) {
                        Token origToken = sentence.getTokens().get(i);
                        Token newToken = new CONLLToken(origToken.getID(), origToken.getForm(), origToken.getLemma(),
                                origToken.getCoarsePOSTag(), Optional.of(tags.get(i)), origToken.getFeatures(),
                                origToken.getHead(), origToken.getDepRel(), origToken.getPHead(), origToken.getPDepRel());
                        taggedtokens.add(newToken);
                    }

                    writer.write(new SimpleSentence(taggedtokens.build()));
                }
            }
        }

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
