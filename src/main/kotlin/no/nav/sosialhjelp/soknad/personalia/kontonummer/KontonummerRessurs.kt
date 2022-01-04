package no.nav.sosialhjelp.soknad.personalia.kontonummer

import no.nav.sbl.soknadsosialhjelp.soknad.common.JsonKilde
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
@Path("/soknader/{behandlingsId}/personalia/kontonummer")
@Timed
@Produces(MediaType.APPLICATION_JSON)
open class KontonummerRessurs(
    private val tilgangskontroll: Tilgangskontroll,
    private val soknadUnderArbeidRepository: SoknadUnderArbeidRepository,
    private val kontonummerSystemdata: KontonummerSystemdata
) {
    @GET
    open fun hentKontonummer(@PathParam("behandlingsId") behandlingsId: String): KontonummerFrontend {
        tilgangskontroll.verifiserAtBrukerHarTilgang()
        val eier = SubjectHandlerUtils.getUserIdFromToken()
        val soknadUnderArbeid = soknadUnderArbeidRepository.hentSoknad(behandlingsId, eier)
        val kontonummer = soknadUnderArbeid.jsonInternalSoknad.soknad.data.personalia.kontonummer
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

    @PUT
    open fun updateKontonummer(
        @PathParam("behandlingsId") behandlingsId: String,
        kontonummerFrontend: KontonummerFrontend
    ) {
        tilgangskontroll.verifiserAtBrukerKanEndreSoknad(behandlingsId)
        val eier = SubjectHandlerUtils.getUserIdFromToken()
        val soknad = soknadUnderArbeidRepository.hentSoknad(behandlingsId, eier)
        val personalia = soknad.jsonInternalSoknad.soknad.data.personalia
        val kontonummer = personalia.kontonummer
        if (kontonummerFrontend.brukerdefinert) {
            kontonummer.kilde = JsonKilde.BRUKER
            kontonummer.verdi = if (kontonummerFrontend.brukerutfyltVerdi == "") null else kontonummerFrontend.brukerutfyltVerdi
            kontonummer.setHarIkkeKonto(kontonummerFrontend.harIkkeKonto ?: null)
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
