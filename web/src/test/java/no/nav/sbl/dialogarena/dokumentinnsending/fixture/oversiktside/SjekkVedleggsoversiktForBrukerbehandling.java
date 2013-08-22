package no.nav.sbl.dialogarena.dokumentinnsending.fixture.oversiktside;

import no.nav.modig.test.Ignore;
import no.nav.modig.test.fitnesse.fixture.ObjectPerRowFixture;
import no.nav.modig.wicket.test.FluentWicketTester;
import no.nav.modig.wicket.test.internal.Parameters;
import no.nav.sbl.dialogarena.dokumentinnsending.domain.Dokument;
import no.nav.sbl.dialogarena.dokumentinnsending.domain.InnsendingsValg;
import no.nav.sbl.dialogarena.dokumentinnsending.pages.oversikt.OversiktPage;
import no.nav.sbl.dialogarena.dokumentinnsending.security.SecurityHandler;
import org.apache.wicket.Component;
import org.apache.wicket.markup.html.list.ListItem;
import org.hamcrest.Matcher;

import java.util.LinkedList;
import java.util.Queue;

import static no.nav.modig.lang.collections.IterUtils.on;
import static no.nav.modig.lang.collections.TransformerUtils.castTo;
import static no.nav.modig.wicket.test.matcher.ComponentMatchers.containedInComponent;
import static no.nav.modig.wicket.test.matcher.ComponentMatchers.ofType;
import static no.nav.modig.wicket.test.matcher.ComponentMatchers.withId;
import static org.hamcrest.Matchers.equalTo;


public class SjekkVedleggsoversiktForBrukerbehandling extends ObjectPerRowFixture<SjekkVedleggsoversiktForBrukerbehandling.Vedleggsvisning> {

    static class Vedleggsvisning {
        String navn;
        InnsendingsValg status;

        @Ignore
        String kommentar;
    }


    private final FluentWicketTester<?> wicket;

    private final Queue<ListItem<Dokument>> vedleggItems = new LinkedList<>();

    public SjekkVedleggsoversiktForBrukerbehandling(String behandlingsId, FluentWicketTester<?> wicket) {
        this.wicket = wicket;
        Parameters parameters = new Parameters();
        parameters.pageParameters.set(0, behandlingsId);
        wicket.goTo(OversiktPage.class, parameters);

        for (OversiktPage.DokumentlisteOversikt items : wicket.get().<OversiktPage.DokumentlisteOversikt>components(ofType(OversiktPage.DokumentlisteOversikt.class))) {
            for (ListItem<Dokument> vedleggItem : on(items).map(castTo(ListItem.class))) {
                this.vedleggItems.add(vedleggItem);
            }
        }
    }

    @Override
    protected void perRow(Row<Vedleggsvisning> row) throws Exception {
        SecurityHandler.setSecurityContext("01019012345");
        ListItem<Dokument> vedleggItem = this.vedleggItems.poll();
        Dokument dokument = (Dokument) vedleggItem.getDefaultModelObject();
        Matcher<Component> inThisVedleggItem = containedInComponent(equalTo(vedleggItem));


        Vedleggsvisning actual = new Vedleggsvisning();
        actual.navn = wicket.get().component(withId("dokumentnavn").and(inThisVedleggItem)).getDefaultModelObjectAsString();
        actual.status = dokument.getValg();

        row.isActually(actual);
    }
}