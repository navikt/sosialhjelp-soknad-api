package no.nav.sbl.dialogarena.service;

import no.nav.sbl.dialogarena.soknadinnsending.business.domain.Faktum;
import no.nav.sbl.dialogarena.soknadinnsending.business.domain.WebSoknad;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;

import java.io.IOException;

import static org.assertj.core.api.Assertions.assertThat;

@RunWith(MockitoJUnitRunner.class)
public class HandleBarHelperTest {

    @Test
    public void helperForPeriodeTidsromFaktaSkalGiFaktaISortertRekkefolge() throws IOException {
        WebSoknad soknad = new WebSoknad().medBehandlingId("1A");
        soknad.medFaktum(getPeriodeFaktum("perioder.tidsrom.1", "2015-03-03", "2015-03-05", "3"))
                .medFaktum(getPeriodeFaktum("perioder.tidsrom.2", "2015-01-04", "2015-01-05", "1"))
                .medFaktum(getPeriodeFaktum("perioder.tidsrom.3", "2015-02-04", "2015-02-05", "2"))
                .medFaktum(getPeriodeFaktum("perioder.tidsrom.4", "2015-04-04", "2015-04-05", "4"));

        String html = new HandleBarKjoerer().fyllHtmlMalMedInnhold(soknad, "/skjema/periodeTidsrom");
        assertThat(html).contains("1234");
    }

    @Test
    public void hvisFlereStarterMedSkalReturnereTrueOmFlereHarVerdienTrue() throws IOException {
        WebSoknad soknad = new WebSoknad().medBehandlingId("1A");
        soknad.medFaktum(new Faktum().medKey("start.1").medValue("true"));
        soknad.medFaktum(new Faktum().medKey("start.2").medValue("true"));
        soknad.medFaktum(new Faktum().medKey("start.3").medValue("true"));

        String html = new HandleBarKjoerer().fyllHtmlMalMedInnhold(soknad, "/skjema/hvisFlereErTrue");
        assertThat(html).contains("flere");
    }

    private Faktum getPeriodeFaktum(String key, String fradato, String tildato, String value) {
        return new Faktum().medKey(key).medProperty("fradato", fradato).medProperty("tildato", tildato).medValue(value);
    }

}