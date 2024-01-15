package no.nav.sosialhjelp.soknad.api.nedetid

import no.nav.security.token.support.core.api.Unprotected
import no.nav.sosialhjelp.soknad.api.nedetid.dto.NedetidFrontend
import org.springframework.http.MediaType
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@Unprotected
@RequestMapping("/nedetid", produces = [MediaType.APPLICATION_JSON_VALUE])
class NedetidRessurs(
    private val nedetidService: NedetidService,
) {

    @GetMapping
    fun hentNedetidInformasjon(): NedetidFrontend {
        return NedetidFrontend(
            isNedetid = nedetidService.isInnenforNedetid,
            isPlanlagtNedetid = nedetidService.isInnenforPlanlagtNedetid,
            nedetidStart = nedetidService.nedetidStartAsString,
            nedetidSlutt = nedetidService.nedetidSluttAsString,
            nedetidStartText = nedetidService.nedetidStartAsHumanReadable,
            nedetidSluttText = nedetidService.nedetidSluttAsHumanReadable,
            nedetidStartTextEn = nedetidService.nedetidStartAsHumanReadableEn,
            nedetidSluttTextEn = nedetidService.nedetidSluttAsHumanReadableEn,
        )
    }
}
