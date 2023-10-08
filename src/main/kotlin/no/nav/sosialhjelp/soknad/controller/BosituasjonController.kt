package no.nav.sosialhjelp.soknad.controller

import no.nav.sbl.soknadsosialhjelp.soknad.bosituasjon.JsonBosituasjon.Botype
import no.nav.security.token.support.core.api.ProtectedWithClaims
import no.nav.sosialhjelp.soknad.app.Constants
import no.nav.sosialhjelp.soknad.bosituasjon.BosituasjonRessurs
import no.nav.sosialhjelp.soknad.model.Bosituasjon
//import no.nav.sosialhjelp.soknad.service.BosituasjonService
import org.springframework.http.MediaType
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import java.util.*

@RestController
@RequestMapping("/soknad/{soknadId}/bosituasjon", produces = [MediaType.APPLICATION_JSON_VALUE])
class BosituasjonController(
//    val bosituasjonService: BosituasjonService
) {


    @PutMapping
    fun oppdaterBosituasjon(
        @PathVariable("soknadId") soknadId: String?,
        @RequestBody bosituasjonFrontend: BosituasjonRessurs.BosituasjonFrontend
    ) {
        // 1. verifiser at bruker har tilgang til soknad (bør kanskje skje i filter/interceptor)

        // 2. send videre til service

        val bosituasjon = Bosituasjon(
            soknadId = UUID.fromString(soknadId),
            botype = bosituasjonFrontend.botype,
            antallPersoner = bosituasjonFrontend.antallPersoner
        )
//        bosituasjonService.oppdaterBosituasjon(bosituasjon)
    }



}