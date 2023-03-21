package no.nav.sosialhjelp.soknad.migration.repo

import jakarta.inject.Inject
import no.nav.sosialhjelp.soknad.db.DbTestConfig
import no.nav.sosialhjelp.soknad.db.repositories.soknadunderarbeid.SoknadUnderArbeid
import no.nav.sosialhjelp.soknad.db.repositories.soknadunderarbeid.SoknadUnderArbeidRepository
import no.nav.sosialhjelp.soknad.db.repositories.soknadunderarbeid.SoknadUnderArbeidStatus
import no.nav.sosialhjelp.soknad.innsending.SoknadService
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.ContextConfiguration
import org.springframework.test.context.junit.jupiter.SpringExtension
import java.time.LocalDateTime
import java.time.temporal.ChronoUnit

@ExtendWith(SpringExtension::class)
@ContextConfiguration(classes = [DbTestConfig::class])
@ActiveProfiles("test")
internal class SoknadUnderArbeidMigrationRepositoryTest {

    @Inject
    private lateinit var jdbcTemplate: JdbcTemplate

    @Inject
    private lateinit var soknadUnderArbeidRepository: SoknadUnderArbeidRepository

    @Inject
    private lateinit var soknadUnderArbeidMigrationRepository: SoknadUnderArbeidMigrationRepository

    @AfterEach
    fun tearDown() {
        jdbcTemplate.update("delete from SOKNAD_UNDER_ARBEID")
    }

    @Test
    internal fun `skal hente soknadUnderArbeid for behandlingsId`() {
        val soknadUnderArbeid = createSoknadUnderArbeid(behandlingsId = "123")
        soknadUnderArbeidRepository.opprettSoknad(soknadUnderArbeid, EIER)

        val result = soknadUnderArbeidMigrationRepository.getSoknadUnderArbeid(behandlingsId = "123")

        assertThat(result).isNotNull
        assertThat(result?.behandlingsId).isEqualTo("123")
    }

    @Test
    internal fun `skal returnere null`() {
        val soknadUnderArbeid = createSoknadUnderArbeid(behandlingsId = "abc")
        soknadUnderArbeidRepository.opprettSoknad(soknadUnderArbeid, EIER)

        val result = soknadUnderArbeidMigrationRepository.getSoknadUnderArbeid(behandlingsId = "123")

        assertThat(result).isNull()
    }

    @Test
    internal fun `count skal returnere antall`() {
        assertThat(soknadUnderArbeidMigrationRepository.count()).isEqualTo(0)

        val soknadUnderArbeid = createSoknadUnderArbeid(behandlingsId = "abc")
        soknadUnderArbeidRepository.opprettSoknad(soknadUnderArbeid, EIER)
        assertThat(soknadUnderArbeidMigrationRepository.count()).isEqualTo(1)

        val soknadUnderArbeid2 = createSoknadUnderArbeid(behandlingsId = "def")
        soknadUnderArbeidRepository.opprettSoknad(soknadUnderArbeid2, EIER)
        assertThat(soknadUnderArbeidMigrationRepository.count()).isEqualTo(2)
    }

    companion object {
        private const val EIER = "eier"

        private fun createSoknadUnderArbeid(behandlingsId: String): SoknadUnderArbeid {
            return SoknadUnderArbeid(
                versjon = 1L,
                behandlingsId = behandlingsId,
                tilknyttetBehandlingsId = null,
                eier = EIER,
                jsonInternalSoknad = SoknadService.createEmptyJsonInternalSoknad(EIER),
                status = SoknadUnderArbeidStatus.UNDER_ARBEID,
                opprettetDato = LocalDateTime.now().minusSeconds(50).truncatedTo(ChronoUnit.MILLIS),
                sistEndretDato = LocalDateTime.now().truncatedTo(ChronoUnit.MILLIS)
            )
        }
    }
}
