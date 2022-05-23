package no.nav.sosialhjelp.soknad.migration.repo

import no.nav.sosialhjelp.soknad.Application
import no.nav.sosialhjelp.soknad.db.repositories.sendtsoknad.SendtSoknad
import no.nav.sosialhjelp.soknad.db.repositories.sendtsoknad.SendtSoknadRepository
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Test
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.test.context.ActiveProfiles
import java.time.LocalDateTime
import java.time.temporal.ChronoUnit
import javax.inject.Inject

@ActiveProfiles(profiles = ["no-redis", "test"])
@SpringBootTest(classes = [Application::class])
internal class SendtSoknadMigrationRepositoryTest {

    @Inject
    private lateinit var sendtSoknadMigrationRepository: SendtSoknadMigrationRepository

    @Inject
    private lateinit var sendtSoknadRepository: SendtSoknadRepository

    @Inject
    private lateinit var jdbcTemplate: JdbcTemplate

    @AfterEach
    fun tearDown() {
        jdbcTemplate.update("delete from SENDT_SOKNAD")
    }

    @Test
    internal fun `skal hente sendtSoknad for behandlingsId`() {
        val sendtSoknad = createSendtSoknad(behandlingsId = "123")
        sendtSoknadRepository.opprettSendtSoknad(sendtSoknad, EIER)

        val result = sendtSoknadMigrationRepository.getSendtSoknad(behandlingsId = "123")

        assertThat(result).isNotNull
        assertThat(result?.behandlingsId).isEqualTo("123")
    }

    @Test
    internal fun `skal returnere null`() {
        val sendtSoknad = createSendtSoknad(behandlingsId = "abc")
        sendtSoknadRepository.opprettSendtSoknad(sendtSoknad, EIER)

        val result = sendtSoknadMigrationRepository.getSendtSoknad(behandlingsId = "123")

        assertThat(result).isNull()
    }

    @Test
    internal fun `count skal returnere antall`() {
        assertThat(sendtSoknadMigrationRepository.count()).isEqualTo(0)

        val sendtSoknad = createSendtSoknad(behandlingsId = "abc", fiksForsendelseId = "1111")
        sendtSoknadRepository.opprettSendtSoknad(sendtSoknad, EIER)
        assertThat(sendtSoknadMigrationRepository.count()).isEqualTo(1)

        val sendtSoknad2 = createSendtSoknad(behandlingsId = "def", fiksForsendelseId = "2222")
        sendtSoknadRepository.opprettSendtSoknad(sendtSoknad2, EIER)
        assertThat(sendtSoknadMigrationRepository.count()).isEqualTo(2)
    }

    companion object {
        private const val EIER = "eier"

        private fun createSendtSoknad(
            eier: String = EIER,
            behandlingsId: String,
            fiksForsendelseId: String = "fiksforsendelseId"
        ): SendtSoknad {
            return SendtSoknad(
                behandlingsId = behandlingsId,
                tilknyttetBehandlingsId = null,
                eier = eier,
                fiksforsendelseId = fiksForsendelseId,
                orgnummer = "ORGNUMMER",
                navEnhetsnavn = "NAVENHETSNAVN",
                brukerOpprettetDato = LocalDateTime.now().minusDays(2).truncatedTo(ChronoUnit.MILLIS),
                brukerFerdigDato = LocalDateTime.now().minusSeconds(50).truncatedTo(ChronoUnit.MILLIS),
                sendtDato = LocalDateTime.now().truncatedTo(ChronoUnit.MILLIS)
            )
        }
    }
}
