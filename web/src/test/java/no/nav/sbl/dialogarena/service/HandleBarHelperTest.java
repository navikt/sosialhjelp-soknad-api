package no.nav.sbl.dialogarena.service;

import no.nav.sbl.dialogarena.soknadinnsending.business.domain.Faktum;
import no.nav.sbl.dialogarena.soknadinnsending.business.domain.Vedlegg;
import no.nav.sbl.dialogarena.soknadinnsending.business.domain.WebSoknad;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

@RunWith(MockitoJUnitRunner.class)
public class HandleBarHelperTest {

    @Test
    public void hvisLikSkalViseInnholdVedToLikeStrenger() throws IOException {
        WebSoknad mockSoknad = mock(WebSoknad.class);
        String html = new HandleBarKjoerer().fyllHtmlMalMedInnhold(mockSoknad, "/skjema/hvisLik");
        assertThat(html).contains("erLik:true");
    }

    @Test
    public void hvisLikSkalViseInnholdIElseVedToUlikeStrenger() throws IOException {
        WebSoknad mockSoknad = mock(WebSoknad.class);
        String html = new HandleBarKjoerer().fyllHtmlMalMedInnhold(mockSoknad, "/skjema/hvisLik");
        assertThat(html).contains("ulike:false");
    }

    @Test
    public void hvisLikSkalViseNostetInnhold() throws IOException {
        WebSoknad mockSoknad = mock(WebSoknad.class);
        String html = new HandleBarKjoerer().fyllHtmlMalMedInnhold(mockSoknad, "/skjema/hvisLik");
        assertThat(html).contains("erLik:nested:true");
    }

    @Test
    public void hvisLikSkalViseInnholdFraSoknad() throws IOException {
        WebSoknad soknad = new WebSoknad();
        soknad.medBehandlingId("1A");
        String html = new HandleBarKjoerer().fyllHtmlMalMedInnhold(soknad, "/skjema/hvisLik");
        assertThat(html).contains("erLik:1A");
    }

    @Test
    public void hvisLikNostetSkalViseInnholdFraSoknad() throws IOException {
        WebSoknad soknad = new WebSoknad();
        soknad.medBehandlingId("1A");
        String html = new HandleBarKjoerer().fyllHtmlMalMedInnhold(soknad, "/skjema/hvisLik");
        assertThat(html).contains("erLik:nested:1A");
    }

    @Test
    public void hvisKunStudentHelperSkalReturnereTrueOmAlleFaktumErFalse() throws IOException {
        String html = new HandleBarKjoerer().fyllHtmlMalMedInnhold(setupHvisKunStudent(), "/skjema/hvisKunStudent");
        assertThat(html).contains("hvisKunStudent:true");
    }

    @Test
    public void hvisKunStudentHelperSkalReturnereFalseOmEtFaktumErTrue() throws IOException {
        WebSoknad soknad = setupHvisKunStudent();
        soknad.getFaktumMedKey("navaerendeSituasjon.forstegangstjeneste").setValue("true");
        String html = new HandleBarKjoerer().fyllHtmlMalMedInnhold(soknad, "/skjema/hvisKunStudent");
        assertThat(html).contains("hvisKunStudent:false");
    }

    @Test
    public void helperForPeriodeTidsromFaktaSkalGiFaktaISortertRekkefolge() throws IOException {
        WebSoknad soknad = new WebSoknad().medBehandlingId("1A");
        soknad.medFaktum(getPeriodeFaktum("perioder.tidsrom.1", "2015-03-03", "2015-03-05", "3"))
                .medFaktum(getPeriodeFaktum("perioder.tidsrom.2", "2015-01-04", "2015-01-05", "1"))
                .medFaktum(getPeriodeFaktum("perioder.tidsrom.3", "2015-02-04", "2015-02-05", "2"))
                .medFaktum(getPeriodeFaktum("perioder.tidsrom.4", "2015-04-04", "2015-04-05", "4"));

        String html = new HandleBarKjoerer().fyllHtmlMalMedInnhold(soknad, "/skjema/periodeTidsrom");
        assertThat(html).contains("1234");
    }

