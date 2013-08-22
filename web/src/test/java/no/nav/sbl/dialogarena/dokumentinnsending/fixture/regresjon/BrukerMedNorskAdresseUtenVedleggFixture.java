package no.nav.sbl.dialogarena.dokumentinnsending.fixture.regresjon;

import static no.nav.modig.wicket.test.matcher.ComponentMatchers.withId;

import java.util.Arrays;
import java.util.List;

import javax.inject.Inject;

import no.nav.modig.wicket.test.FluentWicketTester;
import no.nav.sbl.dialogarena.dokumentinnsending.WicketApplication;
import no.nav.sbl.dialogarena.dokumentinnsending.config.FitNesseApplicationConfig;
import no.nav.sbl.dialogarena.dokumentinnsending.domain.Dokument;
import no.nav.sbl.dialogarena.dokumentinnsending.fixture.pages.BekreftelsesPageObject;
import no.nav.sbl.dialogarena.dokumentinnsending.fixture.utils.TestUtils;
import no.nav.sbl.dialogarena.dokumentinnsending.pages.bekreft.BekreftelsesPage;
import no.nav.sbl.dialogarena.dokumentinnsending.pages.innsendingkvittering.InnsendingKvitteringPage;
import no.nav.sbl.dialogarena.dokumentinnsending.service.DokumentServiceMock;
import no.nav.sbl.dialogarena.soknad.kodeverk.KodeverkSkjema;
import no.nav.tjeneste.domene.brukerdialog.henvendelse.v1.informasjon.WSBrukerBehandlingType;
import no.nav.tjeneste.domene.brukerdialog.henvendelse.v1.informasjon.WSDokumentForventning;

import org.springframework.test.context.ContextConfiguration;

import fit.Fixture;

@ContextConfiguration(classes = FitNesseApplicationConfig.class)
public class BrukerMedNorskAdresseUtenVedleggFixture extends RegresjonsFixture {

    @Inject
    private FluentWicketTester<WicketApplication> wicketTester;

    @Inject
    private DokumentServiceMock dokumentService;

    private String kodeverkId;

    public boolean opprettBrukerbehandlingOgIdentifiserAktorMedFodselsnummerOgMedNorskAdresseOgBehandlingsIdOgSkjemanummerMedDokumentstatus(String fodselsnummer, String registrertINorge, String behandlingsIdVariabelnavn, String kodeverkId, String dokumentStatus) {
        WSDokumentForventning forventning = new WSDokumentForventning().withHovedskjema(true).withKodeverkId(kodeverkId).withInnsendingsValg(finnWSInnsendingsvalgFraDokumentStatus(dokumentStatus));
        String behandlingsId = dokumentService.opprettDokumentBehandling(Arrays.asList(forventning), WSBrukerBehandlingType.DOKUMENT_BEHANDLING);

        Fixture.setSymbol(behandlingsIdVariabelnavn, behandlingsId);
        this.kodeverkId = kodeverkId;
        Fixture.setSymbol(kodeverkId, kodeverkId);

        TestUtils.innloggetBrukerEr(fodselsnummer);

        dokumentService.identifiserAktor(behandlingsId, fodselsnummer);
        dokumentService.opprettDokument(dokumentService.createDokument(kodeverkId), forventning.getId());
        System.setProperty("minehenvendelser.link.url", "");
        return true;
    }

    public boolean brukerGuE5rTilBekreftelsessidenMedBehandlingsId(String behandlingsIdVariabelnavn) {
        String behandlingsId = Fixture.getSymbol(behandlingsIdVariabelnavn).toString();
        KodeverkSkjema kodeverkSkjema = lagKodeverkSkjema("Krav om dagpenger", kodeverkId);
        dokumentService.stub(kodeverkSkjema);
        wicketTester.goTo(BekreftelsesPage.class, withBrukerBehandlingId(behandlingsId));
        return true;
    }

    public boolean dokumentetMedSkjemaIdMedTittelOgDokumentstatusSkalVises(String kodeverkId, String skjemaTittel, String dokumentStatus) {
        List<Dokument> model = (List<Dokument>) wicketTester.get().component(withId("innsendteDokumenter")).getDefaultModelObject();
        return sjekkDokumentVerdier(model.get(0), skjemaTittel, dokumentStatus, kodeverkId);
    }

    public boolean brukerBekrefterOgGuE5rTilKvitteringssidenMedBehandlingsId(String behandlingsIdVariabelnavn) {
        BekreftelsesPageObject page = new BekreftelsesPageObject();
        page.velgAlternativForAdresseOgBekreft(wicketTester, 1);

        wicketTester.should().beOn(InnsendingKvitteringPage.class);

        return true;
    }

    public boolean brukerBekrefterOgGuE5rTilKvitteringssidenMedBehandlingsIdOgStatusEr(String behandlingsIdVariabelnavn, String tittelDokument, String dokumentStatus) {
        List<Dokument> model = (List<Dokument>) wicketTester.get().component(withId("innsendteDokumenter")).getDefaultModelObject();

        return sjekkDokumentVerdier(model.get(0), tittelDokument, dokumentStatus, kodeverkId);
    }

    public boolean innsendingenFerdigOgKvitteringVisesMedBehandlingsIdOgDokumentSendtErOgDokumentstatusEr(String behandlingsIdVariabelnavn, String tittelDokument, String dokumentStatus) {
        List<Dokument> model = (List<Dokument>) wicketTester.get().component(withId("innsendteDokumenter")).getDefaultModelObject();
        return sjekkDokumentVerdier(model.get(0), tittelDokument, dokumentStatus, kodeverkId);
    }
}