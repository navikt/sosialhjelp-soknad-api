package no.nav.sbl.sosialhjelp.pdf;

import org.junit.Before;
import org.junit.Test;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.startsWith;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;

public class UrlUtilsTest {

    private static final String BEHANDLING_ID = "100000XXX";
    private static final String DIALOGINNSENDING_SOKNAD_URL = "https://tjenester.nav.no/dialoginnsending";
    private static final String DIALOGINNSENDING_ETTERSENDING_PATH = "/dialoginnsending/ettersending";
    private static final String REQUEST_URL = "https://tjenester.nav.no/something/234q/endamer";

    @Before
    public void setup() {
        System.setProperty("soknadinnsending.link.url", DIALOGINNSENDING_SOKNAD_URL);
        System.setProperty("soknadinnsending.ettersending.path", DIALOGINNSENDING_ETTERSENDING_PATH);
    }

    @Test
    public void skalLageUrlForFortsettSenere() {
        assertThat(UrlUtils.getFortsettUrl(BEHANDLING_ID), startsWith("https://tjenester.nav.no/dialoginnsending/soknad/" + BEHANDLING_ID));
    }

    @Test
    public void skalLageUrlForEttersending() {
        assertThat(UrlUtils.getEttersendelseUrl(REQUEST_URL, BEHANDLING_ID), equalTo("https://tjenester.nav.no/dialoginnsending/ettersending/" + BEHANDLING_ID));
    }

    @Test
    public void skalHandtereBehandlingsIdSomErNull() {
        assertThat(UrlUtils.getEttersendelseUrl(REQUEST_URL, null), equalTo("https://tjenester.nav.no/dialoginnsending/ettersending/null"));
    }

    @Test
    public void testEndreHyperLenkerTilTekst() {

        String tekst1 = "Her skal du oppgi hva du har av inntekter og utgifter. Feltene under er basert på opplysninger du har gitt underveis i søknaden. Det er viktig at du, så langt det er mulig, fyller ut alle opplysninger om den økonomiske situasjonen din og dokumenterer opplysningene skriftlig. Har du vedlegg på papir kan du skanne dem eller <a href=\"https://www.nav.no/no/NAV+og+samfunn/Kontakt+NAV/Relatert+informasjon/ta-bilde-av-vedleggene-med-mobilen\" target=\"_blank\">ta bilde av vedleggene med mobiltelefonen din</a>. Du har mulighet til å laste opp vedlegg etter søknaden er sendt. Du kan også sende dokumentasjonen i posten eller levere den på NAV-kontoret ditt. ";

        tekst1 = tekst1 + tekst1;

        tekst1 = UrlUtils.endreHyperLenkerTilTekst(tekst1);

        tekst1 = tekst1.trim(); // Fjerner siste whitespace

        String tekst2 = "Her skal du oppgi hva du har av inntekter og utgifter. Feltene under er basert på opplysninger du har gitt underveis i søknaden. Det er viktig at du, så langt det er mulig, fyller ut alle opplysninger om den økonomiske situasjonen din og dokumenterer opplysningene skriftlig. Har du vedlegg på papir kan du skanne dem eller ta bilde av vedleggene med mobiltelefonen din\n" +
                "(https://www.nav.no/no/NAV+og+samfunn/Kontakt+NAV/Relatert+informasjon/ta-bilde-a<br />v-vedleggene-med-mobilen). Du har mulighet til å laste opp vedlegg etter søknaden er sendt. Du kan også sende dokumentasjonen i posten eller levere den på NAV-kontoret ditt. Her skal du oppgi hva du har av inntekter og utgifter. Feltene under er basert på opplysninger du har gitt underveis i søknaden. Det er viktig at du, så langt det er mulig, fyller ut alle opplysninger om den økonomiske situasjonen din og dokumenterer opplysningene skriftlig. Har du vedlegg på papir kan du skanne dem eller ta bilde av vedleggene med mobiltelefonen din\n" +
                "(https://www.nav.no/no/NAV+og+samfunn/Kontakt+NAV/Relatert+informasjon/ta-bilde-a<br />v-vedleggene-med-mobilen). Du har mulighet til å laste opp vedlegg etter søknaden er sendt. Du kan også sende dokumentasjonen i posten eller levere den på NAV-kontoret ditt.";

        assertEquals(tekst2, tekst1);

    }
}


