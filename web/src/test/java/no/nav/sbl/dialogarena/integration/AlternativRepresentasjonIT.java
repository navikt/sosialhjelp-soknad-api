package no.nav.sbl.dialogarena.integration;

import no.nav.melding.virksomhet.soeknadsskjemaengangsstoenad.v1.SoeknadsskjemaEngangsstoenad;
import no.nav.sbl.dialogarena.sendsoknad.domain.kravdialoginformasjon.ForeldrepengerInformasjon;
import org.junit.Before;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class AlternativRepresentasjonIT extends AbstractIT {

    private String engangsstonadAdopsjonSkjemanummer = new ForeldrepengerInformasjon().getSkjemanummer().get(4);

    @Before
    public void setup() throws Exception {
        EndpointDataMocking.setupMockWsEndpointData();
    }

    @Test
    public void alternativRepresentasjonTest() {
        SoknadTester soknadTester = soknadMedDelstegstatusOpprettet(engangsstonadAdopsjonSkjemanummer)
            .faktum("soknadsvalg.fodselelleradopsjon").withValue("fodsel").utforEndring()
            .faktum("rettigheter.overtak").withValue("overtattPaGrunnAvDod").utforEndring();
        SoeknadsskjemaEngangsstoenad soknad = soknadTester
                .hentAlternativRepresentasjon(SoeknadsskjemaEngangsstoenad.class);
        assertThat(soknad.getRettigheter()).isNotNull();

    }

}
