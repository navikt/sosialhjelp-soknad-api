package no.nav.sbl.dialogarena.soknadinnsending.business.db;

import no.nav.sbl.dialogarena.sendsoknad.domain.DelstegStatus;
import no.nav.sbl.dialogarena.sendsoknad.domain.SoknadInnsendingStatus;
import no.nav.sbl.dialogarena.sendsoknad.domain.Vedlegg;
import no.nav.sbl.dialogarena.sendsoknad.domain.WebSoknad;
import no.nav.sbl.dialogarena.soknadinnsending.business.db.soknad.SoknadRepository;
import no.nav.sbl.dialogarena.soknadinnsending.business.db.vedlegg.VedleggRepository;
import org.joda.time.DateTime;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import javax.inject.Inject;
import java.io.IOException;
import java.util.List;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = {DbTestConfig.class})
public class VedleggRepositoryJdbcTest {

    public static final String BEHANDLINGS_ID = "ABC";

    @Inject
    private VedleggRepository vedleggRepository;
    @Inject
    private SoknadRepository soknadRepository;
    @Inject
    private RepositoryTestSupport soknadRepositoryTestSupport;

    private Long soknadId;

    @Before
    public void setUp() {
        soknadId = soknadRepository.opprettSoknad(getSoknad());
    }

    @After
    public void cleanUp() {
        soknadRepositoryTestSupport.getJdbcTemplate().update("delete from Vedlegg");
        soknadRepositoryTestSupport.getJdbcTemplate().update("delete from Soknad");
    }

    @Test
    public void skalLasteOppBlob() throws IOException {
        byte[] bytes = {1, 2, 3};
        Vedlegg v = getVedlegg(bytes);
        vedleggRepository.opprettEllerEndreVedlegg(v, bytes);

        List<Vedlegg> vedlegg = vedleggRepository.hentVedleggUnderBehandling(BEHANDLINGS_ID, v.getFillagerReferanse());
        assertThat(vedlegg.size(), is(equalTo(1)));
        v.setVedleggId(vedlegg.get(0).getVedleggId());
        v.setOpprettetDato(vedlegg.get(0).getOpprettetDato());
        assertThat(vedlegg.get(0), is(equalTo(v)));
    }

    @Test
    public void skalKunneSletteVedlegg() {
        final Vedlegg v = getVedlegg();
        Long id = vedleggRepository.opprettEllerEndreVedlegg(v, new byte[0]);
        List<Vedlegg> hentet = vedleggRepository.hentVedleggUnderBehandling(BEHANDLINGS_ID, v.getFillagerReferanse());
        assertThat(hentet, is(notNullValue()));
        assertThat(hentet.size(), is(1));
        vedleggRepository.slettVedlegg(v.getSoknadId(), id);
        hentet = vedleggRepository.hentVedleggUnderBehandling("ABC", v.getFillagerReferanse());
        assertThat(hentet, is(notNullValue()));
        assertThat(hentet.size(), is(0));
    }

    @Test
    public void skalHenteInnhold() throws IOException {
        byte[] lagret = new byte[]{1, 2, 3};
        final Vedlegg v = getVedlegg(lagret);
        Long id = vedleggRepository.opprettEllerEndreVedlegg(v, lagret);
        byte[] hentet = vedleggRepository.hentVedleggData(id);
        assertThat(hentet, is(equalTo(lagret)));
    }

    @Test
    public void skalHenteBehandligsIdTilVedlegg() {
        Long vedleggId = vedleggRepository.opprettEllerEndreVedlegg(getVedlegg(), null);
        String behandlingsIdTilVedlegg = vedleggRepository.hentBehandlingsIdTilVedlegg(vedleggId);
        assertThat(BEHANDLINGS_ID, is(behandlingsIdTilVedlegg));
    }

    @Test
    public void skalReturnereNullHvisVedleggIdIkkeFinnes() {
        String behandlingsIdTilVedlegg = vedleggRepository.hentBehandlingsIdTilVedlegg(666L);
        assertThat(behandlingsIdTilVedlegg, is(nullValue()));
    }

