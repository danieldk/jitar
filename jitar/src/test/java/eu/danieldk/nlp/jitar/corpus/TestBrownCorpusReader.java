// Copyright 2008, 2009, 2013 Daniel de Kok
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

package eu.danieldk.nlp.jitar.corpus;

import org.junit.Assert;
import org.junit.Test;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

public class TestBrownCorpusReader {
    private static BrownCorpusReader createReader(BufferedReader reader) {
        return createReader(reader, true);
    }

    private static BrownCorpusReader createReader(BufferedReader reader, boolean decapFirstWord) {
        return new BrownCorpusReader(reader, decapFirstWord);
    }

    @Test
    public void testParse() {
        InputStream is = new ByteArrayInputStream("A/AT b/BT\nC/CT".getBytes());
        BufferedReader rdr = new BufferedReader(new InputStreamReader(is));

        CorpusReader corpusReader = createReader(rdr);
        ReaderConsumer handler = new ReaderConsumer();

        try {
            handler.process(corpusReader);
        } catch (IOException e) {
            Assert.fail("Could not parse corpus: " + e.getLocalizedMessage());
        }

        List<List<TaggedToken>> check = new ArrayList<List<TaggedToken>>() {{
            add(new ArrayList<TaggedToken>() {{
                add(new TaggedToken("a", "AT"));
                add(new TaggedToken("b", "BT"));
            }});
            add(new ArrayList<TaggedToken>() {{
                add(new TaggedToken("c", "CT"));
            }});
        }};

        Assert.assertEquals("Parsed corpus is inaccurate.", check, handler.getSentences());
    }

    @Test
    public void testParseWithSlash() {
        InputStream is = new ByteArrayInputStream("A/a/AT".getBytes());
        BufferedReader rdr = new BufferedReader(new InputStreamReader(is));

        CorpusReader corpusReader = createReader(rdr);
        ReaderConsumer handler = new ReaderConsumer();

        try {
            handler.process(corpusReader);
        } catch (IOException e) {
            Assert.fail("Could not parse corpus: " + e.getLocalizedMessage());
        }

        List<List<TaggedToken>> check = new ArrayList<List<TaggedToken>>() {{
            add(new ArrayList<TaggedToken>() {{
                add(new TaggedToken("a/a", "AT"));
            }});
        }};

        Assert.assertEquals("Parsed corpus is inaccurate.", check, handler.getSentences());
    }

    @Test(expected = IOException.class)
    public void testParseEmptyTag() throws IOException {
        InputStream is = new ByteArrayInputStream("A/AT b/BT c/".getBytes());
        BufferedReader rdr = new BufferedReader(new InputStreamReader(is));

        CorpusReader corpusReader = createReader(rdr);
        ReaderConsumer handler = new ReaderConsumer();

        handler.process(corpusReader);
    }

    @Test(expected = IOException.class)
    public void testParseEmptyWord() throws IOException {
        InputStream is = new ByteArrayInputStream("A/AT b/BT /CT".getBytes());
        BufferedReader rdr = new BufferedReader(new InputStreamReader(is));

        CorpusReader corpusReader = createReader(rdr);
        ReaderConsumer handler = new ReaderConsumer();
        handler.process(corpusReader);
    }

    @Test(expected = IOException.class)
    public void testParseMissingTag() throws IOException {
        InputStream is = new ByteArrayInputStream("A/AT b/BT c".getBytes());
        BufferedReader rdr = new BufferedReader(new InputStreamReader(is));

        CorpusReader corpusReader = createReader(rdr);
        ReaderConsumer handler = new ReaderConsumer();

        handler.process(corpusReader);
    }

    private class ReaderConsumer {
        private List<List<TaggedToken>> d_sentences = new ArrayList<>();

        public void process(CorpusReader reader) throws IOException {
            List<TaggedToken> sentence;
            while ((sentence = reader.readSentence()) != null) {
                d_sentences.add(sentence);
            }
        }

        public List<List<TaggedToken>> getSentences() {
            return d_sentences;
        }
    }
}
