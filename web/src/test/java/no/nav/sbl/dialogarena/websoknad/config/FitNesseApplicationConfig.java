package no.nav.sbl.dialogarena.websoknad.config;


import java.util.Locale;

import javax.inject.Inject;

import no.nav.modig.content.CmsContentRetriever;
import no.nav.modig.wicket.test.FluentWicketTester;
import no.nav.sbl.dialogarena.InMemorySoknadInnsendingRepository;
import no.nav.sbl.dialogarena.SoknadInnsendingRepository;
import no.nav.sbl.dialogarena.WebSoknadServiceMock;
import no.nav.sbl.dialogarena.websoknad.WicketApplication;
import no.nav.sbl.dialogarena.websoknad.servlet.SoknadDataController;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;

@Import({FooterConfig.class, GAConfig.class, ContentConfigTest.class})
public class FitNesseApplicationConfig {

    @Value("${websoknad.navigasjonslink.url}")
    private String navigasjonslink;

    @Value("${websoknad.logoutURL.url}")
    private String logoutURL;

	
    @Bean
    public String navigasjonslink() {
        return navigasjonslink;
    }

    @Bean
    public String logoutURL() {
        return logoutURL;
    }

    
	@Bean
    public SoknadDataController soknadDataController() {
        return new SoknadDataController();
    }
	 
	@Bean
	public WebSoknadServiceMock webSoknadService() {
		return new WebSoknadServiceMock();
	}
	
	@Bean
	public SoknadInnsendingRepository soknadInnsendingRepository() {
		return new InMemorySoknadInnsendingRepository();
	}
	
    @Bean
    public FluentWicketTester<WicketApplication> wicketTester(WicketApplication application) {
        FluentWicketTester<WicketApplication> wicketTester = new FluentWicketTester<>(application);
        wicketTester.tester.getSession().setLocale(new Locale("NO"));
        return wicketTester;
    }
    
    @Bean
    public WicketApplication soknadsInnsendingApplication() {
        return new WicketApplication();
    }

}