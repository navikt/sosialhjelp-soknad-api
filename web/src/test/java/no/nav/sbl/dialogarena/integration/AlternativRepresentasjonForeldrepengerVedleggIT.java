package no.nav.sbl.dialogarena.integration;

import no.nav.melding.virksomhet.soeknadsskjemaengangsstoenad.v1.Innsendingsvalg;
import no.nav.melding.virksomhet.soeknadsskjemaengangsstoenad.v1.SoeknadsskjemaEngangsstoenad;
import no.nav.sbl.dialogarena.config.IntegrationConfig;
import no.nav.sbl.dialogarena.sendsoknad.domain.Vedlegg;
import no.nav.sbl.dialogarena.sendsoknad.domain.kravdialoginformasjon.ForeldrepengerInformasjon;
import no.nav.sbl.dialogarena.sendsoknad.domain.message.NavMessageSource;
import no.nav.sbl.dialogarena.sendsoknad.domain.transformer.foreldrepenger.engangsstonad.DokumentTypeId;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import java.util.Locale;
import java.util.stream.Collector;
import java.util.stream.Collectors;

import static java.util.stream.Collectors.groupingBy;
import static java.util.stream.Collectors.reducing;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

public class AlternativRepresentasjonForeldrepengerVedleggIT extends AbstractIT {

    private String engangsstonadAdopsjonSkjemanummer = new ForeldrepengerInformasjon().getSkjemanummer().get(4);

    @Before
    public void setup() throws Exception {
        EndpointDataMocking.setupMockWsEndpointData();
        NavMessageSource navMessageSourceMock = IntegrationConfig.getMocked("navMessageSource");
        when(
                navMessageSourceMock.finnTekst(Mockito.anyString(), Mockito.any(), Mockito.any(Locale.class))
        ).thenReturn("whatever");

    }

    @Test
    public void innsendingsvalgOgErPaakrevOgAarsak() {
        SoeknadsskjemaEngangsstoenad soknad = soknadMedDelstegstatusOpprettet(engangsstonadAdopsjonSkjemanummer)
                .nyttFaktum("ekstraVedlegg").withValue("true").opprett()
                .faktum("arbeidsforhold.yrkesaktiv").withValue("false").utforEndring()
                .nyttFaktum("arbeidsforhold")
                    .withValue("true")
                    .withProperty("stillingstype", "variabel")
                    .withProperty("ansatt", "true")
                    .withParentFaktum("arbeidsforhold.yrkesaktiv")
                    .opprett()
                .hentPaakrevdeVedlegg()
                .vedlegg("N6").withInnsendingsValg(Vedlegg.Status.LastetOpp).utforEndring().hentPaakrevdeVedlegg()
                .vedlegg("M6").withInnsendingsValg(Vedlegg.Status.VedleggAlleredeSendt).utforEndring().hentPaakrevdeVedlegg()
                .vedlegg("K4").withInnsendingsValg(Vedlegg.Status.VedleggSendesIkke).withAarsak("aarsak").utforEndring().hentPaakrevdeVedlegg()
                .vedlegg("L4").withInnsendingsValg(Vedlegg.Status.VedleggSendesAvAndre).utforEndring()
                .hentAlternativRepresentasjon(SoeknadsskjemaEngangsstoenad.class);

        assertThat(soknad.getVedleggListes()).extracting("skjemanummer").contains(
                DokumentTypeId.get("N6"),
                DokumentTypeId.get("M6"),
                DokumentTypeId.get("K4"),
                DokumentTypeId.get("L4")
        );

        soknad.getVedleggListes().forEach(xmlVedlegg -> {
            if (DokumentTypeId.get("N6").equals(xmlVedlegg.getSkjemanummer())) {
                assertThat(xmlVedlegg.getInnsendingsvalg()).isEqualTo(Innsendingsvalg.LASTET_OPP);
                assertThat(xmlVedlegg.isErPaakrevdISoeknadsdialog()).isFalse();
            }
            if (DokumentTypeId.get("M6").equals(xmlVedlegg.getSkjemanummer())) {
                assertThat(xmlVedlegg.getInnsendingsvalg()).isEqualTo(Innsendingsvalg.VEDLEGG_ALLEREDE_SENDT);
                assertThat(xmlVedlegg.isErPaakrevdISoeknadsdialog()).isTrue();
            }
            if (DokumentTypeId.get("K4").equals(xmlVedlegg.getSkjemanummer())) {
                assertThat(xmlVedlegg.getInnsendingsvalg()).isEqualTo(Innsendingsvalg.SENDES_IKKE);
                assertThat(xmlVedlegg.getTilleggsinfo()).isEqualTo("aarsak");
                assertThat(xmlVedlegg.isErPaakrevdISoeknadsdialog()).isTrue();
            }
            if (DokumentTypeId.get("L4").equals(xmlVedlegg.getSkjemanummer())) {
                assertThat(xmlVedlegg.getInnsendingsvalg()).isEqualTo(Innsendingsvalg.VEDLEGG_SENDES_AV_ANDRE);
                assertThat(xmlVedlegg.isErPaakrevdISoeknadsdialog()).isTrue();
            }
        });
    }

