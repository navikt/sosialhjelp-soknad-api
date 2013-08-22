package no.nav.sbl.dialogarena.dokumentinnsending.fixture.oversiktside;

import no.nav.modig.test.Ignore;
import no.nav.modig.test.NoCompare;
import no.nav.modig.test.fitnesse.fixture.ObjectPerRowFixture;
import no.nav.modig.wicket.test.FluentWicketTester;
import no.nav.sbl.dialogarena.dokumentinnsending.WicketApplication;
import no.nav.sbl.dialogarena.dokumentinnsending.pages.oversikt.OversiktPage;
import org.apache.commons.lang3.StringUtils;
import org.apache.wicket.Component;

import javax.inject.Inject;
import java.io.UnsupportedEncodingException;
import java.util.List;

import static no.nav.modig.wicket.test.matcher.ComponentMatchers.containedInComponent;
import static no.nav.modig.wicket.test.matcher.ComponentMatchers.containingComponent;
import static no.nav.modig.wicket.test.matcher.ComponentMatchers.whichIsLabelAndSaying;
import static no.nav.modig.wicket.test.matcher.ComponentMatchers.withId;
import static org.apache.commons.lang3.StringEscapeUtils.unescapeHtml4;

public class SjekkEndreVedleggslinje extends ObjectPerRowFixture<SjekkEndreVedleggslinje.Vedleggslinje> {

    @Inject
    private final FluentWicketTester<WicketApplication> wicketTester;

    static class Vedleggslinje {

        @NoCompare
        String brukerBehandlingId;

        @NoCompare
        String vedlegg;
        @NoCompare
        String beskrivelse;
        String endretBeskrivelse;
        String feilmelding;

        @Ignore
        String idnummer;
        @Ignore
        String kommentar;
    }

    public SjekkEndreVedleggslinje(FluentWicketTester<WicketApplication> wicketTester) {
        this.wicketTester = wicketTester;
    }

    @Override
    protected void perRow(Row<Vedleggslinje> row) throws UnsupportedEncodingException {
        wicketTester.goTo(OversiktPage.class);

        wicketTester.click()
                .link(withId("editer").and(containedInComponent(withId("ekstraVedleggDokumentVindu").and(containingComponent(withId("dokumentnavn").and(whichIsLabelAndSaying(row.expected.vedlegg)))))))
                .inForm("modal:content:addOrUpdateVedleggForm")
                .write("beskrivelse", row.expected.beskrivelse)
                .submitWithAjaxButton(withId("ajax-button"));

        Vedleggslinje vedleggslinje = row.expected;
        vedleggslinje.feilmelding = getErrorMessages(row.expected.feilmelding);
        vedleggslinje.endretBeskrivelse = getEndretBeskrivelse(row.expected.endretBeskrivelse);

        row.isActually(vedleggslinje);
    }

    private String getEndretBeskrivelse(String expectedEndretBeskrivelse) {
        wicketTester.goTo(OversiktPage.class);

        List<Component> dokumentNavnComponents = wicketTester.get().components(withId("dokumentnavn"));

        for (Component dokumentNavnComponent : dokumentNavnComponents) {
            String dokumentNavn = StringUtils.remove(unescapeHtml4(dokumentNavnComponent.getDefaultModelObjectAsString()), "Annet: ");
            if (expectedEndretBeskrivelse.equals(dokumentNavn)) {
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
