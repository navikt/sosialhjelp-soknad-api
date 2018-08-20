package no.nav.sbl.dialogarena.sendsoknad.domain;

import no.nav.sbl.dialogarena.sendsoknad.domain.oppsett.VedleggForFaktumStruktur;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import javax.xml.bind.JAXB;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Arrays;
import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.hamcrest.core.Is.is;

public class WebSoknadTest {

    WebSoknad soknad;
    Long soknadId;
    Long faktumId;

    @Before
    public void setUp() {
        soknadId = 2l;
        faktumId = 33l;
        soknad = new WebSoknad();
        soknad.setSoknadId(soknadId);
    }

    @Test
    public void shouldKunneLageTomSoknad() {
        Assert.assertEquals(0, soknad.antallFakta());
    }

    @Test
    public void skalKunneLeggeTilFakta() {
        soknad.leggTilFaktum(new Faktum().medSoknadId(soknadId).medFaktumId(faktumId).medKey("enKey").medValue("enValue"));
        Assert.assertEquals(1, soknad.antallFakta());
    }

    @Test
    public void skalReturnereTrueDersomSoknadHarN6VedleggSomIkkeErLastetOpp() {
        List<Vedlegg> vedlegg = Arrays.asList(
                new Vedlegg()
                        .medSkjemaNummer("N6")
                        .medFillagerReferanse("uidVedlegg1")
                        .medOpprinneligInnsendingsvalg(Vedlegg.Status.LastetOpp)
                        .medInnsendingsvalg(Vedlegg.Status.LastetOpp)
                        .medStorrelse(0L)
                        .medNavn("Test Annet vedlegg")
                        .medAntallSider(3),
                new Vedlegg()
                        .medSkjemaNummer("N6")
                        .medFillagerReferanse("uidVedlegg1")
                        .medInnsendingsvalg(Vedlegg.Status.VedleggKreves)
                        .medStorrelse(0L)
                        .medNavn("Test Annet vedlegg")
                        .medAntallSider(3),
                new Vedlegg()
                        .medSkjemaNummer("L7")
                        .medInnsendingsvalg(Vedlegg.Status.SendesIkke));

        soknad.setVedlegg(vedlegg);
        assertThat(soknad.harAnnetVedleggSomIkkeErLastetOpp(), is(true));
    }

    @Test
    public void skalReturnereFalseDersomSoknadIkkeHarN6VedleggSomIkkeErLastetOpp() {
        List<Vedlegg> vedlegg = Arrays.asList(
                new Vedlegg()
                        .medSkjemaNummer("N6")
                        .medFillagerReferanse("uidVedlegg1")
                        .medOpprinneligInnsendingsvalg(Vedlegg.Status.LastetOpp)
                        .medInnsendingsvalg(Vedlegg.Status.LastetOpp)
                        .medStorrelse(0L)
                        .medNavn("Test Annet vedlegg")
                        .medAntallSider(3),
                new Vedlegg()
                        .medSkjemaNummer("N6")
                        .medFillagerReferanse("uidVedlegg1")
                        .medInnsendingsvalg(Vedlegg.Status.LastetOpp)
                        .medStorrelse(5L)
                        .medNavn("Test Annet vedlegg")
                        .medAntallSider(3),
                new Vedlegg()
                        .medSkjemaNummer("L7")
                        .medInnsendingsvalg(Vedlegg.Status.SendesIkke));

        soknad.setVedlegg(vedlegg);
        assertThat(soknad.harAnnetVedleggSomIkkeErLastetOpp(), is(false));
    }

    @Test
    public void skalUnmarshalleMellomlagredeSoknaderMedFaktaElementer() {
        InputStream soknadMedFaktaElementer = WebSoknadTest.class.getResourceAsStream("/soknader/soknad-struktur-fakta.xml");
        WebSoknad soknad = JAXB.unmarshal(soknadMedFaktaElementer, WebSoknad.class);
        assertThat(soknad.antallFakta(), is(3L));
    }

    @Test
    public void skalUnmarshalleGamleMellomlagredeSoknaderMedFaktalisteElementer() {
        InputStream soknadMedFaktalisteElementer = WebSoknad.class.getResourceAsStream("/soknader/soknad-struktur-faktaListe.xml");
        WebSoknad soknad = JAXB.unmarshal(soknadMedFaktalisteElementer, WebSoknad.class);
        assertThat(soknad.antallFakta(), is(3L));
    }

    @Test
    public void skalMarshalleSoknaderTilFaktaElementer() {
        InputStream gammelStruktur = WebSoknad.class.getResourceAsStream("/soknader/soknad-struktur-faktaListe.xml");
        WebSoknad soknad = JAXB.unmarshal(gammelStruktur, WebSoknad.class);

        OutputStream output = new ByteArrayOutputStream();
        JAXB.marshal(soknad, output);
        String xml = output.toString();
        assertThat(xml, containsString("<fakta>"));
        assertThat(xml, not(containsString("<faktaListe>")));
    }