    @Test
    public void yrkesaktivArbeidsForholdEDAG() {
        SoeknadsskjemaEngangsstoenad soknad = soknadMedDelstegstatusOpprettet(engangsstonadAdopsjonSkjemanummer)
                .faktum("arbeidsforhold.yrkesaktiv").withValue("false").utforEndring()
                .nyttFaktum("arbeidsforhold")
                .withValue("EDAG")
                .withProperty("stillingstype", "variabel")
                .withProperty("ansatt", "false")
                .withParentFaktum("arbeidsforhold.yrkesaktiv")
                .opprett()
                .hentPaakrevdeVedlegg()
                .vedlegg("T8").withInnsendingsValg(Vedlegg.Status.LastetOpp).utforEndring()
                .hentAlternativRepresentasjon(SoeknadsskjemaEngangsstoenad.class);

        assertThat(soknad.getVedleggListes()).extracting("skjemanummer").contains(
                DokumentTypeId.get("T8")
        );
    }

    @Test
    public void morTerminBekreftelse() {
        SoeknadsskjemaEngangsstoenad soknad = soknadMedDelstegstatusOpprettet(engangsstonadAdopsjonSkjemanummer)
                .faktum("veiledning.mor.terminbekreftelse").withValue("merEnn26Uker").withProperty("skalHaFodselsattest", "true").utforEndring()
                .hentPaakrevdeVedlegg()
                .vedlegg("P3").withInnsendingsValg(Vedlegg.Status.LastetOpp).utforEndring()
                .hentPaakrevdeVedlegg()
                .vedlegg("R4").withInnsendingsValg(Vedlegg.Status.LastetOpp).utforEndring()
                .hentPaakrevdeVedlegg()
                .soknad()
                .hentAlternativRepresentasjon(SoeknadsskjemaEngangsstoenad.class);

        assertThat(soknad.getVedleggListes()).extracting("skjemanummer").contains(
                DokumentTypeId.get("P3"),
                DokumentTypeId.get("R4")
        );
    }

    @Test
    public void adopsjonOvertakelse() {
        SoeknadsskjemaEngangsstoenad soknad = soknadMedDelstegstatusOpprettet(engangsstonadAdopsjonSkjemanummer)
                .faktum("veiledning.adopsjon.overtakelse").withValue("ja").utforEndring()
                .hentPaakrevdeVedlegg()
                .vedlegg("P5").withInnsendingsValg(Vedlegg.Status.LastetOpp).utforEndring()
                .hentAlternativRepresentasjon(SoeknadsskjemaEngangsstoenad.class);

        assertThat(soknad.getVedleggListes()).hasSize(1);
        assertThat(soknad.getVedleggListes()).extracting("skjemanummer").contains(DokumentTypeId.get("P5"));
    }

    @Test
    public void egennaring() {
        SoeknadsskjemaEngangsstoenad soknad = soknadMedDelstegstatusOpprettet(engangsstonadAdopsjonSkjemanummer)
                .faktum("egennaering.arbeidegennaering").withValue("false").utforEndring()
                .nyttFaktum("naeringsvirksomhet").withValue("false").withProperty("regnskapsforer", "true").withParentFaktum("egennaering.arbeidegennaering").opprett()
                .hentPaakrevdeVedlegg()
                .vedlegg("T7").withInnsendingsValg(Vedlegg.Status.LastetOpp).utforEndring()
                .hentAlternativRepresentasjon(SoeknadsskjemaEngangsstoenad.class);

        assertThat(soknad.getVedleggListes()).hasSize(1);
        assertThat(soknad.getVedleggListes()).extracting("skjemanummer").contains(DokumentTypeId.get("T7"));
    }

