package no.nav.sbl.dialogarena.integration;

import no.nav.melding.virksomhet.soeknadsskjemaengangsstoenad.v1.SoeknadsskjemaEngangsstoenad;
import no.nav.sbl.dialogarena.sendsoknad.domain.kravdialoginformasjon.ForeldrepengerInformasjon;
import org.junit.Before;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class AlternativRepresentasjonIT extends AbstractIT {

    private String foreldrepengerSkjemanummer = new ForeldrepengerInformasjon().getSkjemanummer().get(0);

    @Before
    public void setup() throws Exception {
        EndpointDataMocking.setupMockWsEndpointData();
    }

    @Test
    public void alternativRepresentasjonTest() {
        SoeknadsskjemaEngangsstoenad soknad = soknadMedDelstegstatusOpprettet(foreldrepengerSkjemanummer)
                .hentAlternativRepresentasjon(SoeknadsskjemaEngangsstoenad.class);
        assertThat(soknad.getRettigheter()).isNotNull();

    }

}
