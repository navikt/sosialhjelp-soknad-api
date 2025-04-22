package no.nav.sosialhjelp.soknad.navenhet.gt

import io.mockk.every
import io.mockk.mockk
import no.nav.sosialhjelp.soknad.navenhet.gt.dto.GeografiskTilknytningDto
import no.nav.sosialhjelp.soknad.navenhet.gt.dto.GtType
import no.nav.sosialhjelp.soknad.v2.soknad.PersonIdService
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.util.UUID

internal class GeografiskTilknytningServiceTest {
    private val geografiskTilknytningClient: GeografiskTilknytningClient = mockk()
    private val personIdService: PersonIdService = mockk()
    private val geografiskTilknytningService = GeografiskTilknytningService(geografiskTilknytningClient, personIdService)

    private val soknadId = UUID.randomUUID()
    private val personId = "12345612345"
    private val gt = "gt"

    @BeforeEach
    fun setup() {
        every { personIdService.findPersonId(soknadId) } returns personId
    }

    @Test
    fun skalReturnereBydelsnummer() {
        every {
            geografiskTilknytningClient.hentGeografiskTilknytning(
                personId,
            )
        } returns GeografiskTilknytningDto(GtType.BYDEL, null, gt, null)
        val geografiskTilknytning = geografiskTilknytningService.hentGeografiskTilknytning(soknadId)
        assertThat(geografiskTilknytning).isEqualTo(gt)
    }

    @Test
    fun skalReturnereKommunenummer() {
        every {
            geografiskTilknytningClient.hentGeografiskTilknytning(
                personId,
            )
        } returns GeografiskTilknytningDto(GtType.KOMMUNE, gt, null, null)
        val geografiskTilknytning = geografiskTilknytningService.hentGeografiskTilknytning(soknadId)
        assertThat(geografiskTilknytning).isEqualTo(gt)
    }

    @Test
    fun skalReturnereNullHvisUtland() {
        every {
            geografiskTilknytningClient.hentGeografiskTilknytning(
                personId,
            )
        } returns GeografiskTilknytningDto(GtType.UTLAND, null, null, gt)
        val geografiskTilknytning = geografiskTilknytningService.hentGeografiskTilknytning(soknadId)
        assertThat(geografiskTilknytning).isNull()
    }

    @Test
    fun skalReturnereNullHvisUdefinert() {
        every {
            geografiskTilknytningClient.hentGeografiskTilknytning(
                personId,
            )
        } returns GeografiskTilknytningDto(GtType.UDEFINERT, null, null, null)
        val geografiskTilknytning = geografiskTilknytningService.hentGeografiskTilknytning(soknadId)
        assertThat(geografiskTilknytning).isNull()
    }

    @Test
    fun skalReturnereNullHvisConsumerGirNull() {
        every { geografiskTilknytningClient.hentGeografiskTilknytning(any()) } returns null
        val geografiskTilknytning = geografiskTilknytningService.hentGeografiskTilknytning(soknadId)
        assertThat(geografiskTilknytning).isNull()
    }
}
