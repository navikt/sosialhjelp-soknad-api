package no.nav.sbl.dialogarena.websoknad.pages.soknadliste;

import no.nav.modig.wicket.test.FluentWicketTester;
import no.nav.sbl.dialogarena.websoknad.WicketApplication;
import no.nav.sbl.dialogarena.websoknad.config.TestApplicationConfig;
import no.nav.sbl.dialogarena.websoknad.pages.utslagskriterier.UtslagskriterierDagpengerPage;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.springframework.mock.jndi.SimpleNamingContextBuilder;
import org.springframework.mock.web.MockMultipartHttpServletRequest;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import javax.inject.Inject;
import javax.naming.NamingException;
import javax.servlet.ServletContext;
import javax.sql.DataSource;
import java.io.File;
import java.net.MalformedURLException;

import static no.nav.modig.wicket.test.matcher.ComponentMatchers.withId;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ContextConfiguration(classes = { TestApplicationConfig.class })
@RunWith(SpringJUnit4ClassRunner.class)
public class SoknadListePageTest {

	@Inject
	private FluentWicketTester<WicketApplication> wicketTester;

    @BeforeClass
    public static void beforeClass() throws NamingException {
        SimpleNamingContextBuilder builder = new SimpleNamingContextBuilder();
        builder.bind("jdbc/SoknadInnsendingDS", Mockito.mock(DataSource.class));
        builder.activate();
    }

    @Before
    public void setup() throws MalformedURLException {
        String testHtmlFolder = (new MockMultipartHttpServletRequest()).getSession().getServletContext().getRealPath("/") + "/html";
        ServletContext servletContextMock = mock(ServletContext.class);
        wicketTester.tester.getApplication().setServletContext(servletContextMock);
        when(servletContextMock.getResource("/")).thenReturn(new File(testHtmlFolder).toURI().toURL());
    }

	@Test
	public void skalApneSoknadListePage() {
		wicketTester.goTo(SoknadListePage.class);
	}
	
    @Test
    public void skalStarteNySoknadOmDagpenger() {
        wicketTester
                .goTo(SoknadListePage.class)
                .click()
                .link(withId("dagpenger"))
                .should()
                .beOn(UtslagskriterierDagpengerPage.class);
    }
}
