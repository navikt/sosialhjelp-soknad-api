package no.nav.sbl.dialogarena.websoknad.pages.soknadliste;

import static no.nav.modig.wicket.test.matcher.ComponentMatchers.withId;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import no.nav.modig.wicket.test.FluentWicketTester;
import no.nav.sbl.dialogarena.websoknad.WicketApplication;
import no.nav.sbl.dialogarena.websoknad.config.FitNesseApplicationConfig;
import no.nav.sbl.dialogarena.websoknad.pages.startsoknad.StartSoknadPage;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.mock.web.MockMultipartHttpServletRequest;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import javax.inject.Inject;
import javax.servlet.ServletContext;

import java.io.ByteArrayInputStream;
import java.io.InputStream;

@ContextConfiguration(classes = { FitNesseApplicationConfig.class })
@RunWith(SpringJUnit4ClassRunner.class)
public class SoknadListePageTest {

	@Inject
	private FluentWicketTester<WicketApplication> wicketTester;

    @Before
    public void setup() {
        String testHtmlFolder = (new MockMultipartHttpServletRequest()).getSession().getServletContext().getRealPath("/") + "/html";
        ServletContext servletContextMock = mock(ServletContext.class);
        wicketTester.tester.getApplication().setServletContext(servletContextMock);
        when(servletContextMock.getRealPath("/html")).thenReturn(testHtmlFolder);

        InputStream inputStream = new ByteArrayInputStream("liksomHTML".getBytes());
        when(servletContextMock.getResourceAsStream("/html/Dagpenger.html")).thenReturn(inputStream);
    }

	@Test
	public void skalApneSoknadListePage() {
        assertThat(true, is(true));
		wicketTester.goTo(SoknadListePage.class);
	}

    @Test
    public void skalStarteNySoknadOmDagpenger() {
        assertThat(true, is(true));
        wicketTester
                .goTo(SoknadListePage.class)
                .click()
                .link(withId("dagpenger"))
                .should()
                .beOn(StartSoknadPage.class);
    }
}
