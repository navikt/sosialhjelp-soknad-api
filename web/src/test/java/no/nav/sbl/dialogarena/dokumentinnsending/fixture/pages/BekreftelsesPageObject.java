package no.nav.sbl.dialogarena.dokumentinnsending.fixture.pages;

import static no.nav.modig.wicket.test.matcher.ComponentMatchers.withId;

import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.apache.wicket.markup.html.WebMarkupContainer;

import no.nav.modig.wicket.test.FluentWicketTester;
import no.nav.modig.wicket.test.internal.FluentFormTester;
import no.nav.sbl.dialogarena.dokumentinnsending.WicketApplication;
import no.nav.sbl.dialogarena.dokumentinnsending.domain.Dokument;
import no.nav.sbl.dialogarena.dokumentinnsending.domain.InnsendingsValg;
import no.nav.sbl.dialogarena.dokumentinnsending.fixture.utils.TestUtils;
import no.nav.sbl.dialogarena.dokumentinnsending.pages.bekreft.BekreftelsesPage;
import no.nav.sbl.dialogarena.dokumentinnsending.pages.oversikt.OversiktPage;

public class BekreftelsesPageObject {


	private FluentWicketTester<WicketApplication> fluentWicketTester;

	public BekreftelsesPageObject(){}
	public BekreftelsesPageObject(
			FluentWicketTester<WicketApplication> wicketTester, String behandlingsId) {
		this.fluentWicketTester = wicketTester;
		fluentWicketTester.goTo(BekreftelsesPage.class, TestUtils.withBrukerBehandlingId(behandlingsId));
	}

	public void samtykkOgBekreft(FluentWicketTester<WicketApplication> fluentWicketTester) {
		FluentFormTester fluentFormTester = new FluentFormTester(fluentWicketTester, "bekreftelsesForm");
        fluentFormTester.check("samtykket", true);
        bekreft(fluentWicketTester, fluentFormTester);
	}
	
	public void velgAlternativForAdresseOgBekreft(FluentWicketTester<WicketApplication> fluentWicketTester, int valg) {
		FluentFormTester fluentFormTester = new FluentFormTester(fluentWicketTester, "bekreftelsesForm");
        fluentFormTester.check("samtykket", true);
        fluentFormTester.select("utenlandskAdresse:adresseGruppe",  valg - 1);
        
        bekreft(fluentWicketTester, fluentFormTester);
	}

	private void bekreft(FluentWicketTester<WicketApplication> fluentWicketTester,
			FluentFormTester fluentFormTester) {
		fluentWicketTester.get().component(withId("submit")).render();
        fluentWicketTester.get().component(withId("submit")).isEnabled();
        fluentFormTester.submitWithAjaxButton(withId("submit"));
	}
	
	public boolean skalTittelVise(FluentWicketTester<WicketApplication> wicketTester, String tittel) {
		List<Dokument> model = (List<Dokument>) wicketTester.get().component(withId("innsendteDokumenter")).getDefaultModelObject();
        Dokument dokument = model.get(0);
        return tittel.equalsIgnoreCase(dokument.getNavn());
	}

	public boolean navigerTilbake(FluentWicketTester<WicketApplication> fluentWicketTester) {
		fluentWicketTester.click().link(withId("tilbake"))
		        .should().beOn(OversiktPage.class);
		
		return true;
	}

	public boolean erInformasjonOmUtenlandsAdresseSynlig(FluentWicketTester<WicketApplication> fluentWicketTester) {
		WebMarkupContainer utenlandskAdresse = fluentWicketTester.get().component(withId("utenlandskAdresse")); 
        return utenlandskAdresse.isVisibleInHierarchy();
	}

	public boolean riktigStatusVises(FluentWicketTester<WicketApplication> fluentWicketTester,String dokumentNavn, String dokumentStatus) {
		List<Dokument> model = null;
		if (InnsendingsValg.LASTET_OPP.equals(TestUtils.konverterStringTilInnsendingsvalg(dokumentStatus))) {
			model = (List<Dokument>) fluentWicketTester.get().component(withId("innsendteDokumenter")).getDefaultModelObject();
		} else if (InnsendingsValg.SENDES_IKKE.equals(TestUtils.konverterStringTilInnsendingsvalg(dokumentStatus))) {
			model = (List<Dokument>) fluentWicketTester.get().component(withId("ikkeSendteDokumenter")).getDefaultModelObject();
		} else {
			return false;
		}
		
		for (Dokument dokument : model) {
			if (StringUtils.equals(dokument.getKodeverkId(), dokumentNavn)) {
				return true;
			}
		}
		return false;
//		InnsendingsValg forventetInnsendingsvalg = TestUtils.konverterStringTilInnsendingsvalg(dokumentStatus);
//		return dokument.getValg().equals(forventetInnsendingsvalg) && StringUtils.equals(dokument.getKodeverkId(),dokumentNavn);
	}

	public void forsettSenere(FluentWicketTester<WicketApplication> fluentWicketTester) {
		fluentWicketTester.click().link(withId("fortsettSenere"));
	}
}
