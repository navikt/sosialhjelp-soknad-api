package no.nav.sosialhjelp.soknad.db.repositories.sendtsoknad

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
import java.time.temporal.ChronoUnit
import javax.inject.Inject

@ExtendWith(SpringExtension::class)
@ContextConfiguration(classes = [DbTestConfig::class])
@ActiveProfiles("repositoryTest")
internal class BatchSendtSoknadRepositoryJdbcTest {

    @Inject
    private lateinit var sendtSoknadRepository: SendtSoknadRepository

    @Inject
    private lateinit var batchSendtSoknadRepository: BatchSendtSoknadRepository

    @Inject
    private lateinit var jdbcTemplate: JdbcTemplate

    @AfterEach
    fun tearDown() {
        jdbcTemplate.update("delete from SENDT_SOKNAD")
    }

    @Test
    fun hentSendtSoknadHenterSendtSoknadForEierOgBehandlingsid() {
        sendtSoknadRepository.opprettSendtSoknad(lagSendtSoknad(EIER), EIER)
        val sendtSoknadId = batchSendtSoknadRepository.hentSendtSoknad(BEHANDLINGSID)
        assertThat(sendtSoknadId).isNotNull
    }

    @Test
    fun slettSendtSoknadSletterSoknadFraDatabase() {
        val sendtSoknad = lagSendtSoknad(EIER)
        val sendtSoknadId = sendtSoknadRepository.opprettSendtSoknad(sendtSoknad, EIER)
        sendtSoknad.sendtSoknadId = sendtSoknadId!!
        batchSendtSoknadRepository.slettSendtSoknad(sendtSoknadId)
        assertThat(batchSendtSoknadRepository.hentSendtSoknad(BEHANDLINGSID)).isNull()
    }

    private fun lagSendtSoknad(eier: String): SendtSoknad {
        return SendtSoknad(
            behandlingsId = BEHANDLINGSID,
            tilknyttetBehandlingsId = TILKNYTTET_BEHANDLINGSID,
            eier = eier,
            fiksforsendelseId = FIKSFORSENDELSEID,
            orgnummer = ORGNUMMER,
            navEnhetsnavn = NAVENHETSNAVN,
            brukerOpprettetDato = BRUKER_OPPRETTET_DATO,
            brukerFerdigDato = BRUKER_FERDIG_DATO,
            sendtDato = SENDT_DATO
        )
    }

    companion object {
        private const val EIER = "12345678901"
        private const val BEHANDLINGSID = "1100020"
        private const val TILKNYTTET_BEHANDLINGSID = "4567"
        private const val FIKSFORSENDELSEID = "12345"
        private const val ORGNUMMER = "987654"
        private const val NAVENHETSNAVN = "NAV Enhet"
        private val BRUKER_OPPRETTET_DATO = LocalDateTime.now().minusDays(2).truncatedTo(ChronoUnit.MILLIS)
        private val BRUKER_FERDIG_DATO = LocalDateTime.now().minusSeconds(50).truncatedTo(ChronoUnit.MILLIS)
        private val SENDT_DATO = LocalDateTime.now().truncatedTo(ChronoUnit.MILLIS)
    }
}
