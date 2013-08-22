package no.nav.sbl.dialogarena.dokumentinnsending.fixture.oversiktside;

import no.nav.modig.test.Ignore;
import no.nav.modig.test.NoCompare;
import no.nav.modig.test.fitnesse.fixture.ObjectPerRowFixture;
import no.nav.modig.wicket.test.FluentWicketTester;
import no.nav.sbl.dialogarena.dokumentinnsending.WicketApplication;
import no.nav.sbl.dialogarena.dokumentinnsending.pages.oversikt.OversiktPage;
import org.apache.wicket.Component;
import org.apache.wicket.ajax.markup.html.AjaxLink;

import javax.inject.Inject;
import java.io.UnsupportedEncodingException;
import java.util.List;

import static no.nav.modig.wicket.test.matcher.ComponentMatchers.withId;
import static org.apache.commons.lang3.StringEscapeUtils.unescapeHtml4;
import static org.apache.commons.lang3.StringUtils.remove;

public class SjekkLeggTilVedleggslinje extends ObjectPerRowFixture<SjekkLeggTilVedleggslinje.Vedleggslinje> {

    @Inject
    private final FluentWicketTester<WicketApplication> wicketTester;

    static class Vedleggslinje {

        @NoCompare
        String brukerBehandlingId;

        @NoCompare
        String vedlegg;
        @NoCompare
        String beskrivelse;
        String feilmelding;
        String nyVedleggslinje;
        String endreLink;

        @Ignore
        String idnummer;
        @Ignore
        String kommentar;
    }

    public SjekkLeggTilVedleggslinje(FluentWicketTester<WicketApplication> wicketTester) {
        this.wicketTester = wicketTester;
    }

    @Override
    protected void perRow(Row<Vedleggslinje> row) throws UnsupportedEncodingException {

        // Fungerer ikke med withId("addOrUpdateVedleggForm") for å finne form
        wicketTester.goTo(OversiktPage.class)
                .click()
                .link(withId("leggtilvedlegg"))
                .inForm("modal:content:addOrUpdateVedleggForm")
                .write("beskrivelse", row.expected.beskrivelse)
                .submitWithAjaxButton(withId("ajax-button"));

        Vedleggslinje vedleggslinje = row.expected;
        vedleggslinje.feilmelding = getErrorMessages(row.expected.feilmelding);
        vedleggslinje.nyVedleggslinje = findNyVedleggslinje(row.expected.nyVedleggslinje);
        vedleggslinje.endreLink = findEndreLink(row);

        row.isActually(vedleggslinje);
    }

    private String findEndreLink(Row<Vedleggslinje> row) {
        List<Component> ekstraVedlegg = wicketTester.get().components(withId("ekstraVedleggDokumentVindu"));

        for (Component vedlegg : ekstraVedlegg) {
            String dokumentNavn = remove(unescapeHtml4(vedlegg.get("dokumentnavn").getDefaultModelObjectAsString()), "Annet: ");
            if (dokumentNavn.equals(row.expected.beskrivelse)) {
                AjaxLink<?> endreLink = (AjaxLink<?>) vedlegg.get("editer");
                return endreLink.getBody().getObject().toString();
            }
        }
        return "";
    }

    private String findNyVedleggslinje(String expectedNyVedleggslinje) {
        wicketTester.goTo(OversiktPage.class);

        List<Component> dokumentNavnComponents = wicketTester.get().components(withId("dokumentnavn"));

        for (Component dokumentNavnComponent : dokumentNavnComponents) {
            String dokumentNavn = unescapeHtml4(dokumentNavnComponent.getDefaultModelObjectAsString());
            if (expectedNyVedleggslinje.equals(dokumentNavn)) {
                return dokumentNavn;
            }
        }

        return "";
    }

    private String getErrorMessages(String expectedErrorMessage) {
        List<String> errorMessages = wicketTester.get().errorMessages();

        if (errorMessages.isEmpty()) {
            return "";
        } else {
            if (errorMessages.contains(expectedErrorMessage)) {
                return expectedErrorMessage;
            } else {
                return errorMessages.toString();
            }
        }
    }
}
