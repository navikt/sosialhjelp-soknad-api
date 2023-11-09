package no.nav.sosialhjelp.soknad.nymodell.controller.okonomi

import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/soknad/{soknadId}/okonomi")
class OkonomiController {

    @PutMapping
    fun updateInntekt(
        @PathVariable("soknadId") soknadId: String,
//        @RequestBody okonomiRequest: OkonomiRequest
    ) {

    }

    @PutMapping
    fun updateUtgift(
        @PathVariable("soknadId") soknadId: String,
//        @RequestBody okonomiRequest: OkonomiRequest
    ) {

    }

    @PutMapping
    fun updateFormue(
        @PathVariable("soknadId") soknadId: String,
//        @RequestBody okonomiRequest: OkonomiRequest
    ) {

    }
}