package no.nav.sosialhjelp.soknad.controller

import no.nav.sosialhjelp.soknad.app.exceptions.IkkeFunnetException
import no.nav.sosialhjelp.soknad.domene.BosituasjonDto
import no.nav.sosialhjelp.soknad.service.BosituasjonService
import org.springframework.http.MediaType
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import java.util.*

@RestController
@RequestMapping("/soknad/{soknadId}/bosituasjon", produces = [MediaType.APPLICATION_JSON_VALUE])
class BosituasjonController(
    private val bosituasjonService: BosituasjonService
) {
    @GetMapping
    fun hentBosituasjon(
        @PathVariable("soknadId") soknadIdString: String,
    ): BosituasjonDto {
        val soknadId = UUID.fromString(soknadIdString)
        return bosituasjonService.hentBosituasjon(soknadId) ?: throw IkkeFunnetException("Bosituasjon finnes ikke.")
    }

    @PutMapping
    fun oppdaterBosituasjon(
        @PathVariable("soknadId") soknadIdString: String?,
        @RequestBody bosituasjonDto: BosituasjonDto
    ) {
        // 1. verifiser at bruker har tilgang til soknad (bør kanskje skje i filter/interceptor)
        val soknadId = soknadIdString?.let { UUID.fromString(soknadIdString) }
            ?: throw IllegalArgumentException("SøknadId kan ikke være null.")

        // 3. send videre til service
        bosituasjonService.oppdaterBosituasjon(soknadId, bosituasjonDto)
    }
}