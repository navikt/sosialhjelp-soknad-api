package no.nav.sbl.dialogarena.integration;

import no.nav.melding.virksomhet.soeknadsskjemaengangsstoenad.v1.SoeknadsskjemaEngangsstoenad;
import no.nav.melding.virksomhet.soeknadsskjemaengangsstoenad.v1.Utenlandsopphold;
import no.nav.sbl.dialogarena.sendsoknad.domain.Vedlegg;
import no.nav.sbl.dialogarena.sendsoknad.domain.kravdialoginformasjon.ForeldrepengerInformasjon;
import no.nav.sbl.dialogarena.sendsoknad.domain.transformer.foreldrepenger.engangsstonad.DokumentTypeId;
import org.junit.Before;
import org.junit.Test;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static java.util.Collections.singletonMap;
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
                .nyttFaktum("tilknytningnorge.tidligere.periode").withProperties(periodeProperties).opprett()
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
    public void alternativRepresentasjonOpplysningerOmMorTest() {
        SoknadTester testSoknad = soknadMedDelstegstatusOpprettet(engangsstonadAdopsjonSkjemanummer)
                .faktum("infomor.opplysninger.fornavn").withValue("Test").utforEndring()
                .faktum("infomor.opplysninger.etternavn").withValue("Testesen").utforEndring()
                .faktum("infomor.opplysninger.kanIkkeOppgi").withValue("true").utforEndring()
                .faktum("infomor.opplysninger.kanIkkeOppgi.true.arsak").withValue("utenlandsk").withProperties(singletonMap("land", "AFG")).utforEndring()
                .faktum("infomor.opplysninger.kanIkkeOppgi.true.arsak.utenlandsk.fodselsnummer").withValue("1234567890").utforEndring();

        SoeknadsskjemaEngangsstoenad soknad = testSoknad
                .hentAlternativRepresentasjon(SoeknadsskjemaEngangsstoenad.class);

        assertThat(soknad.getOpplysningerOmMor()).isNotNull();
        assertThat(soknad.getOpplysningerOmMor().getFornavn()).isEqualTo("Test");
        assertThat(soknad.getOpplysningerOmMor().getEtternavn()).isEqualTo("Testesen");
        assertThat(soknad.getOpplysningerOmMor().getPersonidentifikator()).isNull();
        assertThat(soknad.getOpplysningerOmMor().getKanIkkeOppgiMor().getAarsak()).isEqualTo("utenlandsk");
        assertThat(soknad.getOpplysningerOmMor().getKanIkkeOppgiMor().getUtenlandskfnr()).isEqualTo("1234567890");
        assertThat(soknad.getOpplysningerOmMor().getKanIkkeOppgiMor().getUtenlandskfnrLand().getKode()).isEqualTo("AFG");
    }

    @Test
    public void alternativRepresentasjonOpplysningerOmFarEnkeltLopTest() {
        Map<String,String> personInfoProperties = new HashMap<>();
        personInfoProperties.put("land", "ARG");

        SoknadTester testSoknad = soknadMedDelstegstatusOpprettet(engangsstonadAdopsjonSkjemanummer)
                .faktum("infofar.opplysninger.fornavn").withValue("Fornavn").utforEndring()
                .faktum("infofar.opplysninger.etternavn").withValue("Etternavn").utforEndring()
                .faktum("infofar.opplysninger.kanIkkeOppgi").withValue("true").utforEndring()
                .faktum("infofar.opplysninger.kanIkkeOppgi.true.arsak").withValue("utenlandsk").withProperties(personInfoProperties).utforEndring()
                .faktum("infofar.opplysninger.kanIkkeOppgi.true.arsak.utenlandsk.fodselsnummer").withValue("11111111111").utforEndring();

        SoeknadsskjemaEngangsstoenad soknad = testSoknad
                .hentAlternativRepresentasjon(SoeknadsskjemaEngangsstoenad.class);

        assertThat(soknad.getOpplysningerOmFar()).isNotNull();
        assertThat(soknad.getOpplysningerOmFar().getFornavn()).isEqualTo("Fornavn");
        assertThat(soknad.getOpplysningerOmFar().getEtternavn()).isEqualTo("Etternavn");
        assertThat(soknad.getOpplysningerOmFar().getKanIkkeOppgiFar().getAarsak()).isEqualTo("utenlandsk");
        assertThat(soknad.getOpplysningerOmFar().getKanIkkeOppgiFar().getUtenlandskfnr()).isEqualTo("11111111111");
        assertThat(soknad.getOpplysningerOmFar().getKanIkkeOppgiFar().getUtenlandskfnrLand().getKode()).isEqualTo("ARG");
    }

    @Test
    public void opplysningerBarnTest() {
        SoknadTester testSoknad = soknadMedDelstegstatusOpprettet(engangsstonadAdopsjonSkjemanummer)
                .faktum("soknadsvalg.stonadstype").withValue("engangsstonadFar").utforEndring()
                .faktum("soknadsvalg.fodselelleradopsjon").withValue("fodsel").utforEndring()
                .faktum("barnet.dato").withValue("2017-01-01").utforEndring()
                .faktum("barnet.antall").withValue("999").utforEndring();

        SoeknadsskjemaEngangsstoenad soknad = testSoknad
                .hentAlternativRepresentasjon(SoeknadsskjemaEngangsstoenad.class);

        assertThat(soknad.getOpplysningerOmBarn().getFoedselsdatoes().get(0)).isEqualTo(LocalDate.of(2017, 1, 1));
        assertThat(soknad.getOpplysningerOmBarn().getAntallBarn()).isEqualTo(999);
   }


}
