package no.nav.sosialhjelp.soknad.api.dialog

import io.mockk.every
import io.mockk.mockk
import no.nav.sosialhjelp.soknad.db.repositories.soknadmetadata.SoknadMetadata
import no.nav.sosialhjelp.soknad.db.repositories.soknadmetadata.SoknadMetadataRepository
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

internal class SistInnsendteSoknadServiceTest {
    private val soknadMetadataRepository: SoknadMetadataRepository = mockk()
    private val sistInnsendteSoknadService = SistInnsendteSoknadService(soknadMetadataRepository)

    private val fnr = "12345"
    private val fiksForsendelseId = "fiksId"
    private val navEnhet = "1234"

    @Test
    fun skalHenteSistInnsendteSoknadForBruker() {
        val innsendtDato = LocalDateTime.now().minusDays(1)
        val soknadMetadata = SoknadMetadata(
            id = 0L,
            behandlingsId = "behandlingsId",
            fnr = fnr,
            fiksForsendelseId = fiksForsendelseId,
            navEnhet = navEnhet,
            opprettetDato = innsendtDato,
            sistEndretDato = innsendtDato,
            innsendtDato = innsendtDato
        )
        every { soknadMetadataRepository.hentAlleInnsendteSoknaderForBruker(any()) } returns listOf(soknadMetadata)

        val dto = sistInnsendteSoknadService.hentSistInnsendteSoknad(fnr)
        assertThat(dto?.ident).isEqualTo(fnr)
        assertThat(dto?.navEnhet).isEqualTo(navEnhet)
        assertThat(dto?.innsendtDato).isEqualTo(innsendtDato.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME))
    }

    @Test
    fun skalHenteSistInnsendteSoknadForBruker_siste() {
        val innsendtDatoNyest = LocalDateTime.now().minusDays(1)
        val innsendtDatoEldst = LocalDateTime.now().minusDays(2)

        val soknadMetadata1 = SoknadMetadata(
            id = 0L,
            behandlingsId = "behandlingsId",
            fnr = fnr,
            fiksForsendelseId = fiksForsendelseId,
            navEnhet = navEnhet,
            opprettetDato = innsendtDatoNyest,
            sistEndretDato = innsendtDatoNyest,
            innsendtDato = innsendtDatoNyest
        )

        val soknadMetadata2 = SoknadMetadata(
            id = 1L,
            behandlingsId = "behandlingsId2",
            fnr = fnr,
            fiksForsendelseId = "fiksId2",
            navEnhet = navEnhet,
            opprettetDato = innsendtDatoEldst,
            sistEndretDato = innsendtDatoEldst,
            innsendtDato = innsendtDatoEldst
        )
        every { soknadMetadataRepository.hentAlleInnsendteSoknaderForBruker(any()) } returns listOf(
            soknadMetadata1,
            soknadMetadata2
        )

        val dto = sistInnsendteSoknadService.hentSistInnsendteSoknad(fnr)
        assertThat(dto?.ident).isEqualTo(fnr)
        assertThat(dto?.navEnhet).isEqualTo(navEnhet)
        assertThat(dto?.innsendtDato).isEqualTo(innsendtDatoNyest.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME))
    }

    @Test
    fun skalReturnereNullVedTomListe() {
        every { soknadMetadataRepository.hentAlleInnsendteSoknaderForBruker(any()) } returns emptyList()

        val dto = sistInnsendteSoknadService.hentSistInnsendteSoknad(fnr)
        assertThat(dto).isNull()
    }
}
