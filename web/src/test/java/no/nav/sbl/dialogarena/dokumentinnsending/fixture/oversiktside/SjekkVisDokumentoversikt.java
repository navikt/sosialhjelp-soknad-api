package no.nav.sbl.dialogarena.dokumentinnsending.fixture.oversiktside;

import no.nav.modig.test.fitnesse.fixture.ObjectPerRowFixture;
import no.nav.modig.wicket.test.FluentWicketTester;
import no.nav.sbl.dialogarena.dokumentinnsending.WicketApplication;
import no.nav.sbl.dialogarena.dokumentinnsending.pages.oversikt.OversiktPage;
import org.apache.commons.lang3.StringUtils;
import org.apache.wicket.Component;
import org.apache.wicket.markup.html.link.ExternalLink;
import org.apache.wicket.markup.html.list.ListView;

import static no.nav.modig.wicket.test.matcher.ComponentMatchers.withId;

public class SjekkVisDokumentoversikt extends ObjectPerRowFixture<DokumentinnsendingData> {

    private final FluentWicketTester<WicketApplication> wicketTester;

    public SjekkVisDokumentoversikt(FluentWicketTester<WicketApplication> wicketTester) {
        this.wicketTester = wicketTester;
    }

    @Override
    protected void perRow(Row<DokumentinnsendingData> row) {
        wicketTester.goTo(OversiktPage.class);
        for (Value value : row.firstValue.andTheRest()) {
            check(value);
        }
    }

    // CHECKSTYLE:OFF
    private void check(Value value) {
        ListView<?> hovedSkjemaDokumentList = wicketTester.get().component(withId("skjemaer"));
        ListView<?> navVedleggDokumentList = wicketTester.get().component(withId("navVedlegg"));
        ListView<?> eksterntVedleggDokumentList = wicketTester.get().component(withId("eksterntVedlegg"));

        if (value.hasName("soknad")) {
            value.isActually(hovedSkjemaDokumentList.get(0).get("hovedSkjemaDokumentVindu").get("dokumentnavn").getDefaultModelObjectAsString());
        } else if (value.hasName("soknadbeskrivelse")) {
            value.isActually(hovedSkjemaDokumentList.get(0).get("hovedSkjemaDokumentVindu").get("beskrivelse").getDefaultModelObjectAsString());
        } else if (value.hasName("soknadLink")) {
            value.isActually(hovedSkjemaDokumentList.get(0).get("hovedSkjemaDokumentVindu").get("lastNed").getDefaultModelObjectAsString());
        } else if (value.hasName("soknadLinkTekst")) {
            value.isActually(cleanExernalLinkText(hovedSkjemaDokumentList.get(0).get("hovedSkjemaDokumentVindu").get("lastNed")));
        } else if (value.name.contains("nAVVedlegg") && value.name.contains("LinkTekst")) {
            checkVedleggLinkTekst("nAVVedlegg", "navVedleggDokumentVindu", value, navVedleggDokumentList);
        } else if (value.name.contains("nAVVedlegg") && value.name.contains("Link")) {
            checkVedleggLink("nAVVedlegg", "navVedleggDokumentVindu", value, navVedleggDokumentList);
        } else if (value.name.contains("nAVVedlegg") && value.name.contains("beskrivelse")) {
            checkVedleggBeskrivelse("nAVVedlegg", "navVedleggDokumentVindu", value, navVedleggDokumentList);
        } else if (value.name.startsWith("nAVVedlegg")) {
            checkVedleggNavn("nAVVedlegg", "navVedleggDokumentVindu", value, navVedleggDokumentList);
        } else if (value.name.contains("eksterntVedlegg") && value.name.contains("LinkTekst")) {
            checkVedleggLinkTekst("eksterntVedlegg", "eksterntVedleggDokumentVindu", value, eksterntVedleggDokumentList);
        } else if (value.name.contains("eksterntVedlegg") && value.name.contains("Link")) {
            checkVedleggLink("eksterntVedlegg", "eksterntVedleggDokumentVindu", value, eksterntVedleggDokumentList);
        } else if (value.name.contains("eksterntVedlegg") && value.name.contains("beskrivelse")) {
            checkVedleggBeskrivelse("eksterntVedlegg", "eksterntVedleggDokumentVindu", value, eksterntVedleggDokumentList);
        } else if (value.name.startsWith("eksterntVedlegg")) {
            checkVedleggNavn("eksterntVedlegg", "eksterntVedleggDokumentVindu", value, eksterntVedleggDokumentList);
        } else if (value.hasName("idnummer") || value.hasName("brukerBehandlingId") || value.hasName("kommentar")) {
            value.ignore();
        } else if (value.hasName("hovedskjemaVises")) {
            value.isActually(hovedSkjemaDokumentList.get(0).get("hovedSkjemaDokumentVindu").findParent(OversiktPage.DokumentlisteOversikt.class).isVisible() ? "Ja" : "Nei");
        } else {
            value.unableToHandle();
        }
    }

