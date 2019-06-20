package no.nav.sbl.dialogarena.soknadinnsending.business.db.soknad;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDateTime;

import javax.inject.Inject;

import org.junit.After;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import no.nav.sbl.dialogarena.sendsoknad.domain.SoknadInnsendingStatus;
import no.nav.sbl.dialogarena.sendsoknad.domain.kravdialoginformasjon.SoknadType;
import no.nav.sbl.dialogarena.soknadinnsending.business.db.DbTestConfig;
import no.nav.sbl.dialogarena.soknadinnsending.business.db.TestSupport;
import no.nav.sbl.dialogarena.soknadinnsending.business.db.soknadmetadata.SoknadMetadataRepository;
import no.nav.sbl.dialogarena.soknadinnsending.business.domain.SoknadMetadata;
import no.nav.sbl.dialogarena.soknadinnsending.business.domain.SoknadMetadata.HovedskjemaMetadata;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = {DbTestConfig.class})
public class SoknadMetadataRepositoryJdbcTest {

    @Inject
    private SoknadMetadataRepository soknadMetadataRepository;

    @Inject
    private TestSupport support;

    private final int dagerGammelSoknad = 20;
    private final String behandlingsId = "1100AAAAA";


    @After
    public void teardown() {
        support.getJdbcTemplate().update("DELETE FROM soknadmetadata WHERE behandlingsid = ?", behandlingsId);
    }

    
    @Test
    public void hentForBatchSkalIkkeReturnereFerdige() {
        opprettSoknadMetadata(soknadMetadata(behandlingsId, SoknadInnsendingStatus.FERDIG, dagerGammelSoknad));
        assertThat(soknadMetadataRepository.hentForBatch(dagerGammelSoknad - 1).isPresent()).isFalse();
    }
    
    @Test
    public void hentForBatchSkalIkkeReturnereAvbruttAutomatisk() {
        opprettSoknadMetadata(soknadMetadata(behandlingsId, SoknadInnsendingStatus.AVBRUTT_AUTOMATISK, dagerGammelSoknad));
        assertThat(soknadMetadataRepository.hentForBatch(dagerGammelSoknad - 1).isPresent()).isFalse();
    }
    
    @Test
    public void hentForBatchSkalIkkeReturnereAvbruttAvBruker() {
        opprettSoknadMetadata(soknadMetadata(behandlingsId, SoknadInnsendingStatus.AVBRUTT_AV_BRUKER, dagerGammelSoknad));
        assertThat(soknadMetadataRepository.hentForBatch(dagerGammelSoknad - 1).isPresent()).isFalse();
    }
    
    @Test
    public void hentForBatchBrukerEndringstidspunkt() {
        opprettSoknadMetadata(soknadMetadata(behandlingsId, SoknadInnsendingStatus.UNDER_ARBEID, dagerGammelSoknad));
        assertThat(soknadMetadataRepository.hentForBatch(dagerGammelSoknad - 1).isPresent()).isTrue();
        assertThat(soknadMetadataRepository.hentForBatch(dagerGammelSoknad + 1).isPresent()).isFalse();
    }

    
    private void opprettSoknadMetadata(SoknadMetadata soknadMetadata) {
        soknadMetadataRepository.opprett(soknadMetadata);
        final SoknadMetadata lagretSoknadMetadata = soknadMetadataRepository.hent(soknadMetadata.behandlingsId);
        soknadMetadataRepository.leggTilbakeBatch(lagretSoknadMetadata.id);
    }
    
    private SoknadMetadata soknadMetadata(String behandlingsId, SoknadInnsendingStatus status, int dagerSiden) {
        SoknadMetadata meta = new SoknadMetadata();
        meta.id = soknadMetadataRepository.hentNesteId();
        meta.behandlingsId = behandlingsId;
        meta.fnr = "21036612271";
        meta.type = SoknadType.SEND_SOKNAD_KOMMUNAL;
        meta.skjema = "";
        meta.status = status;
        meta.innsendtDato = LocalDateTime.now().minusDays(dagerSiden);
        meta.opprettetDato = LocalDateTime.now().minusDays(dagerSiden);
        meta.sistEndretDato = LocalDateTime.now().minusDays(dagerSiden);
        meta.hovedskjema = new HovedskjemaMetadata();
        meta.hovedskjema.filUuid = null;
        
        return meta;
    }
}