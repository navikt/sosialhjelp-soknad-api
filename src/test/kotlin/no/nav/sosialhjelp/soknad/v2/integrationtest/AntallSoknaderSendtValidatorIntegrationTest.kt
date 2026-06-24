package no.nav.sosialhjelp.soknad.v2.integrationtest

import no.nav.sosialhjelp.soknad.app.subjecthandler.StaticSubjectHandlerImpl
import no.nav.sosialhjelp.soknad.app.subjecthandler.SubjectHandlerUtils
import no.nav.sosialhjelp.soknad.v2.AntallSoknaderSendtException
import no.nav.sosialhjelp.soknad.v2.AntallSoknaderSendtValidator
import no.nav.sosialhjelp.soknad.v2.AntallSoknaderSendtValidator.Companion.MAX_ANTALL_SOKNADER
import no.nav.sosialhjelp.soknad.v2.json.generate.TimestampUtil.nowWithMillis
import no.nav.sosialhjelp.soknad.v2.metadata.SoknadStatus
import no.nav.sosialhjelp.soknad.v2.opprettSoknadMetadata
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatCode
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import java.time.LocalDateTime
import java.time.temporal.ChronoUnit
import java.util.UUID

class AntallSoknaderSendtValidatorIntegrationTest : AbstractIntegrationTest() {
    override var opprettSoknadBeforeEach = false
    private val maksAntallSoknader = MAX_ANTALL_SOKNADER

    @Autowired
    private lateinit var antallSoknaderSendtValidator: AntallSoknaderSendtValidator

    @BeforeEach
    fun setup() {
        metadataRepository.deleteAll()
        StaticSubjectHandlerImpl()
            .apply { setUser(userId) }
            .also { SubjectHandlerUtils.setNewSubjectHandlerImpl(it) }
    }

    @AfterEach
    fun tearDown() {
        SubjectHandlerUtils.resetSubjectHandlerImpl()
    }

    @Test
    fun `validering passerer naar bruker har under maks antall sendte soknader siste 24 timer`() {
        repeat(maksAntallSoknader - 1) { sendtSoknadForBruker(sendtInn = nowWithMillis().minusHours(1)) }

        assertThatCode {
            antallSoknaderSendtValidator.validate(UUID.randomUUID())
        }.doesNotThrowAnyException()
    }

    @Test
    fun `validering feiler naar bruker har over maks antall sendte soknader siste 24 timer`() {
        val tidspunkter =
            (0 until maksAntallSoknader + 1).map {
                nowWithMillis().minusHours((1 + it).toLong())
            }
        tidspunkter.forEach { sendtSoknadForBruker(sendtInn = it) }
        val maksgrenseTeNyeste = tidspunkter[maksAntallSoknader - 1]

        assertThatThrownBy {
            antallSoknaderSendtValidator.validate(UUID.randomUUID())
        }
            .isInstanceOfSatisfying(AntallSoknaderSendtException::class.java) { exception ->
                assertThat(exception.antall).isEqualTo(maksAntallSoknader + 1)
                assertThat(exception.innsendingTillattFra).isEqualTo(
                    maksgrenseTeNyeste.plusDays(1).plusMinutes(1).truncatedTo(ChronoUnit.MINUTES),
                )
            }
    }

    @Test
    fun `validering ignorerer soknader eldre enn 24 timer`() {
        repeat(maksAntallSoknader + 1) { sendtSoknadForBruker(sendtInn = nowWithMillis().minusHours(25)) }

        assertThatCode {
            antallSoknaderSendtValidator.validate(UUID.randomUUID())
        }.doesNotThrowAnyException()
    }

    @Test
    fun `validering ignorerer soknader for annen bruker`() {
        repeat(maksAntallSoknader + 1) {
            metadataRepository.save(
                opprettSoknadMetadata(
                    status = SoknadStatus.SENDT,
                    personId = "annenbruker",
                    innsendtDato = nowWithMillis().minusHours(1),
                ),
            )
        }

        assertThatCode {
            antallSoknaderSendtValidator.validate(UUID.randomUUID())
        }.doesNotThrowAnyException()
    }

    @Test
    fun `validering ignorerer soknader med status OPPRETTET og INNSENDING_FEILET`() {
        metadataRepository.save(opprettSoknadMetadata(status = SoknadStatus.OPPRETTET, personId = userId, innsendtDato = nowWithMillis().minusHours(1)))
        metadataRepository.save(opprettSoknadMetadata(status = SoknadStatus.INNSENDING_FEILET, personId = userId, innsendtDato = nowWithMillis().minusHours(1)))
        repeat(maksAntallSoknader - 1) { sendtSoknadForBruker(sendtInn = nowWithMillis().minusHours(1)) }

        assertThatCode {
            antallSoknaderSendtValidator.validate(UUID.randomUUID())
        }.doesNotThrowAnyException()
    }

    private fun sendtSoknadForBruker(sendtInn: LocalDateTime) {
        metadataRepository.save(
            opprettSoknadMetadata(
                status = SoknadStatus.SENDT,
                personId = userId,
                innsendtDato = sendtInn,
            ),
        )
    }
}
