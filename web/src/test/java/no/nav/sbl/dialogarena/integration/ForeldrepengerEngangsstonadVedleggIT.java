package no.nav.sbl.dialogarena.integration;


import no.nav.sbl.dialogarena.sendsoknad.domain.kravdialoginformasjon.ForeldrepengerInformasjon;
import no.nav.sbl.dialogarena.sendsoknad.domain.transformer.foreldrepenger.engangsstonad.Stonadstyper;
import org.junit.Before;
import org.junit.Test;

public class ForeldrepengerEngangsstonadVedleggIT extends AbstractIT {
    private String foreldrepengerOrdinaerSkjemaNummer = new ForeldrepengerInformasjon().getSkjemanummer().get(0);

    @Before
    public void setup() throws Exception {
        EndpointDataMocking.setupMockWsEndpointData();
    }

    @Test
    public void skalHaP5VedleggVedOvertattOmsorgFodsel() {
        soknadMedDelstegstatusOpprettet(foreldrepengerOrdinaerSkjemaNummer)
                .faktum("soknadsvalg.fodselelleradopsjon").withValue("fodsel").utforEndring()
                .faktum("soknadsvalg.stonadstype").withValue(Stonadstyper.ENGANGSSTONAD_FAR).utforEndring()
                .faktum("rettigheter.overtak").withValue("overtattOmsorgInnen53UkerFodsel").utforEndring()
                .hentPaakrevdeVedlegg()
                .skalHaVedleggMedSkjemaNummerTillegg("P5", "overtatt");
    }

    @Test
    public void skalHaP5VedleggVedOvertattOmsorgAdopsjon() {
        soknadMedDelstegstatusOpprettet(foreldrepengerOrdinaerSkjemaNummer)
                .faktum("soknadsvalg.fodselelleradopsjon").withValue("adopsjon").utforEndring()
                .faktum("soknadsvalg.stonadstype").withValue(Stonadstyper.ENGANGSSTONAD_FAR).utforEndring()
                .faktum("rettigheter.overtak").withValue("overtattOmsorgInnen53UkerAdopsjon").utforEndring()
                .hentPaakrevdeVedlegg()
                .skalHaVedleggMedSkjemaNummerTillegg("P5", "overtatt");
    }

    @Test
    public void skalHaP5VedleggVedAdopsjonBekreftetOvertakelse() {
        soknadMedDelstegstatusOpprettet(foreldrepengerOrdinaerSkjemaNummer)
                .faktum("veiledning.adopsjon.overtakelse").withValue("ja").utforEndring()
                .hentPaakrevdeVedlegg()
                .skalHaVedleggMedSkjemaNummerTillegg("P5", "adopsjon");
    }

    @Test
    public void skalHaP5VedleggVedAdopsjonIkkeBekreftetOvertakelse() {
        soknadMedDelstegstatusOpprettet(foreldrepengerOrdinaerSkjemaNummer)
                .faktum("veiledning.adopsjon.overtakelse").withValue("nei").utforEndring()
                .hentPaakrevdeVedlegg()
                .skalHaVedleggMedSkjemaNummerTillegg("P5", "adopsjon");
    }

    @Test
    public void skalHaP3VedleggVedTerminbekreftelseMerEnn26UkerMor() {
        soknadMedDelstegstatusOpprettet(foreldrepengerOrdinaerSkjemaNummer)
                .faktum("veiledning.mor.terminbekreftelse").withValue("merEnn26Uker").utforEndring()
                .hentPaakrevdeVedlegg()
                .skalHaVedlegg("P3");
    }

    @Test
    public void skalHaP3VedleggVedTerminbekreftelseMindreEnn26UkerMor() {
        soknadMedDelstegstatusOpprettet(foreldrepengerOrdinaerSkjemaNummer)
                .faktum("veiledning.mor.terminbekreftelse").withValue("mindreEnn26Uker").utforEndring()
                .hentPaakrevdeVedlegg()
                .skalHaVedlegg("P3");
    }

}
