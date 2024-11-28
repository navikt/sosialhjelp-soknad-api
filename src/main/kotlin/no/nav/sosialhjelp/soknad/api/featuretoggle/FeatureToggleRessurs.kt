package no.nav.sosialhjelp.soknad.api.featuretoggle

import io.getunleash.Unleash
import io.getunleash.UnleashContext
import no.nav.security.token.support.core.api.ProtectedWithClaims
import no.nav.sosialhjelp.soknad.app.Constants
import no.nav.sosialhjelp.soknad.app.subjecthandler.SubjectHandlerUtils
import no.nav.sosialhjelp.soknad.db.repositories.soknadunderarbeid.SoknadUnderArbeidRepository
import no.nav.sosialhjelp.soknad.v2.kontakt.service.AdresseService
import org.springframework.http.MediaType
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import java.util.UUID

@RestController
@ProtectedWithClaims(
    issuer = Constants.SELVBETJENING,
    claimMap = [Constants.CLAIM_ACR_LEVEL_4, Constants.CLAIM_ACR_LOA_HIGH],
    combineWithOr = true,
)
@RequestMapping("/feature-toggle", produces = [MediaType.APPLICATION_JSON_VALUE])
class FeatureToggleRessurs(
    private val unleash: Unleash,
    private val adresseService: AdresseService,
    // TODO: Fjern med ny datamodell
    private val soknadUnderArbeidRepository: SoknadUnderArbeidRepository,
) {
    @GetMapping
    fun featureToggles(): Map<String, Boolean> = unleash.more().evaluateAllToggles().associate { it.name to it.isEnabled }

    @GetMapping("/single")
    fun featureToggle(
        @RequestParam(required = false)
        soknadId: UUID?,
        @RequestParam(required = true)
        toggleName: String,
    ): Boolean {
        val kommunenummer =
            soknadId?.let {
                adresseService.findMottaker(it)?.kommunenummer
                    ?: soknadUnderArbeidRepository.hentSoknadNullable(it.toString(), SubjectHandlerUtils.getUserIdFromToken())?.jsonInternalSoknad?.soknad?.mottaker?.kommunenummer
            }
        val context = kommunenummer?.let { UnleashContext.builder().addProperty("kommunenummer", it).build() } ?: UnleashContext.builder().build()
        return unleash.isEnabled("sosialhjelp.soknad.kort_soknad", context, false)
    }
}
