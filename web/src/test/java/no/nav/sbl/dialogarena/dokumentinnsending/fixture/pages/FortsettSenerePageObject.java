package no.nav.sbl.dialogarena.dokumentinnsending.fixture.pages;

import no.nav.modig.wicket.test.FluentWicketTester;
import no.nav.sbl.dialogarena.dokumentinnsending.WicketApplication;
import no.nav.sbl.dialogarena.dokumentinnsending.pages.fortsettsenere.EpostInput;
import no.nav.sbl.dialogarena.dokumentinnsending.pages.fortsettsenere.FortsettSenerePage;
import org.apache.wicket.Component;

import static no.nav.modig.wicket.test.matcher.ComponentMatchers.withId;

public class FortsettSenerePageObject {

	private FluentWicketTester<WicketApplication> tester;

	public FortsettSenerePageObject(FluentWicketTester<WicketApplication> fluentWicketTester) {
		this.tester = fluentWicketTester;
	}
	
	public boolean erLastet() {
		return tester.should().beOn(FortsettSenerePage.class) != null;
	}
	
	public boolean epostVises() {
		Component epostForm = tester.get().component(withId("epostForm"));
        EpostInput.EmailModel model = (EpostInput.EmailModel) epostForm.getDefaultModel().getObject();
        return model.epost != null;
	}
	
	public void send() {
		tester.inForm("sendLink:epostForm").submitWithAjaxButton(withId("sendEpost"));

	}
}