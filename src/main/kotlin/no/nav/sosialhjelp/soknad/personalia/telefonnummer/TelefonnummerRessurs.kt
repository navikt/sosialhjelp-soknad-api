package no.nav.sosialhjelp.soknad.personalia.telefonnummer

import no.nav.sbl.soknadsosialhjelp.soknad.common.JsonKilde
import no.nav.sbl.soknadsosialhjelp.soknad.personalia.JsonTelefonnummer
import no.nav.security.token.support.core.api.ProtectedWithClaims
import no.nav.sosialhjelp.metrics.aspects.Timed
import no.nav.sosialhjelp.soknad.business.db.repositories.soknadunderarbeid.SoknadUnderArbeidRepository
import no.nav.sosialhjelp.soknad.common.subjecthandler.SubjectHandlerUtils
import no.nav.sosialhjelp.soknad.tilgangskontroll.Tilgangskontroll
import no.nav.sosialhjelp.soknad.web.utils.Constants
import org.springframework.stereotype.Controller
import javax.ws.rs.GET
import javax.ws.rs.PUT
import javax.ws.rs.Path
import javax.ws.rs.PathParam
import javax.ws.rs.Produces
import javax.ws.rs.core.MediaType

@Controller
@ProtectedWithClaims(issuer = Constants.SELVBETJENING, claimMap = [Constants.CLAIM_ACR_LEVEL_4])
@Path("/soknader/{behandlingsId}/personalia/telefonnummer")
@Timed
@Produces(MediaType.APPLICATION_JSON)
open class TelefonnummerRessurs(
    private val tilgangskontroll: Tilgangskontroll,
    private val telefonnummerSystemdata: TelefonnummerSystemdata,
    private val soknadUnderArbeidRepository: SoknadUnderArbeidRepository
) {
    @GET
    open fun hentTelefonnummer(@PathParam("behandlingsId") behandlingsId: String?): TelefonnummerFrontend {
        tilgangskontroll.verifiserAtBrukerHarTilgang()
        val eier = SubjectHandlerUtils.getUserIdFromToken()
        val soknadUnderArbeid = soknadUnderArbeidRepository.hentSoknad(behandlingsId, eier)
        val telefonnummer = soknadUnderArbeid.jsonInternalSoknad.soknad.data.personalia.telefonnummer
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

    @PUT
    open fun updateTelefonnummer(
        @PathParam("behandlingsId") behandlingsId: String?,
        telefonnummerFrontend: TelefonnummerFrontend
    ) {
        tilgangskontroll.verifiserAtBrukerKanEndreSoknad(behandlingsId)
        val eier = SubjectHandlerUtils.getUserIdFromToken()
        val soknad = soknadUnderArbeidRepository.hentSoknad(behandlingsId, eier)
        val personalia = soknad.jsonInternalSoknad.soknad.data.personalia
        val jsonTelefonnummer = personalia.telefonnummer ?: personalia.withTelefonnummer(JsonTelefonnummer()).telefonnummer
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

    data class TelefonnummerFrontend(
        val brukerdefinert: Boolean = false,
        val systemverdi: String? = null,
        val brukerutfyltVerdi: String? = null
    )
}
