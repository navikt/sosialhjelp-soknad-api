package no.nav.sbl.dialogarena.websoknad.config;

import no.nav.modig.cache.CacheConfig;
import no.nav.sbl.dialogarena.person.PersonServiceTPS;
import no.nav.sbl.dialogarena.soknadinnsending.business.BusinessConfig;
import no.nav.sbl.dialogarena.soknadinnsending.business.db.SoknadInnsendingDBConfig;
import no.nav.sbl.dialogarena.websoknad.WicketApplication;
import no.nav.sbl.dialogarena.websoknad.service.EmailService;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.context.support.PropertySourcesPlaceholderConfigurer;
import org.springframework.mail.MailSender;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.web.multipart.MultipartResolver;
import org.springframework.web.multipart.commons.CommonsMultipartResolver;

import static org.slf4j.LoggerFactory.getLogger;

/**
 * Applikasjonskontekst for ear-modulen.
 */
@Configuration
@Import({
        BusinessConfig.class,
        CacheConfig.class,
        FooterConfig.class,
        GAConfig.class,
        ConsumerConfig.class,
        ContentConfig.class,
        ServicesApplicationContext.class,
        SoknadInnsendingDBConfig.class})
public class ApplicationContext {

    private static final Logger LOG = getLogger(ApplicationContext.class);
    @Value("${dialogarena.navnolink.url}")
    private String navigasjonslink;


    //TODO Når enconfig funker må dette fikses
   // @Value("${dokumentinnsending.smtpServer.host}")
    private String smtpServerHost = "smtp.test.local";
    //Value("${dokumentinnsending.smtpServer.port}")
    private int smtpServerPort = 25;

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
        LOG.error("SMTPPORT" + smtpServerPort + "HOST" + smtpServerHost + "Link" + navigasjonslink);
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

    @Bean
    public MultipartResolver multipartResolver() {
        CommonsMultipartResolver commonsMultipartResolver = new CommonsMultipartResolver();
        commonsMultipartResolver.setMaxUploadSize(10 * 1024 * 1000);
        return commonsMultipartResolver;
    }
}