    @Test
    public void sendtInnInfoSkalViseAntallInnsendteOgIkkeInnsendteVedlegg() throws IOException {
        WebSoknad soknad = new WebSoknad().medBehandlingId("1A");
        List<Vedlegg> vedlegg = new ArrayList<>();
        vedlegg.add(new Vedlegg().medInnsendingsvalg(Vedlegg.Status.LastetOpp));
        vedlegg.add(new Vedlegg().medInnsendingsvalg(Vedlegg.Status.LastetOpp));
        vedlegg.add(new Vedlegg().medInnsendingsvalg(Vedlegg.Status.VedleggKreves));
        vedlegg.add(new Vedlegg().medInnsendingsvalg(Vedlegg.Status.VedleggKreves));
        vedlegg.add(new Vedlegg().medInnsendingsvalg(Vedlegg.Status.VedleggKreves));
        soknad.medVedlegg(vedlegg);

        String html = new HandleBarKjoerer().fyllHtmlMalMedInnhold(soknad, "/skjema/sendtInnInfo");
        assertThat(html).contains("antall:2");
        assertThat(html).contains("ikkeSendtInn:5");
    }

    @Test
    public void forInnsendteVedleggSkalIterereOverAlleInnsendteVedlegg() throws IOException {
        WebSoknad soknad = setupInnsendtevedlegg();
        String html = new HandleBarKjoerer().fyllHtmlMalMedInnhold(soknad, "/skjema/forInnsendteVedlegg");
        assertThat(html).contains("innsendt:sendtInn1");
        assertThat(html).contains("innsendt:sendtInn2");
        assertThat(html).doesNotContain("innsendt:ikkeSendtInn1");
        assertThat(html).doesNotContain("innsendt:ikkeSendtInn2");
        assertThat(html).doesNotContain("innsendt:ikkeSendtInn3");
    }

    @Test
    public void forIkkeInnsendteVedleggSkalIterereOverAlleIkkeInnsendteVedlegg() throws IOException {
        WebSoknad soknad = setupInnsendtevedlegg();
        String html = new HandleBarKjoerer().fyllHtmlMalMedInnhold(soknad, "/skjema/forInnsendteVedlegg");
        assertThat(html).doesNotContain("ikke:sendtInn1");
        assertThat(html).doesNotContain("ikke:sendtInn2");
        assertThat(html).contains("ikke:ikkeSendtInn1");
        assertThat(html).contains("ikke:ikkeSendtInn2");
        assertThat(html).contains("ikke:ikkeSendtInn3");
    }

    @Test
    public void hvisFlereStarterMedSkalReturnereTrueOmFlereHarVerdienTrue() throws IOException {
        WebSoknad soknad = new WebSoknad().medBehandlingId("1A");
        soknad.medFaktum(new Faktum().medKey("start.1").medValue("true"));
        soknad.medFaktum(new Faktum().medKey("start.2").medValue("true"));
        soknad.medFaktum(new Faktum().medKey("start.3").medValue("true"));

        String html = new HandleBarKjoerer().fyllHtmlMalMedInnhold(soknad, "/skjema/hvisFlereErTrue");
        assertThat(html).contains("flere");
    }

    private Faktum getPeriodeFaktum(String key, String fradato, String tildato, String value) {
        return new Faktum().medKey(key).medProperty("fradato", fradato).medProperty("tildato", tildato).medValue(value);
    }

    private WebSoknad setupInnsendtevedlegg() {
        WebSoknad soknad = new WebSoknad().medBehandlingId("1A");
        List<Vedlegg> vedlegg = new ArrayList<>();
        vedlegg.add(new Vedlegg().medInnsendingsvalg(Vedlegg.Status.LastetOpp).medNavn("sendtInn1"));
        vedlegg.add(new Vedlegg().medInnsendingsvalg(Vedlegg.Status.LastetOpp).medNavn("sendtInn2"));
        vedlegg.add(new Vedlegg().medInnsendingsvalg(Vedlegg.Status.VedleggKreves).medNavn("ikkeSendtInn1"));
        vedlegg.add(new Vedlegg().medInnsendingsvalg(Vedlegg.Status.VedleggKreves).medNavn("ikkeSendtInn2"));
        vedlegg.add(new Vedlegg().medInnsendingsvalg(Vedlegg.Status.VedleggKreves).medNavn("ikkeSendtInn3"));
        soknad.medVedlegg(vedlegg);
        return soknad;
    }

    private WebSoknad setupHvisKunStudent() {
        WebSoknad soknad = new WebSoknad();
        soknad.medBehandlingId("1A");
        soknad.medFaktum(new Faktum().medKey("navaerendeSituasjon.iArbeid").medValue("false"))
                .medFaktum(new Faktum().medKey("navaerendeSituasjon.sykmeldt").medValue("false"))
                .medFaktum(new Faktum().medKey("navaerendeSituasjon.arbeidsledig").medValue("false"))
                .medFaktum(new Faktum().medKey("navaerendeSituasjon.forstegangstjeneste").medValue("false"))
                .medFaktum(new Faktum().medKey("navaerendeSituasjon.annet").medValue("false"));
        return soknad;
    }
}