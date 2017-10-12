package no.nav.sbl.dialogarena.integration;

import no.nav.foreldrepenger.soeknadsskjema.engangsstoenad.v1.Innsendingsvalg;
import no.nav.foreldrepenger.soeknadsskjema.engangsstoenad.v1.SoeknadsskjemaEngangsstoenad;
import no.nav.foreldrepenger.soeknadsskjema.engangsstoenad.v1.Vedlegg;
import no.nav.sbl.dialogarena.config.IntegrationConfig;
import no.nav.sbl.dialogarena.sendsoknad.domain.Vedlegg.Status;
import no.nav.sbl.dialogarena.sendsoknad.domain.kravdialoginformasjon.ForeldrepengerInformasjon;
import no.nav.sbl.dialogarena.sendsoknad.domain.message.NavMessageSource;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import java.util.List;
import java.util.Locale;
import java.util.function.Function;

import static java.util.stream.Collectors.groupingBy;
import static java.util.stream.Collectors.reducing;
import static no.nav.sbl.dialogarena.sendsoknad.domain.transformer.foreldrepenger.engangsstonad.Skjemanummer.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

public class AlternativRepresentasjonForeldrepengerVedleggIT extends AbstractIT {

    private String engangsstonadAdopsjonSkjemanummer = new ForeldrepengerInformasjon().getSkjemanummer().get(1);

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
                .vedlegg(N6).withInnsendingsValg(Status.LastetOpp).utforEndring().hentPaakrevdeVedlegg()
                .vedlegg(M6).withInnsendingsValg(Status.VedleggAlleredeSendt).utforEndring().hentPaakrevdeVedlegg()
                .vedlegg(K4).withInnsendingsValg(Status.VedleggSendesIkke).withAarsak("aarsak").utforEndring().hentPaakrevdeVedlegg()
                .vedlegg(L4).withInnsendingsValg(Status.VedleggSendesAvAndre).utforEndring()
                .hentAlternativRepresentasjon(SoeknadsskjemaEngangsstoenad.class);

        assertThat(soknad.getVedleggListe().getVedleggs()).extracting("skjemanummer").contains(
                N6.dokumentTypeId(),
                M6.dokumentTypeId(),
                K4.dokumentTypeId(),
                L4.dokumentTypeId()
        );

