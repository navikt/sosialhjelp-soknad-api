package no.nav.sosialhjelp.soknad.business.db.repositories.soknadmetadata;

import no.nav.sosialhjelp.soknad.business.db.RepositoryTestSupport;
import no.nav.sosialhjelp.soknad.business.db.config.DbTestConfig;
import no.nav.sosialhjelp.soknad.business.domain.SoknadMetadata;
import no.nav.sosialhjelp.soknad.domain.SoknadMetadataInnsendingStatus;
import no.nav.sosialhjelp.soknad.domain.model.kravdialoginformasjon.SoknadType;
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
public class BatchSoknadMetadataRepositoryJdbcTest {

    private static final String EIER = "11111111111";
    private final int dagerGammelSoknad = 20;
    private final String behandlingsId = "1100AAAAA";

    @Inject
    private BatchSoknadMetadataRepository batchSoknadMetadataRepository;

    @Inject
    private SoknadMetadataRepository soknadMetadataRepository;

    @Inject
    private RepositoryTestSupport support;

    @After
    public void teardown() {
        support.getJdbcTemplate().update("DELETE FROM soknadmetadata WHERE behandlingsid = ?", behandlingsId);
    }

    @Test
    public void hentForBatchSkalIkkeReturnereFerdige() {
        opprettSoknadMetadata(soknadMetadata(behandlingsId, SoknadMetadataInnsendingStatus.FERDIG, dagerGammelSoknad));
        assertThat(batchSoknadMetadataRepository.hentForBatch(dagerGammelSoknad - 1)).isNotPresent();
    }

    @Test
    public void hentForBatchSkalIkkeReturnereAvbruttAutomatisk() {
        opprettSoknadMetadata(soknadMetadata(behandlingsId, SoknadMetadataInnsendingStatus.AVBRUTT_AUTOMATISK, dagerGammelSoknad));
        assertThat(batchSoknadMetadataRepository.hentForBatch(dagerGammelSoknad - 1)).isNotPresent();
    }

    @Test
    public void hentForBatchSkalIkkeReturnereAvbruttAvBruker() {
        opprettSoknadMetadata(soknadMetadata(behandlingsId, SoknadMetadataInnsendingStatus.AVBRUTT_AV_BRUKER, dagerGammelSoknad));
        assertThat(batchSoknadMetadataRepository.hentForBatch(dagerGammelSoknad - 1)).isNotPresent();
    }

    @Test
    public void hentForBatchBrukerEndringstidspunkt() {
        opprettSoknadMetadata(soknadMetadata(behandlingsId, SoknadMetadataInnsendingStatus.UNDER_ARBEID, dagerGammelSoknad));
        assertThat(batchSoknadMetadataRepository.hentForBatch(dagerGammelSoknad - 1)).isPresent();
        assertThat(batchSoknadMetadataRepository.hentForBatch(dagerGammelSoknad + 1)).isNotPresent();
    }

    @Test
    public void hentEldreEnnBrukerEndringstidspunktUavhengigAvStatus() {
        List<SoknadMetadataInnsendingStatus> statuser = asList(SoknadMetadataInnsendingStatus.UNDER_ARBEID, SoknadMetadataInnsendingStatus.FERDIG,
                SoknadMetadataInnsendingStatus.AVBRUTT_AUTOMATISK, SoknadMetadataInnsendingStatus.AVBRUTT_AV_BRUKER);
        for (SoknadMetadataInnsendingStatus status : statuser) {
            opprettSoknadMetadata(soknadMetadata(behandlingsId, status, dagerGammelSoknad));
            assertThat(batchSoknadMetadataRepository.hentEldreEnn(dagerGammelSoknad - 1)).isPresent();
            assertThat(batchSoknadMetadataRepository.hentEldreEnn(dagerGammelSoknad + 1)).isNotPresent();
            batchSoknadMetadataRepository.slettSoknadMetaData(behandlingsId);
        }
    }


    private void opprettSoknadMetadata(SoknadMetadata soknadMetadata) {
        soknadMetadataRepository.opprett(soknadMetadata);
        final SoknadMetadata lagretSoknadMetadata = soknadMetadataRepository.hent(soknadMetadata.behandlingsId);
        batchSoknadMetadataRepository.leggTilbakeBatch(lagretSoknadMetadata.id);
    }

    private SoknadMetadata soknadMetadata(String behandlingsId, SoknadMetadataInnsendingStatus status, int dagerSiden) {
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
        meta.lestDittNav = false;

        return meta;
    }
}