package no.nav.sosialhjelp.soknad.inntekt.skattbarinntekt

import jakarta.validation.Valid
import no.nav.sosialhjelp.soknad.app.annotation.ProtectionSelvbetjeningHigh
import no.nav.sosialhjelp.soknad.inntekt.skattbarinntekt.dto.SkattbarInntektFrontend
import no.nav.sosialhjelp.soknad.inntekt.skattbarinntekt.dto.SkattbarInntektInputDTO
import no.nav.sosialhjelp.soknad.tilgangskontroll.Tilgangskontroll
import org.springframework.http.MediaType
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@ProtectionSelvbetjeningHigh
@RequestMapping("/soknader/{behandlingsId}/inntekt/skattbarinntektogforskuddstrekk", produces = [MediaType.APPLICATION_JSON_VALUE])
class SkattbarInntektRessurs(
    private val tilgangskontroll: Tilgangskontroll,
    private val skattbarInntektProxy: SkattbarInntektProxy,
) {
    @GetMapping
    fun hentSkattbareInntekter(
        @PathVariable("behandlingsId") behandlingsId: String,
    ): SkattbarInntektFrontend {
        tilgangskontroll.verifiserAtBrukerHarTilgang()

        return skattbarInntektProxy.getSkattbarInntekt(behandlingsId)
    }

    @PostMapping("/samtykke")
    @Deprecated("POST skal ikke ha side effects; bruk PUT mot skattbarinntektogforskuddstrekk")
    fun updateSamtykke(
        @PathVariable("behandlingsId") behandlingsId: String,
        @RequestBody samtykke: Boolean,
    ) {
        tilgangskontroll.verifiserAtBrukerKanEndreSoknad(behandlingsId)

        skattbarInntektProxy.updateSamtykkeSkatteetaten(behandlingsId, samtykke)
    }

    @PutMapping
    fun putSkatteetatenSamtykke(
        @PathVariable("behandlingsId") behandlingsId: String,
        @RequestBody @Valid
        input: SkattbarInntektInputDTO,
    ): SkattbarInntektFrontend {
        tilgangskontroll.verifiserAtBrukerKanEndreSoknad(behandlingsId)

        return skattbarInntektProxy.updateSamtykkeSkatteetaten(behandlingsId, input.samtykke)
    }
}
