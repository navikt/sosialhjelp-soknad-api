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
    public void hvisFlereStarterMedSkalReturnereTrueOmFlereHarVerdienTrue() throws IOException {
        WebSoknad soknad = new WebSoknad().medBehandlingId("1A");
        soknad.medFaktum(new Faktum().medKey("start.1").medValue("true"));
        soknad.medFaktum(new Faktum().medKey("start.2").medValue("true"));
        soknad.medFaktum(new Faktum().medKey("start.3").medValue("true"));

        String html = new HandleBarKjoerer().fyllHtmlMalMedInnhold(soknad, "/skjema/hvisFlereErTrue");
        assertThat(html).contains("flere");
    }

}