    @Test
    public void fedrekvoteSykdom() {
        SoeknadsskjemaEngangsstoenad soknad = soknadMedDelstegstatusOpprettet(engangsstonadAdopsjonSkjemanummer)
                .faktum("fedrekvote").withValue("sykdom").utforEndring()
                .hentPaakrevdeVedlegg()
                .vedlegg("L9").withInnsendingsValg(Vedlegg.Status.LastetOpp).utforEndring()
                .hentAlternativRepresentasjon(SoeknadsskjemaEngangsstoenad.class);

        assertThat(soknad.getVedleggListes()).hasSize(1);
        assertThat(soknad.getVedleggListes()).extracting("skjemanummer").contains(DokumentTypeId.get("L9"));
    }

    @Test
    public void fedrekvoteHelseinstitusjon() {
        SoeknadsskjemaEngangsstoenad soknad = soknadMedDelstegstatusOpprettet(engangsstonadAdopsjonSkjemanummer)
                .faktum("fedrekvote").withValue("helseinstitusjon").utforEndring()
                .hentPaakrevdeVedlegg()
                .vedlegg("N9").withInnsendingsValg(Vedlegg.Status.LastetOpp).utforEndring()
                .hentPaakrevdeVedlegg().soknad()
                .hentAlternativRepresentasjon(SoeknadsskjemaEngangsstoenad.class);

        assertThat(soknad.getVedleggListes()).hasSize(1);
        assertThat(soknad.getVedleggListes()).extracting("skjemanummer").contains(DokumentTypeId.get("N9"));
    }

    @Test
    public void perioderGraderingsskjema() {
        SoeknadsskjemaEngangsstoenad soknad = soknadMedDelstegstatusOpprettet(engangsstonadAdopsjonSkjemanummer)
                .faktum("vedlegg.hjelpefaktum").withValue("true").withProperty("graderingsskjema", "true").utforEndring()
                .hentPaakrevdeVedlegg()
                .vedlegg("H1").withInnsendingsValg(Vedlegg.Status.LastetOpp).utforEndring()
                .hentAlternativRepresentasjon(SoeknadsskjemaEngangsstoenad.class);

        assertThat(soknad.getVedleggListes()).hasSize(1);
        assertThat(soknad.getVedleggListes()).extracting("skjemanummer").contains(DokumentTypeId.get("H1"));
    }

    @Test
    public void perioderTidsromUtsettelser() {
        SoeknadsskjemaEngangsstoenad soknad = soknadMedDelstegstatusOpprettet(engangsstonadAdopsjonSkjemanummer)
                .nyttFaktum("perioder.opphold").withValue("true").opprett()
                .nyttFaktum("perioder.tidsrom.utsettelse")
                    .withParentFaktum("perioder.opphold")
                    .withValue("true")
                    .withProperty("ferie", "true")
                    .withProperty("type", "ferie")
                    .opprett()
                .hentPaakrevdeVedlegg()
                .vedlegg("Z6").withInnsendingsValg(Vedlegg.Status.LastetOpp).utforEndring()
                .hentAlternativRepresentasjon(SoeknadsskjemaEngangsstoenad.class);

        assertThat(soknad.getVedleggListes()).hasSize(1);
        assertThat(soknad.getVedleggListes()).extracting("skjemanummer").contains(DokumentTypeId.get("Z6"));
    }

    @Test
    public void perioderMorsaktivitetStudier() {
        SoeknadsskjemaEngangsstoenad soknad = soknadMedDelstegstatusOpprettet(engangsstonadAdopsjonSkjemanummer)
                .nyttFaktum("perioder.morsaktivitet").withValue("true")
                    .withValue("true")
                    .withProperty("type", "studier")
                    .withProperty("studier", "true")
                    .opprett()
                .hentPaakrevdeVedlegg()
                .vedlegg("O9").withInnsendingsValg(Vedlegg.Status.LastetOpp).utforEndring()
                .hentAlternativRepresentasjon(SoeknadsskjemaEngangsstoenad.class);

        assertThat(soknad.getVedleggListes()).hasSize(1);
        assertThat(soknad.getVedleggListes()).extracting("skjemanummer").contains(DokumentTypeId.get("O9"));
    }

