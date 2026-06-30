package no.nav.sosialhjelp.soknad.v2.validator

import no.nav.sosialhjelp.soknad.v2.BegrenseAntallMottakereValidator.Companion.BEGRENSET_PERIODE
import no.nav.sosialhjelp.soknad.v2.BegrenseAntallMottakereValidator.Companion.doValidate
import no.nav.sosialhjelp.soknad.v2.json.generate.TimestampUtil.nowWithMillis
import no.nav.sosialhjelp.soknad.v2.kontakt.service.ForMangeMottakereException
import no.nav.sosialhjelp.soknad.v2.metadata.SoknadMetadata
import no.nav.sosialhjelp.soknad.v2.metadata.SoknadStatus
import no.nav.sosialhjelp.soknad.v2.metadata.SoknadType
import no.nav.sosialhjelp.soknad.v2.metadata.Tidspunkt
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatCode
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.fail
import java.time.LocalDateTime
import java.time.temporal.ChronoUnit
import java.util.UUID

class BegrenseAntallMottakereValidatorTest {
    @Test
    fun `Hvis antall mottakere er mindre enn maks antall skal ikke ny mottaker feile`() {
        val eksisterendeMottakere = listOf("0301")

        val nyMottaker = "1234"

        val metadatas =
            listOf(
                createSoknadMetadata(eksisterendeMottakere[0], nowWithMillis().minusDays(0)),
                createSoknadMetadata(eksisterendeMottakere[0], nowWithMillis().minusDays(1)),
            )

        assertThatCode { doValidate(metadatas, nyMottaker) }.doesNotThrowAnyException()
    }

    @Test
    fun `Hvis antall mottakere = maks antall skal en allerede eksisterende mottaker fungere`() {
        val eksisterendeMottakere = listOf("0301", "1234")

        val nyMottaker = eksisterendeMottakere.first()

        val metadatas =
            listOf(
                createSoknadMetadata(eksisterendeMottakere[0], nowWithMillis().minusDays(0)),
                createSoknadMetadata(eksisterendeMottakere[0], nowWithMillis().minusDays(1)),
                createSoknadMetadata(eksisterendeMottakere[1], nowWithMillis().minusDays(2)),
                createSoknadMetadata(eksisterendeMottakere[1], nowWithMillis().minusDays(3)),
            )

        assertThatCode { doValidate(metadatas, nyMottaker) }.doesNotThrowAnyException()
    }

    @Test
    fun `Hvis antall mottakere = maks antall skal en ikke-eksisterende mottaker kaste exception`() {
        val eksisterendeMottakere = listOf("0301", "1234")

        val nyMottaker = "5331"

        // utgangstidspunkt for beregning av gyldig ifra
        val innsendingsTidspunkt = nowWithMillis().minusDays(2)

        val metadatas =
            listOf(
                createSoknadMetadata(eksisterendeMottakere[0], nowWithMillis().minusDays(0)),
                createSoknadMetadata(eksisterendeMottakere[0], nowWithMillis().minusDays(1)),
                createSoknadMetadata(eksisterendeMottakere[1], innsendingsTidspunkt),
                createSoknadMetadata(eksisterendeMottakere[1], nowWithMillis().minusDays(3)),
            )

        runCatching { doValidate(metadatas, nyMottaker) }
            .onFailure { e ->
                assertThat(e).isInstanceOf(ForMangeMottakereException::class.java)

                (e as ForMangeMottakereException).info.also {
                    assertThat(it.innsendingGyldigFra)
                        .isEqualTo(
                            innsendingsTidspunkt.plusDays(BEGRENSET_PERIODE).plusMinutes(1).truncatedTo(ChronoUnit.MINUTES),
                        )
                }
            }
            .onSuccess { fail("Skal feile") }
    }

    @Test
    fun `Hvis det er for mange mottakere allerede skal eksisterende kommune fortsatt feile`() {
        val eksisterendeMottakere = listOf("0301", "1234", "3233")

        val nyMottaker = eksisterendeMottakere.first()

        // utgangstidspunkt for beregning av gyldig ifra
        val innsendingsTidspunkt = nowWithMillis().minusDays(1)

        val metadatas =
            listOf(
                createSoknadMetadata(eksisterendeMottakere[0], nowWithMillis().minusDays(0)),
                createSoknadMetadata(eksisterendeMottakere[0], nowWithMillis().minusDays(1)),
                createSoknadMetadata(eksisterendeMottakere[1], innsendingsTidspunkt),
                createSoknadMetadata(eksisterendeMottakere[1], nowWithMillis().minusDays(3)),
                createSoknadMetadata(eksisterendeMottakere[2], nowWithMillis().minusDays(2)),
                createSoknadMetadata(eksisterendeMottakere[2], nowWithMillis().minusDays(5)),
            )

        runCatching { doValidate(metadatas, nyMottaker) }
            .onFailure { e ->
                assertThat(e).isInstanceOf(ForMangeMottakereException::class.java)

                (e as ForMangeMottakereException).info.also {
                    assertThat(it.innsendingGyldigFra)
                        .isEqualTo(innsendingsTidspunkt.plusDays(BEGRENSET_PERIODE).plusMinutes(1).truncatedTo(ChronoUnit.MINUTES))
                }
            }
            .onSuccess { fail("Skal feile") }
    }
}

private fun createSoknadMetadata(
    mottaker: String,
    sendtInn: LocalDateTime,
): SoknadMetadata =
    SoknadMetadata(
        soknadId = UUID.randomUUID(),
        personId = "12345678901",
        tidspunkt =
            Tidspunkt(
                opprettet = nowWithMillis().minusWeeks(1),
                sendtInn = sendtInn.truncatedTo(ChronoUnit.MILLIS),
            ),
        mottakerKommunenummer = mottaker,
        digisosId = UUID.randomUUID(),
        soknadType = SoknadType.STANDARD,
        status = SoknadStatus.SENDT,
    )
