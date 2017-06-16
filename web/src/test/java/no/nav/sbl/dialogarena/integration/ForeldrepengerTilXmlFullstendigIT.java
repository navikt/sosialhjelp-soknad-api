package no.nav.sbl.dialogarena.integration;


import no.nav.foreldrepenger.soeknadsskjema.engangsstoenad.v1.SoeknadsskjemaEngangsstoenad;
import no.nav.sbl.dialogarena.sendsoknad.domain.Vedlegg;
import no.nav.sbl.dialogarena.sendsoknad.domain.kravdialoginformasjon.ForeldrepengerInformasjon;
import no.nav.sbl.dialogarena.sendsoknad.domain.transformer.foreldrepenger.engangsstonad.Stonadstyper;
import org.junit.Before;
import org.junit.Test;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;

import static no.nav.sbl.dialogarena.sendsoknad.domain.transformer.foreldrepenger.engangsstonad.Skjemanummer.P5;
import static org.assertj.core.api.Assertions.assertThat;

public class ForeldrepengerTilXmlFullstendigIT extends AbstractIT {

    private String engangsstonadSkjemanummer = new ForeldrepengerInformasjon().getSkjemanummer().get(1);

    @Before
    public void setup() throws Exception {
        EndpointDataMocking.setupMockWsEndpointData();
    }

    @Test
    public void morFodsel() {
        Map<String, String> periodeProperties = new HashMap<>();
        periodeProperties.put("land", "AFG");
        periodeProperties.put("fradato", "2017-01-02");
        periodeProperties.put("tildato", "2017-04-01");

        LocalDate seksManederTilbakeITid = LocalDate.now().minusMonths(6);

        SoknadTester testSoknad = soknadMedDelstegstatusOpprettet(engangsstonadSkjemanummer)
                .faktum("soknadsvalg.stonadstype").withValue(Stonadstyper.ENGANGSSTONAD_MOR).utforEndring()
                .faktum("soknadsvalg.fodselelleradopsjon").withValue("fodsel").utforEndring()

                .faktum("tilknytningnorge.oppholder").withValue("true").utforEndring()
                .faktum("tilknytningnorge.tidligere").withValue("false").utforEndring()
                .nyttFaktum("tilknytningnorge.tidligere.periode").withProperties(periodeProperties).opprett()
                .faktum("tilknytningnorge.fremtidig").withValue("true").utforEndring()

                .faktum("barnet.dato").withValue(seksManederTilbakeITid.toString()).utforEndring()
                .faktum("barnet.antall").withValue("2").utforEndring()
                .faktum("veiledning.mor.terminbekreftelse").withValue("fodt").utforEndring()
                .faktum("barnet.forsensoknad.fritekst").withValue("Testbegrunnelse").utforEndring()

                .faktum("infofar.opplysninger.fornavn").withValue("Test").utforEndring()
                .faktum("infofar.opplysninger.etternavn").withValue("Testesen").utforEndring()
                .faktum("infofar.opplysninger.kanIkkeOppgi").withValue("true").utforEndring()
                .faktum("infofar.opplysninger.kanIkkeOppgi.true.arsak")
                .withValue("utenlandsk").withProperty("land", "AUS").utforEndring()
                .faktum("infofar.opplysninger.kanIkkeOppgi.true.arsak.utenlandsk.fodselsnummer")
                .withValue("123456").utforEndring()

                .faktum("tilleggsopplysninger.fritekst").withValue("Test").utforEndring()
                .settDelstegstatus("oppsummering");

        SoeknadsskjemaEngangsstoenad soknad = testSoknad.hentAlternativRepresentasjon(SoeknadsskjemaEngangsstoenad.class);

        assertThat(testSoknad.hentAlternativRepresentasjonResponseMedStatus().getStatus()).isEqualTo(200);
        assertThat(soknad.getOpplysningerOmMor()).isNull();
        assertThat(soknad.getOpplysningerOmFar()).isNotNull();
        assertThat(soknad.getOpplysningerOmFar().getPersonidentifikator()).isNull();
        assertThat(soknad.getTilknytningNorge()).isNotNull();
        assertThat(soknad.getTilknytningNorge().getTidligereOppholdUtenlands()).hasSize(1);
        assertThat(soknad.getTilknytningNorge().getFremtidigOppholdUtenlands()).isEmpty();
        assertThat(soknad.getRettigheter()).isNull();
        assertThat(soknad.getTilleggsopplysninger()).isEqualTo("Test");
        assertThat(soknad.getOpplysningerOmBarn().getBegrunnelse()).isEqualTo("Testbegrunnelse");

    }

