package no.nav.sbl.dialogarena.soknadinnsending.business.service;


import no.nav.modig.core.context.StaticSubjectHandler;
import no.nav.sbl.dialogarena.soknadinnsending.business.db.VedleggRepository;
import no.nav.sbl.dialogarena.soknadinnsending.business.domain.Vedlegg;
import org.apache.commons.io.IOUtils;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import static java.lang.System.setProperty;
import static no.nav.modig.core.context.SubjectHandler.SUBJECTHANDLER_KEY;
import static no.nav.sbl.dialogarena.detect.Detect.IS_PDF;
import static no.nav.sbl.dialogarena.test.match.Matchers.match;
import static org.hamcrest.Matchers.contains;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class SoknadServiceTest {

    @Mock
    private VedleggRepository vedleggRepository;
    @InjectMocks
    private SoknadService soknadService;

    public static byte[] getBytesFromFile(String path) throws IOException {
        InputStream resourceAsStream = SoknadServiceTest.class.getResourceAsStream(path);
        return IOUtils.toByteArray(resourceAsStream);
    }

    @Before
    public void before() {
        setProperty(SUBJECTHANDLER_KEY, StaticSubjectHandler.class.getName());
    }

    @Test
    public void skalAKonvertereFilerVedOpplasting() throws IOException {
        Vedlegg vedlegg = new Vedlegg(1L, 1L, 1L, "1", "", 1L, 1, null, null);
        ArgumentCaptor<byte[]> captor = ArgumentCaptor.forClass(byte[].class);
        when(vedleggRepository.lagreVedlegg(any(Vedlegg.class), captor.capture())).thenReturn(11L);

        ByteArrayInputStream bais = new ByteArrayInputStream(getBytesFromFile("/images/bilde.jpg"));
        List<Long> ids = soknadService.splitOgLagreVedlegg(vedlegg, bais);
        assertThat(captor.getValue(), match(IS_PDF));
        assertThat(ids, contains(11L));
    }

    @Test
    public void skalKonverterePdfVedOpplasting() throws IOException {
        Vedlegg vedlegg = new Vedlegg(1L, 1L, 1L, "1", "", 1L, 1, null, null);
        ArgumentCaptor<byte[]> captor = ArgumentCaptor.forClass(byte[].class);
        when(vedleggRepository.lagreVedlegg(any(Vedlegg.class), captor.capture())).thenReturn(10L, 11L, 12L, 13L, 14L);

        ByteArrayInputStream bais = new ByteArrayInputStream(getBytesFromFile("/pdfs/navskjema.pdf"));
        List<Long> ids = soknadService.splitOgLagreVedlegg(vedlegg, bais);
        assertThat(captor.getValue(), match(IS_PDF));
        assertThat(ids, contains(10L, 11L, 12L, 13L, 14L));
    }
}
