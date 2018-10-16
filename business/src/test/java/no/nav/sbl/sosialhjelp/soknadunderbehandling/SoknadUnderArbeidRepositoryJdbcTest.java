package no.nav.sbl.sosialhjelp.soknadunderbehandling;

import no.nav.sbl.dialogarena.soknadinnsending.business.db.DbTestConfig;
import no.nav.sbl.dialogarena.soknadinnsending.business.db.RepositoryTestSupport;
import no.nav.sbl.sosialhjelp.domain.*;
import org.junit.After;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import javax.inject.Inject;
import java.time.LocalDateTime;
import java.util.Optional;

import static java.time.LocalDateTime.now;
import static no.nav.sbl.dialogarena.sendsoknad.domain.SoknadInnsendingStatus.AVBRUTT_AV_BRUKER;
import static no.nav.sbl.dialogarena.sendsoknad.domain.SoknadInnsendingStatus.UNDER_ARBEID;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.junit.Assert.assertThat;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = {DbTestConfig.class})
public class SoknadUnderArbeidRepositoryJdbcTest {

    private static final String EIER = "12345678901";
    private static final String EIER2 = "10987654321";
    private static final String BEHANDLINGSID = "1100020";
    private static final String TILKNYTTET_BEHANDLINGSID = "4567";
    private static final LocalDateTime OPPRETTET_DATO = now().minusSeconds(50);
    private static final LocalDateTime SIST_ENDRET_DATO = now();
    private static final LocalDateTime SIST_ENDRET_DATO_OPPDATERT = now().minusSeconds(20);
    private static final byte[] DATA = {1, 2, 3, 4};
    private static final byte[] DATA_OPPDATERT = {1, 2, 3, 4, 5, 6, 7, 8};

    @Inject
    private RepositoryTestSupport soknadRepositoryTestSupport;

    @Inject
    private SoknadUnderArbeidRepository soknadUnderArbeidRepository;

    @Inject
    private OpplastetVedleggRepository opplastetVedleggRepository;

    @After
    public void cleanUp() {
        soknadRepositoryTestSupport.getJdbcTemplate().update("delete from SOKNAD_UNDER_ARBEID");
        soknadRepositoryTestSupport.getJdbcTemplate().update("delete from OPPLASTET_VEDLEGG");
    }

    @Test
    public void opprettSoknadOppretterSoknadUnderArbeidIDatabasen() {
        final Long soknadUnderArbeidId = soknadUnderArbeidRepository.opprettSoknad(lagSoknadUnderArbeid(BEHANDLINGSID), EIER);

        assertThat(soknadUnderArbeidId, notNullValue());
    }

    @Test
    public void hentSoknadHenterSoknadUnderArbeidGittRiktigEierOgSoknadId() {
        final Long soknadUnderArbeidId = soknadUnderArbeidRepository.opprettSoknad(lagSoknadUnderArbeid(BEHANDLINGSID), EIER);

        SoknadUnderArbeid soknadUnderArbeid = soknadUnderArbeidRepository.hentSoknad(soknadUnderArbeidId, EIER).get();

        assertThat(soknadUnderArbeid.getSoknadId(), notNullValue());
        assertThat(soknadUnderArbeid.getVersjon(), is(1L));
        assertThat(soknadUnderArbeid.getBehandlingsId(), is(BEHANDLINGSID));
        assertThat(soknadUnderArbeid.getTilknyttetBehandlingsId(), is(TILKNYTTET_BEHANDLINGSID));
        assertThat(soknadUnderArbeid.getEier(), is(EIER));
        assertThat(soknadUnderArbeid.getData(), is(DATA));
        assertThat(soknadUnderArbeid.getInnsendingStatus(), is(UNDER_ARBEID));
        assertThat(soknadUnderArbeid.getOpprettetDato(), is(OPPRETTET_DATO));
        assertThat(soknadUnderArbeid.getSistEndretDato(), is(SIST_ENDRET_DATO));
    }

    @Test
    public void hentSoknadHenterIngenSoknadUnderArbeidHvisEiesAvAnnenBruker() {
        final Long soknadUnderArbeidId = soknadUnderArbeidRepository.opprettSoknad(lagSoknadUnderArbeid(BEHANDLINGSID), EIER);

        Optional<SoknadUnderArbeid> soknadUnderArbeid = soknadUnderArbeidRepository.hentSoknad(soknadUnderArbeidId, EIER2);

        assertThat(soknadUnderArbeid.isPresent(), is(false));
    }

