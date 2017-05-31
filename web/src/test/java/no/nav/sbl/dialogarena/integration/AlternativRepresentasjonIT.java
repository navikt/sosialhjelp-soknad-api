package no.nav.sbl.dialogarena.integration;

import no.nav.melding.virksomhet.soeknadsskjemaengangsstoenad.v1.SoeknadsskjemaEngangsstoenad;
import no.nav.sbl.dialogarena.sendsoknad.domain.kravdialoginformasjon.ForeldrepengerInformasjon;
import org.junit.Before;
import org.junit.Test;

import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

public class AlternativRepresentasjonIT extends AbstractIT {

    private String engangsstonadAdopsjonSkjemanummer = new ForeldrepengerInformasjon().getSkjemanummer().get(4);

    @Before
    public void setup() throws Exception {
        EndpointDataMocking.setupMockWsEndpointData();
    }

    @Test
    public void alternativRepresentasjonRettigheterTest() {
        SoknadTester testSoknad = soknadMedDelstegstatusOpprettet(engangsstonadAdopsjonSkjemanummer)
            .faktum("soknadsvalg.fodselelleradopsjon").withValue("fodsel").utforEndring()
            .faktum("rettigheter.overtak").withValue("overtattPaGrunnAvDod").utforEndring();
        SoeknadsskjemaEngangsstoenad soknad = testSoknad
                .hentAlternativRepresentasjon(SoeknadsskjemaEngangsstoenad.class);
        assertThat(soknad.getRettigheter()).isNotNull();
        assertThat(soknad.getRettigheter().getGrunnlagForAnsvarsovertakelse()).isEqualTo("overtattPaGrunnAvDod");

    }

    @Test
    public void alternativRepresentasjonTilknytningTest() {
        Map<String, String> periodeProperties = new HashMap<>();
        periodeProperties.put("land", "AFG");
        periodeProperties.put("fradato", "2017-01-02");
        periodeProperties.put("tildato", "2017-04-01");

        SoknadTester testSoknad = soknadMedDelstegstatusOpprettet(engangsstonadAdopsjonSkjemanummer)
                .faktum("tilknytningnorge.oppholder").withValue("true").utforEndring()
                .faktum("tilknytningnorge.tidligere").withValue("false").utforEndring()
                .opprettFaktumWithValueAndProperties("tilknytningnorge.tidligere.periode", null, periodeProperties)
                .faktum("tilknytningnorge.fremtidig").withValue("true").utforEndring();

        SoeknadsskjemaEngangsstoenad soknad = testSoknad
                .hentAlternativRepresentasjon(SoeknadsskjemaEngangsstoenad.class);

        assertThat(soknad.getTilknytningNorge()).isNotNull();
        assertThat(soknad.getTilknytningNorge().isOppholdNorgeNaa()).isTrue();
    }

}
