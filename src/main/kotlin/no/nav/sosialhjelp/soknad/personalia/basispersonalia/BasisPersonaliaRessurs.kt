package no.nav.sosialhjelp.soknad.personalia.basispersonalia

import no.nav.sbl.soknadsosialhjelp.soknad.common.JsonNavn
import no.nav.sbl.soknadsosialhjelp.soknad.personalia.JsonPersonalia
import no.nav.security.token.support.core.api.ProtectedWithClaims
import no.nav.sosialhjelp.metrics.aspects.Timed
import no.nav.sosialhjelp.soknad.business.db.repositories.soknadunderarbeid.SoknadUnderArbeidRepository
import no.nav.sosialhjelp.soknad.client.kodeverk.KodeverkService
import no.nav.sosialhjelp.soknad.domain.model.oidc.SubjectHandler
import no.nav.sosialhjelp.soknad.personalia.basispersonalia.dto.BasisPersonaliaFrontend
import no.nav.sosialhjelp.soknad.personalia.basispersonalia.dto.NavnFrontend
import no.nav.sosialhjelp.soknad.tilgangskontroll.Tilgangskontroll
import no.nav.sosialhjelp.soknad.web.utils.Constants
import org.springframework.stereotype.Controller
import javax.ws.rs.GET
import javax.ws.rs.Path
import javax.ws.rs.PathParam
import javax.ws.rs.Produces
import javax.ws.rs.core.MediaType

@Controller
@ProtectedWithClaims(issuer = Constants.SELVBETJENING, claimMap = [Constants.CLAIM_ACR_LEVEL_4])
@Path("/soknader/{behandlingsId}/personalia/basisPersonalia")
@Timed
@Produces(MediaType.APPLICATION_JSON)
open class BasisPersonaliaRessurs(
    private val kodeverkService: KodeverkService,
    private val soknadUnderArbeidRepository: SoknadUnderArbeidRepository,
    private val tilgangskontroll: Tilgangskontroll
) {
    @GET
    open fun hentBasisPersonalia(@PathParam("behandlingsId") behandlingsId: String?): BasisPersonaliaFrontend {
        tilgangskontroll.verifiserAtBrukerHarTilgang()
        val eier = SubjectHandler.getUserId()
        val soknad = soknadUnderArbeidRepository.hentSoknad(behandlingsId, eier).jsonInternalSoknad
        return mapToBasisPersonaliaFrontend(soknad.soknad.data.personalia)
    }

    private fun mapToBasisPersonaliaFrontend(jsonPersonalia: JsonPersonalia): BasisPersonaliaFrontend {
        val navn: JsonNavn = jsonPersonalia.navn
        return BasisPersonaliaFrontend(
            navn = NavnFrontend(navn.fornavn, navn.mellomnavn, navn.etternavn),
            fodselsnummer = jsonPersonalia.personIdentifikator.verdi,
            statsborgerskap = if (jsonPersonalia.statsborgerskap == null) null else kodeverkService.getLand(jsonPersonalia.statsborgerskap.verdi),
            nordiskBorger = if (jsonPersonalia.nordiskBorger != null) jsonPersonalia.nordiskBorger.verdi else null
        )
    }
}
