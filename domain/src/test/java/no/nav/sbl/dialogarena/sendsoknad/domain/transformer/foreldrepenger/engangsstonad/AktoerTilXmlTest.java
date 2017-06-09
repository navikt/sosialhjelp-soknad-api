package no.nav.sbl.dialogarena.sendsoknad.domain.transformer.foreldrepenger.engangsstonad;

import no.nav.melding.virksomhet.soeknadsskjemaengangsstoenad.v1.Aktoer;
import no.nav.melding.virksomhet.soeknadsskjemaengangsstoenad.v1.AktoerId;
import no.nav.sbl.dialogarena.sendsoknad.domain.Faktum;
import no.nav.sbl.dialogarena.sendsoknad.domain.WebSoknad;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class AktoerTilXmlTest {

    @Test
    public void medAktoerId(){
        WebSoknad soknad = new WebSoknad();

        soknad.medAktorId("***REMOVED***1");

        AktoerId resultat = (AktoerId) new AktoerTilXml().apply(soknad);
        assertThat(resultat.getAktoerId()).isEqualTo("***REMOVED***1");

    }
}
