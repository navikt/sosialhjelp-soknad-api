package no.nav.sosialhjelp.soknad.personalia.telefonnummer

import io.swagger.v3.oas.annotations.media.Schema
import no.nav.sosialhjelp.soknad.app.annotation.ProtectionSelvbetjeningHigh
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
@RequestMapping("/soknader/{behandlingsId}/personalia/telefonnummer", produces = [MediaType.APPLICATION_JSON_VALUE])
class TelefonnummerRessurs(
    private val tilgangskontroll: Tilgangskontroll,
    private val telefonnummerProxy: TelefonnummerProxy,
) {
    @GetMapping
    fun hentTelefonnummer(
        @PathVariable("behandlingsId") behandlingsId: String?,
    ): TelefonnummerFrontend {
        tilgangskontroll.verifiserAtBrukerHarTilgang()

        return telefonnummerProxy.getTelefonnummer(behandlingsId!!)
    }

    @PutMapping
    fun updateTelefonnummer(
        @PathVariable("behandlingsId") behandlingsId: String?,
        @RequestBody telefonnummerFrontend: TelefonnummerFrontend,
    ): TelefonnummerFrontend {
        tilgangskontroll.verifiserAtBrukerKanEndreSoknad(behandlingsId)

        return telefonnummerProxy.updateTelefonnummer(behandlingsId!!, telefonnummerFrontend.brukerutfyltVerdi)
    }
}

data class TelefonnummerFrontend(
    val brukerdefinert: Boolean = false,
    val systemverdi: String? = null,
    @Schema(nullable = true)
    val brukerutfyltVerdi: String? = null,
)
