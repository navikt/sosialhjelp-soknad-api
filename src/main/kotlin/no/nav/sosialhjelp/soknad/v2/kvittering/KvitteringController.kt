package no.nav.sosialhjelp.soknad.v2.kvittering

import no.nav.sosialhjelp.soknad.app.annotation.ProtectionSelvbetjeningHigh
import org.springframework.http.MediaType
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
        @PathVariable digisosId: UUID,
    ): KvitteringDto = kvitteringService.hentKvittering(digisosId)
}
