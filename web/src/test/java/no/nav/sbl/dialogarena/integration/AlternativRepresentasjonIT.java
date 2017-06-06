package no.nav.sbl.dialogarena.integration;

import no.nav.melding.virksomhet.soeknadsskjemaengangsstoenad.v1.SoeknadsskjemaEngangsstoenad;
import no.nav.melding.virksomhet.soeknadsskjemaengangsstoenad.v1.Utenlandsopphold;
import no.nav.sbl.dialogarena.sendsoknad.domain.kravdialoginformasjon.ForeldrepengerInformasjon;
import org.junit.Before;
import org.junit.Test;

import java.util.HashMap;
import java.util.List;
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
        assertThat(soknad.getTilknytningNorge().isTidligereOppholdNorge()).isFalse();
        assertThat(soknad.getTilknytningNorge().isFremtidigOppholdNorge()).isTrue();

        List<Utenlandsopphold> tidligereUtenlandsopphold = soknad.getTilknytningNorge().getTidligereOppholdUtenlands();
        assertThat(tidligereUtenlandsopphold.get(0).getLand().getKode()).isEqualTo("AFG");
        assertThat(tidligereUtenlandsopphold.get(0).getPeriode().getFom().toString()).isEqualTo("2017-01-02");
        assertThat(tidligereUtenlandsopphold.get(0).getPeriode().getTom().toString()).isEqualTo("2017-04-01");
    }

    @Test
    public void alternativRepresentasjonOpplysningerOmFarEnkeltLøpTest() {
        Map<String,String> personInfoProperties = new HashMap<>();
        personInfoProperties.put("land", "ARG");

        SoknadTester testSoknad = soknadMedDelstegstatusOpprettet(engangsstonadAdopsjonSkjemanummer)
                .faktum("infofar.opplysninger.fornavn").withValue("Fornavn").utforEndring()
                .faktum("infofar.opplysninger.etternavn").withValue("Etternavn").utforEndring()
                .faktum("infofar.opplysninger.kanIkkeOppgi").withValue("true").utforEndring()
                .opprettFaktumWithValueAndProperties("infofar.opplysninger.kanIkkeOppgi.true.arsak","utenlandsk", personInfoProperties)
                .faktum("infofar.opplysninger.kanIkkeOppgi.true.arsak.utenlandsk.fodselsnummer").withValue("***REMOVED***").utforEndring();

        SoeknadsskjemaEngangsstoenad soknad = testSoknad
                .hentAlternativRepresentasjon(SoeknadsskjemaEngangsstoenad.class);

        assertThat(soknad.getOpplysningerOmFar().getFornavn()).isEqualTo("Fornavn");
        assertThat(soknad.getOpplysningerOmFar().getEtternavn()).isEqualTo("Etternavn");
        assertThat(soknad.getOpplysningerOmFar().getKanIkkeOppgiFar().getAarsak()).isEqualTo("utenlandsk");
        assertThat(soknad.getOpplysningerOmFar().getKanIkkeOppgiFar().getUtenlandskfnr()).isEqualTo("***REMOVED***");
        assertThat(soknad.getOpplysningerOmFar().getKanIkkeOppgiFar().getUtenlandskfnrLand().getKode()).isEqualTo("ARG");
    }

}
