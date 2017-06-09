package no.nav.sbl.dialogarena.sendsoknad.domain.transformer.foreldrepenger.engangsstonad;

import no.nav.foreldrepenger.soeknadsskjema.engangsstoenad.v1.Bruker;
import no.nav.sbl.dialogarena.sendsoknad.domain.WebSoknad;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class BrukerTilXmlTest {

    @Test
    public void medPersonnummerTest() {
        WebSoknad soknad = new WebSoknad();

        soknad.medAktorId("010101010101");

        Bruker resultat = (Bruker) new BrukerTilXml().apply(soknad);
        assertThat(resultat.getPersonidentifikator()).isEqualTo("010101010101");

    }
}
