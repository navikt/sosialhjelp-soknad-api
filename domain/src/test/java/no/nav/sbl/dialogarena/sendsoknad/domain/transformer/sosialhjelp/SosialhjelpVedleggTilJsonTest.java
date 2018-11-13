package no.nav.sbl.dialogarena.sendsoknad.domain.transformer.sosialhjelp;

import no.nav.sbl.dialogarena.sendsoknad.domain.AlternativRepresentasjon;
import no.nav.sbl.dialogarena.sendsoknad.domain.Faktum;
import no.nav.sbl.dialogarena.sendsoknad.domain.Vedlegg;
import no.nav.sbl.dialogarena.sendsoknad.domain.WebSoknad;
import no.nav.sbl.dialogarena.sendsoknad.domain.transformer.AlternativRepresentasjonType;
import no.nav.sbl.dialogarena.sendsoknad.domain.util.ServiceUtils;
import no.nav.sbl.soknadsosialhjelp.vedlegg.JsonVedlegg;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

import static junit.framework.TestCase.assertTrue;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.isEmptyString;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;

public class SosialhjelpVedleggTilJsonTest {

    private SosialhjelpVedleggTilJson sosialhjelpVedleggTilJson;
    private WebSoknad soknad;
    private byte[] data;

    @Before
    public void setup() {
        sosialhjelpVedleggTilJson = new SosialhjelpVedleggTilJson();
        soknad = opprettWebSoknadUtenVedlegg().medVedlegg(
                        new Vedlegg().medSkjemaNummer("V1").medSkjemanummerTillegg("BARN")
                                .medFaktumId(2L).medFilnavn("fil1.png").medInnsendingsvalg(Vedlegg.Status.LastetOpp),
                        new Vedlegg().medSkjemaNummer("V1").medSkjemanummerTillegg("BARN")
                                .medFaktumId(3L).medFilnavn("fil2.png").medInnsendingsvalg(Vedlegg.Status.LastetOpp),
                        new Vedlegg().medSkjemaNummer("V1").medSkjemanummerTillegg("ANNET")
                                .medFaktumId(5L).medFilnavn("fil3.png").medInnsendingsvalg(Vedlegg.Status.LastetOpp),
                        new Vedlegg().medSkjemaNummer("V2").medSkjemanummerTillegg("YOYO")
                                .medFaktumId(7L).medFilnavn("fil4.png").medInnsendingsvalg(Vedlegg.Status.SendesSenere)
                );

        final String pathToDir = "src/test/java/no/nav/sbl/dialogarena/sendsoknad/domain";
        final Path dokument = Paths.get(pathToDir + "/soknad.pdf");
        try {
            data = Files.readAllBytes(dokument);
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    @Test
    public void opprettJsonVedleggFraWebSoknadInkludererVedleggSomAlleredeErSendt() {
        WebSoknad soknad = opprettWebSoknadUtenVedlegg();
        soknad.medVedlegg(new Vedlegg().medSkjemaNummer("V1").medSkjemanummerTillegg("BARN")
                .medFaktumId(2L).medInnsendingsvalg(Vedlegg.Status.VedleggAlleredeSendt));

        List<JsonVedlegg> jsonVedlegg = sosialhjelpVedleggTilJson.opprettJsonVedleggFraWebSoknad(soknad);

        assertThat(jsonVedlegg.size(), is(1));
        assertThat(jsonVedlegg.get(0).getStatus(), is(Vedlegg.Status.VedleggAlleredeSendt.name()));
        assertThat(jsonVedlegg.get(0).getFiler().size(), is(0));
    }

    @Test
    public void gruppererFiler() {
        List<JsonVedlegg> vedlegg = sosialhjelpVedleggTilJson.grupperVedleggFiler(soknad);

        JsonVedlegg vedlegg1 = vedlegg.get(0);
        JsonVedlegg vedlegg2 = vedlegg.get(1);
        assertEquals(2, vedlegg.size());
        assertEquals("V1", vedlegg1.getType());
        assertEquals("BARN", vedlegg1.getTilleggsinfo());
        assertEquals(2, vedlegg1.getFiler().size());
        assertEquals(1, vedlegg2.getFiler().size());
    }

    @Test
    public void lagerRepresentasjon() {
        AlternativRepresentasjon representasjon = sosialhjelpVedleggTilJson.transform(soknad);

        assertEquals("vedlegg.json", representasjon.getFilnavn());
        assertEquals(AlternativRepresentasjonType.JSON, representasjon.getRepresentasjonsType());
        assertThat(new String(representasjon.getContent()), containsString("garbage"));
    }

    @Test
    public void tomDataSkalGiTomStrengSomSha512() {
        Vedlegg vedlegg = new Vedlegg().medSkjemaNummer("V1").medSkjemanummerTillegg("BARN")
                .medFaktumId(2L).medFilnavn("fil1.png").medInnsendingsvalg(Vedlegg.Status.LastetOpp);

        assertThat(vedlegg.getSha512(), isEmptyString());
    }

    @Test
    public void vedleggsklassenSkalGenerereRiktigSha512BasertPaaFildataene() {
        String sha1 = ServiceUtils.getSha512FromByteArray(data);
        Vedlegg vedlegg = new Vedlegg().medData(data);

        String sha2 = vedlegg.getSha512();

        assertEquals(sha1, sha2);
    }

    @Test
    public void jsonObjektetSinStrengRepresentasjonSkalInneholdeShaen() {
        String sha = ServiceUtils.getSha512FromByteArray(data);
        WebSoknad soknad = opprettWebSoknadUtenVedlegg();
        soknad.medVedlegg(new Vedlegg().medSkjemaNummer("V1").medSkjemanummerTillegg("BARN")
                .medFaktumId(2L).medFilnavn("soknad.pdf").medInnsendingsvalg(Vedlegg.Status.LastetOpp).medData(data));

        AlternativRepresentasjon representasjon = sosialhjelpVedleggTilJson.transform(soknad);

        String json = new String(representasjon.getContent());
        assertTrue(json.contains(sha));
    }

    private WebSoknad opprettWebSoknadUtenVedlegg() {
        return new WebSoknad()
                .medFaktum(new Faktum().medKey("belop1").medFaktumId(1L))
                .medFaktum(new Faktum().medKey("proxy1-1").medFaktumId(2L).medParrentFaktumId(1L))
                .medFaktum(new Faktum().medKey("proxy1-2").medFaktumId(3L).medParrentFaktumId(1L))
                .medFaktum(new Faktum().medKey("belop2").medFaktumId(4L))
                .medFaktum(new Faktum().medKey("proxy2-1").medFaktumId(5L).medParrentFaktumId(4L))
                .medFaktum(new Faktum().medKey("belop3").medFaktumId(6L))
                .medFaktum(new Faktum().medKey("proxy2-1").medFaktumId(7L).medParrentFaktumId(6L));
    }
}