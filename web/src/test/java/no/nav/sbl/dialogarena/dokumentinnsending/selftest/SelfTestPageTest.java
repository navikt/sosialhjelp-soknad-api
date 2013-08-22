package no.nav.sbl.dialogarena.dokumentinnsending.selftest;

import no.nav.modig.core.context.ThreadLocalSubjectHandler;
import no.nav.modig.wicket.test.FluentWicketTester;
import no.nav.sbl.dialogarena.dokumentinnsending.WicketApplication;
import no.nav.sbl.dialogarena.dokumentinnsending.config.FitNesseApplicationConfig;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import javax.inject.Inject;

@ContextConfiguration(classes = FitNesseApplicationConfig.class)
@RunWith(SpringJUnit4ClassRunner.class)
public class SelfTestPageTest {

    @Inject
    private FluentWicketTester<WicketApplication> wicketTester;

    @Before
    public void oppsett() {
        System.setProperty("no.nav.modig.core.context.subjectHandlerImplementationClass", ThreadLocalSubjectHandler.class.getName());
    }

    @Test
    public void shouldRenderSelfTestPage() {
        wicketTester.goTo(SelfTestPage.class);
    }
}