    @Test
    public void farAdopsjon() {
        LocalDate enMaanedTilbakeITid = LocalDate.now().minusMonths(1);

        SoknadTester testSoknad = soknadMedDelstegstatusOpprettet(engangsstonadSkjemanummer)
                .faktum("soknadsvalg.stonadstype").withValue(Stonadstyper.ENGANGSSTONAD_FAR).utforEndring()
                .faktum("soknadsvalg.fodselelleradopsjon").withValue("adopsjon").utforEndring()

                .faktum("rettigheter.overtak").withValue("overtattOmsorgInnen53UkerAdopsjon").utforEndring()

                .faktum("tilknytningnorge.oppholder").withValue("true").utforEndring()
                .faktum("tilknytningnorge.tidligere").withValue("true").utforEndring()
                .faktum("tilknytningnorge.fremtidig").withValue("true").utforEndring()

                .faktum("barnet.dato").withValue(enMaanedTilbakeITid.toString()).utforEndring()
                .faktum("barnet.antall").withValue("2").utforEndring()
                .faktum("barnet.alder").withValue("2016-10-20").utforEndring()

                .faktum("infomor.opplysninger.fornavn").withValue("Test").utforEndring()
                .faktum("infomor.opplysninger.etternavn").withValue("Testesen").utforEndring()
                .faktum("infomor.opplysninger.personinfo").withProperty("personnummer", "01019001011").utforEndring()

                .hentPaakrevdeVedlegg()
                .vedlegg(P5).withInnsendingsValg(Vedlegg.Status.LastetOpp).utforEndring()
                .hentPaakrevdeVedlegg()
                .soknad().settDelstegstatus("oppsummering");

        SoeknadsskjemaEngangsstoenad soknad = testSoknad.hentAlternativRepresentasjon(SoeknadsskjemaEngangsstoenad.class);

        assertThat(testSoknad.hentAlternativRepresentasjonResponseMedStatus().getStatus()).isEqualTo(200);
        assertThat(soknad.getOpplysningerOmFar()).isNull();
        assertThat(soknad.getRettigheter().getGrunnlagForAnsvarsovertakelse()).isEqualTo("overtattOmsorgInnen53UkerAdopsjon");
        assertThat(soknad.getOpplysningerOmBarn().getBegrunnelse()).isNull();
        assertThat(soknad.getOpplysningerOmMor()).isNotNull();
        assertThat(soknad.getOpplysningerOmMor().getKanIkkeOppgiMor()).isNull();
        assertThat(soknad.getTilknytningNorge().isOppholdNorgeNaa()).isTrue();
        assertThat(soknad.getTilknytningNorge().isFremtidigOppholdNorge()).isTrue();
        assertThat(soknad.getTilknytningNorge().isTidligereOppholdNorge()).isTrue();
        assertThat(soknad.getTilknytningNorge().getTidligereOppholdUtenlands()).isEmpty();
        assertThat(soknad.getTilknytningNorge().getFremtidigOppholdUtenlands()).isEmpty();

    }

    @Test
    public void farFodsel() {

        Map<String, String> periodeProperties = new HashMap<>();
        periodeProperties.put("land", "FRO");
        periodeProperties.put("fradato", "2016-10-06");
        periodeProperties.put("tildato", "2017-05-01");

        SoknadTester testSoknad = soknadMedDelstegstatusOpprettet(engangsstonadSkjemanummer)
                .faktum("soknadsvalg.stonadstype").withValue(Stonadstyper.ENGANGSSTONAD_FAR).utforEndring()

                .faktum("rettigheter.overtak").withValue("overtattOmsorgInnen53UkerFodsel").utforEndring()

                .faktum("tilknytningnorge.oppholder").withValue("true").utforEndring()
                .faktum("tilknytningnorge.tidligere").withValue("false").utforEndring()
                .nyttFaktum("tilknytningnorge.tidligere.periode").withProperties(periodeProperties).opprett()

                .faktum("barnet.antall").withValue("3").utforEndring()
                .faktum("barnet.dato").withValue("2017-01-26").utforEndring()

                .faktum("infomor.opplysninger.fornavn").withValue("Prøve").utforEndring()
                .faktum("infomor.opplysninger.etternavn").withValue("Kanin").utforEndring()
                .faktum("infomor.opplysninger.kanIkkeOppgi").withValue("true").utforEndring()
                .faktum("infomor.opplysninger.kanIkkeOppgi.true.arsak")
                    .withValue("utenlandsk").withProperty("land", "AZE").utforEndring()
                .faktum("infomor.opplysninger.kanIkkeOppgi.true.arsak.utenlandsk.fodselsnummer")
                    .withValue("010185").utforEndring()

                .faktum("tilleggsopplysninger.fritekst").withValue("Joda, her er det masse info!").utforEndring()

                .hentPaakrevdeVedlegg()
                .vedlegg(P5).withInnsendingsValg(Vedlegg.Status.SendesSenere).utforEndring()
                .hentPaakrevdeVedlegg()
                .soknad().settDelstegstatus("oppsummering");

        //Er alt med?
        assertThat(testSoknad.hentAlternativRepresentasjonResponseMedStatus().getStatus()).isNotEqualTo(200);

        //Glemte disse!
        testSoknad
                .faktum("soknadsvalg.fodselelleradopsjon").withValue("fodsel").utforEndring()
                .faktum("tilknytningnorge.fremtidig").withValue("true").utforEndring();

        //Nå da?
        assertThat(testSoknad.hentAlternativRepresentasjonResponseMedStatus().getStatus()).isEqualTo(200);

        SoeknadsskjemaEngangsstoenad soknad = testSoknad.hentAlternativRepresentasjon(SoeknadsskjemaEngangsstoenad.class);

        assertThat(soknad.getOpplysningerOmFar()).isNull();
        assertThat(soknad.getRettigheter().getGrunnlagForAnsvarsovertakelse()).isEqualTo("overtattOmsorgInnen53UkerFodsel");
        assertThat(soknad.getOpplysningerOmBarn().getBegrunnelse()).isNull();
        assertThat(soknad.getOpplysningerOmBarn().getAntallBarn()).isEqualTo(3);

        assertThat(soknad.getOpplysningerOmMor()).isNotNull();
        assertThat(soknad.getOpplysningerOmMor().getFornavn().equals("Prøve"));
        assertThat(soknad.getOpplysningerOmMor().getEtternavn().equals("Kanin"));
        assertThat(soknad.getOpplysningerOmMor().getKanIkkeOppgiMor().getUtenlandskfnrLand().equals("AZE"));

        assertThat(soknad.getTilknytningNorge().getTidligereOppholdUtenlands().size()).isGreaterThan(0);
        assertThat(soknad.getTilknytningNorge().isOppholdNorgeNaa()).isTrue();
        assertThat(soknad.getTilknytningNorge().isFremtidigOppholdNorge()).isTrue();

    }
}
