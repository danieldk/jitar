package eu.danieldk.nlp.jitar.cli;

import eu.danieldk.nlp.jitar.corpus.BrownCorpusReader;
import eu.danieldk.nlp.jitar.corpus.CONLLCorpusReader;
import eu.danieldk.nlp.jitar.corpus.CorpusReader;
import eu.danieldk.nlp.jitar.corpus.TaggedToken;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.List;

/**
 * Some helpful methods.
 * @author Daniël de Kok <me@danieldk.eu>
 */
class Util {
    public static CorpusReader newCorpusReader(String corpusType, File corpus, List<TaggedToken> startMarkers,
                                                List<TaggedToken> endMarkers) throws IOException {
        CorpusReader corpusReader;
        switch (corpusType) {
            case "brown":
                corpusReader = new BrownCorpusReader(new BufferedReader(new FileReader(corpus)), startMarkers, endMarkers, true);
                break;
            case "conll":
                corpusReader = new CONLLCorpusReader(new BufferedReader(new FileReader(corpus)), startMarkers, endMarkers, true);
                break;
            default:
                throw new IOException(String.format("Unknown corpus type: %s", corpusType));
        }
        return corpusReader;
    }

    private Util() {}
}
