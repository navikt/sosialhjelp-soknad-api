package no.nav.sbl.dialogarena.dokumentinnsending.fixture.oversiktside;

import no.nav.modig.test.Ignore;
import no.nav.modig.test.fitnesse.fixture.ObjectPerRowFixture;
import no.nav.modig.wicket.test.FluentWicketTester;
import no.nav.modig.wicket.test.internal.Parameters;
import no.nav.sbl.dialogarena.dokumentinnsending.domain.Dokument;
import no.nav.sbl.dialogarena.dokumentinnsending.domain.InnsendingsValg;
import no.nav.sbl.dialogarena.dokumentinnsending.pages.bekreft.BekreftelsesPage;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;

import java.util.LinkedList;
import java.util.Queue;

import static no.nav.modig.lang.collections.IterUtils.on;
import static no.nav.modig.lang.collections.TransformerUtils.castTo;
import static no.nav.modig.wicket.test.matcher.ComponentMatchers.containedInComponent;
import static no.nav.modig.wicket.test.matcher.ComponentMatchers.withId;
import static org.hamcrest.Matchers.both;
import static org.hamcrest.Matchers.equalTo;


public class SjekkBekreftingAvVedleggFoerInnsending extends ObjectPerRowFixture<SjekkBekreftingAvVedleggFoerInnsending.Bekreftelse> {

    static class Bekreftelse {
        InnsendingsValg status;
        String navn;

        @Ignore
        String kommentar;
    }

    private final FluentWicketTester<?> wicket;
    private final Queue<ListItem<Dokument>> vedleggItems = new LinkedList<>();

    public SjekkBekreftingAvVedleggFoerInnsending(String behandlingsId, FluentWicketTester<?> wicket) {
        this.wicket = wicket;
        Parameters parameters = new Parameters();
        parameters.pageParameters.set(0, behandlingsId);
        wicket.goTo(BekreftelsesPage.class, parameters);

        vedleggItems.addAll(vedleggItemsInListView("opplastetDokumentContainer"));
        vedleggItems.addAll(vedleggItemsInListView("sendesPost"));
        vedleggItems.addAll(vedleggItemsInListView("sendesSenere"));
        vedleggItems.addAll(vedleggItemsInListView("sendesIkke"));
        vedleggItems.addAll(vedleggItemsInListView("sendesAvAndre"));
    }

    @Override
    protected void perRow(Row<Bekreftelse> row) throws Exception {
        ListItem<Dokument> vedleggItem = vedleggItems.poll();

        Bekreftelse faktiskVedlegg = new Bekreftelse();
        faktiskVedlegg.navn = wicket.get().component(withId("dokumentnavn").and(containedInComponent(equalTo(vedleggItem)))).getDefaultModelObjectAsString();
        faktiskVedlegg.status = vedleggItem.getModelObject().getValg();
        row.isActually(faktiskVedlegg);
    }

    private Queue<ListItem<Dokument>> vedleggItemsInListView(String listViewContainerId) {
        Queue<ListItem<Dokument>> items = new LinkedList<>();

        ListView<Dokument> listView = this.wicket.get().component(both(containedInComponent(withId(listViewContainerId))).
                and(withId("dokumentListe")));
        for (ListItem<Dokument> opplastet : on(listView).map(castTo(ListItem.class))) {
            items.add(opplastet);
        }
        return items;
    }
}