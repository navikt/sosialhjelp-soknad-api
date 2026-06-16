package no.nav.sosialhjelp.soknad.v2.integrationtest

import no.nav.sosialhjelp.soknad.api.minesaker.AntallInnsendteSoknaderDto
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
        repeat(2) { sendtSoknadForBruker(sendtInn = nowWithMillis().minusHours(12)) }

        doGet(URL, AntallInnsendteSoknaderDto::class.java).also {
            assertThat(it.antall).isEqualTo(2)
            assertThat(it.innsendingTillattFra).isNull()
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
    fun `antallSisteDogn returnerer innsendingTillattFra naar antall er 3`() {
        val eldsteTidspunkt = nowWithMillis().minusHours(20)
        sendtSoknadForBruker(sendtInn = eldsteTidspunkt)
        repeat(2) { sendtSoknadForBruker(sendtInn = nowWithMillis().minusHours(1)) }

        doGet(URL, AntallInnsendteSoknaderDto::class.java).also {
            assertThat(it.antall).isEqualTo(3)
            assertThat(it.innsendingTillattFra).isEqualTo(
                eldsteTidspunkt.plusDays(1).plusMinutes(1).truncatedTo(ChronoUnit.MINUTES),
            )
        }
    }

    @Test
    fun `antallSisteDogn returnerer null for innsendingTillattFra naar antall er under 3`() {
        repeat(2) { sendtSoknadForBruker(sendtInn = nowWithMillis().minusHours(1)) }

        doGet(URL, AntallInnsendteSoknaderDto::class.java).also {
            assertThat(it.antall).isEqualTo(2)
            assertThat(it.innsendingTillattFra).isNull()
        }
    }

    @Test
    fun `antallSisteDogn with more than 3 soknader returnerer innsendingTillatt for 3rd newest timestamp`() {
        val tidspunkter =
            listOf(
                nowWithMillis().minusHours(10), // newest
                nowWithMillis().minusHours(11), // 2nd newest
                nowWithMillis().minusHours(12), // 3rd newest — this should be used for innsendingTillatt
                nowWithMillis().minusHours(13), // oldest
            )

        tidspunkter.forEach { tidspunkt ->
            sendtSoknadForBruker(sendtInn = tidspunkt)
        }

        val tredje = tidspunkter[2]

        doGet(URL, AntallInnsendteSoknaderDto::class.java).also {
            assertThat(it.antall).isEqualTo(4)
            assertThat(it.innsendingTillattFra).isEqualTo(
                tredje.plusDays(1).plusMinutes(1).truncatedTo(ChronoUnit.MINUTES),
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
