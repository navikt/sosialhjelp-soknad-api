package no.nav.sbl.dialogarena.config;

import no.finn.unleash.DefaultUnleash;
import no.finn.unleash.Unleash;
import no.finn.unleash.UnleashContext;
import no.finn.unleash.UnleashContextProvider;
import no.finn.unleash.util.UnleashConfig;
import no.nav.modig.core.context.SubjectHandler;
import no.nav.sbl.dialogarena.service.EmailService;
import no.nav.sbl.dialogarena.service.HandleBarKjoerer;
import no.nav.sbl.dialogarena.service.HtmlGenerator;
import no.nav.sbl.dialogarena.utils.InnloggetBruker;
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

import javax.inject.Inject;
import java.net.InetAddress;
import java.net.UnknownHostException;

/**
 * Applikasjonskontekst for ear-modulen.
 */
@Configuration
public class ApplicationConfig {

    @Value("${dialogarena.navnolink.url}")
    private String navigasjonslink;
    @Value("${dokumentinnsending.smtpServer.port}")
    private String smtpServerPort;
    @Value("${dokumentinnsending.smtpServer.host}")
    private String smtpServerHost;
    @Value("${unleash.api.url}")
    private String unleashApiUrl;
    @Bean
    public static PropertySourcesPlaceholderConfigurer placeholderConfigurer() {
        PropertySourcesPlaceholderConfigurer propertySourcesPlaceholderConfigurer = new PropertySourcesPlaceholderConfigurer();
        propertySourcesPlaceholderConfigurer.setLocalOverride(true);
        return propertySourcesPlaceholderConfigurer;
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
        javaMailSender.setPort(Integer.parseInt(smtpServerPort));
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
    public HtmlGenerator handleBarKjoerer() {
        return new HandleBarKjoerer();
    }

    @Bean
    public UnleashContextProvider unleashContextProvider(){
        return () -> UnleashContext
                .builder()
                .userId(SubjectHandler.getSubjectHandler().getUid())
                .build();
    }

    @Inject
    @Bean
    public Unleash unleashToggle(UnleashContextProvider provider) throws UnknownHostException {
        UnleashConfig config = UnleashConfig.builder()
                .appName(System.getProperty("application.name"))
                .instanceId(InetAddress.getLocalHost().getHostName())
                .unleashContextProvider(provider)
                .unleashAPI(unleashApiUrl)
                .build();

        return new DefaultUnleash(config);
    }

}