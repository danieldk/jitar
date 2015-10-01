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

import eu.danieldk.nlp.jitar.corpus.BrownCorpusReader;
import eu.danieldk.nlp.jitar.corpus.CONLLCorpusReader;
import eu.danieldk.nlp.jitar.corpus.CorpusReader;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;

/**
 * Some helpful methods.
 *
 * @author Daniël de Kok <me@danieldk.eu>
 */
class Util {
    public static CorpusReader newCorpusReader(String corpusType, File corpus) throws IOException {
        return newCorpusReader(corpusType, new BufferedReader(new FileReader(corpus)));
    }

    public static CorpusReader newCorpusReader(String corpusType, BufferedReader reader) throws IOException {
        CorpusReader corpusReader;
        switch (corpusType) {
            case "brown":
                corpusReader = new BrownCorpusReader(reader, false);
                break;
            case "conll":
                corpusReader = new CONLLCorpusReader(reader, false);
                break;
            default:
                throw new IOException(String.format("Unknown corpus type: %s", corpusType));
        }
        return corpusReader;
    }

    private Util() {
    }
}
