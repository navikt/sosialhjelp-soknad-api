package no.nav.sosialhjelp.soknad.v2.integrationtest

import no.nav.sosialhjelp.soknad.api.minesaker.AntallInnsendteSoknaderDto
import no.nav.sosialhjelp.soknad.v2.AntallSoknaderSendtValidator.Companion.MAX_ANTALL_SOKNADER
import no.nav.sosialhjelp.soknad.v2.json.generate.TimestampUtil.nowWithMillis
import no.nav.sosialhjelp.soknad.v2.metadata.SoknadStatus
import no.nav.sosialhjelp.soknad.v2.opprettSoknadMetadata
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.time.LocalDateTime
import java.time.temporal.ChronoUnit

class AntallInnsendteSoknaderIntegrationTest : AbstractIntegrationTest(useTokenX = false) {
    override var opprettSoknadBeforeEach = false
    private val maksAntallSoknader = MAX_ANTALL_SOKNADER

    @BeforeEach
    fun setup() {
        metadataRepository.deleteAll()
    }

    @Test
    fun `antallSisteDogn returnerer 0 naar bruker har ingen innsendte soknader`() {
        doGet(URL, AntallInnsendteSoknaderDto::class.java).also {
            assertThat(it.antall).isEqualTo(0)
            assertThat(it.innsendingTillattFra).isNull()
        }
    }

    @Test
    fun `antallSisteDogn returnerer antall innsendte soknader siste 24 timer`() {
        repeat(maksAntallSoknader) { sendtSoknadForBruker(sendtInn = nowWithMillis().minusHours(12)) }

        doGet(URL, AntallInnsendteSoknaderDto::class.java).also {
            assertThat(it.antall).isEqualTo(maksAntallSoknader)
        }
    }

    @Test
    fun `antallSisteDogn inkluderer ikke soknader eldre enn 24 timer`() {
        sendtSoknadForBruker(sendtInn = nowWithMillis().minusHours(12))
        sendtSoknadForBruker(sendtInn = nowWithMillis().minusHours(25))

        doGet(URL, AntallInnsendteSoknaderDto::class.java).also {
            assertThat(it.antall).isEqualTo(1)
        }
    }

    @Test
    fun `antallSisteDogn inkluderer ikke soknader med status OPPRETTET eller INNSENDING_FEILET`() {
        metadataRepository.save(opprettSoknadMetadata(status = SoknadStatus.OPPRETTET, personId = userId, innsendtDato = nowWithMillis().minusHours(1)))
        metadataRepository.save(opprettSoknadMetadata(status = SoknadStatus.INNSENDING_FEILET, personId = userId, innsendtDato = nowWithMillis().minusHours(1)))
        sendtSoknadForBruker(sendtInn = nowWithMillis().minusHours(1))

        doGet(URL, AntallInnsendteSoknaderDto::class.java).also {
            assertThat(it.antall).isEqualTo(1)
        }
    }

    @Test
    fun `antallSisteDogn returnerer innsendingTillattFra naar antall er lik maksgrense`() {
        val eldsteTidspunkt = nowWithMillis().minusHours(20)
        sendtSoknadForBruker(sendtInn = eldsteTidspunkt)
        repeat(maksAntallSoknader - 1) { sendtSoknadForBruker(sendtInn = nowWithMillis().minusHours(1)) }

        doGet(URL, AntallInnsendteSoknaderDto::class.java).also {
            assertThat(it.antall).isEqualTo(maksAntallSoknader)
            assertThat(it.innsendingTillattFra).isEqualTo(
                eldsteTidspunkt.plusDays(1).plusMinutes(1).truncatedTo(ChronoUnit.MINUTES),
            )
        }
    }

    @Test
    fun `antallSisteDogn returnerer null for innsendingTillattFra naar antall er under maksgrense`() {
        repeat(maksAntallSoknader - 1) { sendtSoknadForBruker(sendtInn = nowWithMillis().minusHours(1)) }

        doGet(URL, AntallInnsendteSoknaderDto::class.java).also {
            assertThat(it.antall).isEqualTo(maksAntallSoknader - 1)
            assertThat(it.innsendingTillattFra).isNull()
        }
    }

    @Test
    fun `antallSisteDogn med flere enn maksgrense bruker maksgrense-te nyeste tidspunkt`() {
        val tidspunkter =
            (0 until maksAntallSoknader + 2).map {
                nowWithMillis().minusHours((10 + it).toLong())
            }

        tidspunkter.forEach { tidspunkt -> sendtSoknadForBruker(sendtInn = tidspunkt) }

        val maksgrenseTeNyeste = tidspunkter[maksAntallSoknader - 1]

        doGet(URL, AntallInnsendteSoknaderDto::class.java).also {
            assertThat(it.antall).isEqualTo(tidspunkter.size)
            assertThat(it.innsendingTillattFra).isEqualTo(
                maksgrenseTeNyeste.plusDays(1).plusMinutes(1).truncatedTo(ChronoUnit.MINUTES),
            )
        }
    }

    @Test
    fun `antallSisteDogn inkluderer ikke soknader for annen bruker`() {
        metadataRepository.save(
            opprettSoknadMetadata(
                status = SoknadStatus.SENDT,
                personId = "annenbruker",
                innsendtDato = nowWithMillis().minusHours(1),
            ),
        )
        sendtSoknadForBruker(sendtInn = nowWithMillis().minusHours(1))

        doGet(URL, AntallInnsendteSoknaderDto::class.java).also {
            assertThat(it.antall).isEqualTo(1)
        }
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

    companion object {
        private const val URL = "/minesaker/antallSisteDogn"
    }
}
