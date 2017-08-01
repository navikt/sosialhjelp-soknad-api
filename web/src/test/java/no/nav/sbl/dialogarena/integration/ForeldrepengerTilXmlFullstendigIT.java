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

import static java.util.stream.Collectors.toList;

import static no.nav.sbl.dialogarena.sendsoknad.domain.transformer.foreldrepenger.engangsstonad.Skjemanummer.P5;
import static org.assertj.core.api.Assertions.assertThat;

public class ForeldrepengerTilXmlFullstendigIT extends AbstractIT {

    private String engangsstonadSkjemanummer = new ForeldrepengerInformasjon().getSkjemanummer().get(1);
    private SoknadTester testSoknadMorFodsel, testSoknadFarAdopsjon, testSoknadFarOvertaOmsorg, testSoknadUkomplett;
    private SoeknadsskjemaEngangsstoenad soknadMorFodsel, soknadFarAdopsjon, soknadFarOvertaOmsorg;

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
        assertThat(testSoknadMorFodsel.hentAlternativRepresentasjonResponseMedStatus().getStatus()).isEqualTo(200);
        assertThat(testSoknadFarAdopsjon.hentAlternativRepresentasjonResponseMedStatus().getStatus()).isEqualTo(200);
        assertThat(testSoknadFarOvertaOmsorg.hentAlternativRepresentasjonResponseMedStatus().getStatus()).isEqualTo(200);
        assertThat(testSoknadUkomplett.hentAlternativRepresentasjonResponseMedStatus().getStatus()).isEqualTo(500);
    }

    @Test
    public void testOpplysningerOmMorOgFar() {
        assertThat(soknadMorFodsel.getOpplysningerOmMor()).isNull();
        assertThat(soknadMorFodsel.getOpplysningerOmFar()).isNotNull();
        assertThat(soknadMorFodsel.getOpplysningerOmFar().getPersonidentifikator()).isNull();
        assertThat(soknadMorFodsel.getOpplysningerOmFar().getFornavn()).isEqualTo("Test");
        assertThat(soknadMorFodsel.getOpplysningerOmFar().getEtternavn()).isEqualTo("Testesen");

        assertThat(soknadFarAdopsjon.getOpplysningerOmFar()).isNull();
        assertThat(soknadFarAdopsjon.getOpplysningerOmMor()).isNotNull();
        assertThat(soknadFarAdopsjon.getOpplysningerOmMor().getKanIkkeOppgiMor()).isNull();
        assertThat(soknadFarAdopsjon.getOpplysningerOmMor().getFornavn()).isEqualTo("Test");
        assertThat(soknadFarAdopsjon.getOpplysningerOmMor().getEtternavn()).isEqualTo("Testesen");
        assertThat(soknadFarAdopsjon.getOpplysningerOmMor().getPersonidentifikator()).isEqualTo("01019001011");

        assertThat(soknadFarOvertaOmsorg.getOpplysningerOmFar()).isNull();
        assertThat(soknadFarOvertaOmsorg.getOpplysningerOmMor()).isNotNull();
        assertThat(soknadFarOvertaOmsorg.getOpplysningerOmMor().getFornavn()).isEqualTo("Prøve");
        assertThat(soknadFarOvertaOmsorg.getOpplysningerOmMor().getKanIkkeOppgiMor().getUtenlandskfnrLand().getKode()).isEqualTo("AZE");
        assertThat(soknadFarOvertaOmsorg.getOpplysningerOmMor().getKanIkkeOppgiMor().getUtenlandskfnrEllerForklaring()).isEqualTo("Ukjent");
    }

    @Test
    public void testOpplysningerOmBarn() {
        assertThat(soknadMorFodsel.getOpplysningerOmBarn().getBegrunnelse()).isEqualTo("Testbegrunnelse");

        assertThat(soknadFarAdopsjon.getOpplysningerOmBarn().getBegrunnelse()).isNull();

        assertThat(soknadFarOvertaOmsorg.getOpplysningerOmBarn().getBegrunnelse()).isNull();
        assertThat(soknadFarOvertaOmsorg.getOpplysningerOmBarn().getAntallBarn()).isEqualTo(3);
    }

    @Test
    public void testRettigheter() {
        assertThat(soknadMorFodsel.getRettigheter()).isNull();

        assertThat(soknadFarAdopsjon.getRettigheter().getGrunnlagForAnsvarsovertakelse().value()).isEqualTo("overtattOmsorgInnen53UkerAdopsjon");

        assertThat(soknadFarOvertaOmsorg.getRettigheter().getGrunnlagForAnsvarsovertakelse().value()).isEqualTo("overtattOmsorgInnen53UkerFodsel");
    }

    @Test
    public void testTilknyttningNorge() {
        assertThat(soknadMorFodsel.getTilknytningNorge()).isNotNull();
        assertThat(soknadMorFodsel.getTilknytningNorge().getTidligereOppholdUtenlands()).isNotNull();
        assertThat(soknadMorFodsel.getTilknytningNorge().getTidligereOppholdUtenlands().getUtenlandsoppholds()).hasSize(1);
        assertThat(soknadMorFodsel.getTilknytningNorge().getFremtidigOppholdUtenlands()).isNull();

        assertThat(soknadFarAdopsjon.getTilknytningNorge().isOppholdNorgeNaa()).isTrue();
        assertThat(soknadFarAdopsjon.getTilknytningNorge().isFremtidigOppholdNorge()).isTrue();
        assertThat(soknadFarAdopsjon.getTilknytningNorge().isTidligereOppholdNorge()).isTrue();
        assertThat(soknadFarAdopsjon.getTilknytningNorge().getTidligereOppholdUtenlands()).isNull();
        assertThat(soknadFarAdopsjon.getTilknytningNorge().getFremtidigOppholdUtenlands()).isNull();

        assertThat(soknadFarOvertaOmsorg.getTilknytningNorge().getTidligereOppholdUtenlands().getUtenlandsoppholds().size()).isGreaterThan(0);
        assertThat(soknadFarOvertaOmsorg.getTilknytningNorge().isOppholdNorgeNaa()).isTrue();
        assertThat(soknadFarOvertaOmsorg.getTilknytningNorge().isFremtidigOppholdNorge()).isTrue();
    }

    @Test
    public void testTillegsopplysninger() {
        assertThat(soknadMorFodsel.getTilleggsopplysninger()).isEqualTo("Test tilleggsopplysninger");

        assertThat(soknadFarOvertaOmsorg.getTilleggsopplysninger()).isEqualTo("Joda, her er det masse info!");
    }

    @Test
    public void testVedlegg() {
        assertThat(soknadMorFodsel.getVedleggListe().getVedleggs()).isEmpty();

        assertThat(soknadFarAdopsjon.getVedleggListe().getVedleggs()).isNotEmpty();
        assertThat(soknadFarAdopsjon.getVedleggListe().getVedleggs()
                .stream()
                .map(v -> v.getSkjemanummer())
                .collect(toList()))
                .contains(P5.dokumentTypeId());
    }

    private void lagMorFodsel() {
        Map<String, String> periodeProperties = new HashMap<>();
        periodeProperties.put("land", "AFG");
        periodeProperties.put("fradato", "2017-01-02");
        periodeProperties.put("tildato", "2017-04-01");

        LocalDate seksManederTilbakeITid = LocalDate.now().minusMonths(6);

        testSoknadMorFodsel = soknadMedDelstegstatusOpprettet(engangsstonadSkjemanummer)
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

        soknadMorFodsel = testSoknadMorFodsel.hentAlternativRepresentasjon(SoeknadsskjemaEngangsstoenad.class);
    }

    private void lagFarAdopsjon() {
        LocalDate enMaanedTilbakeITid = LocalDate.now().minusMonths(1);

        testSoknadFarAdopsjon = soknadMedDelstegstatusOpprettet(engangsstonadSkjemanummer)
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

        soknadFarAdopsjon = testSoknadFarAdopsjon.hentAlternativRepresentasjon(SoeknadsskjemaEngangsstoenad.class);
    }

    private void lagFarOvertaOmsorg() {

        Map<String, String> periodeProperties = new HashMap<>();
        periodeProperties.put("land", "FRO");
        periodeProperties.put("fradato", "2016-10-06");
        periodeProperties.put("tildato", "2017-05-01");
        LocalDate treMaanederTilbakeITid = LocalDate.now().minusMonths(3);

        testSoknadFarOvertaOmsorg = soknadMedDelstegstatusOpprettet(engangsstonadSkjemanummer)
                .faktum("soknadsvalg.stonadstype").withValue(Stonadstyper.ENGANGSSTONAD_FAR).utforEndring()
                .faktum("soknadsvalg.fodselelleradopsjon").withValue("fodsel").utforEndring()

                .faktum("rettigheter.overtak").withValue("overtattOmsorgInnen53UkerFodsel").utforEndring()

                .faktum("tilknytningnorge.oppholder").withValue("true").utforEndring()
                .faktum("tilknytningnorge.tidligere").withValue("false").utforEndring()
                .nyttFaktum("tilknytningnorge.tidligere.periode").withProperties(periodeProperties).opprett()
                .faktum("tilknytningnorge.fremtidig").withValue("true").utforEndring()

                .faktum("barnet.antall").withValue("3").utforEndring()
                .faktum("barnet.dato").withValue(treMaanederTilbakeITid.toString()).utforEndring()

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

        soknadFarOvertaOmsorg = testSoknadFarOvertaOmsorg.hentAlternativRepresentasjon(SoeknadsskjemaEngangsstoenad.class);
    }

    private void lagUkomplett() {

        testSoknadUkomplett = soknadMedDelstegstatusOpprettet(engangsstonadSkjemanummer)

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
