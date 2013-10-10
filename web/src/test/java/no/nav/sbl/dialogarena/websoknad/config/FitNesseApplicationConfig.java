package no.nav.sbl.dialogarena.websoknad.config;


import no.nav.sbl.dialogarena.InMemorySoknadInnsendingRepository;
import no.nav.sbl.dialogarena.SoknadInnsendingRepository;
import no.nav.sbl.dialogarena.websoknad.service.LocalDBSoknadService;
import no.nav.sbl.dialogarena.websoknad.servlet.SoknadDataController;

import org.springframework.context.annotation.Bean;


public class FitNesseApplicationConfig {

	@Bean
    public SoknadDataController soknadDataController() {
        return new SoknadDataController();
    }
	 
	@Bean
	public LocalDBSoknadService webSoknadService() {
		return new LocalDBSoknadService();
	}
	
	@Bean
	public SoknadInnsendingRepository soknadInnsendingRepository() {
		return new InMemorySoknadInnsendingRepository();
	}
}