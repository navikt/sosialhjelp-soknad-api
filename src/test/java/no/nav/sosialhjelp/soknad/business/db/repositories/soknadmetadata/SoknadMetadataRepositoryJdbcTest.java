package no.nav.sosialhjelp.soknad.business.db.repositories.soknadmetadata;

import no.nav.sosialhjelp.soknad.business.db.RepositoryTestSupport;
import no.nav.sosialhjelp.soknad.business.domain.SoknadMetadata;
import no.nav.sosialhjelp.soknad.config.DbTestConfig;
import no.nav.sosialhjelp.soknad.domain.SoknadMetadataInnsendingStatus;
import no.nav.sosialhjelp.soknad.domain.model.kravdialoginformasjon.SoknadType;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import javax.inject.Inject;
import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = {DbTestConfig.class})
@ActiveProfiles("test")
class SoknadMetadataRepositoryJdbcTest {

    private static final String EIER = "11111111111";
    private final int dagerGammelSoknad = 20;
    private final String behandlingsId = "1100AAAAA";

    @Inject
    private SoknadMetadataRepository soknadMetadataRepository;

    @Inject
    private RepositoryTestSupport support;

    @AfterEach
    public void teardown() {
        support.getJdbcTemplate().update("DELETE FROM soknadmetadata");
    }

    @Test
    void oppdaterLestDittNav() {
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
