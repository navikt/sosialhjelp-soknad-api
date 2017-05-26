package no.nav.sbl.dialogarena.integration;


import no.nav.sbl.dialogarena.sendsoknad.domain.kravdialoginformasjon.ForeldrepengerInformasjon;
import org.junit.Before;
import org.junit.Test;

public class ForeldrepengerEngangsstonadVedleggIT extends AbstractIT {
    private String foreldrepengerOrdinaerSkjemaNummer = new ForeldrepengerInformasjon().getSkjemanummer().get(0);

    @Before
    public void setup() throws Exception {
        EndpointDataMocking.setupMockWsEndpointData();
    }

    @Test
    public void skalHaP5VedleggVedRettigheterOvertakFodsel() {
        soknadMedDelstegstatusOpprettet(foreldrepengerOrdinaerSkjemaNummer)
                .faktum("soknadsvalg.fodselelleradopsjon").withValue("fodsel").utforEndring()
                .faktum("rettigheter.overtak").withValue("overtattOmsorgInnen53UkerFodsel").utforEndring()
                .hentPaakrevdeVedlegg()
                .skalHaVedleggMedSkjemaNummerTillegg("P5", "overtatt");
    }

    @Test
    public void skalHaP5VedleggVedRettigheterOvertakAdopsjon() {
        soknadMedDelstegstatusOpprettet(foreldrepengerOrdinaerSkjemaNummer)
                .faktum("soknadsvalg.fodselelleradopsjon").withValue("fodsel").utforEndring()
                .faktum("rettigheter.overtak").withValue("overtattOmsorgInnen53UkerAdopsjon").utforEndring()
                .hentPaakrevdeVedlegg()
                .skalHaVedleggMedSkjemaNummerTillegg("P5", "overtatt");
    }

    @Test
    public void skalHaP5VedleggVedAdopsjonJa() {
        soknadMedDelstegstatusOpprettet(foreldrepengerOrdinaerSkjemaNummer)
                .faktum("veiledning.adopsjon.overtakelse").withValue("ja").utforEndring()
                .hentPaakrevdeVedlegg()
                .skalHaVedleggMedSkjemaNummerTillegg("P5", "adopsjon");
    }

    @Test
    public void skalHaP5VedleggVedAdopsjonNei() {
        soknadMedDelstegstatusOpprettet(foreldrepengerOrdinaerSkjemaNummer)
                .faktum("veiledning.adopsjon.overtakelse").withValue("nei").utforEndring()
                .hentPaakrevdeVedlegg()
                .skalHaVedleggMedSkjemaNummerTillegg("P5", "adopsjon");
    }

    @Test
    public void skalHaP3VedleggVedTerminbekreftelseMor() {
        soknadMedDelstegstatusOpprettet(foreldrepengerOrdinaerSkjemaNummer)
                .faktum("veiledning.mor.terminbekreftelse").withValue("merEnn26Uker").utforEndring()
                .hentPaakrevdeVedlegg()
                .skalHaVedlegg("P3");
    }

}
