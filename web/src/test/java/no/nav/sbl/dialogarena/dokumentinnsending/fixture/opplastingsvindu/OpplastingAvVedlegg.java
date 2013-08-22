package no.nav.sbl.dialogarena.dokumentinnsending.fixture.opplastingsvindu;

import no.nav.modig.core.exception.ApplicationException;
import no.nav.modig.lang.collections.iter.Elem;
import no.nav.modig.test.Ignore;
import no.nav.modig.test.NoCompare;
import no.nav.modig.test.fitnesse.fixture.ObjectPerRowFixture;
import no.nav.modig.wicket.test.FluentWicketTester;
import no.nav.modig.wicket.test.matcher.ComponentMatcher;
import no.nav.sbl.dialogarena.dokumentinnsending.WicketApplication;
import no.nav.sbl.dialogarena.dokumentinnsending.domain.DokumentSoknad;
import no.nav.sbl.dialogarena.dokumentinnsending.domain.InnsendingsValg;
import no.nav.sbl.dialogarena.dokumentinnsending.pages.oversikt.OversiktPage;
import no.nav.sbl.dialogarena.dokumentinnsending.service.SoknadService;
import org.apache.commons.collections15.Closure;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.protocol.http.mock.MockHttpServletRequest;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import static no.nav.modig.lang.collections.IterUtils.on;
import static no.nav.modig.lang.collections.PredicateUtils.equalTo;
import static no.nav.modig.lang.collections.PredicateUtils.where;
import static no.nav.modig.wicket.test.matcher.ComponentMatchers.containedInComponent;
import static no.nav.modig.wicket.test.matcher.ComponentMatchers.containingComponent;
import static no.nav.modig.wicket.test.matcher.ComponentMatchers.ofType;
import static no.nav.modig.wicket.test.matcher.ComponentMatchers.whichIsLabelAndSaying;
import static no.nav.modig.wicket.test.matcher.ComponentMatchers.withId;
import static no.nav.sbl.dialogarena.detect.Functions.CONTENT_TYPE;
import static no.nav.sbl.dialogarena.dokumentinnsending.domain.Dokument.INNSENDINGSVALG;
import static no.nav.sbl.dialogarena.dokumentinnsending.domain.Dokument.NAVN;
import static no.nav.sbl.dialogarena.dokumentinnsending.domain.InnsendingsValg.IKKE_VALGT;
import static org.apache.commons.collections15.CollectionUtils.forAllDo;
import static org.apache.commons.io.IOUtils.closeQuietly;
import static org.apache.commons.io.IOUtils.toByteArray;

public class OpplastingAvVedlegg extends ObjectPerRowFixture<OpplastingAvVedlegg.Opplasting> {

    static class Opplasting {

        @NoCompare
        String brukerBehandlingId;
        @NoCompare
        List<File> vedlegg;
        @NoCompare
        String dokument;

        String kommerTilSide;

        InnsendingsValg dokumentstatus;

        @Ignore
        String kommentar;
    }

    private FluentWicketTester<WicketApplication> wicketTester;

    private SoknadService service;

    public OpplastingAvVedlegg(FluentWicketTester<WicketApplication> wicketTester, SoknadService dokumentForventningServiceIntegration) {
        this.wicketTester = wicketTester;
        this.service = dokumentForventningServiceIntegration;
    }

    @Override
    protected void perRow(Row<Opplasting> row) throws Exception {
        Opplasting opplasting = row.expected;

        wicketTester
            .goTo(OversiktPage.class)
            .click().link(withId("uploadKnapp").and(inListItemContainingText(opplasting.dokument)));

        // TODO: Må endres etter nytt GUI
//        wicketTester.get().component(ofType(VelgFilerForm.class).and(containedInComponent(thatIsModalWindowShowing())));

        forAllDo(on(opplasting.vedlegg).indexed(), addFile(wicketTester.tester.getRequest()));


        /*wicketTester
            .click().ajaxButton(withId("submit").and(containedInComponent(ofType(VelgFilerForm.class))))
            .click().link(withId("done"));*/

        wicketTester
            .click().link(withId("sendbutton"));

        opplasting.kommerTilSide = wicketTester.tester.getLastRenderedPage().getClass().getSimpleName();


        DokumentSoknad soknad = service.hentSoknad(opplasting.brukerBehandlingId);
        opplasting.dokumentstatus = on(soknad.getDokumenter())
                .filter(where(NAVN, equalTo(opplasting.dokument)))
                .head().map(INNSENDINGSVALG).getOrElse(IKKE_VALGT);

        row.isActually(opplasting);
    }

    public static Closure<Elem<File>> addFile(final MockHttpServletRequest request) {
        return new Closure<Elem<File>>() {
            @Override
            public void execute(Elem<File> vedlegg) {
                String fieldName = String.format("fileInput_mf_%d", vedlegg.index);
                String contentType;
                InputStream fileIn = null;
                try {
                    fileIn = new FileInputStream(vedlegg.value);
                    contentType = CONTENT_TYPE.transform(toByteArray(fileIn));
                } catch (IOException e) {
                    throw new ApplicationException(e.getMessage(), e);
                } finally {
                    closeQuietly(fileIn);
                }
                request.addFile(fieldName, new org.apache.wicket.util.file.File(vedlegg.value), contentType);
            }
        };
    }


    public static ComponentMatcher inListItemContainingText(String labelText) {
        return containedInComponent(ofType(ListItem.class).and(containingComponent(whichIsLabelAndSaying(labelText))));
    }}