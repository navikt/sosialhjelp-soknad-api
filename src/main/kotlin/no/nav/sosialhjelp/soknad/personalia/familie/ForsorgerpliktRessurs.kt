package no.nav.sosialhjelp.soknad.personalia.familie

import no.nav.sosialhjelp.soknad.app.annotation.ProtectionSelvbetjeningHigh
import no.nav.sosialhjelp.soknad.db.repositories.soknadunderarbeid.SoknadUnderArbeidRepository
import no.nav.sosialhjelp.soknad.personalia.familie.dto.ForsorgerpliktFrontend
import no.nav.sosialhjelp.soknad.tekster.TextService
import no.nav.sosialhjelp.soknad.tilgangskontroll.Tilgangskontroll
import org.springframework.http.MediaType
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@ProtectionSelvbetjeningHigh
@RequestMapping("/soknader/{behandlingsId}/familie/forsorgerplikt", produces = [MediaType.APPLICATION_JSON_VALUE])
class ForsorgerpliktRessurs(
    private val tilgangskontroll: Tilgangskontroll,
    private val textService: TextService,
    private val soknadUnderArbeidRepository: SoknadUnderArbeidRepository,
    private val forsorgerpliktProxy: ForsorgerpliktProxy,
) {
    @GetMapping
    fun hentForsorgerplikt(
        @PathVariable("behandlingsId") behandlingsId: String,
    ): ForsorgerpliktFrontend {
        tilgangskontroll.verifiserAtBrukerHarTilgang()

        return forsorgerpliktProxy.getForsorgerplikt(behandlingsId)
    }

    @PutMapping
    fun updateForsorgerplikt(
        @PathVariable("behandlingsId") behandlingsId: String,
        @RequestBody forsorgerpliktFrontend: ForsorgerpliktFrontend,
    ) {
        tilgangskontroll.verifiserAtBrukerKanEndreSoknad(behandlingsId)

        forsorgerpliktProxy.updateForsorgerplikt(behandlingsId, forsorgerpliktFrontend)
    }
}
