package no.nav.sbl.dialogarena.dokumentinnsending.fixture.oversiktside;

import no.nav.modig.lang.collections.iter.Elem;
import no.nav.modig.test.NoCompare;
import no.nav.modig.test.fitnesse.fixture.ObjectPerRowFixture;
import no.nav.modig.wicket.errorhandling.pages.ApplicationExceptionPage;
import no.nav.modig.wicket.test.FluentWicketTester;
import no.nav.sbl.dialogarena.dokumentinnsending.WicketApplication;
import no.nav.sbl.dialogarena.dokumentinnsending.pages.oversikt.OversiktPage;
import org.apache.commons.collections15.Transformer;
import org.apache.wicket.Component;

import java.util.ArrayList;
import java.util.List;

import static java.util.Collections.emptyList;
import static no.nav.modig.lang.collections.IterUtils.on;
import static no.nav.modig.wicket.test.matcher.CombinableMatcher.both;
import static no.nav.modig.wicket.test.matcher.ComponentMatchers.containedInComponent;
import static no.nav.modig.wicket.test.matcher.ComponentMatchers.containingComponent;
import static no.nav.modig.wicket.test.matcher.ComponentMatchers.whichIsLabelAndSaying;
import static no.nav.modig.wicket.test.matcher.ComponentMatchers.withId;
import static org.apache.commons.lang3.StringUtils.isBlank;
import static org.hamcrest.core.IsNot.not;

public class SjekkOppdaterOversikt extends ObjectPerRowFixture<SjekkOppdaterOversikt.Oversikt> {

    static class Oversikt {
        String brukerbehandlingId;
        String soknad;
        String soknadStatus;
        List<String> navVedlegg = emptyList();
        List<String> statuserNavVedlegg = emptyList();

        @NoCompare
        String kommentar;
    }

    private FluentWicketTester<WicketApplication> wicketTester;

    public SjekkOppdaterOversikt(FluentWicketTester<WicketApplication> wicketTester) {
        this.wicketTester = wicketTester;
    }

    //CHECKSTYLE:OFF
    @Override
    protected void perRow(Row<Oversikt> row) throws Exception {

        if (isBlank(row.expected.soknad)) {
            wicketTester.tester.startPage(OversiktPage.class);

            if (wicketTester.tester.getLastRenderedPage().getClass().equals(ApplicationExceptionPage.class)) {
                row.right();
            } else {
                row.wrong("Ble ikke videresendt til application exception page");
            }
        } else {
            wicketTester.goTo(OversiktPage.class);
            Oversikt oversiktSiden = new Oversikt();
            oversiktSiden.brukerbehandlingId = row.expected.brukerbehandlingId;
            oversiktSiden.soknad = wicketTester.get().component(both(withId("dokumentnavn")).and(containedInComponent(withId("skjemaer")))).getDefaultModelObjectAsString();
            oversiktSiden.soknadStatus = wicketTester.get().components(both(withId("uploadKnapp")).and(containedInComponent(withId("skjemaer")))).isEmpty() ? "Lastet opp" : "Ikke valgt";
            oversiktSiden.navVedlegg = on(wicketTester.get().components(both(withId("dokumentnavn")).and(not(containedInComponent(withId("skjemaer"))))))
                    .map(COMPONENT_TIL_MODELOBJECT_AS_STRING)
                    .collect();

            oversiktSiden.statuserNavVedlegg = new ArrayList<>();
            for (Elem<String> vedlegg : on(row.expected.navVedlegg).indexed()) {

                boolean lastetOpp = wicketTester.get().components(
                        both(containingComponent(both(withId("dokumentnavn")).and(whichIsLabelAndSaying(row.expected.navVedlegg.get(vedlegg.index)))))
                        .and(containedInComponent(withId("navVedlegg")))
                        .and(containingComponent(withId("uploadKnapp")))
                ).isEmpty();

                String status = lastetOpp ? "Lastet opp" : "Ikke valgt";
                oversiktSiden.statuserNavVedlegg.add(status);
            }

            row.isActually(oversiktSiden);
        }
    }
    //CHECKSTYLE:ON

    private static final Transformer<Component, String> COMPONENT_TIL_MODELOBJECT_AS_STRING = new Transformer<Component, String>() {
        @Override
        public String transform(Component component) {
            return component.getDefaultModelObjectAsString();
        }
    };
}
