package no.nav.sbl.dialogarena.config;

import no.nav.sbl.dialogarena.rest.actions.*;
import no.nav.sbl.dialogarena.rest.utils.*;
import no.nav.sbl.dialogarena.service.*;
import no.nav.sbl.dialogarena.soknadinnsending.business.WebSoknadConfig;
import no.nav.sbl.dialogarena.soknadinnsending.business.service.*;
import no.nav.sbl.dialogarena.soknadinnsending.business.service.soknadservice.*;
import org.springframework.context.annotation.*;

import static org.mockito.Mockito.*;

@Configuration
@Import(DummyHolderConfig.class)
public class SoknadActionsTestConfig {

    @Bean
    public NavMessageSource tekster() {
        return mock(NavMessageSource.class);
    }

    @Bean
    public EmailService emailService() {
        return mock(EmailService.class);
    }

    @Bean
    public SoknadService soknadService() {
        return mock(SoknadService.class);
    }

    @Bean
    public VedleggService vedleggService() {
        return mock(VedleggService.class);
    }

    @Bean
    public HtmlGenerator pdfTemplate() {
        return mock(HtmlGenerator.class);
    }

    @Bean
    public SoknadActions soknadActions() {
        return new SoknadActions();
    }

    @Bean
    public PDFService pdfService() {
        return new PDFService();
    }

    @Bean
    public WebSoknadConfig config() {
        return mock(WebSoknadConfig.class);
    }

}