    @Test
    public void perioderMorsaktivitIntroduksjonsprogram() {
        SoeknadsskjemaEngangsstoenad soknad = soknadMedDelstegstatusOpprettet(engangsstonadAdopsjonSkjemanummer)
                .nyttFaktum("perioder.morsaktivitet").withValue("true")
                .withValue("true")
                .withProperty("type", "introduksjonsprogram")
                .withProperty("introduksjonsprogram", "true")
                .opprett()
                .hentPaakrevdeVedlegg()
                .vedlegg("T1").withInnsendingsValg(Vedlegg.Status.LastetOpp).utforEndring()
                .hentAlternativRepresentasjon(SoeknadsskjemaEngangsstoenad.class);

        assertThat(soknad.getVedleggListes()).hasSize(1);
        assertThat(soknad.getVedleggListes()).extracting("skjemanummer").contains(DokumentTypeId.get("T1"));
    }

    @Test
    public void perioderMorsaktivitKvalifiseringsprogram() {
        SoeknadsskjemaEngangsstoenad soknad = soknadMedDelstegstatusOpprettet(engangsstonadAdopsjonSkjemanummer)
                .nyttFaktum("perioder.morsaktivitet").withValue("true")
                .withValue("true")
                .withProperty("type", "kvalifiseringsprogram")
                .withProperty("kvalifiseringsprogram", "true")
                .opprett()
                .hentPaakrevdeVedlegg()
                .vedlegg("K3").withInnsendingsValg(Vedlegg.Status.LastetOpp).utforEndring()
                .hentAlternativRepresentasjon(SoeknadsskjemaEngangsstoenad.class);

        assertThat(soknad.getVedleggListes()).hasSize(1);
        assertThat(soknad.getVedleggListes()).extracting("skjemanummer").contains(DokumentTypeId.get("K3"));
    }

    @Test
    public void ytelserVartpenger() {
        SoeknadsskjemaEngangsstoenad soknad = soknadMedDelstegstatusOpprettet(engangsstonadAdopsjonSkjemanummer)
                .faktum("ytelser.vartpenger").withValue("true").utforEndring()
                .hentPaakrevdeVedlegg()
                .vedlegg("K1").withInnsendingsValg(Vedlegg.Status.LastetOpp).utforEndring()
                .hentAlternativRepresentasjon(SoeknadsskjemaEngangsstoenad.class);

        assertThat(soknad.getVedleggListes()).hasSize(1);
        assertThat(soknad.getVedleggListes()).extracting("skjemanummer").contains(DokumentTypeId.get("K1"));
    }

    @Test
    public void ytelserAvtjening() {
        SoeknadsskjemaEngangsstoenad soknad = soknadMedDelstegstatusOpprettet(engangsstonadAdopsjonSkjemanummer)
                .faktum("ytelser.avtjening").withValue("true").utforEndring()
                .hentPaakrevdeVedlegg()
                .vedlegg("O5").withInnsendingsValg(Vedlegg.Status.LastetOpp).utforEndring()
                .hentAlternativRepresentasjon(SoeknadsskjemaEngangsstoenad.class);

        assertThat(soknad.getVedleggListes()).hasSize(1);
        assertThat(soknad.getVedleggListes()).extracting("skjemanummer").contains(DokumentTypeId.get("O5"));
    }

    @Test
    public void ytelserSluttpakke() {
        SoeknadsskjemaEngangsstoenad soknad = soknadMedDelstegstatusOpprettet(engangsstonadAdopsjonSkjemanummer)
                .faktum("ytelser.sluttpakke").withValue("true").utforEndring()
                .hentPaakrevdeVedlegg()
                .vedlegg("Y4").withInnsendingsValg(Vedlegg.Status.LastetOpp).utforEndring()
                .hentPaakrevdeVedlegg()
                .soknad()
                .hentAlternativRepresentasjon(SoeknadsskjemaEngangsstoenad.class);

        assertThat(soknad.getVedleggListes()).hasSize(1);
        assertThat(soknad.getVedleggListes()).extracting("skjemanummer").contains(DokumentTypeId.get("Y4"));
    }
}
