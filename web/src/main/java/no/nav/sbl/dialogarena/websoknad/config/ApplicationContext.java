package no.nav.sbl.dialogarena.websoknad.config;

import net.bull.javamelody.MonitoredWithInterfacePointcut;
import net.bull.javamelody.MonitoringProxy;
import net.bull.javamelody.MonitoringSpringAdvisor;
import no.nav.modig.cache.CacheConfig;
import no.nav.sbl.dialogarena.dokumentinnsending.WicketApplication;
import no.nav.sbl.dialogarena.dokumentinnsending.config.ContentConfig;
import no.nav.sbl.dialogarena.dokumentinnsending.config.GAConfig;
import no.nav.sbl.dialogarena.dokumentinnsending.convert.ThumbnailConverter;
import no.nav.sbl.dialogarena.dokumentinnsending.convert.ThumbnailConverterImpl;
import no.nav.sbl.dialogarena.dokumentinnsending.repository.SoknadRepository;
import no.nav.sbl.dialogarena.dokumentinnsending.resource.DokumentForhandsvisningResourceReference;
import no.nav.sbl.dialogarena.dokumentinnsending.service.EmailService;
import no.nav.sbl.dialogarena.dokumentinnsending.service.PersonServiceTPS;
import org.springframework.aop.support.DefaultPointcutAdvisor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.ImportResource;
import org.springframework.context.support.PropertySourcesPlaceholderConfigurer;
import org.springframework.mail.MailSender;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

/**
 * Applikasjonskontekst for ear-modulen.
 */
@Configuration
@Import({CacheConfig.class, no.nav.sbl.dialogarena.dokumentinnsending.config.FooterConfig.class, GAConfig.class, ConsumerConfig.class, ContentConfig.class})
@ImportResource("classpath:net/bull/javamelody/monitoring-spring.xml")
public class ApplicationContext {

    @Value("${dokumentinnsending.smtpServer.host}")
    private String smtpServerHost;

    @Value("${dokumentinnsending.smtpServer.port}")
    private Integer smtpServerPort;

    @Value("${dokumentinnsending.navigasjonslink.url}")
    private String navigasjonslink;

    @Value("${dokumentinnsending.logoutURL.url}")
    private String logoutURL;
    private boolean monitor = false;

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
    public String logoutURL() {
        return logoutURL;
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

    @Bean()
    public ThumbnailConverter thumbnailConverter() {
        ThumbnailConverter thumbnailConverter = new ThumbnailConverterImpl();
        if (monitor) {
            return MonitoringProxy.createProxy(thumbnailConverter);
        }
        return thumbnailConverter;
    }

    @Bean
    public DokumentForhandsvisningResourceReference thumbnailRef() {
        return new DokumentForhandsvisningResourceReference();
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
    public DefaultPointcutAdvisor advisor() throws ClassNotFoundException {
        DefaultPointcutAdvisor advisor = new MonitoringSpringAdvisor();
        MonitoredWithInterfacePointcut pointcut = new MonitoredWithInterfacePointcut();
        pointcut.setInterfaceName("no.nav.sbl.dialogarena.dokumentinnsending.service.SoknadService");
        advisor.setPointcut(pointcut);
        return advisor;
    }

    @Bean
    public DefaultPointcutAdvisor advisor2() throws ClassNotFoundException {
        DefaultPointcutAdvisor advisor = new MonitoringSpringAdvisor();
        MonitoredWithInterfacePointcut pointcut = new MonitoredWithInterfacePointcut();
        pointcut.setInterfaceName("no.nav.sbl.dialogarena.dokumentinnsending.service.PersonService");
        advisor.setPointcut(pointcut);
        return advisor;
    }

    @Bean
    public DefaultPointcutAdvisor advisor3() throws ClassNotFoundException {
        DefaultPointcutAdvisor advisor = new MonitoringSpringAdvisor();
        MonitoredWithInterfacePointcut pointcut = new MonitoredWithInterfacePointcut();
        pointcut.setInterfaceName("no.nav.sbl.dialogarena.dokumentinnsending.service.OpprettBrukerBehandlingService");
        advisor.setPointcut(pointcut);
        return advisor;
    }

    @Bean
    public DefaultPointcutAdvisor advisor4() throws ClassNotFoundException {
        DefaultPointcutAdvisor advisor = new MonitoringSpringAdvisor();
        MonitoredWithInterfacePointcut pointcut = new MonitoredWithInterfacePointcut();
        pointcut.setInterfaceName("no.nav.sbl.dialogarena.dokumentinnsending.service.BrukerBehandlingServiceIntegration");
        advisor.setPointcut(pointcut);
        return advisor;
    }


}