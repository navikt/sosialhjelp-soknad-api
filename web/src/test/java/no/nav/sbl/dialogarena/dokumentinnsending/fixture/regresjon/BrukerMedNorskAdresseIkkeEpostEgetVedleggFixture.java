package no.nav.sbl.dialogarena.dokumentinnsending.fixture.regresjon;

import fit.Fixture;
import no.nav.modig.wicket.test.FluentWicketTester;
import no.nav.sbl.dialogarena.dokumentinnsending.WicketApplication;
import no.nav.sbl.dialogarena.dokumentinnsending.config.FitNesseApplicationConfig;
import no.nav.sbl.dialogarena.dokumentinnsending.domain.Dokument;
import no.nav.sbl.dialogarena.dokumentinnsending.fixture.pages.BekreftelsesPageObject;
import no.nav.sbl.dialogarena.dokumentinnsending.fixture.utils.TestUtils;
import no.nav.sbl.dialogarena.dokumentinnsending.pages.bekreft.BekreftelsesPage;
import no.nav.sbl.dialogarena.dokumentinnsending.pages.fortsettsenere.FortsettSenerePage;
import no.nav.sbl.dialogarena.dokumentinnsending.pages.innsendingkvittering.InnsendingKvitteringPage;
import no.nav.sbl.dialogarena.dokumentinnsending.pages.oversikt.OversiktPage;
import no.nav.sbl.dialogarena.dokumentinnsending.service.DokumentServiceMock;
import no.nav.sbl.dialogarena.soknad.kodeverk.KodeverkSkjema;
import no.nav.tjeneste.domene.brukerdialog.henvendelse.v1.informasjon.WSBrukerBehandlingType;
import no.nav.tjeneste.domene.brukerdialog.henvendelse.v1.informasjon.WSDokumentForventning;
import org.springframework.test.context.ContextConfiguration;

import javax.inject.Inject;
import java.util.Arrays;
import java.util.List;

import static no.nav.modig.wicket.test.matcher.ComponentMatchers.withId;

@ContextConfiguration(classes = FitNesseApplicationConfig.class)
public class BrukerMedNorskAdresseIkkeEpostEgetVedleggFixture extends RegresjonsFixture {

    @Inject
    private FluentWicketTester<WicketApplication> wicketTester;

    @Inject
    private DokumentServiceMock dokumentService;

    private String kodeverkIdSoknad;
    private String kodeverkIdEgetVedlegg;

    public boolean opprettBrukerbehandlingOgIdentifiserAktorMedFodselsnummerOgBehandlingsIdOgSkjemanummerMedDokumentstatusOgEgetVedleggMedDokumentstatus(
            String fodselsnummer, String behandlingsIdVariabelnavn, String kodeverkIdSoknad, String soknadDokumentStatus, String kodeverkIdEgetVedlegg, String egetVedleggDokumentStatus) throws Exception {

        WSDokumentForventning dokumentForventningSoknad = new WSDokumentForventning().withHovedskjema(true).withKodeverkId(kodeverkIdSoknad).withInnsendingsValg(finnWSInnsendingsvalgFraDokumentStatus(soknadDokumentStatus));
        WSDokumentForventning dokumentForventningEgetVedlegg = new WSDokumentForventning().withHovedskjema(false).withKodeverkId(kodeverkIdEgetVedlegg).withInnsendingsValg(finnWSInnsendingsvalgFraDokumentStatus(egetVedleggDokumentStatus));
        String behandlingsId = dokumentService.opprettDokumentBehandling(Arrays.asList(dokumentForventningSoknad, dokumentForventningEgetVedlegg), WSBrukerBehandlingType.DOKUMENT_BEHANDLING);

        Fixture.setSymbol(behandlingsIdVariabelnavn, behandlingsId);
        this.kodeverkIdSoknad = kodeverkIdSoknad;
        this.kodeverkIdEgetVedlegg = kodeverkIdEgetVedlegg;

        TestUtils.innloggetBrukerEr(fodselsnummer);

        dokumentService.identifiserAktor(behandlingsId, fodselsnummer);
        dokumentService.opprettDokument(dokumentService.createDokument(kodeverkIdSoknad), dokumentForventningSoknad.getId());
        dokumentService.opprettDokument(dokumentService.createDokument(kodeverkIdEgetVedlegg), dokumentForventningEgetVedlegg.getId());

        System.setProperty("minehenvendelser.link.url", "");
        return true;
    }

