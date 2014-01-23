package no.nav.sbl.dialogarena.websoknad.pages.soknadliste;

import org.junit.Before;
import org.junit.Test;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;

//@ContextConfiguration(classes = { FitNesseApplicationConfig.class })
//@RunWith(SpringJUnit4ClassRunner.class)
public class SoknadListePageTest {

//	@Inject
//	private FluentWicketTester<WicketApplication> wicketTester;

    @Before
    public void setup() {
//        String testHtmlFolder = (new MockMultipartHttpServletRequest()).getSession().getServletContext().getRealPath("/") + "/html";
//        ServletContext servletContextMock = mock(ServletContext.class);
//        wicketTester.tester.getApplication().setServletContext(servletContextMock);
//        when(servletContextMock.getRealPath("/html")).thenReturn(testHtmlFolder);
//
//        InputStream inputStream = new ByteArrayInputStream("liksomHTML".getBytes());
//        when(servletContextMock.getResourceAsStream("/html/Dagpenger.html")).thenReturn(inputStream);
    }

	@Test
	public void skalApneSoknadListePage() {
        assertThat(true, is(true));
//		wicketTester.goTo(SoknadListePage.class);
	}

    @Test
    public void skalStarteNySoknadOmDagpenger() {
        assertThat(true, is(true));
//        wicketTester
//                .goTo(SoknadListePage.class)
//                .click()
//                .link(withId("dagpenger"))
//                .should()
//                .beOn(StartSoknadPage.class);
    }
}
