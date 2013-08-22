package no.nav.sbl.dialogarena.dokumentinnsending.fixture.regresjon;

import static no.nav.modig.wicket.test.matcher.ComponentMatchers.withId;
import static org.apache.commons.lang3.StringEscapeUtils.unescapeHtml4;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import no.nav.modig.test.fitnesse.fixture.SpringAwareDoFixture;
import no.nav.modig.wicket.test.FluentWicketTester;
import no.nav.sbl.dialogarena.dokumentinnsending.WicketApplication;
import no.nav.sbl.dialogarena.dokumentinnsending.config.FitNesseApplicationConfig;
import no.nav.sbl.dialogarena.dokumentinnsending.fixture.data.PersonProfil;
import no.nav.sbl.dialogarena.dokumentinnsending.fixture.data.SetupBrukerprofilIntegration;
import no.nav.sbl.dialogarena.dokumentinnsending.fixture.pages.BekreftelsesPageObject;
import no.nav.sbl.dialogarena.dokumentinnsending.fixture.pages.FortsettSenereKvitteringPageObject;
import no.nav.sbl.dialogarena.dokumentinnsending.fixture.pages.FortsettSenerePageObject;
import no.nav.sbl.dialogarena.dokumentinnsending.fixture.pages.InnsendingKvitteringPageObject;
import no.nav.sbl.dialogarena.dokumentinnsending.fixture.pages.OversiktPageObject;
import no.nav.sbl.dialogarena.dokumentinnsending.fixture.utils.TestUtils;
import no.nav.sbl.dialogarena.dokumentinnsending.pages.bekreft.BekreftelsesPage;
import no.nav.sbl.dialogarena.dokumentinnsending.service.DokumentServiceMock;
import no.nav.sbl.dialogarena.dokumentinnsending.service.PersonServiceMock;
import no.nav.sbl.dialogarena.soknad.kodeverk.KodeverkSkjema;
import no.nav.tjeneste.domene.brukerdialog.henvendelse.v1.informasjon.WSBrukerBehandlingType;
import no.nav.tjeneste.domene.brukerdialog.henvendelse.v1.informasjon.WSDokumentForventning;
import no.nav.tjeneste.domene.brukerdialog.henvendelse.v1.informasjon.WSInnsendingsValg;

import org.apache.commons.lang3.StringUtils;
import org.apache.wicket.Component;
import org.springframework.test.context.ContextConfiguration;

@ContextConfiguration(classes = FitNesseApplicationConfig.class)
public class SlimDriver extends SpringAwareDoFixture {

	@Inject
	public DokumentServiceMock dokumentService;

	@Inject
	private FluentWicketTester<WicketApplication> wicketTester;

	@Inject
	private PersonServiceMock personService;

	private String behandlingsId;
	
	private List<WSDokumentForventning> dokumentForventninger;

	private String foedselsnr;
	private SetupBrukerprofilIntegration integration;
	
	private FluentWicketTester<WicketApplication> fluentWicketTester;
	
	private BekreftelsesPageObject bekreftelsesPage;
	
	private OversiktPageObject oversiktPage;

	private FortsettSenerePageObject fortsettSenerePage;

	private FortsettSenereKvitteringPageObject forsettSenereKvitteringPage;

	private InnsendingKvitteringPageObject kvitteringsPage;
	
	public SlimDriver(String navSkjema, String[] vedlegg) throws Exception {
		super.setUp();
		init();
		this.dokumentForventninger = new ArrayList<>();
		
		if (navSkjema != null) {
			dokumentService.stub(lagKodeverkSkjema(navSkjema, "Krav om dagpenger"));
		}
		for (String v : vedlegg) {
			dokumentService.stub(lagKodeverkSkjema(v, ""));
		}
		integration = new SetupBrukerprofilIntegration(personService, dokumentService);
		bekreftelsesPage = new BekreftelsesPageObject();
		oversiktPage = new OversiktPageObject();
	}
	
	private void init() {
        System.setProperty("minehenvendelser.link.url", "");
		if (dokumentService != null) {
			dokumentService.clear();
		}
	}

	public void clean()  {
		integration = null;
		bekreftelsesPage = null;
		oversiktPage = null;
		dokumentForventninger = null;
	}
	
	public void gittAtBrukerMedFoedselsnrHarAdresse(String foedselsnr, String adresse) throws Exception{
		this.foedselsnr = foedselsnr;
		PersonProfil personProfil = null;

		if (StringUtils.equalsIgnoreCase("midlertidig adresse utland",adresse)) {
			personProfil = PersonProfil.lagPersonMedMidlertidigAdresseUtland(foedselsnr);
		} else if (StringUtils.equalsIgnoreCase("postadresse utland", adresse))  {
			personProfil = PersonProfil.lagPersonMedPostadresseUtland(foedselsnr);
		} else {
			personProfil = PersonProfil.lagPersonBosattINorgeMedEpost(foedselsnr);
		}
		integration.stub(personProfil);

		TestUtils.innloggetBrukerEr(foedselsnr);
		this.foedselsnr = foedselsnr;
	}

	public boolean senderElektroniskSoknadMedIdOgMedInnsendingsvalg(String id, String innsendingsvalg) {
		return lagDokumentForventning(innsendingsvalg, id, true) != null;
	}
	
	public boolean senderVedleggMedIdOgMedInnsendingsvalg(String id, String innsendingsValg) {
		return lagDokumentForventning(innsendingsValg, id, false) != null;
	}
	
	public boolean ettersenderSkjemaMedIdOgMedTittelOgMedInnsendingsvalg(String id, String tittel, String innsendingsvalg) {
		return lagDokumentForventning(innsendingsvalg, id, true) != null;
	}
	
