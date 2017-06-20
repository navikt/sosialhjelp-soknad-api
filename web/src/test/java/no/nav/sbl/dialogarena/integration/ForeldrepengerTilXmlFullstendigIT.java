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
    private SoknadTester testSoknad_morFodsel, testSoknad_farAdopsjon, testSoknad_farOvertaOmsorg, testSoknad_ukomplett;
    private SoeknadsskjemaEngangsstoenad soknad_morFodsel, soknad_farAdopsjon, soknad_farOvertaOmsorg;

    @Before
    public void setup() throws Exception {
        EndpointDataMocking.setupMockWsEndpointData();
        lagMorFodsel();
        lagFarAdopsjon();
        lagFarOvertaOmsorg();
        lagUkomplett();
    }

    @Test
    public void testXMLKonverteringGirKorrektRespons() {
        assertThat(testSoknad_morFodsel.hentAlternativRepresentasjonResponseMedStatus().getStatus()).isEqualTo(200);
        assertThat(testSoknad_farAdopsjon.hentAlternativRepresentasjonResponseMedStatus().getStatus()).isEqualTo(200);
        assertThat(testSoknad_farOvertaOmsorg.hentAlternativRepresentasjonResponseMedStatus().getStatus()).isEqualTo(200);
        assertThat(testSoknad_ukomplett.hentAlternativRepresentasjonResponseMedStatus().getStatus()).isEqualTo(500);
    }

    @Test
    public void testOpplysningerOmMorOgFar() {
        assertThat(soknad_morFodsel.getOpplysningerOmMor()).isNull();
        assertThat(soknad_morFodsel.getOpplysningerOmFar()).isNotNull();
        assertThat(soknad_morFodsel.getOpplysningerOmFar().getPersonidentifikator()).isNull();
        assertThat(soknad_morFodsel.getOpplysningerOmFar().getFornavn()).isEqualTo("Test");
        assertThat(soknad_morFodsel.getOpplysningerOmFar().getEtternavn()).isEqualTo("Testesen");

        assertThat(soknad_farAdopsjon.getOpplysningerOmFar()).isNull();
        assertThat(soknad_farAdopsjon.getOpplysningerOmMor()).isNotNull();
        assertThat(soknad_farAdopsjon.getOpplysningerOmMor().getKanIkkeOppgiMor()).isNull();
        assertThat(soknad_farAdopsjon.getOpplysningerOmMor().getFornavn()).isEqualTo("Test");
        assertThat(soknad_farAdopsjon.getOpplysningerOmMor().getEtternavn()).isEqualTo("Testesen");
        assertThat(soknad_farAdopsjon.getOpplysningerOmMor().getPersonidentifikator()).isEqualTo("01019001011");

        assertThat(soknad_farOvertaOmsorg.getOpplysningerOmFar()).isNull();
        assertThat(soknad_farOvertaOmsorg.getOpplysningerOmMor()).isNotNull();
        assertThat(soknad_farOvertaOmsorg.getOpplysningerOmMor().getFornavn()).isEqualTo("Prøve");
        assertThat(soknad_farOvertaOmsorg.getOpplysningerOmMor().getKanIkkeOppgiMor().getUtenlandskfnrLand().getKode()).isEqualTo("AZE");
        assertThat(soknad_farOvertaOmsorg.getOpplysningerOmMor().getKanIkkeOppgiMor().getUtenlandskfnrEllerForklaring()).isEqualTo("Ukjent");
    }

    @Test
    public void testOpplysningerOmBarn() {
        assertThat(soknad_morFodsel.getOpplysningerOmBarn().getBegrunnelse()).isEqualTo("Testbegrunnelse");

        assertThat(soknad_farAdopsjon.getOpplysningerOmBarn().getBegrunnelse()).isNull();

        assertThat(soknad_farOvertaOmsorg.getOpplysningerOmBarn().getBegrunnelse()).isNull();
        assertThat(soknad_farOvertaOmsorg.getOpplysningerOmBarn().getAntallBarn()).isEqualTo(3);
    }

    @Test
    public void testRettigheter() {
        assertThat(soknad_morFodsel.getRettigheter()).isNull();

        assertThat(soknad_farAdopsjon.getRettigheter().getGrunnlagForAnsvarsovertakelse().value()).isEqualTo("overtattOmsorgInnen53UkerAdopsjon");

        assertThat(soknad_farOvertaOmsorg.getRettigheter().getGrunnlagForAnsvarsovertakelse().value()).isEqualTo("overtattOmsorgInnen53UkerFodsel");
    }

    @Test
    public void testTilknyttningNorge() {
        assertThat(soknad_morFodsel.getTilknytningNorge()).isNotNull();
        assertThat(soknad_morFodsel.getTilknytningNorge().getTidligereOppholdUtenlands()).isNotNull();
        assertThat(soknad_morFodsel.getTilknytningNorge().getTidligereOppholdUtenlands().getUtenlandsoppholds()).hasSize(1);
        assertThat(soknad_morFodsel.getTilknytningNorge().getFremtidigOppholdUtenlands()).isNull();

        assertThat(soknad_farAdopsjon.getTilknytningNorge().isOppholdNorgeNaa()).isTrue();
        assertThat(soknad_farAdopsjon.getTilknytningNorge().isFremtidigOppholdNorge()).isTrue();
        assertThat(soknad_farAdopsjon.getTilknytningNorge().isTidligereOppholdNorge()).isTrue();
           assertThat(soknad_farAdopsjon.getTilknytningNorge().getTidligereOppholdUtenlands()).isNull();
           assertThat(soknad_farAdopsjon.getTilknytningNorge().getFremtidigOppholdUtenlands()).isNull();

        assertThat(soknad_farOvertaOmsorg.getTilknytningNorge().getTidligereOppholdUtenlands().getUtenlandsoppholds().size()).isGreaterThan(0);
        assertThat(soknad_farOvertaOmsorg.getTilknytningNorge().isOppholdNorgeNaa()).isTrue();
        assertThat(soknad_farOvertaOmsorg.getTilknytningNorge().isFremtidigOppholdNorge()).isTrue();
    }

    @Test
    public void testTillegsopplysninger() {
        assertThat(soknad_morFodsel.getTilleggsopplysninger()).isEqualTo("Test tilleggsopplysninger");

        assertThat(soknad_farOvertaOmsorg.getTilleggsopplysninger()).isEqualTo("Joda, her er det masse info!");
    }

    @Test
    public void testVedlegg() {
        assertThat(soknad_morFodsel.getVedleggListe().getVedleggs()).isEmpty();

        assertThat(soknad_farAdopsjon.getVedleggListe().getVedleggs()).isNotEmpty();
        assertThat(soknad_farAdopsjon.getVedleggListe().getVedleggs().contains(P5));
    }

    private void lagMorFodsel() {
        Map<String, String> periodeProperties = new HashMap<>();
        periodeProperties.put("land", "AFG");
        periodeProperties.put("fradato", "2017-01-02");
        periodeProperties.put("tildato", "2017-04-01");

        LocalDate seksManederTilbakeITid = LocalDate.now().minusMonths(6);

        testSoknad_morFodsel = soknadMedDelstegstatusOpprettet(engangsstonadSkjemanummer)
                .faktum("soknadsvalg.stonadstype").withValue(Stonadstyper.ENGANGSSTONAD_MOR).utforEndring()
                .faktum("soknadsvalg.fodselelleradopsjon").withValue("fodsel").utforEndring()
                .faktum("veiledning.mor.terminbekreftelse").withValue("fodt").utforEndring()

                .faktum("tilknytningnorge.oppholder").withValue("true").utforEndring()
                .faktum("tilknytningnorge.tidligere").withValue("false").utforEndring()
                .nyttFaktum("tilknytningnorge.tidligere.periode").withProperties(periodeProperties).opprett()
                .faktum("tilknytningnorge.fremtidig").withValue("true").utforEndring()

                .faktum("barnet.dato").withValue(seksManederTilbakeITid.toString()).utforEndring()
                .faktum("barnet.antall").withValue("2").utforEndring()
                .faktum("barnet.forsensoknad.fritekst").withValue("Testbegrunnelse").utforEndring()

                .faktum("infofar.opplysninger.fornavn").withValue("Test").utforEndring()
                .faktum("infofar.opplysninger.etternavn").withValue("Testesen").utforEndring()
                .faktum("infofar.opplysninger.kanIkkeOppgi").withValue("true").utforEndring()
                .faktum("infofar.opplysninger.kanIkkeOppgi.true.arsak")
                .withValue("utenlandsk").withProperty("land", "AUS").utforEndring()
                .faktum("infofar.opplysninger.kanIkkeOppgi.true.arsak.utenlandsk.fodselsnummer")
                .withValue("123456").utforEndring()

                .faktum("tilleggsopplysninger.fritekst").withValue("Test tilleggsopplysninger").utforEndring()
                .settDelstegstatus("oppsummering");

        soknad_morFodsel = testSoknad_morFodsel.hentAlternativRepresentasjon(SoeknadsskjemaEngangsstoenad.class);
    }

    private void lagFarAdopsjon() {
        LocalDate enMaanedTilbakeITid = LocalDate.now().minusMonths(1);

        testSoknad_farAdopsjon = soknadMedDelstegstatusOpprettet(engangsstonadSkjemanummer)
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

        soknad_farAdopsjon = testSoknad_farAdopsjon.hentAlternativRepresentasjon(SoeknadsskjemaEngangsstoenad.class);
    }

    private void lagFarOvertaOmsorg() {

        Map<String, String> periodeProperties = new HashMap<>();
        periodeProperties.put("land", "FRO");
        periodeProperties.put("fradato", "2016-10-06");
        periodeProperties.put("tildato", "2017-05-01");

        testSoknad_farOvertaOmsorg = soknadMedDelstegstatusOpprettet(engangsstonadSkjemanummer)
                .faktum("soknadsvalg.stonadstype").withValue(Stonadstyper.ENGANGSSTONAD_FAR).utforEndring()
                .faktum("soknadsvalg.fodselelleradopsjon").withValue("fodsel").utforEndring()

                .faktum("rettigheter.overtak").withValue("overtattOmsorgInnen53UkerFodsel").utforEndring()

                .faktum("tilknytningnorge.oppholder").withValue("true").utforEndring()
                .faktum("tilknytningnorge.tidligere").withValue("false").utforEndring()
                .nyttFaktum("tilknytningnorge.tidligere.periode").withProperties(periodeProperties).opprett()
                .faktum("tilknytningnorge.fremtidig").withValue("true").utforEndring()

                .faktum("barnet.antall").withValue("3").utforEndring()
                .faktum("barnet.dato").withValue("2017-01-26").utforEndring()

                .faktum("infomor.opplysninger.fornavn").withValue("Prøve").utforEndring()
                .faktum("infomor.opplysninger.etternavn").withValue("Kanin").utforEndring()
                .faktum("infomor.opplysninger.kanIkkeOppgi").withValue("true").utforEndring()
                .faktum("infomor.opplysninger.kanIkkeOppgi.true.arsak")
                .withValue("utenlandsk").withProperty("land", "AZE").utforEndring()
                .faktum("infomor.opplysninger.kanIkkeOppgi.true.arsak.utenlandsk.fodselsnummer")
                .withValue("Ukjent").utforEndring()

                .faktum("tilleggsopplysninger.fritekst").withValue("Joda, her er det masse info!").utforEndring()

                .hentPaakrevdeVedlegg()
                .vedlegg(P5).withInnsendingsValg(Vedlegg.Status.SendesSenere).utforEndring()
                .hentPaakrevdeVedlegg()
                .soknad().settDelstegstatus("oppsummering");

        soknad_farOvertaOmsorg = testSoknad_farOvertaOmsorg.hentAlternativRepresentasjon(SoeknadsskjemaEngangsstoenad.class);
    }

    private void lagUkomplett() {

        testSoknad_ukomplett = soknadMedDelstegstatusOpprettet(engangsstonadSkjemanummer)

                .faktum("soknadsvalg.stonadstype").withValue(Stonadstyper.ENGANGSSTONAD_FAR).utforEndring()

                .faktum("rettigheter.overtak").withValue("overtattOmsorgInnen53UkerFodsel").utforEndring()

                .faktum("tilknytningnorge.oppholder").withValue("true").utforEndring()
                .faktum("tilknytningnorge.tidligere").withValue("false").utforEndring()

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
    }
}
