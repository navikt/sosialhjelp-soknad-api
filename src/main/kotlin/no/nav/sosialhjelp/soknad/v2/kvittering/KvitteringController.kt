package no.nav.sosialhjelp.soknad.v2.kvittering

import no.nav.sosialhjelp.soknad.app.annotation.ProtectionSelvbetjeningHigh
import no.nav.sosialhjelp.soknad.app.exceptions.IkkeFunnetException
import org.springframework.http.CacheControl
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import java.util.UUID

@RestController
@ProtectionSelvbetjeningHigh
@RequestMapping("/kvittering", produces = [MediaType.APPLICATION_JSON_VALUE])
class KvitteringController(
    private val kvitteringService: KvitteringService,
) {
    @GetMapping("/{digisosId}")
    fun hentKvittering(
        @PathVariable digisosId: String,
    ): ResponseEntity<KvitteringDto> {
        val id = runCatching { UUID.fromString(digisosId) }.getOrElse { throw IkkeFunnetException("Ugyldig digisosId") }

        return ResponseEntity
            .ok()
            .cacheControl(CacheControl.noStore())
            .body(kvitteringService.hentKvittering(id))
    }
}
