package no.nav.sosialhjelp.soknad.personalia.telefonnummer

import io.swagger.v3.oas.annotations.media.Schema
import no.nav.sbl.soknadsosialhjelp.soknad.common.JsonKilde
import no.nav.sbl.soknadsosialhjelp.soknad.personalia.JsonTelefonnummer
import no.nav.security.token.support.core.api.ProtectedWithClaims
import no.nav.sosialhjelp.soknad.app.Constants
import no.nav.sosialhjelp.soknad.app.subjecthandler.SubjectHandlerUtils
import no.nav.sosialhjelp.soknad.db.repositories.soknadunderarbeid.SoknadUnderArbeidRepository
import no.nav.sosialhjelp.soknad.tilgangskontroll.Tilgangskontroll
import org.springframework.http.MediaType
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@ProtectedWithClaims(issuer = Constants.SELVBETJENING, claimMap = [Constants.CLAIM_ACR_LEVEL_4])
@RequestMapping("/soknader/{behandlingsId}/personalia/telefonnummer", produces = [MediaType.APPLICATION_JSON_VALUE])
open class TelefonnummerRessurs(
    private val tilgangskontroll: Tilgangskontroll,
    private val telefonnummerSystemdata: TelefonnummerSystemdata,
    private val soknadUnderArbeidRepository: SoknadUnderArbeidRepository
) {
    @GetMapping
    open fun hentTelefonnummer(
        @PathVariable("behandlingsId") behandlingsId: String?
    ): TelefonnummerFrontend {
        tilgangskontroll.verifiserAtBrukerHarTilgang()
        val eier = SubjectHandlerUtils.getUserIdFromToken()
        val soknadUnderArbeid = soknadUnderArbeidRepository.hentSoknad(behandlingsId, eier)
        val jsonInternalSoknad = soknadUnderArbeid.jsonInternalSoknad
            ?: throw IllegalStateException("Kan ikke hente søknaddata hvis SoknadUnderArbeid.jsonInternalSoknad er null")
        val telefonnummer = jsonInternalSoknad.soknad.data.personalia.telefonnummer
        val systemverdi: String? = if (telefonnummer != null && telefonnummer.kilde == JsonKilde.SYSTEM) {
            telefonnummer.verdi
        } else {
            telefonnummerSystemdata.innhentSystemverdiTelefonnummer(eier)
        }
        return TelefonnummerFrontend(
            brukerdefinert = telefonnummer == null || telefonnummer.kilde == JsonKilde.BRUKER,
            systemverdi = systemverdi,
            brukerutfyltVerdi = if (telefonnummer != null && telefonnummer.kilde == JsonKilde.BRUKER) telefonnummer.verdi else null
        )
    }

    @PutMapping
    open fun updateTelefonnummer(
        @PathVariable("behandlingsId") behandlingsId: String?,
        @RequestBody telefonnummerFrontend: TelefonnummerFrontend
    ) {
        tilgangskontroll.verifiserAtBrukerKanEndreSoknad(behandlingsId)
        val eier = SubjectHandlerUtils.getUserIdFromToken()
        val soknad = soknadUnderArbeidRepository.hentSoknad(behandlingsId, eier)
        val jsonInternalSoknad = soknad.jsonInternalSoknad
            ?: throw IllegalStateException("Kan ikke oppdatere søknaddata hvis SoknadUnderArbeid.jsonInternalSoknad er null")
        val personalia = jsonInternalSoknad.soknad.data.personalia
        val jsonTelefonnummer =
            personalia.telefonnummer ?: personalia.withTelefonnummer(JsonTelefonnummer()).telefonnummer
        if (telefonnummerFrontend.brukerdefinert) {
            if (telefonnummerFrontend.brukerutfyltVerdi.isNullOrBlank()) {
                personalia.telefonnummer = null
            } else {
                jsonTelefonnummer.kilde = JsonKilde.BRUKER
                jsonTelefonnummer.verdi = telefonnummerFrontend.brukerutfyltVerdi
            }
        } else {
            jsonTelefonnummer.kilde = JsonKilde.SYSTEM
            telefonnummerSystemdata.updateSystemdataIn(soknad)
        }
        soknadUnderArbeidRepository.oppdaterSoknadsdata(soknad, eier)
    }

}

data class TelefonnummerFrontend(
    val brukerdefinert: Boolean = false,
    val systemverdi: String? = null,
    @Schema(nullable = true)
    val brukerutfyltVerdi: String? = null
)
