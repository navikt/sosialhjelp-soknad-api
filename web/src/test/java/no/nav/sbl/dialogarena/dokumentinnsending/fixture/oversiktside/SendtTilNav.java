package no.nav.sbl.dialogarena.dokumentinnsending.fixture.oversiktside;

import no.nav.modig.test.NoCompare;
import no.nav.modig.test.fitnesse.fixture.ObjectPerRowFixture;
import no.nav.modig.wicket.test.FluentWicketTester;
import no.nav.sbl.dialogarena.dokumentinnsending.WicketApplication;
import no.nav.sbl.dialogarena.dokumentinnsending.pages.bekreft.BekreftelsesPage;
import no.nav.sbl.dialogarena.dokumentinnsending.pages.innsendingkvittering.InnsendingKvitteringPage;
import no.nav.sbl.dialogarena.dokumentinnsending.service.DokumentServiceMock;
import org.apache.commons.collections15.Predicate;
import org.apache.wicket.Component;

import java.util.List;

import static no.nav.modig.lang.collections.IterUtils.on;
import static no.nav.modig.wicket.test.matcher.ComponentMatchers.withId;

public class SendtTilNav extends ObjectPerRowFixture<SendtTilNav.Oversikt> {
    private final DokumentServiceMock dokumentServiceMock;
    private final FluentWicketTester<WicketApplication> wicketTester;

    public SendtTilNav(DokumentServiceMock dokumentServiceMock, FluentWicketTester<WicketApplication> wicketTester) {
        this.dokumentServiceMock = dokumentServiceMock;
        this.wicketTester = wicketTester;
    }

    static class Oversikt {
        public String brukerbehandlingId;
        public String hovedskjema;
        @NoCompare
        boolean harBekreftet;
        boolean innsendingsknappAktiv;
        boolean samtykkeOpprettet;
        boolean skalKommeTilKvitteringssiden;
        boolean hovedskjemaVises;

        @NoCompare
        String kommentar;
        @NoCompare
        List<String> navVedlegg;
    }


    @Override
    protected void perRow(Row<Oversikt> row) throws Exception {
        dokumentServiceMock.settSecurityContextFor(row.expected.brukerbehandlingId);

        FluentWicketTester<WicketApplication> tester = wicketTester.goTo(BekreftelsesPage.class);
        if (row.expected.harBekreftet) {
            //Hack for å kunne teste at send inn knappen enables når checkbox blir klikket. Ikke funnet noen løsning på å kalle et ajax behaviour uten en tilknyttet form.
            tester.get().component(withId("bekreftVilkaar")).setDefaultModelObject(true);
            tester.get().component(withId("sendInn")).render();
            //Fungerer ikke når det ikke er en form rundt knappen.
            //tester.click().ajaxCheckbox(withId("bekreftVilkaar"))
        }
        final Oversikt o = new Oversikt();
        o.brukerbehandlingId = row.expected.brukerbehandlingId;
        o.hovedskjema = tester.get().component(withId("skjemaNavn")).getDefaultModelObject().toString();
        o.innsendingsknappAktiv = tester.get().component(withId("sendInn")).isEnabled();
        if (row.expected.harBekreftet) {
            tester.click().link(withId("sendInn"))
                    .should()
                    .beOn(InnsendingKvitteringPage.class);
        }
        o.skalKommeTilKvitteringssiden = tester.tester.getLastRenderedPage().getClass() == InnsendingKvitteringPage.class;
        o.samtykkeOpprettet = dokumentServiceMock.harInnsendt(row.expected.brukerbehandlingId) && dokumentServiceMock.harSamtykket(row.expected.brukerbehandlingId);

        int hovedDokumenter = on(tester.get().components(withId("dokumentnavn"))).filter(new Predicate<Component>() {
            @Override
            public boolean evaluate(Component component) {
                return component.getDefaultModelObject().equals(o.hovedskjema);
            }
        }).collect().size();
        o.hovedskjemaVises = hovedDokumenter > 0;
        row.isActually(o);
    }


}
