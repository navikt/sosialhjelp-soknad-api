package no.nav.sbl.dialogarena.soknadinnsending.business.domain;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import javax.xml.bind.JAXB;
import java.io.*;
import java.util.Arrays;
import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.not;
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
}
