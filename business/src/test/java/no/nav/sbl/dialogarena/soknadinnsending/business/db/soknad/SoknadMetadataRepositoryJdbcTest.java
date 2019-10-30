package no.nav.sbl.dialogarena.soknadinnsending.business.db.soknad;

import no.nav.sbl.dialogarena.sendsoknad.domain.SoknadInnsendingStatus;
import no.nav.sbl.dialogarena.sendsoknad.domain.kravdialoginformasjon.SoknadType;
import no.nav.sbl.dialogarena.soknadinnsending.business.db.DbTestConfig;
import no.nav.sbl.dialogarena.soknadinnsending.business.db.TestSupport;
import no.nav.sbl.dialogarena.soknadinnsending.business.db.soknadmetadata.SoknadMetadataRepository;
import no.nav.sbl.dialogarena.soknadinnsending.business.domain.SoknadMetadata;
import org.junit.After;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import javax.inject.Inject;
import java.time.LocalDateTime;
import java.util.List;

import static java.util.Arrays.asList;
import static org.assertj.core.api.Assertions.assertThat;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = {DbTestConfig.class})
public class SoknadMetadataRepositoryJdbcTest {

    private static final String EIER = "21036612271";
    private final int dagerGammelSoknad = 20;
    private final String behandlingsId = "1100AAAAA";

    @Inject
    private SoknadMetadataRepository soknadMetadataRepository;

    @Inject
    private TestSupport support;

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

    @Test
    public void hentEldreEnnBrukerEndringstidspunktUavhengigAvStatus() {
        List<SoknadInnsendingStatus> statuser = asList(SoknadInnsendingStatus.UNDER_ARBEID, SoknadInnsendingStatus.FERDIG,
                SoknadInnsendingStatus.AVBRUTT_AUTOMATISK, SoknadInnsendingStatus.AVBRUTT_AV_BRUKER);
        for (SoknadInnsendingStatus status : statuser) {
            opprettSoknadMetadata(soknadMetadata(behandlingsId, status, dagerGammelSoknad));
            assertThat(soknadMetadataRepository.hentEldreEnn(dagerGammelSoknad - 1).isPresent()).isTrue();
            assertThat(soknadMetadataRepository.hentEldreEnn(dagerGammelSoknad + 1).isPresent()).isFalse();
            soknadMetadataRepository.slettSoknadMetaData(behandlingsId, EIER);
        }
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
        meta.fnr = EIER;
        meta.type = SoknadType.SEND_SOKNAD_KOMMUNAL;
        meta.skjema = "";
        meta.status = status;
        meta.innsendtDato = LocalDateTime.now().minusDays(dagerSiden);
        meta.opprettetDato = LocalDateTime.now().minusDays(dagerSiden);
        meta.sistEndretDato = LocalDateTime.now().minusDays(dagerSiden);

        return meta;
    }
}