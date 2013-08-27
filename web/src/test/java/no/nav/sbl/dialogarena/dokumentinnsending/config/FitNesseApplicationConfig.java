package no.nav.sbl.dialogarena.dokumentinnsending.config;

import no.nav.modig.content.CmsContentRetriever;
import no.nav.modig.wicket.test.FluentWicketTester;
import no.nav.sbl.dialogarena.dokumentinnsending.WicketApplication;
import no.nav.sbl.dialogarena.dokumentinnsending.convert.ThumbnailConverter;
import no.nav.sbl.dialogarena.dokumentinnsending.convert.ThumbnailConverterImpl;
import no.nav.sbl.dialogarena.dokumentinnsending.repository.SoknadRepository;
import no.nav.sbl.dialogarena.dokumentinnsending.resource.DokumentForhandsvisningResourceReference;
import no.nav.sbl.dialogarena.dokumentinnsending.service.DokumentServiceMock;
import no.nav.sbl.dialogarena.dokumentinnsending.service.EmailService;
import no.nav.sbl.dialogarena.dokumentinnsending.service.PersonService;
import no.nav.sbl.dialogarena.dokumentinnsending.service.PersonServiceMock;
import no.nav.sbl.dialogarena.dokumentinnsending.service.PersonServiceTPS;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.concurrent.ConcurrentMapCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.core.task.AsyncTaskExecutor;
import org.springframework.core.task.SyncTaskExecutor;
import org.springframework.mail.MailSender;
import org.springframework.scheduling.concurrent.ConcurrentTaskExecutor;

import javax.inject.Inject;
import java.util.Locale;

import static org.mockito.Mockito.mock;

@Import({ConsumerConfigTest.class, FooterConfigTest.class, GAConfig.class, ContentConfigTest.class})
public class
        FitNesseApplicationConfig {

    @Value("${dokumentinnsending.navigasjonslink.url}")
    private String navigasjonslink;

    @Value("${dokumentinnsending.logoutURL.url}")
    private String logoutURL;

    @Inject
    protected CmsContentRetriever cmsContentRetriever;


    @Bean
    public FluentWicketTester<WicketApplication> wicketTester(WicketApplication application) {
        FluentWicketTester<WicketApplication> wicketTester = new FluentWicketTester<>(application);
        wicketTester.tester.getSession().setLocale(new Locale("NO"));
        return wicketTester;
    }

    @Bean
    public EmailService emailService() {
        return new EmailService();
    }

    @Bean
    public MailSender mailSender() {
        return mock(MailSender.class);
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
    public ConcurrentMapCacheManager cacheManager() {
        return new ConcurrentMapCacheManager();
    }

    @Bean
    public SoknadRepository soknadRepository() {
        return new SoknadRepository();
    }

    @Bean
    public ThumbnailConverter thumbnailConverter() {
        return new ThumbnailConverterImpl();
    }

    @Bean
    public AsyncTaskExecutor syncTaskExecutor() {
        return new ConcurrentTaskExecutor(new SyncTaskExecutor());
    }

    @Bean
    public DokumentServiceMock dokumentServiceMock() {
        return new DokumentServiceMock();
    }

    @Bean
    public WicketApplication dokumentinnsendingApplication() {
        return new WicketApplication();
    }

    @Bean
    public PersonService personService() {
        return new PersonServiceTPS();
    }

    @Bean
    public PersonServiceMock personServiceMoxk() {
        return new PersonServiceMock();
    }

    @Bean
    public DokumentForhandsvisningResourceReference thumbnailRef() {
        return new DokumentForhandsvisningResourceReference();
    }
}