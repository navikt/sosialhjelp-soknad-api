package no.nav.sbl.dialogarena.websoknad.config;

import no.nav.sbl.dialogarena.print.HandleBarKjoerer;
import no.nav.sbl.dialogarena.print.HtmlGenerator;
import no.nav.sbl.dialogarena.selftest.SelfTest;
import no.nav.sbl.dialogarena.soknadinnsending.business.selftest.SelfTestService;
import no.nav.sbl.dialogarena.soknadinnsending.sikkerhet.SikkerhetsAspect;
import no.nav.sbl.dialogarena.soknadinnsending.sikkerhet.Tilgangskontroll;
import no.nav.sbl.dialogarena.websoknad.service.EmailService;
import no.nav.sbl.dialogarena.utils.InnloggetBruker;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.support.PropertySourcesPlaceholderConfigurer;
import org.springframework.core.task.TaskExecutor;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.web.multipart.MultipartResolver;
import org.springframework.web.multipart.commons.CommonsMultipartResolver;

import static org.slf4j.LoggerFactory.getLogger;

/**
 * Applikasjonskontekst for ear-modulen.
 */
@Configuration
public class ApplicationConfig {

    private static final Logger logger = getLogger(ApplicationConfig.class);

    @Value("${dialogarena.navnolink.url}")
    private String navigasjonslink;
    @Value("${dokumentinnsending.smtpServer.port}")
    private String smtpServerPort;
    @Value("${dokumentinnsending.smtpServer.host}")
    private String smtpServerHost;
    @Bean
    public static PropertySourcesPlaceholderConfigurer placeholderConfigurer() {
        PropertySourcesPlaceholderConfigurer propertySourcesPlaceholderConfigurer = new PropertySourcesPlaceholderConfigurer();
        propertySourcesPlaceholderConfigurer.setLocalOverride(true);
        return propertySourcesPlaceholderConfigurer;
    }

    @Bean
    public SikkerhetsAspect sikkerhet() {
        return new SikkerhetsAspect();
    }

    @Bean
    public Tilgangskontroll tilgangskontroll() {
        return new Tilgangskontroll();
    }

    @Bean
    public TaskExecutor thumbnailExecutor() {
        ThreadPoolTaskExecutor threadPoolTaskExecutor = new ThreadPoolTaskExecutor();
        threadPoolTaskExecutor.setCorePoolSize(1);
        threadPoolTaskExecutor.setMaxPoolSize(10);
        return threadPoolTaskExecutor;
    }

    @Bean
    public EmailService epostUtsender() {
        return new EmailService();
    }

    @Bean
    public JavaMailSender mailSender() {
        JavaMailSenderImpl javaMailSender = new JavaMailSenderImpl();
        javaMailSender.setDefaultEncoding("UTF-8");
        javaMailSender.setHost(smtpServerHost);
        //TODO: if/else er quickfix inntil vi får ApplicationContextTest til å lese mailserverport.
        if (smtpServerHost.matches("-?\\d+")) {
            javaMailSender.setPort(Integer.parseInt(smtpServerPort));
        } else {
            javaMailSender.setPort(25);
            logger.error("Smtpport not set properly, using default port 25");
        }
        return javaMailSender;
    }

    @Bean
    public String navigasjonslink() {
        return navigasjonslink;
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
        commonsMultipartResolver.setMaxUploadSize(10 * 1024 * 1024);
        return commonsMultipartResolver;
    }

    @Bean
    public InnloggetBruker innloggetBruker() {
        return new InnloggetBruker();
    }

    @Bean
    public HtmlGenerator handleBarKjoerer(){
        return new HandleBarKjoerer();
    }

    @Bean
    public SelfTestService selfTestService() {
        return new SelfTestService();
    }

    @Bean
    public SelfTest selfTest(){
        return new SelfTest();
    }

}