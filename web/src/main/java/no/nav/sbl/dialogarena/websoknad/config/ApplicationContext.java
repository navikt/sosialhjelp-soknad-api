package no.nav.sbl.dialogarena.websoknad.config;

import no.nav.modig.cache.CacheConfig;
import no.nav.sbl.dialogarena.person.PersonServiceTPS;
import no.nav.sbl.dialogarena.soknadinnsending.db.SoknadInnsendingDBConfig;
import no.nav.sbl.dialogarena.websoknad.WicketApplication;
import no.nav.sbl.dialogarena.websoknad.service.EmailService;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.context.support.PropertySourcesPlaceholderConfigurer;
import org.springframework.mail.MailSender;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

/**
 * Applikasjonskontekst for ear-modulen.
 */
@Configuration
@Import({CacheConfig.class, FooterConfig.class, GAConfig.class, ConsumerConfig.class, ContentConfig.class, ServicesApplicationContext.class, SoknadInnsendingDBConfig.class})
public class ApplicationContext {

    @Value("${dialogarena.navnolink.url}")
    private String navigasjonslink;
    
    @Value("${dokumentinnsending.smtpServer.host}")
    private String smtpServerHost;

    @Value("${dokumentinnsending.smtpServer.port}")
    private Integer smtpServerPort;

    @Bean
    public static PropertySourcesPlaceholderConfigurer placeholderConfigurer() {
        return new PropertySourcesPlaceholderConfigurer();
    }

    @Bean
    public EmailService epostUtsender() {
        return new EmailService();
    }

    @Bean
    public MailSender mailSender() {
        JavaMailSenderImpl javaMailSender = new JavaMailSenderImpl();
        javaMailSender.setDefaultEncoding("UTF-8");
        javaMailSender.setHost(smtpServerHost);
        javaMailSender.setPort(smtpServerPort);
        return javaMailSender;
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
    public PersonServiceTPS personService() {
        return new PersonServiceTPS();
    }

    @Bean
    public ThreadPoolTaskExecutor threadPoolTaskExecutor() {
        ThreadPoolTaskExecutor threadPoolTaskExecutor = new ThreadPoolTaskExecutor();
        threadPoolTaskExecutor.setCorePoolSize(5);
        threadPoolTaskExecutor.setMaxPoolSize(20);
        threadPoolTaskExecutor.setWaitForTasksToCompleteOnShutdown(true);
        return threadPoolTaskExecutor;
    }
}