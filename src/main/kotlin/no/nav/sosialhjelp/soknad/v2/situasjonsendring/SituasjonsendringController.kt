package no.nav.sosialhjelp.soknad.v2.situasjonsendring

import no.nav.sosialhjelp.soknad.app.annotation.ProtectionSelvbetjeningHigh
import org.springframework.http.MediaType
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import java.util.UUID

@RestController
@ProtectionSelvbetjeningHigh
@RequestMapping("/soknad/{soknadId}/situasjonsendring", produces = [MediaType.APPLICATION_JSON_VALUE])
class SituasjonsendringController(
    private val situasjonsendringService: SituasjonsendringService,
) {
    @GetMapping
    fun getSituasjonsendring(
        @PathVariable soknadId: UUID,
    ): SituasjonsendringDto = situasjonsendringService.getSituasjonsendring(soknadId)?.toSituasjonsendringDto() ?: SituasjonsendringDto()

    @PutMapping
    fun updateSituasjonsendring(
        @PathVariable soknadId: UUID,
        @RequestBody situasjonsendringDto: SituasjonsendringDto,
    ): SituasjonsendringDto = situasjonsendringService.updateSituasjonsendring(soknadId, situasjonsendringDto.hvaErEndret, situasjonsendringDto.endring).toSituasjonsendringDto()
}

data class SituasjonsendringDto(
    val hvaErEndret: String? = null,
    val endring: Boolean? = null,
)

private fun Situasjonsendring.toSituasjonsendringDto() =
    SituasjonsendringDto(
        hvaErEndret = hvaErEndret,
        endring = endring,
    )
