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
  private CorpusReader<TaggedWord> d_corpusReader;

  @Before
  public void initialize() {
    List<TaggedWord> startMarkers = new ArrayList<TaggedWord>();
    startMarkers.add(new TaggedWord("<START>", "<START>"));
    List<TaggedWord> endMarkers = new ArrayList<TaggedWord>();
    endMarkers.add(new TaggedWord("<END>", "<END>"));

    d_corpusReader = new BrownCorpusReader(startMarkers, endMarkers, true);
  }

  @Test
  public void testParse() {
    StoreHandler handler = new StoreHandler();
    d_corpusReader.addHandler(handler);

    InputStream is = new ByteArrayInputStream("A/AT b/BT\nC/CT".getBytes());
    BufferedReader rdr = new BufferedReader(new InputStreamReader(is));

    try {
      d_corpusReader.parse(rdr);
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

    d_corpusReader.removeHandler(handler);

    Assert.assertEquals("Parsed corpus is inaccurate.", check, handler.getSentences());
  }

  @Test(expected = CorpusReaderException.class)
  public void testParseEmptyTag() throws CorpusReaderException {

    InputStream is = new ByteArrayInputStream("A/AT b/BT c/".getBytes());
    BufferedReader rdr = new BufferedReader(new InputStreamReader(is));

    try {
      d_corpusReader.parse(rdr);
    } catch (IOException e) {
      Assert.fail("IO error in corpus mock");
    }
  }

  @Test(expected = CorpusReaderException.class)
  public void testParseEmptyWord() throws CorpusReaderException {

    InputStream is = new ByteArrayInputStream("A/AT b/BT /CT".getBytes());
    BufferedReader rdr = new BufferedReader(new InputStreamReader(is));

    try {
      d_corpusReader.parse(rdr);
    } catch (IOException e) {
      Assert.fail("IO error in corpus mock");
    }
  }

  @Test(expected = CorpusReaderException.class)
  public void testParseMissingTag() throws CorpusReaderException {

    InputStream is = new ByteArrayInputStream("A/AT b/BT c".getBytes());
    BufferedReader rdr = new BufferedReader(new InputStreamReader(is));

    try {
      d_corpusReader.parse(rdr);
    } catch (IOException e) {
      Assert.fail("IO error in corpus mock");
    }
  }
}
