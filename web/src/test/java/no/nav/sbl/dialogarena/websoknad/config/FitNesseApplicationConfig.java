package no.nav.sbl.dialogarena.websoknad.config;


import no.nav.modig.wicket.test.FluentWicketTester;
import no.nav.sbl.dialogarena.soknadinnsending.db.SoknadRepository;
import no.nav.sbl.dialogarena.soknadinnsending.db.SoknadRepositoryJdbc;
import no.nav.sbl.dialogarena.soknadinnsending.db.config.DatabaseTestContext;
import no.nav.sbl.dialogarena.websoknad.service.LocalDBSoknadService;
import no.nav.sbl.dialogarena.websoknad.WicketApplication;
import no.nav.sbl.dialogarena.websoknad.servlet.SoknadDataController;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;

import java.util.Locale;

@Import({FooterConfig.class, GAConfig.class, ContentConfigTest.class, DatabaseTestContext.class,ConsumerConfigTest.class})
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
	public LocalDBSoknadService webSoknadService() {
		return new LocalDBSoknadService();
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
    
    @Bean
    public SoknadRepository soknadInnsendingRepository() {
    	return new SoknadRepositoryJdbc();
	
    }

}