        soknad.getVedleggListe().getVedleggs().forEach(xmlVedlegg -> {
            if (N6.dokumentTypeId().equals(xmlVedlegg.getSkjemanummer())) {
                assertThat(xmlVedlegg.getInnsendingsvalg()).isEqualTo(Innsendingsvalg.LASTET_OPP);
                assertThat(xmlVedlegg.isErPaakrevdISoeknadsdialog()).isFalse();
            }
            if (M6.dokumentTypeId().equals(xmlVedlegg.getSkjemanummer())) {
                assertThat(xmlVedlegg.getInnsendingsvalg()).isEqualTo(Innsendingsvalg.VEDLEGG_ALLEREDE_SENDT);
                assertThat(xmlVedlegg.isErPaakrevdISoeknadsdialog()).isTrue();
            }
            if (K4.dokumentTypeId().equals(xmlVedlegg.getSkjemanummer())) {
                assertThat(xmlVedlegg.getInnsendingsvalg()).isEqualTo(Innsendingsvalg.SENDES_IKKE);
                assertThat(xmlVedlegg.getTilleggsinfo()).isEqualTo("aarsak");
                assertThat(xmlVedlegg.isErPaakrevdISoeknadsdialog()).isTrue();
            }
            if (L4.dokumentTypeId().equals(xmlVedlegg.getSkjemanummer())) {
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
                .vedlegg(T8).withInnsendingsValg(Status.LastetOpp).utforEndring()
                .hentAlternativRepresentasjon(SoeknadsskjemaEngangsstoenad.class);

        assertThat(soknad.getVedleggListe().getVedleggs()).extracting("skjemanummer").contains(
                T8.dokumentTypeId()
        );
    }

    @Test
    public void morTerminBekreftelse() {
        SoeknadsskjemaEngangsstoenad soknad = soknadMedDelstegstatusOpprettet(engangsstonadAdopsjonSkjemanummer)
                .faktum("veiledning.mor.terminbekreftelse").withValue("merEnn26Uker").withProperty("skalHaFodselsattest", "true").utforEndring()
                .hentPaakrevdeVedlegg()
                .vedlegg(P3).withInnsendingsValg(Status.LastetOpp).utforEndring()
                .hentPaakrevdeVedlegg()
                .vedlegg(R4).withInnsendingsValg(Status.LastetOpp).utforEndring()
                .hentPaakrevdeVedlegg()
                .soknad()
                .hentAlternativRepresentasjon(SoeknadsskjemaEngangsstoenad.class);

        assertThat(soknad.getVedleggListe().getVedleggs()).extracting("skjemanummer").contains(
                P3.dokumentTypeId(),
                R4.dokumentTypeId()
        );
    }

    @Test
    public void adopsjonOvertakelse() {
        SoeknadsskjemaEngangsstoenad soknad = soknadMedDelstegstatusOpprettet(engangsstonadAdopsjonSkjemanummer)
                .faktum("veiledning.adopsjon.overtakelse").withValue("ja").utforEndring()
                .hentPaakrevdeVedlegg()
                .vedlegg(P5).withInnsendingsValg(Status.LastetOpp).utforEndring()
                .hentAlternativRepresentasjon(SoeknadsskjemaEngangsstoenad.class);

        assertThat(soknad.getVedleggListe().getVedleggs()).hasSize(1);
        assertThat(soknad.getVedleggListe().getVedleggs()).extracting("skjemanummer").contains(P5.dokumentTypeId());
    }

    @Test
    public void egennaring() {
        SoeknadsskjemaEngangsstoenad soknad = soknadMedDelstegstatusOpprettet(engangsstonadAdopsjonSkjemanummer)
                .faktum("egennaering.arbeidegennaering").withValue("false").utforEndring()
                .nyttFaktum("naeringsvirksomhet").withValue("false").withProperty("regnskapsforer", "true").withParentFaktum("egennaering.arbeidegennaering").opprett()
                .hentPaakrevdeVedlegg()
                .vedlegg(T7).withInnsendingsValg(Status.LastetOpp).utforEndring()
                .hentAlternativRepresentasjon(SoeknadsskjemaEngangsstoenad.class);

        assertThat(soknad.getVedleggListe().getVedleggs()).hasSize(1);
        assertThat(soknad.getVedleggListe().getVedleggs()).extracting("skjemanummer").contains(T7.dokumentTypeId());
    }

    @Test
    public void fedrekvoteSykdom() {
        SoeknadsskjemaEngangsstoenad soknad = soknadMedDelstegstatusOpprettet(engangsstonadAdopsjonSkjemanummer)
                .faktum("fedrekvote").withValue("sykdom").utforEndring()
                .hentPaakrevdeVedlegg()
                .vedlegg(L9).withInnsendingsValg(Status.LastetOpp).utforEndring()
                .hentAlternativRepresentasjon(SoeknadsskjemaEngangsstoenad.class);

        assertThat(soknad.getVedleggListe().getVedleggs()).hasSize(1);
        assertThat(soknad.getVedleggListe().getVedleggs()).extracting("skjemanummer").contains(L9.dokumentTypeId());
    }

    @Test
    public void fedrekvoteHelseinstitusjon() {
        SoeknadsskjemaEngangsstoenad soknad = soknadMedDelstegstatusOpprettet(engangsstonadAdopsjonSkjemanummer)
                .faktum("fedrekvote").withValue("helseinstitusjon").utforEndring()
                .hentPaakrevdeVedlegg()
                .vedlegg(N9).withInnsendingsValg(Status.LastetOpp).utforEndring()
                .hentPaakrevdeVedlegg().soknad()
                .hentAlternativRepresentasjon(SoeknadsskjemaEngangsstoenad.class);

        assertThat(soknad.getVedleggListe().getVedleggs()).hasSize(1);
        assertThat(soknad.getVedleggListe().getVedleggs()).extracting("skjemanummer").contains(N9.dokumentTypeId());
    }

    @Test
    public void perioderGraderingsskjema() {
        SoeknadsskjemaEngangsstoenad soknad = soknadMedDelstegstatusOpprettet(engangsstonadAdopsjonSkjemanummer)
                .faktum("vedlegg.hjelpefaktum").withValue("true").withProperty("graderingsskjema", "true").utforEndring()
                .hentPaakrevdeVedlegg()
                .vedlegg(H1).withInnsendingsValg(Status.LastetOpp).utforEndring()
                .hentAlternativRepresentasjon(SoeknadsskjemaEngangsstoenad.class);

        assertThat(soknad.getVedleggListe().getVedleggs()).hasSize(1);
        assertThat(soknad.getVedleggListe().getVedleggs()).extracting("skjemanummer").contains(H1.dokumentTypeId());
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
                .vedlegg(Z6).withInnsendingsValg(Status.LastetOpp).utforEndring()
                .hentAlternativRepresentasjon(SoeknadsskjemaEngangsstoenad.class);

        assertThat(soknad.getVedleggListe().getVedleggs()).hasSize(1);
        assertThat(soknad.getVedleggListe().getVedleggs()).extracting("skjemanummer").contains(Z6.dokumentTypeId());
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
                .vedlegg(O9).withInnsendingsValg(Status.LastetOpp).utforEndring()
                .hentAlternativRepresentasjon(SoeknadsskjemaEngangsstoenad.class);

        assertThat(soknad.getVedleggListe().getVedleggs()).hasSize(1);
        assertThat(soknad.getVedleggListe().getVedleggs()).extracting("skjemanummer").contains(O9.dokumentTypeId());
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
                .vedlegg(T1).withInnsendingsValg(Status.LastetOpp).utforEndring()
                .hentAlternativRepresentasjon(SoeknadsskjemaEngangsstoenad.class);

        assertThat(soknad.getVedleggListe().getVedleggs()).hasSize(1);
        assertThat(soknad.getVedleggListe().getVedleggs()).extracting("skjemanummer").contains(T1.dokumentTypeId());
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
                .vedlegg(K3).withInnsendingsValg(Status.LastetOpp).utforEndring()
                .hentAlternativRepresentasjon(SoeknadsskjemaEngangsstoenad.class);

        assertThat(soknad.getVedleggListe().getVedleggs()).hasSize(1);
        assertThat(soknad.getVedleggListe().getVedleggs()).extracting("skjemanummer").contains(K3.dokumentTypeId());
    }

    @Test
    public void ytelserVartpenger() {
        SoeknadsskjemaEngangsstoenad soknad = soknadMedDelstegstatusOpprettet(engangsstonadAdopsjonSkjemanummer)
                .faktum("ytelser.vartpenger").withValue("true").utforEndring()
                .hentPaakrevdeVedlegg()
                .vedlegg(K1).withInnsendingsValg(Status.LastetOpp).utforEndring()
                .hentAlternativRepresentasjon(SoeknadsskjemaEngangsstoenad.class);

        assertThat(soknad.getVedleggListe().getVedleggs()).hasSize(1);
        assertThat(soknad.getVedleggListe().getVedleggs()).extracting("skjemanummer").contains(K1.dokumentTypeId());
    }

    @Test
    public void ytelserAvtjening() {
        SoeknadsskjemaEngangsstoenad soknad = soknadMedDelstegstatusOpprettet(engangsstonadAdopsjonSkjemanummer)
                .faktum("ytelser.avtjening").withValue("true").utforEndring()
                .hentPaakrevdeVedlegg()
                .vedlegg(O5).withInnsendingsValg(Status.LastetOpp).utforEndring()
                .hentAlternativRepresentasjon(SoeknadsskjemaEngangsstoenad.class);

        assertThat(soknad.getVedleggListe().getVedleggs()).hasSize(1);
        assertThat(soknad.getVedleggListe().getVedleggs()).extracting("skjemanummer").contains(O5.dokumentTypeId());
    }

    @Test
    public void ytelserSluttpakke() {
        SoeknadsskjemaEngangsstoenad soknad = soknadMedDelstegstatusOpprettet(engangsstonadAdopsjonSkjemanummer)
                .faktum("ytelser.sluttpakke").withValue("true").utforEndring()
                .hentPaakrevdeVedlegg()
                .vedlegg(Y4).withInnsendingsValg(Status.LastetOpp).utforEndring()
                .hentPaakrevdeVedlegg()
                .soknad()
                .hentAlternativRepresentasjon(SoeknadsskjemaEngangsstoenad.class);

        assertThat(soknad.getVedleggListe().getVedleggs()).hasSize(1);
        assertThat(soknad.getVedleggListe().getVedleggs()).extracting("skjemanummer").contains(Y4.dokumentTypeId());
    }

    @Test
    public void skalSetteTilleggsinfoForVedleggSomIkkeErLastetOpp() {
        SoknadTester soknadTester = soknadMedDelstegstatusOpprettet(engangsstonadAdopsjonSkjemanummer)
                .faktum("ytelser.sluttpakke").withValue("true").utforEndring()
                .hentPaakrevdeVedlegg()
                .vedlegg(Y4).withInnsendingsValg(Status.LastetOpp).utforEndring()
                .hentPaakrevdeVedlegg()
                .soknad();

        SoeknadsskjemaEngangsstoenad soknad = soknadTester.hentAlternativRepresentasjon(SoeknadsskjemaEngangsstoenad.class);
        assertThat(singleVedlegg(soknad).getTilleggsinfo()).isNull();

        soknad = soknadTester.hentPaakrevdeVedlegg()
                .vedlegg(Y4).withInnsendingsValg(Status.VedleggSendesIkke).withAarsak("aarsak").utforEndring()
                .hentAlternativRepresentasjon(SoeknadsskjemaEngangsstoenad.class);
        assertThat(singleVedlegg(soknad).getTilleggsinfo()).isEqualTo("aarsak");

        soknad = soknadTester.hentPaakrevdeVedlegg()
                .vedlegg(Y4).withInnsendingsValg(Status.VedleggSendesAvAndre).withAarsak("aarsak").utforEndring()
                .hentAlternativRepresentasjon(SoeknadsskjemaEngangsstoenad.class);
        assertThat(singleVedlegg(soknad).getTilleggsinfo()).isEqualTo("aarsak");

        soknad = soknadTester.hentPaakrevdeVedlegg()
                .vedlegg(Y4).withInnsendingsValg(Status.VedleggAlleredeSendt).withAarsak("aarsak").utforEndring()
                .hentAlternativRepresentasjon(SoeknadsskjemaEngangsstoenad.class);
        assertThat(singleVedlegg(soknad).getTilleggsinfo()).isEqualTo("aarsak");

        soknad = soknadTester.hentPaakrevdeVedlegg()
                .vedlegg(Y4).withInnsendingsValg(Status.SendesSenere).withAarsak("aarsak").utforEndring()
                .hentAlternativRepresentasjon(SoeknadsskjemaEngangsstoenad.class);
        assertThat(singleVedlegg(soknad).getTilleggsinfo()).isEqualTo("aarsak");
    }

    private Vedlegg singleVedlegg(SoeknadsskjemaEngangsstoenad soknad) {
        List<Vedlegg> vedlegg = soknad.getVedleggListe().getVedleggs();
        assertThat(vedlegg).hasSize(1);
        return soknad.getVedleggListe().getVedleggs().get(0);
    }
}
