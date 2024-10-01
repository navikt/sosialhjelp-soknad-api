package no.nav.sosialhjelp.soknad.innsending

import io.mockk.every
import io.mockk.mockk
import no.nav.sosialhjelp.soknad.db.repositories.soknadmetadata.SoknadMetadataRepository
import no.nav.sosialhjelp.soknad.innsending.SoknadServiceOld.Companion.createEmptyJsonInternalSoknad
import no.nav.sosialhjelp.soknad.innsending.digisosapi.DigisosApiService
import no.nav.sosialhjelp.soknad.innsending.digisosapi.humanifyHvaSokesOm
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.time.Clock
import java.time.Instant
import java.time.ZoneId

class KortSoknadServiceTest {
    private lateinit var digisosApiService: DigisosApiService
    private lateinit var soknadMetadataRepository: SoknadMetadataRepository
    private lateinit var clock: Clock
    private lateinit var kortSoknadService: KortSoknadService

    @BeforeEach
    fun setUp() {
        digisosApiService = mockk()
        soknadMetadataRepository = mockk()
        clock = Clock.fixed(Instant.parse("2023-01-01T00:00:00Z"), ZoneId.of("UTC"))
        kortSoknadService = KortSoknadService(digisosApiService)

        every { soknadMetadataRepository.hentInnsendteSoknaderForBrukerEtterTidspunkt(any(), any()) } returns emptyList()
        every { digisosApiService.qualifiesForKortSoknadThroughSoknader(any(), any(), any()) } returns false
        every { digisosApiService.qualifiesForKortSoknadThroughUtbetalinger(any(), any(), any()) } returns false
    }

    @Test
    fun `should qualify if there is a recent soknad from fiks`() {
        every { digisosApiService.qualifiesForKortSoknadThroughSoknader(any(), any(), any()) } returns true

        val result = kortSoknadService.qualifies("12345678901", "token")

        assertTrue(result)
    }

    @Test
    fun `should qualify if there are recent or upcoming utbetalinger`() {
        every { digisosApiService.qualifiesForKortSoknadThroughUtbetalinger(any(), any(), any()) } returns true

        val result = kortSoknadService.qualifies("12345678901", "token")

        assertTrue(result)
    }

    @Test
    fun `should not qualify if there are no recent soknader or utbetalinger`() {
        val result = kortSoknadService.qualifies("12345678901", "token")

        assertFalse(result)
    }

    @Test
    fun `Humanify skal returnere tom streng hvis ingen kategorier`() {
        val jsonInternalSoknad =
            createEmptyJsonInternalSoknad("12345678901", true)
                .apply { soknad.data.begrunnelse.hvaSokesOm = "[]" }

        jsonInternalSoknad.humanifyHvaSokesOm()

        assertThat(jsonInternalSoknad.soknad.data.begrunnelse.hvaSokesOm).isEqualTo("")
    }
}
