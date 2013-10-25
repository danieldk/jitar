package eu.danieldk.nlp.jitar.models.brown;

import eu.danieldk.nlp.jitar.data.Model;

import java.io.IOException;
import java.io.InputStream;

/**
 * This is a utility class that makes it easy to read and use the Brown-based corpus model. Please ensure
 * that you use the Brown corpus in correspondance to its license.
 *
 * @author Daniël de Kok <me@danieldk.eu>
 */
public class BrownModel {
    private BrownModel() {
    }

    /**
     * Construct a {@link Model} instance based on the Brown corpus.
     *
     * @return Model trained on the Brown corpus.
     * @throws IOException
     */
    public static Model createInstance() throws IOException {
        InputStream lexiconStream = null;
        InputStream ngramsStream = null;

        try {
            lexiconStream = BrownModel.class.getResourceAsStream("/eu/danieldk/nlp/jitar/models/brown/brown-simplified.lexicon");
            ngramsStream = BrownModel.class.getResourceAsStream("/eu/danieldk/nlp/jitar/models/brown/brown-simplified.ngrams");
            return Model.readModel(lexiconStream, ngramsStream);
        } finally {
            if (lexiconStream != null)
                lexiconStream.close();
            if (ngramsStream != null)
                ngramsStream.close();
        }

    }
}
