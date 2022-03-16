package no.nav.sosialhjelp.soknad.utdanning

import no.nav.sbl.soknadsosialhjelp.json.SoknadJsonTyper
import no.nav.sbl.soknadsosialhjelp.soknad.common.JsonKilde
import no.nav.sbl.soknadsosialhjelp.soknad.utdanning.JsonUtdanning.Studentgrad
import no.nav.security.token.support.core.api.ProtectedWithClaims
import no.nav.sosialhjelp.metrics.aspects.Timed
import no.nav.sosialhjelp.soknad.common.Constants
import no.nav.sosialhjelp.soknad.common.subjecthandler.SubjectHandlerUtils
import no.nav.sosialhjelp.soknad.db.repositories.soknadunderarbeid.SoknadUnderArbeidRepository
import no.nav.sosialhjelp.soknad.tilgangskontroll.Tilgangskontroll
import org.springframework.stereotype.Controller
import javax.ws.rs.GET
import javax.ws.rs.PUT
import javax.ws.rs.Path
import javax.ws.rs.PathParam
import javax.ws.rs.Produces
import javax.ws.rs.core.MediaType

@Controller
@ProtectedWithClaims(issuer = Constants.SELVBETJENING, claimMap = [Constants.CLAIM_ACR_LEVEL_4])
@Path("/soknader/{behandlingsId}/utdanning")
@Timed
@Produces(MediaType.APPLICATION_JSON)
open class UtdanningRessurs(
    private val tilgangskontroll: Tilgangskontroll,
    private val soknadUnderArbeidRepository: SoknadUnderArbeidRepository
) {
    @GET
    open fun hentUtdanning(@PathParam("behandlingsId") behandlingsId: String): UtdanningFrontend {
        tilgangskontroll.verifiserAtBrukerHarTilgang()
        val eier = SubjectHandlerUtils.getUserIdFromToken()
        val soknad = soknadUnderArbeidRepository.hentSoknad(behandlingsId, eier).jsonInternalSoknad
            ?: throw IllegalStateException("Kan ikke hente søknaddata hvis SoknadUnderArbeid.jsonInternalSoknad er null")
        val utdanning = soknad.soknad.data.utdanning
        return UtdanningFrontend(utdanning.erStudent, toStudentgradErHeltid(utdanning.studentgrad))
    }

    @PUT
    open fun updateUtdanning(
        @PathParam("behandlingsId") behandlingsId: String,
        utdanningFrontend: UtdanningFrontend
    ) {
        tilgangskontroll.verifiserAtBrukerKanEndreSoknad(behandlingsId)
        val eier = SubjectHandlerUtils.getUserIdFromToken()
        val soknad = soknadUnderArbeidRepository.hentSoknad(behandlingsId, eier)
        val jsonInternalSoknad = soknad.jsonInternalSoknad
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
    }

    data class UtdanningFrontend(
        var erStudent: Boolean?,
        var studengradErHeltid: Boolean?
    )

    companion object {
        private fun toStudentgradErHeltid(studentgrad: Studentgrad?): Boolean? {
            return if (studentgrad == null) {
                null
            } else studentgrad == Studentgrad.HELTID
        }

        private fun toStudentgrad(studentgrad: Boolean?): Studentgrad? {
            if (studentgrad == null) {
                return null
            }
            return if (studentgrad) Studentgrad.HELTID else Studentgrad.DELTID
        }
    }
}
