package no.nav.sbl.dialogarena.websoknad.config;

import no.nav.modig.cache.CacheConfig;
import no.nav.sbl.dialogarena.dokumentinnsending.repository.SoknadRepository;
import no.nav.sbl.dialogarena.dokumentinnsending.service.PersonServiceTPS;
import no.nav.sbl.dialogarena.websoknad.WicketApplication;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.ImportResource;
import org.springframework.context.support.PropertySourcesPlaceholderConfigurer;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

/**
 * Applikasjonskontekst for ear-modulen.
 */
@Configuration
@Import({CacheConfig.class, FooterConfig.class, GAConfig.class, ConsumerConfig.class, ContentConfig.class})
public class ApplicationContext {

    @Value("${dialogarena.navnolink.url}")
    private String navigasjonslink;

    @Bean
    public static PropertySourcesPlaceholderConfigurer placeholderConfigurer() {
        return new PropertySourcesPlaceholderConfigurer();
    }

    @Bean
    public String navigasjonslink() {
        return navigasjonslink;
    }

    @Bean
    public WicketApplication dokumentinnsendingApplication() {
        return new WicketApplication();
    }

    @Bean
    public SoknadRepository soknadRepository() {
        return new SoknadRepository();
    }

    @Bean
    public PersonServiceTPS personService() {
        return new PersonServiceTPS();
    }

    /*@Bean
    public PersonServiceMock personService() {
        return new PersonServiceMock();
    }*/

    @Bean
    public ThreadPoolTaskExecutor threadPoolTaskExecutor() {
        ThreadPoolTaskExecutor threadPoolTaskExecutor = new ThreadPoolTaskExecutor();
        threadPoolTaskExecutor.setCorePoolSize(5);
        threadPoolTaskExecutor.setMaxPoolSize(20);
        threadPoolTaskExecutor.setWaitForTasksToCompleteOnShutdown(true);
        return threadPoolTaskExecutor;
    }
}