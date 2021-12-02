package no.nav.sosialhjelp.soknad.api.dialog

import io.mockk.every
import io.mockk.mockk
import no.nav.sosialhjelp.soknad.business.db.repositories.soknadmetadata.SoknadMetadataRepository
import no.nav.sosialhjelp.soknad.business.domain.SoknadMetadata
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
        val soknadMetadata = SoknadMetadata()
        soknadMetadata.fnr = fnr
        soknadMetadata.fiksForsendelseId = fiksForsendelseId
        soknadMetadata.navEnhet = navEnhet
        soknadMetadata.innsendtDato = innsendtDato
        every { soknadMetadataRepository.hentAlleInnsendteSoknaderForBruker(any()) } returns listOf(soknadMetadata)

        val dto = sistInnsendteSoknadService.hentSistInnsendteSoknad(fnr)
        assertThat(dto?.ident).isEqualTo(fnr)
        assertThat(dto?.navEnhet).isEqualTo(navEnhet)
        assertThat(dto?.innsendtDato).isEqualTo(innsendtDato.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME))
    }

    @Test
    fun skalHenteSistInnsendteSoknadForBruker_siste() {
        val innsendtDatoNyest = LocalDateTime.now().minusDays(1)
        val innsendtDatoElst = LocalDateTime.now().minusDays(2)

        val soknadMetadata1 = SoknadMetadata()
        soknadMetadata1.fnr = fnr
        soknadMetadata1.fiksForsendelseId = fiksForsendelseId
        soknadMetadata1.navEnhet = navEnhet
        soknadMetadata1.innsendtDato = innsendtDatoNyest

        val soknadMetadata2 = SoknadMetadata()
        soknadMetadata2.fnr = fnr
        soknadMetadata2.fiksForsendelseId = "fiksId2"
        soknadMetadata2.navEnhet = navEnhet
        soknadMetadata2.innsendtDato = innsendtDatoElst
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
    fun skalReturnereNullVedNull() {
        every { soknadMetadataRepository.hentAlleInnsendteSoknaderForBruker(any()) } returns null

        val dto = sistInnsendteSoknadService.hentSistInnsendteSoknad(fnr)
        assertThat(dto).isNull()
    }

    @Test
    fun skalReturnereNullVedTomListe() {
        every { soknadMetadataRepository.hentAlleInnsendteSoknaderForBruker(any()) } returns emptyList()

        val dto = sistInnsendteSoknadService.hentSistInnsendteSoknad(fnr)
        assertThat(dto).isNull()
    }
}
