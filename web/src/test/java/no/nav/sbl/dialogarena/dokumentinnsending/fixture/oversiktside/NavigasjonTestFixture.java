package no.nav.sbl.dialogarena.dokumentinnsending.fixture.oversiktside;


import no.nav.modig.test.NoCompare;
import no.nav.modig.test.fitnesse.fixture.ObjectPerRowFixture;
import no.nav.modig.wicket.test.FluentWicketTester;
import no.nav.sbl.dialogarena.dokumentinnsending.WicketApplication;
import no.nav.sbl.dialogarena.dokumentinnsending.pages.fortsettsenere.FortsettSenerePage;
import no.nav.sbl.dialogarena.dokumentinnsending.pages.oversikt.OversiktPage;
import org.apache.wicket.Page;

import javax.inject.Inject;

import static no.nav.modig.wicket.test.matcher.ComponentMatchers.withId;

public class NavigasjonTestFixture extends ObjectPerRowFixture<NavigasjonTestFixture.Navigasjon> {

    @Inject
    private FluentWicketTester<WicketApplication> wicketTester;


    public static class Navigasjon {
        String brukerbehandlingId;
        Side startside;
        Funksjon funksjon;
        Side resultatSide;
        @NoCompare
        String kommentar;


        public enum Side {
            OVERSIKT(OversiktPage.class),
            FORTSETT_SENERE(FortsettSenerePage.class);

            public final Class<? extends Page> page;

            Side(Class<? extends Page> page) {
                this.page = page;
            }
        }

        public enum Funksjon {
            FORTSETT_SENERE(Side.OVERSIKT, "fortsettSenere"),
            TILBAKE_TIL_INNSENDING(Side.FORTSETT_SENERE, "tilOversikt");

            public final Side side;
            public final String id;

            Funksjon(Side side, String id) {
                this.side = side;
                this.id = id;
            }
        }


    }

    @Override
    protected void perRow(Row<Navigasjon> row) throws Exception {
        wicketTester = wicketTester.goTo(row.expected.startside.page)
                .click().link(withId(row.expected.funksjon.id))
                .should().beOn(row.expected.resultatSide.page);
        row.isActually(row.expected);
    }
}
