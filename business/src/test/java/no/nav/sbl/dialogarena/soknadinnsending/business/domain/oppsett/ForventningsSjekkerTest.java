package no.nav.sbl.dialogarena.soknadinnsending.business.domain.oppsett;


import no.nav.sbl.dialogarena.soknadinnsending.business.domain.Faktum;
import org.apache.commons.collections15.Predicate;
import org.junit.Test;

import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import java.util.Arrays;
import java.util.List;

import static java.lang.String.format;
import static javax.xml.bind.JAXBContext.newInstance;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

public class ForventningsSjekkerTest {

    @Test
    public void skalKunneSammenligneDoublesOgInt(){
        assertThat(ForventningsSjekker.sjekkForventning("value >= 6", new Faktum().medValue("6")), is(true));
        assertThat(ForventningsSjekker.sjekkForventning("value >= 6", new Faktum().medValue("6,0")), is(true));
        assertThat(ForventningsSjekker.sjekkForventning("value >= 6", new Faktum().medValue("6.0")), is(true));

        assertThat(ForventningsSjekker.sjekkForventning("value >= 6.0", new Faktum().medValue("6")), is(true));
        assertThat(ForventningsSjekker.sjekkForventning("value >= 6.0", new Faktum().medValue("6.0")), is(true));
        assertThat(ForventningsSjekker.sjekkForventning("value >= 6.0", new Faktum().medValue("6,0")), is(true));
        assertThat(ForventningsSjekker.sjekkForventning("6.0 <= value", new Faktum().medValue("6,0")), is(true));
    }

    @Test
    public void skalKunneSammenligneStrings(){
        assertThat(ForventningsSjekker.sjekkForventning("value == 'en'", new Faktum().medValue("en")), is(true));
        assertThat(ForventningsSjekker.sjekkForventning("value == 'en'", new Faktum().medValue("to")), is(false));
    }
}
