package eu.danieldk.nlp.jitar.corpus;

import org.apache.commons.lang3.StringUtils;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class CONLLCorpusReader extends CorpusReader<TaggedWord> {
    public CONLLCorpusReader(List<TaggedWord> startMarkers, List<TaggedWord> endMarkers, boolean decapitalizeFirstWord) {
        super(startMarkers, endMarkers, decapitalizeFirstWord);
    }

    @Override
    public void parse(BufferedReader reader) throws IOException, CorpusReaderException {
        List<TaggedWord> sentence = new ArrayList<TaggedWord>(d_startMarkers);

        String line;
        while ((line = reader.readLine()) != null) {
            String parts[] = StringUtils.split(line.trim(), '\t');

            if (parts.length == 0) {
                sentence.addAll(d_endMarkers);
                callHandlers(sentence);
                sentence = new ArrayList<TaggedWord>(d_startMarkers);
                continue;
            }

            if (parts.length < 5)
                throw new CorpusReaderException(String.format("Line has fewer than five columns: %s", line),
                        CorpusReaderException.CorpusReadError.MISSING_TAG);

            sentence.add(new TaggedWord(parts[1], parts[4]));
        }

        if (sentence.size() != 0) {
            sentence.addAll(d_endMarkers);
            callHandlers(sentence);
        }
    }

    private void callHandlers(List<TaggedWord> sentence) {
        for (CorpusSentenceHandler<TaggedWord> handler : d_sentenceHandlers)
            handler.handleSentence(sentence);
    }
}