    private void checkVedleggNavn(String vedleggId, String dokumentVinduId, Value value, ListView<?> vedleggDokumentList) {
        int id = Integer.parseInt(value.name.substring(vedleggId.length()));
        assertEmptyValue(value, vedleggDokumentList, id - 1, "dokumentnavn", dokumentVinduId);
    }

    private void checkVedleggBeskrivelse(String vedleggId, String dokumentVinduId, Value value, ListView<?> vedleggDokumentList) {
        int id = Integer.parseInt(value.name.substring(vedleggId.length(), value.name.length() - "beskrivelse".length()));
        assertEmptyValue(value, vedleggDokumentList, id - 1, "beskrivelse", dokumentVinduId);
    }

    private void checkVedleggLink(String vedleggId, String dokumentVinduId, Value value, ListView<?> vedleggDokumentList) {
        int id = Integer.parseInt(value.name.substring(vedleggId.length(), value.name.length() - "Link".length()));
        if (StringUtils.isBlank(value.expected)) {
            if (vedleggDokumentList.size() <= id
                    || vedleggDokumentList.get(id - 1).get(dokumentVinduId).get("lastNed") == null
                    || vedleggDokumentList.get(id - 1).get(dokumentVinduId).get("lastNed").getDefaultModelObjectAsString().equals("")) {
                value.right();
            } else {
                value.wrong();
            }
        } else {
            assertEmptyValue(value, vedleggDokumentList, id - 1, "lastNed", dokumentVinduId);
        }
    }

    private void checkVedleggLinkTekst(String vedleggsId, String dokumentVinduId, Value value, ListView<?> vedleggDokumentList) {
        int id = Integer.parseInt(value.name.substring(vedleggsId.length(), value.name.length() - "LinkTekst".length()));
        if (vedleggDokumentList.size() > 0) {
            if (vedleggDokumentList.get(id - 1).get(dokumentVinduId).get("lastNed").isVisible()) {
                assertEmptyLinkText(value, vedleggDokumentList, id - 1, "lastNed", dokumentVinduId);
            } else {
                assertEmptyValue(value, vedleggDokumentList, id - 1, "instruks", dokumentVinduId);
            }
        } else {
            if (StringUtils.isBlank(value.expected)) {
                value.right();
            } else {
                value.wrong();
            }
        }
    }


    private void assertEmptyValue(Value value, ListView<?> list, int index, String componentId, String dokumentVinduId) {
        if (StringUtils.isBlank(value.expected)) {
            handleEmptyValue(value, list, index);
        } else {
            value.isActually(cleanText(list.get(index).get(dokumentVinduId).get(componentId).getDefaultModelObjectAsString()));
        }
    }

    private void assertEmptyLinkText(Value value, ListView<?> list, int index, String componentId, String dokumentVinduId) {
        if (StringUtils.isBlank(value.expected)) {
            handleEmptyValue(value, list, index);
        } else {
            value.isActually(cleanExernalLinkText(list.get(index).get(dokumentVinduId).get(componentId)));
        }
    }

    private void handleEmptyValue(Value value, ListView<?> list, int index) {
        if (list.size() <= index) {
            value.right();
        } else {
            value.wrong();
        }
    }

    private String cleanExernalLinkText(Component linkText) {
        return cleanText(((ExternalLink) linkText).getBody().getObject().toString());
    }

    private String cleanText(String text) {
        return text.replace("<span>", "").replace("</span>", "");
    }
}
