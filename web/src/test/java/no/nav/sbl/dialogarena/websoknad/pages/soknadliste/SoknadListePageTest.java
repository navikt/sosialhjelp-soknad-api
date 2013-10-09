package no.nav.sbl.dialogarena.websoknad.pages.soknadliste;

import javax.inject.Inject;

import no.nav.modig.wicket.test.FluentWicketTester;
import no.nav.sbl.dialogarena.websoknad.WicketApplication;
import no.nav.sbl.dialogarena.websoknad.config.FitNesseApplicationConfig;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

@ContextConfiguration(classes = { FitNesseApplicationConfig.class })
@RunWith(SpringJUnit4ClassRunner.class)
public class SoknadListePageTest {

	@Inject
	private FluentWicketTester<WicketApplication> wicketTester;


	@Test
	public void shouldRenderHomePage() {
		wicketTester.goTo(SoknadListePage.class);
	}

}
