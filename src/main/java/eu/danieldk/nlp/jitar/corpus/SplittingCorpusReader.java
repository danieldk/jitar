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

package eu.danieldk.nlp.jitar.corpus;

import java.io.IOException;
import java.util.List;
import java.util.Set;

public class SplittingCorpusReader implements CorpusReader {
    private final CorpusReader embeddedReader;

    private final int numFolds;

    private final Set<Integer> folds;

    private int count;

    public SplittingCorpusReader(CorpusReader corpusReader, int numFolds, Set<Integer> folds) {
        if (numFolds < 1)
            throw new IllegalArgumentException("The data should be 'splitted' in at least 1 fold.");

        this.embeddedReader = corpusReader;
        this.numFolds = numFolds;
        this.folds = folds;
        count = 0;
    }

    @Override
    public void close() throws IOException {
        if (embeddedReader != null)
            embeddedReader.close();
    }

    @Override
    public List<TaggedToken> readSentence() throws IOException {
        List<TaggedToken> sentence;
        while ((sentence = embeddedReader.readSentence()) != null) {
            if (count == numFolds)
                count = 0;

            if (folds.contains(count++))
                return sentence;
        }

        return null;
    }
}
