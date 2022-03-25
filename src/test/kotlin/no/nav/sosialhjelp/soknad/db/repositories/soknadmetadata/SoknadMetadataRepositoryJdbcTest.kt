package no.nav.sosialhjelp.soknad.db.repositories.soknadmetadata

import no.nav.sosialhjelp.soknad.Application
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Test
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.test.context.ActiveProfiles
import java.time.LocalDateTime
import javax.inject.Inject

@ActiveProfiles(profiles = ["no-redis", "test"])
@SpringBootTest(classes = [Application::class])
internal class SoknadMetadataRepositoryJdbcTest {

    private val behandlingsId = "1100AAAAA"

    @Inject
    private lateinit var soknadMetadataRepository: SoknadMetadataRepository

    @Inject
    private lateinit var jdbcTemplate: JdbcTemplate

    @AfterEach
    fun teardown() {
        jdbcTemplate.update("DELETE FROM soknadmetadata")
    }

    @Test
    fun oppdaterLestDittNav() {
        var soknadMetadata = soknadMetadata(behandlingsId, SoknadMetadataInnsendingStatus.UNDER_ARBEID, 12)
        assertThat(soknadMetadata.lestDittNav).isFalse
        soknadMetadataRepository.opprett(soknadMetadata)

        soknadMetadata = soknadMetadataRepository.hent(soknadMetadata.behandlingsId)!!
        soknadMetadata.lestDittNav = true

        soknadMetadataRepository.oppdaterLestDittNav(soknadMetadata, EIER)

        val soknadMetadataFraDb = soknadMetadataRepository.hent(behandlingsId)
        assertThat(soknadMetadataFraDb).isNotNull
        assertThat(soknadMetadataFraDb?.lestDittNav).isTrue
    }

    private fun soknadMetadata(
        behandlingsId: String,
        status: SoknadMetadataInnsendingStatus,
        dagerSiden: Int,
    ): SoknadMetadata {
        return SoknadMetadata(
            id = soknadMetadataRepository.hentNesteId(),
            behandlingsId = behandlingsId,
            fnr = EIER,
            type = SoknadMetadataType.SEND_SOKNAD_KOMMUNAL,
            skjema = "",
            status = status,
            innsendtDato = LocalDateTime.now().minusDays(dagerSiden.toLong()),
            opprettetDato = LocalDateTime.now().minusDays(dagerSiden.toLong()),
            sistEndretDato = LocalDateTime.now().minusDays(dagerSiden.toLong()),
            lestDittNav = false,
        )
    }

    companion object {
        private const val EIER = "11111111111"
    }
}