    public boolean brukerGuE5rTilOversiktsidenMedBehandlingsId(String behandlingsIdVariabelnavn) {
        String behandlingsId = Fixture.getSymbol(behandlingsIdVariabelnavn).toString();
        KodeverkSkjema kodeverkSoknadSkjema = lagKodeverkSkjema("Krav om barnetrygd ordinær", this.kodeverkIdSoknad);
        dokumentService.stub(kodeverkSoknadSkjema);

        KodeverkSkjema kodeverkEgetVedlegg = lagKodeverkSkjema("Fødselsattest", this.kodeverkIdEgetVedlegg);
        dokumentService.stub(kodeverkEgetVedlegg);

        wicketTester.goTo(OversiktPage.class, withBrukerBehandlingId(behandlingsId));
        return true;
    }

    public boolean dokumentetMedSkjemaIdMedTittelOgDokumentstatusOgDokumentMedSkjemaIdMedTittelOgDokumentstatusSkalVises(
            String kodeverkIdSoknad, String skjemaTittelSoknad, String dokumentStatusSoknad, String kodeverkIdEgetVedlegg, String skjemaTittelEgetVedlegg, String dokumentStatusEgetVedlegg) {
        List<Dokument> model = (List<Dokument>) wicketTester.get().component(withId("innsendteDokumenter")).getDefaultModelObject();

        boolean verdierSoknad = sjekkDokumentVerdier(model.get(0), skjemaTittelSoknad, dokumentStatusSoknad, kodeverkIdSoknad);
        boolean verdierVedlegg = sjekkDokumentVerdier(model.get(1), skjemaTittelEgetVedlegg, dokumentStatusEgetVedlegg, kodeverkIdEgetVedlegg);

        return verdierSoknad && verdierVedlegg;
    }

    public boolean brukerGuE5rTilSidenForFortsettSenereMedBehandlingsIdOgPreutfyltEpostSkalVuE6re(String behandlingsIdVariabelnavn, String preutfyltEpostadresse) throws Exception {
        String behandlingsId = Fixture.getSymbol(behandlingsIdVariabelnavn).toString();

        wicketTester.goTo(FortsettSenerePage.class, withBrukerBehandlingId(behandlingsId));

        String epost = wicketTester.get().component(withId("epost")).getDefaultModelObjectAsString();
        String blankEpostAdresse = "blank";
        if (blankEpostAdresse.equalsIgnoreCase(preutfyltEpostadresse)) {
            return "".equalsIgnoreCase(epost);
        }

        return preutfyltEpostadresse.equalsIgnoreCase(epost);
    }

    public boolean getBrukerAvslutterUtenuC5SendeEpostOgSkalIkkeFuE5KvitteringPuE5Sendt() {
        wicketTester.click().link(withId("tilOversikt")).should().beOn(OversiktPage.class);

        return true;
    }

    public boolean brukerGuE5rTilBekreftelsessidenMedBehandlingsId(String behandlingsIdVariabelnavn) {
        String behandlingsId = Fixture.getSymbol(behandlingsIdVariabelnavn).toString();
        wicketTester.goTo(BekreftelsesPage.class, withBrukerBehandlingId(behandlingsId)).should().beOn(BekreftelsesPage.class);
        return true;
    }

    public boolean getBrukerBekrefterOgSender() {
        BekreftelsesPageObject page = new BekreftelsesPageObject();
        page.velgAlternativForAdresseOgBekreft(wicketTester, 1);

        wicketTester.should().beOn(InnsendingKvitteringPage.class);

        return true;
    }

    public boolean innsendingenFullfuF8rtOgKvitteringVisesMedBehandlingsIdOgHovedskjemaMedTittelOgDokumentMedTittelSkalVuE6reSendtNei(String behandlingsIdVariabelnavn, String tittelHovedskjema, String tittelEgetVedlegg, String sendesNavInt) {
        List<Dokument> model = (List<Dokument>) wicketTester.get().component(withId("innsendteDokumenter")).getDefaultModelObject();

        Dokument hovedskjema = model.get(0);
        Dokument egetVedlegg = model.get(1);
        boolean riktigNavnHovedskjema = hovedskjema.getNavn().equalsIgnoreCase(tittelHovedskjema);
        boolean riktigNavnEgetVedlegg = egetVedlegg.getNavn().equalsIgnoreCase(tittelEgetVedlegg);
        String behandlingsId = Fixture.getSymbol(behandlingsIdVariabelnavn).toString();
        boolean riktigBehandlingsId = hovedskjema.getBehandlingsId().equalsIgnoreCase(behandlingsId);

        return riktigBehandlingsId && riktigNavnHovedskjema && riktigNavnEgetVedlegg;
    }
}