package no.nav.sosialhjelp.soknad.v2.repository

import no.nav.sosialhjelp.soknad.v2.metadata.SoknadMetadata
import no.nav.sosialhjelp.soknad.v2.metadata.SoknadStatus
import no.nav.sosialhjelp.soknad.v2.metadata.Tidspunkt
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertDoesNotThrow
import java.time.LocalDateTime
import java.time.temporal.ChronoUnit
import java.util.UUID

class SoknadMetadataValidationTest {
    private val soknadMetadata = SoknadMetadata(soknadId = UUID.randomUUID(), personId = "12345678901")

    @Test
    fun `Metadata med Innsendt og NavMottaker skal validere`() {
        assertDoesNotThrow {
            soknadMetadata.copy(
                status = SoknadStatus.SENDT,
                tidspunkt = Tidspunkt(sendtInn = LocalDateTime.now().truncatedTo(ChronoUnit.MILLIS)),
                mottakerKommunenummer = "1234",
                digisosId = UUID.randomUUID(),
            )
        }

        assertDoesNotThrow {
            soknadMetadata.copy(
                status = SoknadStatus.MOTTATT_FSL,
                tidspunkt = Tidspunkt(sendtInn = LocalDateTime.now().truncatedTo(ChronoUnit.MILLIS)),
                mottakerKommunenummer = "1234",
                digisosId = UUID.randomUUID(),
            )
        }
    }

    @Test
    fun `Status FERDIGSTILT uten innsendt-dato skal feile`() {
        assertThatThrownBy { soknadMetadata.copy(status = SoknadStatus.SENDT) }
            .isInstanceOf(IllegalStateException::class.java)
    }

    @Test
    fun `Status FERDIGSTILT uten mottaker skal feile`() {
        assertThatThrownBy {
            soknadMetadata.copy(
                status = SoknadStatus.SENDT,
                tidspunkt = Tidspunkt(sendtInn = LocalDateTime.now().truncatedTo(ChronoUnit.MILLIS)),
            )
        }.isInstanceOf(IllegalStateException::class.java)
    }

    @Test
    fun `Status FERDIGSTILT uten DigisosId skal feile`() {
        assertThatThrownBy {
            soknadMetadata.copy(
                status = SoknadStatus.SENDT,
                tidspunkt = Tidspunkt(sendtInn = LocalDateTime.now().truncatedTo(ChronoUnit.MILLIS)),
                mottakerKommunenummer = "1234",
            )
        }.isInstanceOf(IllegalStateException::class.java)
    }

    @Test
    fun `Status MOTTATT_FSL uten innsendt-dato skal feile`() {
        assertThatThrownBy { soknadMetadata.copy(status = SoknadStatus.MOTTATT_FSL) }
            .isInstanceOf(IllegalStateException::class.java)
    }

    @Test
    fun `Status MOTTATT_FSL uten mottaker skal feile`() {
        assertThatThrownBy {
            soknadMetadata.copy(
                status = SoknadStatus.MOTTATT_FSL,
                tidspunkt = Tidspunkt(sendtInn = LocalDateTime.now().truncatedTo(ChronoUnit.MILLIS)),
            )
        }.isInstanceOf(IllegalStateException::class.java)
    }

    @Test
    fun `Status MOTTATT_FSL uten DigisosId skal feile`() {
        assertThatThrownBy {
            soknadMetadata.copy(
                status = SoknadStatus.MOTTATT_FSL,
                tidspunkt = Tidspunkt(sendtInn = LocalDateTime.now().truncatedTo(ChronoUnit.MILLIS)),
                mottakerKommunenummer = "1234",
            )
        }.isInstanceOf(IllegalStateException::class.java)
    }

    @Test
    fun `Mottaker med feil lengde pa kommunenummer skal feile`() {
        assertThatThrownBy {
            soknadMetadata.copy(
                status = SoknadStatus.SENDT,
                tidspunkt = Tidspunkt(sendtInn = LocalDateTime.now().truncatedTo(ChronoUnit.MILLIS)),
                mottakerKommunenummer = "12345",
            )
        }
            .isInstanceOf(IllegalStateException::class.java)
    }
}
