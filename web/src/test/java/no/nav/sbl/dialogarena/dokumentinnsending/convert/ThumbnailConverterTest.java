package no.nav.sbl.dialogarena.dokumentinnsending.convert;

import no.nav.sbl.dialogarena.dokumentinnsending.domain.Dokument;
import no.nav.sbl.dialogarena.dokumentinnsending.domain.DokumentInnhold;
import no.nav.sbl.dialogarena.dokumentinnsending.service.SoknadService;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.concurrent.ConcurrentMapCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import javax.inject.Inject;
import java.awt.Dimension;
import java.io.IOException;
import java.io.InputStream;
import java.util.UUID;

import static org.apache.commons.io.IOUtils.toByteArray;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ActiveProfiles("thumbnailtest")
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = ThumbnailServiceTestConfiguration.class)
public class ThumbnailConverterTest {


    @Inject
    private ThumbnailConverter converter;

    @Inject
    private SoknadService dokumentService;
    private InputStream testFile, testPdf;

    @Before
    public void init() throws IOException {
        testFile = getFile("/testFiles/liteSkjema.png");
        testPdf = getFile("/testFiles/litenPdf.pdf");
    }


    @Test
    public void shouldReturnUUID() throws IOException {
        UUID id1 = converter.createThumbnail(toByteArray(testFile));
        UUID id2 = converter.createThumbnail(toByteArray(testFile));
        assertThat(id1, is(notNullValue()));
        assertThat(id2, is(notNullValue()));
        assertThat(id1, is(not(equalTo(id2))));
    }

    @Test(expected = RuntimeException.class)
    public void throwsExceptionIfUUIDDoesNotExist() {
        converter.isDone(UUID.randomUUID());
    }


    @Test//(timeout = 5000)
    public void shouldReturnThumbWhenImageIsDone() throws Exception {
        UUID id = converter.createThumbnail(toByteArray(testFile));
        while (!converter.isDone(id)) {
            Thread.sleep(100);
        }

        byte[] thumbnail = converter.getThumbnail(id);
        assertThat(thumbnail, is(notNullValue()));

    }

    @Test
    public void shouldReadFromDokumentServiceForContent() throws IOException, InterruptedException {
        Dokument dokument = new Dokument(Dokument.Type.HOVEDSKJEMA);
        DokumentInnhold di = new DokumentInnhold();
        di.setInnhold(toByteArray(testPdf));
        dokument.setDokumentInnhold(di);
        when(dokumentService.hentDokumentInnhold(eq(dokument))).thenReturn(di);
        UUID id = converter.bestillForhaandsvisning(dokument);
        while (!converter.isDone(id)) {
            Thread.sleep(100);
        }
        byte[] thumbnail = converter.getThumbnail(id);
        assertThat(thumbnail, is(notNullValue()));
    }

    public void shouldReadThumb() throws IOException {
        byte[] bilde = toByteArray(testPdf);
        byte[] ens = converter.hentForhaandsvisning("en", new Dimension(10, 10), 0, bilde);
        byte[] ens2 = converter.hentForhaandsvisning("en", new Dimension(10, 10), 0, bilde);
        assertThat(ens, is(ens2));

    }

    private static InputStream getFile(String path) throws IOException {
        return ThumbnailConverterTest.class.getResourceAsStream(path);
    }
}

@Configuration
@Profile("thumbnailtest")
@EnableCaching(proxyTargetClass = true)
class ThumbnailServiceTestConfiguration {
    public ThumbnailServiceTestConfiguration() {
    }

    @Bean
    public ThreadPoolTaskExecutor threadPoolTaskExecutor() {
        ThreadPoolTaskExecutor tpte = new ThreadPoolTaskExecutor();
        tpte.setMaxPoolSize(1);
        tpte.setCorePoolSize(1);
        return tpte;
    }

    @Bean
    public ThumbnailConverter thumbnailConverter() {
        return new ThumbnailConverterImpl();
    }

    @Bean
    public SoknadService dokumentService() {
        return mock(SoknadService.class);
    }

    @Bean
    public CacheManager cacheManager() {
        return new ConcurrentMapCacheManager();
    }

}