	public boolean ettersenderNavVedleggMedTittelOgMedInnsendingsvalg(String tittel, String innsendingsValg) {
		return lagDokumentForventning(innsendingsValg, tittel, true) != null;
	}
	
	public boolean ettersenderEgetVedleggMedTittelOgMedInnsendingsvalg(String tittel, String innsendingsValg) {
		return lagDokumentForventning(innsendingsValg, tittel, false) != null;
	}
	
	public boolean ettersenderAnnetVedleggMedTittelOgMedInnsendingsvalg(String tittel, String innsendingsValg) {
		return lagDokumentForventning(innsendingsValg, tittel, false) != null;
	}
	
	public boolean naarBrukerKommerTilBekreftelsessiden() {
		behandlingsId = dokumentService.opprettDokumentBehandling(dokumentForventninger, WSBrukerBehandlingType.DOKUMENT_ETTERSENDING, foedselsnr);
		dokumentService.identifiserAktor(behandlingsId, foedselsnr);
		
		for (WSDokumentForventning wsDokumentForventning : dokumentForventninger) {
			if (wsDokumentForventning.getInnsendingsValg().equals(WSInnsendingsValg.LASTET_OPP)) {
				dokumentService.opprettDokument(dokumentService.createDokument(wsDokumentForventning.getKodeverkId()), wsDokumentForventning.getId());
			}
		}
		
        fluentWicketTester = wicketTester.goTo(BekreftelsesPage.class, TestUtils.withBrukerBehandlingId(behandlingsId));
        return true;
	}
	
	public void naarBrukerVelgerFortsettSenere() {
		bekreftelsesPage.forsettSenere(fluentWicketTester);
		this.fortsettSenerePage = new FortsettSenerePageObject(fluentWicketTester);
	}
	
	public void naarBrukerVelgerSend() {
		fortsettSenerePage.send();
		this.forsettSenereKvitteringPage = new FortsettSenereKvitteringPageObject(fluentWicketTester);
	}
	
	public boolean bekrefterOgSenderInn() {
		return true;
	}
	
	public boolean fortsettSenereDialogenVises() {
		return fortsettSenerePage.erLastet();
	}
	
	public boolean epostVaereFerdigUtfylt() {
		return fortsettSenerePage.epostVises();
	}
	
	public boolean kvitteringForSendtEpostVises()  {
		return forsettSenereKvitteringPage.vises();
	}
	
	public boolean brukerNavigererTilbakeTilForrigeSide() {
		return bekreftelsesPage.navigerTilbake(fluentWicketTester);
	}
	
	public boolean brukerEndrerTittelPaaEgetVedleggTil(String tittel) {
		oversiktPage.endreTittelPaaVedlegg(fluentWicketTester, tittel);
		return false;
	}	

	public boolean nyTittelViseDokumentnavn(String dokumentnavn) {
		
		List<Component> dokumentNavnComponents = wicketTester.get().components(withId("dokumentNavn"));
		
        for (Component dokumentNavnComponent : dokumentNavnComponents) {
            String dokumentNavn = StringUtils.remove(unescapeHtml4(dokumentNavnComponent.getDefaultModelObjectAsString()), "Annet: ");
            if (dokumentNavn.equals(dokumentnavn)) {
                return true;
            }
        }

        return false;
	}
	
	public boolean innsendingskvitteringenVises() {
		return kvitteringsPage.vises();
	}
	
	public boolean journalfoerendeNavenhetSendes(String navenhet) {
		return dokumentService.skalSendesTilNavInternasjonalEnhet(navenhet, behandlingsId);
	}
	
	public void og() {}
	
	public void saa() {}
	
	public void saaSkal() {}

	public boolean informasjonOmUtenlandsadresseVises() {
		return bekreftelsesPage.erInformasjonOmUtenlandsAdresseSynlig(fluentWicketTester);
	}
	
	public boolean informasjonOmUtlandsadresseIkkeVises() {
		return !bekreftelsesPage.erInformasjonOmUtenlandsAdresseSynlig(fluentWicketTester);
	}
	
	public boolean naarBrukerVelgerAlternativ(String alternativ) {
		bekreftelsesPage.velgAlternativForAdresseOgBekreft(fluentWicketTester, Integer.parseInt(alternativ.replace("valg", "")));
		kvitteringsPage = new InnsendingKvitteringPageObject(fluentWicketTester);
        return kvitteringsPage.vises();
	}
	
	public boolean naarBrukerBekrefter() {
		bekreftelsesPage.samtykkOgBekreft(fluentWicketTester);
		kvitteringsPage = new InnsendingKvitteringPageObject(fluentWicketTester);
		return true;
	}
	
	private WSDokumentForventning lagDokumentForventning(String innsendingsValg, String kodeverkId, boolean hovedSkjema) {
		WSDokumentForventning forventning = new WSDokumentForventning()
			.withKodeverkId(kodeverkId)
			.withInnsendingsValg(TestUtils.mapInnsendingsValg(innsendingsValg))
			.withHovedskjema(hovedSkjema);
		dokumentForventninger.add(forventning);
		
		return forventning;
	}
	
	private KodeverkSkjema lagKodeverkSkjema(String kodeverkId, String tittel) {
		KodeverkSkjema skjema = new KodeverkSkjema();
		skjema.setVedleggsid(kodeverkId);
        skjema.setTittel(tittel);
        skjema.setSkjemanummer(kodeverkId);
        return skjema;
	}

	public boolean tittelVise(String tittel) {
		return bekreftelsesPage.skalTittelVise(fluentWicketTester, tittel);
	}
	
	public boolean dokumentViseStatus(String dokumentNavn, String status) {
		try{
			return bekreftelsesPage.riktigStatusVises(fluentWicketTester, dokumentNavn, status);
		}catch (Exception e) {
			return false;
		}
	}
}