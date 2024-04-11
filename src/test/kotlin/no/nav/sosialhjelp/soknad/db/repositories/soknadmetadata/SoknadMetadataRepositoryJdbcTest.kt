package no.nav.sosialhjelp.soknad.db.repositories.soknadmetadata

import no.nav.sosialhjelp.soknad.innsending.SenderUtils
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.fail
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.test.context.ActiveProfiles
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDateTime
import java.util.*

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
@Transactional
@ActiveProfiles("no-redis", "test", "test-container")
internal class SoknadMetadataRepositoryJdbcTest {

    private val behandlingsId = UUID.randomUUID().toString()

    @Autowired
    private lateinit var soknadMetadataRepository: SoknadMetadataRepository

    @Autowired
    private lateinit var jdbcTemplate: JdbcTemplate

    @AfterEach
    fun teardown() {
        jdbcTemplate.update("DELETE FROM soknadmetadata")
    }

    @Test
    fun `Hent neste id i id_sequence`() {
        val nesteId = soknadMetadataRepository.hentNesteId()
        // ved bytte fra langlevende Oracle-base til ny Postgres, er dette neste nummer i sekvensen
        assertThat(nesteId).isEqualTo(4360796)

        val behandlingsIds = mutableSetOf<String>()

        for (i in 1..100) {
            val nextId = soknadMetadataRepository.hentNesteId()
            assertThat(nextId).isEqualTo(nesteId + i)
            // legger til i Set for å sikre at alle er unike
            behandlingsIds.add(SenderUtils.lagBehandlingsId(nextId))
        }
        assertThat(behandlingsIds.size).isEqualTo(100)
    }

    @Test
    fun `Id gammelt format lagres riktig`() {
        val soknadMetadata =
            lagSoknadMetadata(UUID.randomUUID().toString(), SoknadMetadataInnsendingStatus.UNDER_ARBEID, 10)
                .run {
                    idGammeltFormat = SenderUtils.lagBehandlingsId(soknadMetadataRepository.hentNesteId())
                    soknadMetadataRepository.opprett(this)
                    this
                }

        soknadMetadataRepository.hent(soknadMetadata.behandlingsId)
            ?.let { assertThat(it.idGammeltFormat).isEqualTo(soknadMetadata.idGammeltFormat) }
            ?: fail("Kunne ikke lagre SoknadMetadata")
    }

    @Test
    fun oppdaterLest() {
        var soknadMetadata = lagSoknadMetadata(behandlingsId, SoknadMetadataInnsendingStatus.UNDER_ARBEID, 12)
        assertThat(soknadMetadata.lest).isFalse
        soknadMetadataRepository.opprett(soknadMetadata)

        soknadMetadata = soknadMetadataRepository.hent(soknadMetadata.behandlingsId)!!
        soknadMetadata.lest = true

        soknadMetadataRepository.oppdaterLest(soknadMetadata, EIER)

        val soknadMetadataFraDb = soknadMetadataRepository.hent(behandlingsId)
        assertThat(soknadMetadataFraDb).isNotNull
        assertThat(soknadMetadataFraDb?.lest).isTrue
    }

    private fun lagSoknadMetadata(
        behandlingsId: String,
        status: SoknadMetadataInnsendingStatus,
        dagerSiden: Int
    ): SoknadMetadata {
        return SoknadMetadata(
            id = 0,
            behandlingsId = behandlingsId,
            idGammeltFormat = behandlingsId,
            fnr = EIER,
            type = SoknadMetadataType.SEND_SOKNAD_KOMMUNAL,
            skjema = "",
            status = status,
            innsendtDato = LocalDateTime.now().minusDays(dagerSiden.toLong()),
            opprettetDato = LocalDateTime.now().minusDays(dagerSiden.toLong()),
            sistEndretDato = LocalDateTime.now().minusDays(dagerSiden.toLong()),
            lest = false
        )
    }

    companion object {
        private const val EIER = "11111111111"
    }
}
