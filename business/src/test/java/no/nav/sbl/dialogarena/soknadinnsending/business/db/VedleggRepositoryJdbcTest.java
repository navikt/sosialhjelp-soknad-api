package no.nav.sbl.dialogarena.soknadinnsending.business.db;

import no.nav.sbl.dialogarena.soknadinnsending.business.domain.Vedlegg;
import org.joda.time.DateTime;
import org.junit.After;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import javax.inject.Inject;
import java.io.IOException;
import java.util.List;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;

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
        vedleggRepository.opprettVedlegg(v, bytes);

        List<Vedlegg> vedlegg = vedleggRepository.hentVedleggUnderBehandling(v.getSoknadId(), v.getFillagerReferanse());
         assertThat(vedlegg.size(), is(equalTo(1)));
        v.setVedleggId(vedlegg.get(0).getVedleggId());
        assertThat(vedlegg.get(0), is(equalTo(v)));
    }

    @Test
    public void skalKunneSletteVedlegg() {
        final Vedlegg v = getVedlegg();
        Long id = vedleggRepository.opprettVedlegg(v, new byte[0]);
        List<Vedlegg> hentet = vedleggRepository.hentVedleggUnderBehandling(v.getSoknadId(), v.getFillagerReferanse());
        assertThat(hentet, is(notNullValue()));
        assertThat(hentet.size(), is(1));
        vedleggRepository.slettVedlegg(v.getSoknadId(), id);
        hentet = vedleggRepository.hentVedleggUnderBehandling(v.getSoknadId(), v.getFillagerReferanse());
        assertThat(hentet, is(notNullValue()));
        assertThat(hentet.size(), is(0));
    }

    @Test
    public void skalHenteInnhold() throws IOException {
        byte[] lagret = new byte[]{1, 2, 3};
        final Vedlegg v = getVedlegg(lagret);
        Long id = vedleggRepository.opprettVedlegg(v, lagret);
        byte[] hentet = vedleggRepository.hentVedleggData(v.getSoknadId(), id);
        assertThat(hentet, is(equalTo(lagret)));
    }

    @Test
    public void skalLagreVedleggMedData() {
        Long id = vedleggRepository.opprettVedlegg(getVedlegg(), null);
        vedleggRepository.lagreVedleggMedData(12L, id, getVedlegg().medData(new byte[]{1, 2, 3}));
        Vedlegg vedlegg = vedleggRepository.hentVedleggMedInnhold(12L, id);
        assertThat(vedlegg, is(equalTo(getVedlegg().medData(new byte[]{1, 2, 3}).medVedleggId(id))));
    }

    @Test
    public void skalSletteVedleggOgData() {
        Long id = vedleggRepository.opprettVedlegg(getVedlegg(), new byte[]{1, 2, 3});
        Long id2 = vedleggRepository.opprettVedlegg(getVedlegg().medSkjemaNummer("2"), new byte[]{1, 2, 3});
        vedleggRepository.slettVedleggOgData(12L, 10L, "1");
        try {
            vedleggRepository.hentVedlegg(12L, id);
            fail("ikke slettet");
        } catch (Exception e) {
        }
        vedleggRepository.hentVedlegg(12L, id2);
    }

    @Test
    public void skalSletteVedleggUnderBehandling() {
        Long id = vedleggRepository.opprettVedlegg(getVedlegg().medInnsendingsvalg(Vedlegg.Status.UnderBehandling), new byte[]{1, 2, 3});
        Long id2 = vedleggRepository.opprettVedlegg(getVedlegg().medInnsendingsvalg(Vedlegg.Status.SendesSenere), new byte[]{1, 2, 3});
        vedleggRepository.slettVedleggUnderBehandling(12L, 10L, "1");
        vedleggRepository.hentVedlegg(12L, id2);
        try {
            vedleggRepository.hentVedlegg(12L, id);
            fail("ikke slettet");
        } catch (Exception e) {
        }
    }

    @Test
    public void skalHenteVedleggForSkjema() {
        Long id = vedleggRepository.opprettVedlegg(getVedlegg().medInnsendingsvalg(Vedlegg.Status.LastetOpp), new byte[]{1, 2, 3});
        Long id2 = vedleggRepository.opprettVedlegg(getVedlegg().medFaktumId(null).medInnsendingsvalg(Vedlegg.Status.LastetOpp), new byte[]{1, 2, 3});
        assertThat(vedleggRepository.hentVedleggForskjemaNummer(12L, 10L, "1").getVedleggId(), is(equalTo(id)));
        assertThat(vedleggRepository.hentVedleggForskjemaNummer(12L, null, "1").getVedleggId(), is(equalTo(id2)));
    }

    private Vedlegg getVedlegg() {
        return getVedlegg(new byte[]{1, 2, 3});
    }

    private Vedlegg getVedlegg(byte[] bytes) {
        return new Vedlegg()
                .medVedleggId(null)
                .medSoknadId(12L)
                .medFaktumId(10L)
                .medSkjemaNummer("1")
                .medNavn("navn")
                .medStorrelse((long) bytes.length)
                .medAntallSider(1)
                .medFillagerReferanse("1234")
                .medData(null)
                .medOpprettetDato(DateTime.now().getMillis())
                .medInnsendingsvalg(Vedlegg.Status.UnderBehandling);
    }

}