    @Test
    public void hentSoknadHenterSoknadUnderArbeidGittRiktigEierOgBehandlingsId() {
        final Long soknadUnderArbeidId = soknadUnderArbeidRepository.opprettSoknad(lagSoknadUnderArbeid(BEHANDLINGSID), EIER);

        SoknadUnderArbeid soknadUnderArbeid = soknadUnderArbeidRepository.hentSoknad(BEHANDLINGSID, EIER).get();

        assertThat(soknadUnderArbeid.getSoknadId(), is(soknadUnderArbeidId));
        assertThat(soknadUnderArbeid.getBehandlingsId(), is(BEHANDLINGSID));
        assertThat(soknadUnderArbeid.getEier(), is(EIER));
    }

    @Test
    public void oppdaterSoknadsdataOppdatererVersjonOgDataOgSistEndretDato() {
        SoknadUnderArbeid soknadUnderArbeid = lagSoknadUnderArbeid(BEHANDLINGSID);
        final Long soknadUnderArbeidId = soknadUnderArbeidRepository.opprettSoknad(soknadUnderArbeid, EIER);
        soknadUnderArbeid.withSoknadId(soknadUnderArbeidId).withVersjon(2L)
                .withData(DATA_OPPDATERT).withSistEndretDato(SIST_ENDRET_DATO_OPPDATERT);

        soknadUnderArbeidRepository.oppdaterSoknadsdata(soknadUnderArbeid, EIER);

        SoknadUnderArbeid soknadUnderArbeidFraDb = soknadUnderArbeidRepository.hentSoknad(soknadUnderArbeidId, EIER).get();
        assertThat(soknadUnderArbeidFraDb.getVersjon(), is(2L));
        assertThat(soknadUnderArbeidFraDb.getData(), is(DATA_OPPDATERT));
        assertThat(soknadUnderArbeidFraDb.getSistEndretDato(), is(SIST_ENDRET_DATO_OPPDATERT));
    }

    @Test
    public void oppdaterInnsendingStatusOppdatererStatusOgSistEndretDato() {
        SoknadUnderArbeid soknadUnderArbeid = lagSoknadUnderArbeid(BEHANDLINGSID);
        final Long soknadUnderArbeidId = soknadUnderArbeidRepository.opprettSoknad(soknadUnderArbeid, EIER);
        soknadUnderArbeid.withSoknadId(soknadUnderArbeidId).withInnsendingStatus(AVBRUTT_AV_BRUKER)
                .withSistEndretDato(SIST_ENDRET_DATO_OPPDATERT);

        soknadUnderArbeidRepository.oppdaterInnsendingStatus(soknadUnderArbeid, EIER);

        SoknadUnderArbeid soknadUnderArbeidFraDb = soknadUnderArbeidRepository.hentSoknad(soknadUnderArbeidId, EIER).get();
        assertThat(soknadUnderArbeidFraDb.getVersjon(), is(1L));
        assertThat(soknadUnderArbeidFraDb.getInnsendingStatus(), is(AVBRUTT_AV_BRUKER));
        assertThat(soknadUnderArbeidFraDb.getSistEndretDato(), is(SIST_ENDRET_DATO_OPPDATERT));
    }

    @Test
    public void slettSoknadSletterSoknadUnderArbeidFraDatabasen() {
        SoknadUnderArbeid soknadUnderArbeid = lagSoknadUnderArbeid(BEHANDLINGSID);
        final Long soknadUnderArbeidId = soknadUnderArbeidRepository.opprettSoknad(soknadUnderArbeid, EIER);
        soknadUnderArbeid.setSoknadId(soknadUnderArbeidId);
        final String opplastetVedleggUuid = opplastetVedleggRepository.opprettVedlegg(lagOpplastetVedlegg(soknadUnderArbeidId), EIER);

        soknadUnderArbeidRepository.slettSoknad(soknadUnderArbeid, EIER);

        assertThat(soknadUnderArbeidRepository.hentSoknad(soknadUnderArbeidId, EIER).isPresent(), is(false));
        assertThat(opplastetVedleggRepository.hentVedlegg(opplastetVedleggUuid, EIER).isPresent(), is(false));
    }

    private SoknadUnderArbeid lagSoknadUnderArbeid(String behandlingsId) {
        return new SoknadUnderArbeid().withVersjon(1L)
                .withBehandlingsId(behandlingsId)
                .withTilknyttetBehandlingsId(TILKNYTTET_BEHANDLINGSID)
                .withEier(EIER)
                .withData(DATA)
                .withInnsendingStatus(UNDER_ARBEID)
                .withOpprettetDato(OPPRETTET_DATO)
                .withSistEndretDato(SIST_ENDRET_DATO);
    }

    private OpplastetVedlegg lagOpplastetVedlegg(Long soknadId) {
        return new OpplastetVedlegg()
                .withEier(EIER)
                .withVedleggType(new VedleggType("bostotte", "annetboutgift"))
                .withData(new byte[]{1, 2, 3})
                .withSoknadId(soknadId)
                .withFilnavn("dokumentasjon.pdf")
                .withSha512("aaa");
    }
}