    @Test
    public void skalReturnereVedleggSomMatcherForventningMedFlereTillattOgLikFaktumid() {
        Long faktumId = 123456L;
        String skjemanummer = "M6";
        VedleggForFaktumStruktur forventning = new VedleggForFaktumStruktur()
                .medSkjemanummer(skjemanummer);
        forventning.setFlereTillatt(true);

        Vedlegg vedlegg = new Vedlegg(1L, faktumId, skjemanummer, Vedlegg.Status.UnderBehandling);

        WebSoknad webSoknad = new WebSoknad().medVedlegg(vedlegg);
        assertThat(webSoknad.finnVedleggSomMatcherForventning(forventning, faktumId), equalTo(vedlegg));
    }

    @Test
    public void skalReturnereVedleggSomMatcherForventningMedIkkeFlereTillattOgIkkeFaktumid() {
        String skjemanummer = "M6";
        VedleggForFaktumStruktur forventning = new VedleggForFaktumStruktur()
                .medSkjemanummer(skjemanummer);
        forventning.setFlereTillatt(false);

        Vedlegg vedlegg = new Vedlegg(1L, null, skjemanummer, Vedlegg.Status.UnderBehandling);

        WebSoknad webSoknad = new WebSoknad().medVedlegg(vedlegg);
        assertThat(webSoknad.finnVedleggSomMatcherForventning(forventning, null), equalTo(vedlegg));
    }

    @Test
    public void skalIkkeReturnereVedleggSomIkkeHarFaktumIdOgFlereTillatt() {
        String skjemanummer = "M6";
        VedleggForFaktumStruktur forventning = new VedleggForFaktumStruktur()
                .medSkjemanummer(skjemanummer);
        forventning.setFlereTillatt(true);

        Vedlegg vedlegg = new Vedlegg(1L, faktumId, skjemanummer, Vedlegg.Status.UnderBehandling);
        WebSoknad webSoknad = new WebSoknad().medVedlegg(vedlegg);

        assertThat(webSoknad.finnVedleggSomMatcherForventning(forventning, null), not(equalTo(vedlegg)));
    }

    @Test
    public void skalIkkeReturnereVedleggSomHarUlikeFaktumIdFraForventning() {
        Long faktumId = 123456L;
        String skjemanummer = "M6";
        VedleggForFaktumStruktur forventning = new VedleggForFaktumStruktur()
                .medSkjemanummer(skjemanummer);
        forventning.setFlereTillatt(true);

        Vedlegg vedlegg = new Vedlegg(1L, faktumId, skjemanummer, Vedlegg.Status.UnderBehandling);
        WebSoknad webSoknad = new WebSoknad().medVedlegg(vedlegg);
        long ulikFaktumid = 1L;
        assertThat(webSoknad.finnVedleggSomMatcherForventning(forventning, ulikFaktumid), not(equalTo(vedlegg)));
    }

    @Test
    public void skalIkkeReturnereVedleggSomIkkeMatcherForventningPaSkjemanummer() {
        Long faktumId = 123456L;
        String skjemanummer = "M6";
        VedleggForFaktumStruktur forventning = new VedleggForFaktumStruktur()
                .medSkjemanummer(skjemanummer);
        forventning.setFlereTillatt(true);

        Vedlegg vedlegg = new Vedlegg(1L, faktumId, "K4", Vedlegg.Status.UnderBehandling);

        WebSoknad webSoknad = new WebSoknad().medVedlegg(vedlegg);
        assertThat(webSoknad.finnVedleggSomMatcherForventning(forventning, faktumId), not(equalTo(vedlegg)));
    }

    @Test
    public void skalIkkeReturnereVedleggSomIkkeMatcherForventningPaSkjemanummertillegg() {
        Long faktumId = 123456L;
        String skjemanummer = "M6";
        VedleggForFaktumStruktur forventning = new VedleggForFaktumStruktur()
                .medSkjemanummer(skjemanummer);

        forventning.setFlereTillatt(true);
        forventning.setSkjemanummerTillegg("skjemanummertillegg");

        Vedlegg vedlegg = new Vedlegg(1L, faktumId, "K4", Vedlegg.Status.UnderBehandling);
        vedlegg.medSkjemanummerTillegg("annetSkjemanummertillegg");

        WebSoknad webSoknad = new WebSoknad().medVedlegg(vedlegg);
        assertThat(webSoknad.finnVedleggSomMatcherForventning(forventning, faktumId), not(equalTo(vedlegg)));
    }
}
