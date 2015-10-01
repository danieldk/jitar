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

import eu.danieldk.nlp.jitar.corpus.CorpusReader;
import eu.danieldk.nlp.jitar.corpus.TaggedToken;
import eu.danieldk.nlp.jitar.data.Model;
import eu.danieldk.nlp.jitar.training.FrequenciesCollector;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.List;

public class Train {
    public static void main(String[] args) throws IOException {
        if (args.length != 3) {
            System.out.println("Train [brown/conll] corpus model");
            System.exit(1);
        }

        FrequenciesCollector frequenciesCollector = new FrequenciesCollector();

        try (CorpusReader corpusReader = Util.newCorpusReader(args[0], new File(args[1]))) {
            frequenciesCollector.process(corpusReader);
        } catch (IOException e) {
            System.err.println(String.format("Error reading corpus: %s", e.getMessage()));
            System.exit(1);
        }

        Model model = frequenciesCollector.model();

        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(args[2]))) {
            oos.writeObject(model);
        } catch (IOException e) {
            System.out.println("Could not write model!");
            e.printStackTrace();
            System.exit(1);
        }
    }
}
