package no.nav.sosialhjelp.soknad.v2.integrationtest

import no.nav.sosialhjelp.soknad.api.minesaker.AntallInnsendteSoknaderDto
import no.nav.sosialhjelp.soknad.v2.json.generate.TimestampUtil.nowWithMillis
import no.nav.sosialhjelp.soknad.v2.metadata.SoknadStatus
import no.nav.sosialhjelp.soknad.v2.opprettSoknadMetadata
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.time.LocalDateTime

class AntallInnsendteSoknaderIntegrationTest : AbstractIntegrationTest(useTokenX = true) {
    override var opprettSoknadBeforeEach = false

    @BeforeEach
    fun setup() {
        metadataRepository.deleteAll()
    }

    @Test
    fun `antallSisteDogn returnerer 0 naar bruker har ingen innsendte soknader`() {
        doGet(URL, AntallInnsendteSoknaderDto::class.java).also {
            assertThat(it.antall).isEqualTo(0)
            assertThat(it.innsendingTillatt).isNull()
        }
    }

    @Test
    fun `antallSisteDogn returnerer antall innsendte soknader siste 24 timer`() {
        repeat(3) { sendtSoknadForBruker(sendtInn = nowWithMillis().minusHours(12)) }

        doGet(URL, AntallInnsendteSoknaderDto::class.java).also {
            assertThat(it.antall).isEqualTo(3)
            assertThat(it.innsendingTillatt).isNull()
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
    fun `antallSisteDogn returnerer eldsteSendtInn kun naar antall er 10`() {
        val eldsteTidspunkt = nowWithMillis().minusHours(20)
        sendtSoknadForBruker(sendtInn = eldsteTidspunkt)
        repeat(9) { sendtSoknadForBruker(sendtInn = nowWithMillis().minusHours(1)) }

        doGet(URL, AntallInnsendteSoknaderDto::class.java).also {
            assertThat(it.antall).isEqualTo(10)
            assertThat(it.innsendingTillatt).isEqualTo(eldsteTidspunkt.plusDays(1))
        }
    }

    @Test
    fun `antallSisteDogn returnerer null for eldsteSendtInn naar antall er under 10`() {
        repeat(9) { sendtSoknadForBruker(sendtInn = nowWithMillis().minusHours(1)) }

        doGet(URL, AntallInnsendteSoknaderDto::class.java).also {
            assertThat(it.antall).isEqualTo(9)
            assertThat(it.innsendingTillatt).isNull()
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

    @Test
    fun `antallSisteDogn with more than 10 soknader returnerer innsendingTillatt for 10th timestamp`() {
        val tidspunkter = listOf(
            nowWithMillis().minusHours(10),  // 11th (newest)
            nowWithMillis().minusHours(11),  // 10th oldest - this should be used for innsendingTillatt
            nowWithMillis().minusHours(12),  // 9th
            nowWithMillis().minusHours(13),  // 8th
            nowWithMillis().minusHours(14),  // 7th
            nowWithMillis().minusHours(15),  // 6th
            nowWithMillis().minusHours(16),  // 5th
            nowWithMillis().minusHours(17),  // 4th
            nowWithMillis().minusHours(18),  // 3rd
            nowWithMillis().minusHours(19),  // 2nd
            nowWithMillis().minusHours(20),  // 1st oldest
        )

        tidspunkter.forEach { tidspunkt ->
            sendtSoknadForBruker(sendtInn = tidspunkt)
        }

        val tiende = tidspunkter[1]

        doGet(URL, AntallInnsendteSoknaderDto::class.java).also {
            assertThat(it.antall).isEqualTo(11)
            assertThat(it.innsendingTillatt).isEqualTo(tiende.plusDays(1))
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
        private const val URL = "/minesaker/innsendte/antallSisteDogn"
    }
}
