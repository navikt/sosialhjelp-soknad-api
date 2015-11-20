package no.nav.sbl.dialogarena.rest.actions;

import no.nav.sbl.dialogarena.rest.utils.PDFService;
import no.nav.sbl.dialogarena.service.EmailService;
import no.nav.sbl.dialogarena.service.HtmlGenerator;
import no.nav.sbl.dialogarena.soknadinnsending.business.message.NavMessageSource;
import no.nav.sbl.dialogarena.soknadinnsending.business.service.VedleggService;
import no.nav.sbl.dialogarena.soknadinnsending.business.service.soknadservice.SoknadService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

import static org.mockito.Mockito.mock;

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

}
