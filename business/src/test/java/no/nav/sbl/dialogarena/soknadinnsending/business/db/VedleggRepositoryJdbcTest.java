package no.nav.sbl.dialogarena.soknadinnsending.business.db;

import no.nav.sbl.dialogarena.soknadinnsending.business.domain.Vedlegg;
import org.apache.commons.io.IOUtils;
import org.junit.After;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import javax.inject.Inject;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.Assert.assertThat;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = {DbConfig.class})
public class VedleggRepositoryJdbcTest {

    @Inject
    private VedleggRepository vedleggRepository;
    @Inject
    private RepositoryTestSupport soknadRepositoryTestSupport;

    @After
    public void cleanUp() {
        soknadRepositoryTestSupport.getJdbcTemplate().update("delete from Vedlegg");
    }

    @Test
    public void skalLasteOppBlob() throws IOException {
        byte[] bytes = {1, 2, 3};
        Vedlegg v = getVedlegg(bytes);
        vedleggRepository.lagreVedlegg(v, bytes);
        List<Vedlegg> vedlegg = vedleggRepository.hentVedleggForFaktum(v.getSoknadId(), v.getFaktumId());
        assertThat(vedlegg.size(), is(equalTo(1)));
        v.setId(vedlegg.get(0).getId());
        assertThat(vedlegg.get(0), is(equalTo(v)));
    }

    @Test
    public void skalKunneSletteVedlegg() {
        final Vedlegg v = getVedlegg();
        Long id = vedleggRepository.lagreVedlegg(v, new byte[0]);
        List<Vedlegg> hentet = vedleggRepository.hentVedleggForFaktum(v.getSoknadId(), v.getFaktumId());
        assertThat(hentet, is(notNullValue()));
        assertThat(hentet.size(), is(1));
        vedleggRepository.slettVedlegg(v.getSoknadId(), id);
        hentet = vedleggRepository.hentVedleggForFaktum(v.getSoknadId(), v.getFaktumId());
        assertThat(hentet, is(notNullValue()));
        assertThat(hentet.size(), is(0));
    }

    @Test
    public void skalHenteInnhold() throws IOException {
        byte[] lagret = new byte[]{1, 2, 3};
        final Vedlegg v = getVedlegg(lagret);
        Long id = vedleggRepository.lagreVedlegg(v, lagret);
        InputStream hentet = vedleggRepository.hentVedleggStream(v.getSoknadId(), id);
        byte[] bytes = IOUtils.toByteArray(hentet);
        assertThat(bytes, is(equalTo(lagret)));
    }

    private Vedlegg getVedlegg() {
        return getVedlegg(new byte[]{1, 2, 3});
    }

    private Vedlegg getVedlegg(byte[] bytes) {
        return new Vedlegg(null, 12L, 10L, "navn", (long) bytes.length, 1, null, bytes);
    }

}