    @Test
    public void skalLagreVedleggMedData() {
        Long id = vedleggRepository.opprettEllerEndreVedlegg(getVedlegg(), null);
        vedleggRepository.lagreVedleggMedData(soknadId, id, getVedlegg().medData(new byte[]{1, 2, 3}));
        Vedlegg vedlegg = vedleggRepository.hentVedleggMedInnhold(id);
        assertThat(vedlegg, is(equalTo(getVedlegg().medData(new byte[]{1, 2, 3}).medVedleggId(id).medOpprettetDato(vedlegg.getOpprettetDato()))));
    }

    @Test
    public void skalSletteVedleggOgDataMedVedleggParameter() {
        Long id = vedleggRepository.opprettEllerEndreVedlegg(getVedlegg(), new byte[]{1, 2, 3});
        Long id2 = vedleggRepository.opprettEllerEndreVedlegg(getVedlegg().medSkjemaNummer("2"), new byte[]{1, 2, 3});
        vedleggRepository.slettVedleggOgData(soknadId, new Vedlegg().medFaktumId(10L).medSkjemaNummer("1"));
        try {
            vedleggRepository.hentVedlegg(id);
            fail("ikke slettet");
        } catch (Exception ignore) {

        }
        vedleggRepository.hentVedlegg(id2);
    }

    @Test(expected = Exception.class)
    public void skalSletteVedleggUnderBehandling() {
        Long id = vedleggRepository.opprettEllerEndreVedlegg(getVedlegg().medInnsendingsvalg(Vedlegg.Status.UnderBehandling), new byte[]{1, 2, 3});
        Long id2 = vedleggRepository.opprettEllerEndreVedlegg(getVedlegg().medInnsendingsvalg(Vedlegg.Status.SendesSenere), new byte[]{1, 2, 3});
        vedleggRepository.slettVedleggUnderBehandling(soknadId, 10L, "1", null);
        vedleggRepository.hentVedlegg(id2);
        vedleggRepository.hentVedlegg(id);
    }

    @Test
    public void skalHenteVedleggForSkjema() {
        Long id = vedleggRepository.opprettEllerEndreVedlegg(getVedlegg().medInnsendingsvalg(Vedlegg.Status.LastetOpp), new byte[]{1, 2, 3});
        Long id2 = vedleggRepository.opprettEllerEndreVedlegg(getVedlegg().medFaktumId(null).medInnsendingsvalg(Vedlegg.Status.LastetOpp), new byte[]{1, 2, 3});
        assertThat(vedleggRepository.hentVedleggForskjemaNummer(soknadId, 10L, "1").getVedleggId(), is(equalTo(id)));
        assertThat(vedleggRepository.hentVedleggForskjemaNummer(soknadId, null, "1").getVedleggId(), is(equalTo(id2)));
    }

    @Test(expected = EmptyResultDataAccessException.class)
    public void skalSletteVedleggMedId() {
        Long id = vedleggRepository.opprettEllerEndreVedlegg(getVedlegg().medInnsendingsvalg(Vedlegg.Status.VedleggKreves), null);

        vedleggRepository.slettVedleggMedVedleggId(id);
        vedleggRepository.hentVedlegg(id);
    }

    private Vedlegg getVedlegg() {
        return getVedlegg(new byte[]{1, 2, 3});
    }

    private Vedlegg getVedlegg(byte[] bytes) {
        return new Vedlegg()
                .medVedleggId(null)
                .medSoknadId(soknadId)
                .medFaktumId(10L)
                .medSkjemaNummer("1")
                .medNavn("navn")
                .medStorrelse((long) bytes.length)
                .medAntallSider(1)
                .medFillagerReferanse("1234")
                .medData(null)
                .medOpprettetDato(DateTime.now().getMillis())
                .medAarsak("")
                .medInnsendingsvalg(Vedlegg.Status.UnderBehandling);
    }

    private WebSoknad getSoknad() {
        return new WebSoknad()
                .medBehandlingId(BEHANDLINGS_ID)
                .medskjemaNummer("NAV007")
                .medAktorId("DEF")
                .medOppretteDato(DateTime.now())
                .medStatus(SoknadInnsendingStatus.UNDER_ARBEID)
                .medDelstegStatus(DelstegStatus.UTFYLLING)
                .medUuid("C4F3B4B3");
    }
}
