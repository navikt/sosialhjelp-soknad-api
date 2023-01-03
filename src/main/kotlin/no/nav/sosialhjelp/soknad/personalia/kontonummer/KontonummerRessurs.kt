package no.nav.sosialhjelp.soknad.personalia.kontonummer

import no.nav.sbl.soknadsosialhjelp.soknad.common.JsonKilde
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
@RequestMapping("/soknader/{behandlingsId}/personalia/kontonummer", produces = [MediaType.APPLICATION_JSON_VALUE])
open class KontonummerRessurs(
    private val tilgangskontroll: Tilgangskontroll,
    private val soknadUnderArbeidRepository: SoknadUnderArbeidRepository,
    private val kontonummerSystemdata: KontonummerSystemdata
) {
    @GetMapping
    open fun hentKontonummer(
        @PathVariable("behandlingsId") behandlingsId: String
    ): KontonummerFrontend {
        tilgangskontroll.verifiserAtBrukerHarTilgang()
        val eier = SubjectHandlerUtils.getUserIdFromToken()
        val soknadUnderArbeid = soknadUnderArbeidRepository.hentSoknad(behandlingsId, eier)
        val jsonInternalSoknad = soknadUnderArbeid.jsonInternalSoknad
            ?: throw IllegalStateException("Kan ikke hente søknaddata hvis SoknadUnderArbeid.jsonInternalSoknad er null")
        val kontonummer = jsonInternalSoknad.soknad.data.personalia.kontonummer
        val systemverdi: String? = if (kontonummer.kilde == JsonKilde.SYSTEM) {
            kontonummer.verdi
        } else {
            kontonummerSystemdata.innhentSystemverdiKontonummer(eier)
        }
        return KontonummerFrontend(
            brukerdefinert = kontonummer.kilde == JsonKilde.BRUKER,
            systemverdi = systemverdi,
            brukerutfyltVerdi = if (kontonummer.kilde == JsonKilde.BRUKER) kontonummer.verdi else null,
            harIkkeKonto = kontonummer.harIkkeKonto
        )
    }

    @PutMapping
    open fun updateKontonummer(
        @PathVariable("behandlingsId") behandlingsId: String,
        @RequestBody kontonummerFrontend: KontonummerFrontend
    ) {
        tilgangskontroll.verifiserAtBrukerKanEndreSoknad(behandlingsId)
        val eier = SubjectHandlerUtils.getUserIdFromToken()
        val soknad = soknadUnderArbeidRepository.hentSoknad(behandlingsId, eier)
        val jsonInternalSoknad = soknad.jsonInternalSoknad
            ?: throw IllegalStateException("Kan ikke oppdatere søknaddata hvis SoknadUnderArbeid.jsonInternalSoknad er null")
        val personalia = jsonInternalSoknad.soknad.data.personalia
        val kontonummer = personalia.kontonummer
        if (kontonummerFrontend.brukerdefinert) {
            kontonummer.kilde = JsonKilde.BRUKER
            kontonummer.verdi = if (kontonummerFrontend.brukerutfyltVerdi == "") null else kontonummerFrontend.brukerutfyltVerdi
            kontonummer.setHarIkkeKonto(kontonummerFrontend.harIkkeKonto)
        } else if (kontonummer.kilde == JsonKilde.BRUKER) {
            kontonummer.kilde = JsonKilde.SYSTEM
            kontonummerSystemdata.updateSystemdataIn(soknad)
            kontonummer.setHarIkkeKonto(null)
        }
        soknadUnderArbeidRepository.oppdaterSoknadsdata(soknad, eier)
    }

    data class KontonummerFrontend(
        val brukerdefinert: Boolean = false,
        val systemverdi: String? = null,
        val brukerutfyltVerdi: String? = null,
        val harIkkeKonto: Boolean? = null,
    )
}
