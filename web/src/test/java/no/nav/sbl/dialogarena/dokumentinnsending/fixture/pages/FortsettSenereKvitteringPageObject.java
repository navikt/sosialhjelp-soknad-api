package no.nav.sbl.dialogarena.dokumentinnsending.fixture.pages;

import no.nav.modig.wicket.test.FluentWicketTester;
import no.nav.sbl.dialogarena.dokumentinnsending.WicketApplication;
import no.nav.sbl.dialogarena.dokumentinnsending.pages.fortsettsenere.FortsettSenereKvitteringPage;

public class FortsettSenereKvitteringPageObject {

	private FluentWicketTester<WicketApplication> tester;

	public FortsettSenereKvitteringPageObject(FluentWicketTester<WicketApplication> fluentWicketTester) {
		this.tester = fluentWicketTester;
	}
	
	public boolean vises() {
		return tester.should().beOn(FortsettSenereKvitteringPage.class) != null;
	}
}