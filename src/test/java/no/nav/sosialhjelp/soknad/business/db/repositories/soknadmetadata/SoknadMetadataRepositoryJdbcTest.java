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

import static org.assertj.core.api.Assertions.assertThat;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = {DbTestConfig.class})
public class SoknadMetadataRepositoryJdbcTest {

    private static final String EIER = "11111111111";
    private final int dagerGammelSoknad = 20;
    private final String behandlingsId = "1100AAAAA";

    @Inject
    private SoknadMetadataRepository soknadMetadataRepository;

    @Inject
    private RepositoryTestSupport support;

    @After
    public void teardown() {
        support.getJdbcTemplate().update("DELETE FROM soknadmetadata");
    }

    @Test
    public void oppdaterLestDittNav() {
        var soknadMetadata = soknadMetadata(behandlingsId, SoknadMetadataInnsendingStatus.UNDER_ARBEID, 12);
        assertThat(soknadMetadata.lestDittNav).isFalse();
        soknadMetadataRepository.opprett(soknadMetadata);

        soknadMetadata = soknadMetadataRepository.hent(soknadMetadata.behandlingsId);
        soknadMetadata.lestDittNav = true;

        soknadMetadataRepository.oppdaterLestDittNav(soknadMetadata, EIER);

        var soknadMetadataFraDb = soknadMetadataRepository.hent(behandlingsId);
        assertThat(soknadMetadataFraDb).isNotNull();
        assertThat(soknadMetadataFraDb.lestDittNav).isTrue();
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
