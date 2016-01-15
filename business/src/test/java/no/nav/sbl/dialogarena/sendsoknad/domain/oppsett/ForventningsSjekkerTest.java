package no.nav.sbl.dialogarena.sendsoknad.domain.oppsett;


import no.nav.sbl.dialogarena.sendsoknad.domain.Faktum;
import org.junit.Test;

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
