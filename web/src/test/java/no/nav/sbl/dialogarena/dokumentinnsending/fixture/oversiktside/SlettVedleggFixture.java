package no.nav.sbl.dialogarena.dokumentinnsending.fixture.oversiktside;

import no.nav.modig.test.NoCompare;
import no.nav.modig.test.fitnesse.fixture.ObjectPerRowFixture;
import no.nav.modig.wicket.test.FluentWicketTester;
import no.nav.sbl.dialogarena.dokumentinnsending.WicketApplication;
import no.nav.sbl.dialogarena.dokumentinnsending.domain.InnsendingsValg;
import no.nav.sbl.dialogarena.dokumentinnsending.pages.oversikt.OversiktPage;
import no.nav.sbl.dialogarena.dokumentinnsending.service.SoknadService;

import static no.nav.modig.lang.collections.IterUtils.on;
import static no.nav.modig.lang.collections.PredicateUtils.equalTo;
import static no.nav.modig.lang.collections.PredicateUtils.where;
import static no.nav.modig.wicket.test.matcher.ComponentMatchers.withLinkText;
import static no.nav.sbl.dialogarena.dokumentinnsending.domain.Dokument.INNSENDINGSVALG;
import static no.nav.sbl.dialogarena.dokumentinnsending.domain.Dokument.NAVN;


public class SlettVedleggFixture extends ObjectPerRowFixture<SlettVedleggFixture.Sletting> {


    static class Sletting {
        @NoCompare
        String brukerbehandlingId;
        @NoCompare
        String dokument;

        InnsendingsValg dokumentstatusEtterSletting;

        InnsendingsValg dokumentstatusForSletting;
    }

    private final FluentWicketTester<WicketApplication> wicket;
    private final SoknadService service;

    public SlettVedleggFixture(FluentWicketTester<WicketApplication> wicketTester, SoknadService service) {
        this.wicket = wicketTester;
        this.service = service;
    }

    @Override
    protected void perRow(Row<Sletting> row) throws Exception {
        Sletting sletting = row.expected;

        sletting.dokumentstatusForSletting = hentDokumentstatus(sletting);

        wicket.
                goTo(OversiktPage.class)
                .click().link(withLinkText("Slett dokument"));
        //.click().link(both(withLinkText("Slett dokument")).and(inListItem(sletting.dokument)));

        sletting.dokumentstatusEtterSletting = hentDokumentstatus(sletting);

        row.isActually(sletting);
    }


    private InnsendingsValg hentDokumentstatus(Sletting sletting) {
        return on(service.hentSoknad(sletting.brukerbehandlingId).getDokumenter())
                .filter(where(NAVN, equalTo(sletting.dokument)))
                .head().map(INNSENDINGSVALG).getOrElse(null);
    }

    // TODO: Må endres etter nytt GUI
/*    private ContainedInComponentMatcher inListItem(final String dokumentnavn) {
        return containedInComponent(ofType(DokumentVinduLastetOpp.class).and(containingComponent(whichIsLabelAndSaying(dokumentnavn))));
    }*/


}
