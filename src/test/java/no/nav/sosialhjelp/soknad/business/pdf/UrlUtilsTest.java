package no.nav.sosialhjelp.soknad.business.pdf;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class UrlUtilsTest {

    @Test
    public void testEndreHyperLenkerTilTekst() {
        String tekst1 = "Her skal du oppgi hva du har av inntekter og utgifter. Feltene under er basert på opplysninger du har gitt underveis i søknaden. Det er viktig at du, så langt det er mulig, fyller ut alle opplysninger om den økonomiske situasjonen din og dokumenterer opplysningene skriftlig. Har du vedlegg på papir kan du skanne dem eller <a href=\"https://www.nav.no/no/NAV+og+samfunn/Kontakt+NAV/Relatert+informasjon/ta-bilde-av-vedleggene-med-mobilen\" target=\"_blank\">ta bilde av vedleggene med mobiltelefonen din</a>. Du har mulighet til å laste opp vedlegg etter søknaden er sendt. Du kan også sende dokumentasjonen i posten eller levere den på NAV-kontoret ditt. ";

        tekst1 = tekst1 + tekst1;

        tekst1 = UrlUtils.endreHyperLenkerTilTekst(tekst1);

        tekst1 = tekst1.trim(); // Fjerner siste whitespace

        String tekst2 = "Her skal du oppgi hva du har av inntekter og utgifter. Feltene under er basert på opplysninger du har gitt underveis i søknaden. Det er viktig at du, så langt det er mulig, fyller ut alle opplysninger om den økonomiske situasjonen din og dokumenterer opplysningene skriftlig. Har du vedlegg på papir kan du skanne dem eller ta bilde av vedleggene med mobiltelefonen din\n" +
                "(https://www.nav.no/no/NAV+og+samfunn/Kontakt+NAV/Relatert+informasjon/ta-bilde-a<br />v-vedleggene-med-mobilen). Du har mulighet til å laste opp vedlegg etter søknaden er sendt. Du kan også sende dokumentasjonen i posten eller levere den på NAV-kontoret ditt. Her skal du oppgi hva du har av inntekter og utgifter. Feltene under er basert på opplysninger du har gitt underveis i søknaden. Det er viktig at du, så langt det er mulig, fyller ut alle opplysninger om den økonomiske situasjonen din og dokumenterer opplysningene skriftlig. Har du vedlegg på papir kan du skanne dem eller ta bilde av vedleggene med mobiltelefonen din\n" +
                "(https://www.nav.no/no/NAV+og+samfunn/Kontakt+NAV/Relatert+informasjon/ta-bilde-a<br />v-vedleggene-med-mobilen). Du har mulighet til å laste opp vedlegg etter søknaden er sendt. Du kan også sende dokumentasjonen i posten eller levere den på NAV-kontoret ditt.";

        assertThat(tekst2).isEqualTo(tekst1);

    }
}


