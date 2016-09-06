package no.nav.sbl.dialogarena.service.helpers;

import com.github.jknack.handlebars.Handlebars;
import no.nav.sbl.dialogarena.sendsoknad.domain.Vedlegg;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;

import static org.assertj.core.api.Assertions.assertThat;

public class VedleggCmsNokkelTest {
    private Handlebars handlebars;

    @Before
    public void setup(){
        handlebars = new Handlebars();
        VedleggCmsNokkel helper = new VedleggCmsNokkel();
        handlebars.registerHelper(helper.getNavn(), helper);
    }
    @Test
    public void skalReturnereKorrektNokkel() throws IOException {
        String innhold = handlebars.compileInline("{{vedleggCmsNokkel this}}").apply(new Vedlegg().medSkjemaNummer("skjema").medSkjemanummerTillegg("tillegg"));
        assertThat(innhold).isEqualTo("Dagpenger.vedlegg.skjema.tillegg.bekrefte");
    }

}