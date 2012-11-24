package eu.danieldk.nlp.jitar.corpus;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class TestBrownCorpusReader {
  private class StoreHandler implements CorpusSentenceHandler<TaggedWord> {
    @Override
    public void handleSentence(List<TaggedWord> sentence) {
      d_sentences.add(sentence);
    }

    public List<List<TaggedWord>> getSentences() {
      return d_sentences;
    }

    private List<List<TaggedWord>> d_sentences = new ArrayList<List<TaggedWord>>();
  }

  private static BrownCorpusReader createReader() {
    return createReader(true);
  }

  private static BrownCorpusReader createReader(boolean decapFirstWord) {
    List<TaggedWord> startMarkers = new ArrayList<TaggedWord>();
    startMarkers.add(new TaggedWord("<START>", "<START>"));
    List<TaggedWord> endMarkers = new ArrayList<TaggedWord>();
    endMarkers.add(new TaggedWord("<END>", "<END>"));

    return new BrownCorpusReader(startMarkers, endMarkers, decapFirstWord);
  }

  @Test
  public void testParse() {
    StoreHandler handler = new StoreHandler();
    CorpusReader<TaggedWord> corpusReader = createReader();
    corpusReader.addHandler(handler);

    InputStream is = new ByteArrayInputStream("A/AT b/BT\nC/CT".getBytes());
    BufferedReader rdr = new BufferedReader(new InputStreamReader(is));

    try {
      corpusReader.parse(rdr);
    } catch (IOException e) {
      Assert.fail("IO error in corpus mock");
    } catch (CorpusReaderException e) {
      Assert.fail("Could not parse corpus: " + e.getLocalizedMessage());
    }

    List<List<TaggedWord>> check = new ArrayList<List<TaggedWord>>(){{
      add(new ArrayList<TaggedWord>(){{
        add(new TaggedWord("<START>", "<START>"));
        add(new TaggedWord("a", "AT"));
        add(new TaggedWord("b", "BT"));
        add(new TaggedWord("<END>", "<END>"));
      }});
      add(new ArrayList<TaggedWord>(){{
        add(new TaggedWord("<START>", "<START>"));
        add(new TaggedWord("c", "CT"));
        add(new TaggedWord("<END>", "<END>"));
      }});
    }};

    Assert.assertEquals("Parsed corpus is inaccurate.", check, handler.getSentences());
  }

  @Test
  public void testParseWithSlash() {
    StoreHandler handler = new StoreHandler();
    BrownCorpusReader corpusReader = createReader();
    corpusReader.addHandler(handler);

    InputStream is = new ByteArrayInputStream("A/a/AT".getBytes());
    BufferedReader rdr = new BufferedReader(new InputStreamReader(is));

    try {
      corpusReader.parse(rdr);
    } catch (IOException e) {
      Assert.fail("IO error in corpus mock");
    } catch (CorpusReaderException e) {
      Assert.fail("Could not parse corpus: " + e.getLocalizedMessage());
    }

    List<List<TaggedWord>> check = new ArrayList<List<TaggedWord>>(){{
      add(new ArrayList<TaggedWord>(){{
        add(new TaggedWord("<START>", "<START>"));
        add(new TaggedWord("a/a", "AT"));
        add(new TaggedWord("<END>", "<END>"));
      }});
    }};

    Assert.assertEquals("Parsed corpus is inaccurate.", check, handler.getSentences());
  }

  @Test(expected = CorpusReaderException.class)
  public void testParseEmptyTag() throws CorpusReaderException {
    BrownCorpusReader corpusReader = createReader();

    InputStream is = new ByteArrayInputStream("A/AT b/BT c/".getBytes());
    BufferedReader rdr = new BufferedReader(new InputStreamReader(is));

    try {
      corpusReader.parse(rdr);
    } catch (IOException e) {
      Assert.fail("IO error in corpus mock");
    }
  }

  @Test(expected = CorpusReaderException.class)
  public void testParseEmptyWord() throws CorpusReaderException {
    BrownCorpusReader corpusReader = createReader();

    InputStream is = new ByteArrayInputStream("A/AT b/BT /CT".getBytes());
    BufferedReader rdr = new BufferedReader(new InputStreamReader(is));

    try {
      corpusReader.parse(rdr);
    } catch (IOException e) {
      Assert.fail("IO error in corpus mock");
    }
  }

  @Test(expected = CorpusReaderException.class)
  public void testParseMissingTag() throws CorpusReaderException {
    BrownCorpusReader corpusReader = createReader();

    InputStream is = new ByteArrayInputStream("A/AT b/BT c".getBytes());
    BufferedReader rdr = new BufferedReader(new InputStreamReader(is));

    try {
      corpusReader.parse(rdr);
    } catch (IOException e) {
      Assert.fail("IO error in corpus mock");
    }
  }

  @Test(expected = UnsupportedOperationException.class)
  public void testParseReadOnly() {
    BrownCorpusReader corpusReader = createReader();

    InputStream is = new ByteArrayInputStream("A/AT b/BT".getBytes());
    BufferedReader rdr = new BufferedReader(new InputStreamReader(is));

    StoreHandler handler = new StoreHandler();
    corpusReader.addHandler(handler);

    try {
      corpusReader.parse(rdr);
    } catch (IOException e) {
      Assert.fail("IO error in corpus mock");
    } catch (CorpusReaderException e) {
      Assert.fail("Could not parse corpus: " + e.getLocalizedMessage());
    }
    handler.getSentences().get(0).add(new TaggedWord("foo", "BAR"));
  }
}
