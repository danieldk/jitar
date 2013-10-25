package eu.danieldk.nlp.jitar.models.brown;

import eu.danieldk.nlp.jitar.data.Model;
import org.junit.Assert;
import org.junit.Test;

import java.io.IOException;

/**
 * Unit tests for {@link BrownModel}.
 * @author Daniël de Kok <me@danieldk.eu>
 */
public class BrownModelTest {
    @Test
    public void createInstanceTest() throws IOException {
        Model model = BrownModel.createInstance();
        Assert.assertEquals(55176, model.lexicon().size());
        Assert.assertEquals(48132, model.triGrams().size());
    }
}
