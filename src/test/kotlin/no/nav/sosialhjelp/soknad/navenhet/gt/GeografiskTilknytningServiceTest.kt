package no.nav.sosialhjelp.soknad.navenhet.gt

import io.mockk.every
import io.mockk.mockk
import no.nav.sosialhjelp.soknad.navenhet.gt.dto.GeografiskTilknytningDto
import no.nav.sosialhjelp.soknad.navenhet.gt.dto.GtType
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import reactor.core.publisher.Mono

internal class GeografiskTilknytningServiceTest {

    private val geografiskTilknytningClient: GeografiskTilknytningClient = mockk()
    private val geografiskTilknytningService = GeografiskTilknytningService(geografiskTilknytningClient)

    private val ident = "ident"
    private val gt = "gt"

    @Test
    fun skalReturnereBydelsnummer() {
        every { geografiskTilknytningClient.hentGeografiskTilknytning(ident) } returns Mono.just(GeografiskTilknytningDto(GtType.BYDEL, null, gt, null))
        val geografiskTilknytning = geografiskTilknytningService.hentGeografiskTilknytning(ident)
        assertThat(geografiskTilknytning).isEqualTo(gt)
    }

    @Test
    fun skalReturnereKommunenummer() {
        every { geografiskTilknytningClient.hentGeografiskTilknytning(ident) } returns Mono.just(GeografiskTilknytningDto(GtType.KOMMUNE, gt, null, null))
        val geografiskTilknytning = geografiskTilknytningService.hentGeografiskTilknytning(ident)
        assertThat(geografiskTilknytning).isEqualTo(gt)
    }

    @Test
    fun skalReturnereNullHvisUtland() {
        every { geografiskTilknytningClient.hentGeografiskTilknytning(ident) } returns Mono.just(GeografiskTilknytningDto(GtType.UTLAND, null, null, gt))
        val geografiskTilknytning = geografiskTilknytningService.hentGeografiskTilknytning(ident)
        assertThat(geografiskTilknytning).isNull()
    }

    @Test
    fun skalReturnereNullHvisUdefinert() {
        every { geografiskTilknytningClient.hentGeografiskTilknytning(ident) } returns Mono.just(GeografiskTilknytningDto(GtType.UDEFINERT, null, null, null))
        val geografiskTilknytning = geografiskTilknytningService.hentGeografiskTilknytning(ident)
        assertThat(geografiskTilknytning).isNull()
    }

    @Test
    fun skalReturnereNullHvisConsumerGirNull() {
        every { geografiskTilknytningClient.hentGeografiskTilknytning(ident) } returns Mono.empty()
        val geografiskTilknytning = geografiskTilknytningService.hentGeografiskTilknytning(ident)
        assertThat(geografiskTilknytning).isNull()
    }
}
