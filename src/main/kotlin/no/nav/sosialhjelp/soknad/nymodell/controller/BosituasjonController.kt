package no.nav.sosialhjelp.soknad.nymodell.controller

import no.nav.security.token.support.core.api.Unprotected
import no.nav.sosialhjelp.soknad.app.exceptions.IkkeFunnetException
import no.nav.sosialhjelp.soknad.nymodell.controller.dto.BosituasjonDto
import no.nav.sosialhjelp.soknad.nymodell.domene.livssituasjon.Bosituasjon
import no.nav.sosialhjelp.soknad.nymodell.service.LivssituasjonService
//import no.nav.sosialhjelp.soknad.nymodell.service.LivssituasjonService
import org.springframework.http.MediaType
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import java.util.*

@RestController
@Unprotected
@RequestMapping("/soknad/{soknadId}/bosituasjon", produces = [MediaType.APPLICATION_JSON_VALUE])
class BosituasjonController(
    private val service: LivssituasjonService
) {
    @GetMapping
    fun hentBosituasjon(
        @PathVariable("soknadId") soknadId: UUID,
    ): BosituasjonDto {
        return service.hentBosituasjon(soknadId)?.toDto()
            ?: throw IkkeFunnetException("Bosituasjon finnes ikke.")
    }

    @PutMapping
    fun oppdaterBosituasjon(
        @PathVariable("soknadId") soknadId: UUID,
        @RequestBody bosituasjonDto: BosituasjonDto
    ): BosituasjonDto {
        return Bosituasjon(
            soknadId = soknadId,
            botype = bosituasjonDto.botype,
            antallPersoner = bosituasjonDto.antallPersoner
        )
            .let { service.oppdaterBosituasjon(it) }
            .toDto()
    }

    fun Bosituasjon.toDto() = BosituasjonDto(
        botype = botype,
        antallPersoner = antallPersoner
    )
}
