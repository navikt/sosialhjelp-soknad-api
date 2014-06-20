package no.nav.sbl.dialogarena.soknadinnsending.business.domain;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.Arrays;
import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
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
		Assert.assertEquals(0,soknad.antallFakta());
	}
	
	@Test
	public void skalKunneLeggeTilFakta() {
		soknad.leggTilFaktum(new Faktum().medSoknadId(soknadId).medFaktumId(faktumId).medKey("enKey").medValue("enValue"));
		Assert.assertEquals(1,soknad.antallFakta());
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
}
