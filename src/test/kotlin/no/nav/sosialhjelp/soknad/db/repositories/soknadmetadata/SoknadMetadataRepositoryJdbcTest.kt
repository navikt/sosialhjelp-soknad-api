package no.nav.sosialhjelp.soknad.db.repositories.soknadmetadata

import no.nav.sosialhjelp.soknad.config.DbTestConfig
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.ContextConfiguration
import org.springframework.test.context.junit.jupiter.SpringExtension
import java.time.LocalDateTime
import javax.inject.Inject

@ExtendWith(SpringExtension::class)
@ContextConfiguration(classes = [DbTestConfig::class])
@ActiveProfiles("repositoryTest")
internal class SoknadMetadataRepositoryJdbcTest {

    private val dagerGammelSoknad = 20
    private val behandlingsId = "1100AAAAA"

    @Inject
    private val soknadMetadataRepository: SoknadMetadataRepository? = null

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
        soknadMetadataRepository!!.opprett(soknadMetadata)

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
            id = soknadMetadataRepository!!.hentNesteId(),
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
