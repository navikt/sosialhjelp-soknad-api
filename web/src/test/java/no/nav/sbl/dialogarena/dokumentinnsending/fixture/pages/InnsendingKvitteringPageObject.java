package no.nav.sbl.dialogarena.dokumentinnsending.fixture.pages;

import no.nav.modig.wicket.test.FluentWicketTester;
import no.nav.sbl.dialogarena.dokumentinnsending.WicketApplication;
import no.nav.sbl.dialogarena.dokumentinnsending.pages.innsendingkvittering.InnsendingKvitteringPage;

public class InnsendingKvitteringPageObject {

	private FluentWicketTester<WicketApplication> tester;

	public InnsendingKvitteringPageObject(
			FluentWicketTester<WicketApplication> fluentWicketTester) {
		this.tester = fluentWicketTester;
	}

	public boolean vises() {
		return tester.should().beOn(InnsendingKvitteringPage.class) != null;
	}
}