package no.nav.sbl.dialogarena.dokumentinnsending.fixture.oversiktside;

import no.nav.modig.test.fitnesse.fixture.ObjectPerRowFixture;
import no.nav.modig.wicket.test.FluentWicketTester;
import no.nav.sbl.dialogarena.dokumentinnsending.WicketApplication;
import no.nav.sbl.dialogarena.dokumentinnsending.pages.innsendingslettet.InnsendingSlettetPage;
import no.nav.sbl.dialogarena.dokumentinnsending.pages.oversikt.OversiktPage;

import static no.nav.modig.wicket.test.matcher.ComponentMatchers.containedInComponent;
import static no.nav.modig.wicket.test.matcher.ComponentMatchers.thatIsModalWindowShowing;
import static no.nav.modig.wicket.test.matcher.ComponentMatchers.withId;
import static no.nav.sbl.dialogarena.dokumentinnsending.fixture.oversiktside.SjekkSlettSoknad.Sletting.Bekreft.JA;
import static org.hamcrest.core.CombinableMatcher.both;

public class SjekkSlettSoknad extends ObjectPerRowFixture<SjekkSlettSoknad.Sletting> {

    static class Sletting {
        String brukerbehandlingId;
        String soknad;
        Bekreft bekreftSletting;
        String kommerTilSide;
        String kommentar;

        public enum Bekreft {
            JA,NEI;

            public boolean er(Bekreft bekreft) {
                return this.equals(bekreft);
            }
        }
    }

    private final FluentWicketTester<WicketApplication> wicketTester;

    public SjekkSlettSoknad(FluentWicketTester<WicketApplication> wicketTester) {
        this.wicketTester = wicketTester;
    }

    @Override
    protected void perRow(Row<Sletting> row) throws Exception {
        Sletting sletting = row.expected;

        Class expectedPage = sletting.bekreftSletting.er(JA) ? InnsendingSlettetPage.class : OversiktPage.class;
        String confirmButtonId = sletting.bekreftSletting.er(JA) ? "bekreft" : "avbryt";

        wicketTester.goTo(OversiktPage.class)
                .click()
                .link(withId("slettInnsending"))
                .click()
                .link(both(withId(confirmButtonId)).and(containedInComponent(thatIsModalWindowShowing())));

        if (wicketTester.tester.getLastRenderedPage().getClass() == expectedPage) {
            row.right();
        } else {
            row.wrong("Endte opp på gal side");
        }
    }
}
