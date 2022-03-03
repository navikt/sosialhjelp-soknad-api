package no.nav.sosialhjelp.soknad.db.repositories.sendtsoknad

import no.nav.sosialhjelp.soknad.config.DbTestConfig
import no.nav.sosialhjelp.soknad.config.RepositoryTestSupport
import no.nav.sosialhjelp.soknad.domain.SendtSoknad
import org.assertj.core.api.Assertions
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.ContextConfiguration
import org.springframework.test.context.junit.jupiter.SpringExtension
import java.time.LocalDateTime
import java.time.temporal.ChronoUnit
import javax.inject.Inject

@ExtendWith(SpringExtension::class)
@ContextConfiguration(classes = [DbTestConfig::class])
@ActiveProfiles("test")
internal class BatchSendtSoknadRepositoryJdbcTest {

    @Inject
    private val sendtSoknadRepository: SendtSoknadRepository? = null

    @Inject
    private val batchSendtSoknadRepository: BatchSendtSoknadRepository? = null

    @Inject
    private val soknadRepositoryTestSupport: RepositoryTestSupport? = null

    @AfterEach
    fun tearDown() {
        soknadRepositoryTestSupport!!.getJdbcTemplate().update("delete from SENDT_SOKNAD")
    }

    @Test
    fun hentSendtSoknadHenterSendtSoknadForEierOgBehandlingsid() {
        sendtSoknadRepository!!.opprettSendtSoknad(lagSendtSoknad(EIER), EIER)
        val sendtSoknadId = batchSendtSoknadRepository!!.hentSendtSoknad(BEHANDLINGSID).get()
        Assertions.assertThat(sendtSoknadId).isNotNull
    }

    @Test
    fun slettSendtSoknadSletterSoknadFraDatabase() {
        val sendtSoknad = lagSendtSoknad(EIER)
        val sendtSoknadId = sendtSoknadRepository!!.opprettSendtSoknad(sendtSoknad, EIER)
        sendtSoknad.sendtSoknadId = sendtSoknadId
        batchSendtSoknadRepository!!.slettSendtSoknad(sendtSoknadId)
        Assertions.assertThat(batchSendtSoknadRepository.hentSendtSoknad(BEHANDLINGSID)).isEmpty
    }

    private fun lagSendtSoknad(eier: String): SendtSoknad {
        return SendtSoknad().withEier(eier)
            .withBehandlingsId(BEHANDLINGSID)
            .withTilknyttetBehandlingsId(TILKNYTTET_BEHANDLINGSID)
            .withFiksforsendelseId(FIKSFORSENDELSEID)
            .withOrgnummer(ORGNUMMER)
            .withNavEnhetsnavn(NAVENHETSNAVN)
            .withBrukerOpprettetDato(BRUKER_OPPRETTET_DATO)
            .withBrukerFerdigDato(BRUKER_FERDIG_DATO)
            .withSendtDato(SENDT_DATO)
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
