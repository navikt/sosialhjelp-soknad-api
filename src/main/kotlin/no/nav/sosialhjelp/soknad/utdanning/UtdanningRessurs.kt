package no.nav.sosialhjelp.soknad.utdanning

import io.swagger.v3.oas.annotations.media.Schema
import no.nav.sbl.soknadsosialhjelp.json.SoknadJsonTyper
import no.nav.sbl.soknadsosialhjelp.soknad.common.JsonKilde
import no.nav.sbl.soknadsosialhjelp.soknad.utdanning.JsonUtdanning.Studentgrad
import no.nav.security.token.support.core.api.ProtectedWithClaims
import no.nav.sosialhjelp.soknad.app.Constants
import no.nav.sosialhjelp.soknad.app.subjecthandler.SubjectHandlerUtils
import no.nav.sosialhjelp.soknad.db.repositories.soknadunderarbeid.SoknadUnderArbeidRepository
import no.nav.sosialhjelp.soknad.tilgangskontroll.Tilgangskontroll
import no.nav.sosialhjelp.soknad.v2.shadow.V2ControllerAdapter
import org.springframework.http.MediaType.APPLICATION_JSON_VALUE
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@ProtectedWithClaims(
    issuer = Constants.SELVBETJENING,
    claimMap = [Constants.CLAIM_ACR_LEVEL_4, Constants.CLAIM_ACR_LOA_HIGH],
    combineWithOr = true,
)
@RequestMapping("/soknader/{behandlingsId}/utdanning", produces = [APPLICATION_JSON_VALUE])
class UtdanningRessurs(
    private val tilgangskontroll: Tilgangskontroll,
    private val soknadUnderArbeidRepository: SoknadUnderArbeidRepository,
    private val controllerAdapter: V2ControllerAdapter,
) {
    @GetMapping
    fun hentUtdanning(
        @PathVariable("behandlingsId") behandlingsId: String,
    ): UtdanningFrontend {
        tilgangskontroll.verifiserAtBrukerHarTilgang()
        val eier = SubjectHandlerUtils.getUserIdFromToken()
        val soknad =
            soknadUnderArbeidRepository.hentSoknad(behandlingsId, eier).jsonInternalSoknad
                ?: throw IllegalStateException("Kan ikke hente søknaddata hvis SoknadUnderArbeid.jsonInternalSoknad er null")
        val utdanning = soknad.soknad.data.utdanning
        return UtdanningFrontend(utdanning.erStudent, toStudentgradErHeltid(utdanning.studentgrad))
    }

    @PutMapping
    fun updateUtdanning(
        @PathVariable("behandlingsId") behandlingsId: String,
        @RequestBody utdanningFrontend: UtdanningFrontend,
    ) {
        tilgangskontroll.verifiserAtBrukerKanEndreSoknad(behandlingsId)
        val eier = SubjectHandlerUtils.getUserIdFromToken()
        val soknad = soknadUnderArbeidRepository.hentSoknad(behandlingsId, eier)
        val jsonInternalSoknad =
            soknad.jsonInternalSoknad
                ?: throw IllegalStateException("Kan ikke oppdatere søknaddata hvis SoknadUnderArbeid.jsonInternalSoknad er null")
        val utdanning = jsonInternalSoknad.soknad.data.utdanning
        val inntekter = jsonInternalSoknad.soknad.data.okonomi.oversikt.inntekt
        utdanning.kilde = JsonKilde.BRUKER
        utdanning.erStudent = utdanningFrontend.erStudent
        if (utdanningFrontend.erStudent == true) {
            utdanning.studentgrad = toStudentgrad(utdanningFrontend.studengradErHeltid)
        } else {
            utdanning.studentgrad = null
            val opplysninger = jsonInternalSoknad.soknad.data.okonomi.opplysninger
            if (opplysninger.bekreftelse != null) {
                opplysninger.bekreftelse.removeIf { it.type == SoknadJsonTyper.STUDIELAN }
                inntekter.removeIf { it.type == SoknadJsonTyper.STUDIELAN }
            }
        }
        soknadUnderArbeidRepository.oppdaterSoknadsdata(soknad, eier)

        // NyModell
        controllerAdapter.updateUtdanning(behandlingsId, utdanningFrontend)
    }

    companion object {
        private fun toStudentgradErHeltid(studentgrad: Studentgrad?): Boolean? {
            return if (studentgrad == null) {
                null
            } else {
                studentgrad == Studentgrad.HELTID
            }
        }

        private fun toStudentgrad(studentgrad: Boolean?): Studentgrad? {
            if (studentgrad == null) {
                return null
            }
            return if (studentgrad) Studentgrad.HELTID else Studentgrad.DELTID
        }
    }
}

data class UtdanningFrontend(
    @Schema(nullable = true)
    var erStudent: Boolean?,
    @Schema(nullable = true)
    var studengradErHeltid: Boolean?,
)
