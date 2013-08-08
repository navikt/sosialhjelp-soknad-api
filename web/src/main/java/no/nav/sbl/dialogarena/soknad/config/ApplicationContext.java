package no.nav.sbl.dialogarena.soknad.config;

import no.nav.sbl.dialogarena.soknad.WicketApplication;
import no.nav.sbl.dialogarena.soknad.service.SoknadService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.context.support.PropertySourcesPlaceholderConfigurer;

@Configuration
@Import({ConsumerConfig.class, FooterConfig.class, ContentConfig.class})
public class ApplicationContext {

    @Value("${soknad.navigasjonslink.url}")
    private String navigasjonslink;

    @Value("${soknad.logoutURL.url}")
    private String logoutURL;

    @Bean
    public static PropertySourcesPlaceholderConfigurer placeholderConfigurer() {
        return new PropertySourcesPlaceholderConfigurer();
    }

    @Bean
    public WicketApplication soknadApplication() {
        return new WicketApplication();
    }

    @Bean
    public SoknadService soknadService() {
        return new SoknadService();
    }

    @Bean
    public String navigasjonslink() {
        return navigasjonslink;
    }

    @Bean
    public String logoutURL() {
        return logoutURL;
    }
}