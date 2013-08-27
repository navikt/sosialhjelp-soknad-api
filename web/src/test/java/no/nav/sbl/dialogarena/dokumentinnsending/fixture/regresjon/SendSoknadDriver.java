package no.nav.sbl.dialogarena.dokumentinnsending.fixture.regresjon;

import javax.inject.Inject;

import no.nav.modig.test.fitnesse.fixture.SpringAwareDoFixture;
import no.nav.modig.wicket.test.FluentWicketTester;
import no.nav.sbl.dialogarena.dokumentinnsending.WicketApplication;
import no.nav.sbl.dialogarena.dokumentinnsending.config.FitNesseApplicationConfig;
import no.nav.sbl.dialogarena.dokumentinnsending.fixture.pages.StartSoeknadPage;

import org.springframework.test.context.ContextConfiguration;

@ContextConfiguration(classes = FitNesseApplicationConfig.class)
public class SendSoknadDriver extends SpringAwareDoFixture {

	@Inject
	private FluentWicketTester<WicketApplication> wicketTester;
	
	private StartSoeknadPage startSoeknadPage;
	
	public SendSoknadDriver() throws Exception {
		super.setUp();
	}
	
	public long startSoeknad() {
		return 1L;
	}
	      
}