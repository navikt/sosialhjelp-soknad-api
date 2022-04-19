package no.nav.sosialhjelp.soknad.migration.repo

import no.nav.sosialhjelp.soknad.config.DbTestConfig
import no.nav.sosialhjelp.soknad.db.repositories.sendtsoknad.SendtSoknad
import no.nav.sosialhjelp.soknad.db.repositories.sendtsoknad.SendtSoknadRepository
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
import javax.inject.Inject

@ExtendWith(SpringExtension::class)
@ContextConfiguration(classes = [DbTestConfig::class])
@ActiveProfiles("test")
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

    companion object {
        private const val EIER = "eier"

        private fun createSendtSoknad(eier: String = EIER, behandlingsId: String): SendtSoknad {
            return SendtSoknad(
                behandlingsId = behandlingsId,
                tilknyttetBehandlingsId = null,
                eier = eier,
                fiksforsendelseId = "fiksforsendelseId",
                orgnummer = "ORGNUMMER",
                navEnhetsnavn = "NAVENHETSNAVN",
                brukerOpprettetDato = LocalDateTime.now().minusDays(2).truncatedTo(ChronoUnit.MILLIS),
                brukerFerdigDato = LocalDateTime.now().minusSeconds(50).truncatedTo(ChronoUnit.MILLIS),
                sendtDato = LocalDateTime.now().truncatedTo(ChronoUnit.MILLIS)
            )
        }
    }
}
