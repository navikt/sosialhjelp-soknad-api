package no.nav.sbl.dialogarena.dokumentinnsending.fixture.opplastingsvindu;

import no.nav.modig.test.fitnesse.fixture.ObjectPerRowFixture;
import no.nav.modig.wicket.test.FluentWicketTester;
import no.nav.sbl.dialogarena.dokumentinnsending.WicketApplication;
import no.nav.sbl.dialogarena.dokumentinnsending.pages.oversikt.OversiktPage;
import org.apache.wicket.Component;

import static no.nav.modig.wicket.test.matcher.ComponentMatchers.containedInComponent;
import static no.nav.modig.wicket.test.matcher.ComponentMatchers.thatIsModalWindowShowing;
import static no.nav.modig.wicket.test.matcher.ComponentMatchers.withId;
import static no.nav.sbl.dialogarena.dokumentinnsending.fixture.opplastingsvindu.OpplastingAvVedlegg.inListItemContainingText;
import static org.hamcrest.Matchers.equalTo;

public class SjekkVisOpplastingsvindu extends ObjectPerRowFixture<ModalvinduForventning> {
    private final FluentWicketTester<WicketApplication> wicketTester;

    public SjekkVisOpplastingsvindu(FluentWicketTester<WicketApplication> wicketTester) {
        this.wicketTester = wicketTester;
    }

    @Override
    protected void perRow(Row<ModalvinduForventning> row) {
        ModalvinduForventning modalvindu = row.expected;

        Component vindu = wicketTester
            .goTo(OversiktPage.class)
            .click().link(inListItemContainingText(modalvindu.dokument).and(withId("uploadKnapp")))
            .get().component(thatIsModalWindowShowing());

        modalvindu.tittel = wicketTester.get().component(withId("tittel").and(containedInComponent(equalTo(vindu)))).getDefaultModelObjectAsString();
        modalvindu.ingress = wicketTester.get().component(withId("ingress").and(containedInComponent(equalTo(vindu)))).getDefaultModelObjectAsString();

        row.isActually(modalvindu);
    }
}