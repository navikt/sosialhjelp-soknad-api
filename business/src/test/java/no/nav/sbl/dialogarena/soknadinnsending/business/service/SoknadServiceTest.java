package no.nav.sbl.dialogarena.soknadinnsending.business.service;


import no.nav.sbl.dialogarena.detect.IsPdf;
import no.nav.sbl.dialogarena.soknadinnsending.business.db.SoknadRepository;
import no.nav.sbl.dialogarena.soknadinnsending.business.domain.Vedlegg;
import org.apache.commons.io.IOUtils;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

import static no.nav.sbl.dialogarena.test.match.Matchers.match;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class SoknadServiceTest {

    @Mock
    private SoknadRepository repository;
    @InjectMocks
    private SoknadService soknadService;

    @Test
    public void skalKonvertereFilerVedOpplasting() throws IOException {
        ByteArrayInputStream bais = new ByteArrayInputStream(getBytesFromFile("/images/bilde.png"));
        Vedlegg vedlegg = new Vedlegg();
        ArgumentCaptor<byte[]> captor = ArgumentCaptor.forClass(byte[].class);
        when(repository.lagreVedlegg(eq(vedlegg), captor.capture())).thenReturn(11L);
        Long id = soknadService.lagreVedlegg(vedlegg, bais);
        assertThat(captor.getValue(), match(new IsPdf()));
        assertThat(id, is(equalTo(11L)));
    }
    @Test
    @Ignore
    public void skalKonverterePdfVedOpplasting() throws IOException {
        ByteArrayInputStream bais = new ByteArrayInputStream(getBytesFromFile("/pdfs/navskjema.pdf"));
        Vedlegg vedlegg = new Vedlegg();
        ArgumentCaptor<byte[]> captor = ArgumentCaptor.forClass(byte[].class);
        when(repository.lagreVedlegg(eq(vedlegg), captor.capture())).thenReturn(11L);
        Long id = soknadService.lagreVedlegg(vedlegg, bais);
        assertThat(captor.getValue(), match(new IsPdf()));
        assertThat(id, is(equalTo(11L)));
        File file = new File("testoutput.pdf");
        System.out.println(file.getAbsolutePath());
        IOUtils.write(captor.getValue(), new FileOutputStream(file));
    }

    public static byte[] getBytesFromFile(String path) throws IOException {
        InputStream resourceAsStream = SoknadServiceTest.class.getResourceAsStream(path);
        return IOUtils.toByteArray(resourceAsStream);
    }


}
