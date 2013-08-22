package no.nav.sbl.dialogarena.dokumentinnsending.fixture.opplastingsvindu;

import no.nav.modig.test.Ignore;
import no.nav.modig.test.NoCompare;
import no.nav.modig.test.fitnesse.fixture.ObjectPerRowFixture;
import no.nav.modig.wicket.test.FluentWicketTester;
import no.nav.sbl.dialogarena.dokumentinnsending.WicketApplication;
import no.nav.sbl.dialogarena.dokumentinnsending.pages.oversikt.OversiktPage;
import org.apache.commons.lang3.StringUtils;
import org.apache.wicket.Component;
import org.apache.wicket.markup.html.form.upload.FileUpload;
import org.apache.wicket.markup.html.list.ListView;

import java.io.File;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import static java.text.DateFormat.getDateInstance;
import static no.nav.modig.lang.collections.IterUtils.on;
import static no.nav.modig.wicket.test.matcher.ComponentMatchers.withId;
import static no.nav.sbl.dialogarena.dokumentinnsending.fixture.opplastingsvindu.OpplastingAvVedlegg.addFile;
import static no.nav.sbl.dialogarena.dokumentinnsending.fixture.opplastingsvindu.OpplastingAvVedlegg.inListItemContainingText;
import static org.apache.commons.collections15.CollectionUtils.forAllDo;

public class SjekkFilformaterOgStorrelse extends ObjectPerRowFixture<SjekkFilformaterOgStorrelse.Opplasting> {

    static class Opplasting {

        @NoCompare
        String brukerBehandlingId;
        @NoCompare
        List<File> filnavn;
        @NoCompare
        String dokument;

        List<String> filnavnSomVises;
        List<String> feilmelding;
        List<String> nyDokumentStatus;

        @Ignore
        String idnummer;
        @Ignore
        String kommentar;

        public boolean bleLastetOpp() {
            return filnavnSomVises != null && !filnavnSomVises.isEmpty();
        }
    }

    private FluentWicketTester<WicketApplication> wicketTester;

    public SjekkFilformaterOgStorrelse(FluentWicketTester<WicketApplication> wicketTester) {
        this.wicketTester = wicketTester;
    }

    @Override
    protected void perRow(Row<Opplasting> row) throws Exception {
        Opplasting opplasting = row.expected;

        uploadFile(opplasting.dokument, opplasting.filnavn);

        opplasting.feilmelding = formatErrorMessages();
        opplasting.filnavnSomVises = formatFilnavnSomVisesEtterPreview();

        if (opplasting.bleLastetOpp()) {
            wicketTester
                    .click().link(withId("done"))
                    .goTo(OversiktPage.class);
        } else {
            opplasting.nyDokumentStatus = new ArrayList<>();
        }

        row.isActually(opplasting);
    }

    @SuppressWarnings("unused")
    private String formaterOpplastetDato(String opplastetDato, Locale locale) throws ParseException {
        Date parsetDato = getDateInstance(DateFormat.LONG, locale).parse(opplastetDato);
        SimpleDateFormat s = new SimpleDateFormat("dd.MM.yyyy");
        return s.format(parsetDato);
    }

    private List<String> formatFilnavnSomVisesEtterPreview() {
        List<String> filnavnSomVises = new ArrayList<>();

        ListView<FileUpload> fileListView = wicketTester.get().component(withId("fileList"));

        for (Component component : fileListView) {
            filnavnSomVises.add(component.get("filename").getDefaultModelObjectAsString());
        }

        return filnavnSomVises;
    }

    private List<String> formatErrorMessages() {
        List<String> uploadErrorMessages = wicketTester.get().errorMessages();
        List<String> feilmeldinger = new ArrayList<>();

        for (String uploadErrorMessage : uploadErrorMessages) {
            String formattedUploadErrorMessage = StringUtils.remove(uploadErrorMessage, ",");
            if (StringUtils.containsIgnoreCase(uploadErrorMessage, "strong")) {
                formattedUploadErrorMessage = StringUtils.remove(formattedUploadErrorMessage, "<strong>");
                formattedUploadErrorMessage = StringUtils.remove(formattedUploadErrorMessage, "</strong>");
            }
            feilmeldinger.add(formattedUploadErrorMessage);
        }
        return feilmeldinger;
    }

    private void uploadFile(String dokument, List<File> vedlegg) {
        wicketTester
                .goTo(OversiktPage.class)
                .click().link(withId("uploadKnapp").and(inListItemContainingText(dokument)));

        forAllDo(on(vedlegg).indexed(), addFile(wicketTester.tester.getRequest()));

    }
}