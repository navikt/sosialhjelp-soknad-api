package no.nav.sosialhjelp.soknad.begrunnelse

import no.nav.sbl.soknadsosialhjelp.soknad.common.JsonKildeBruker
import no.nav.security.token.support.core.api.ProtectedWithClaims
import no.nav.sosialhjelp.metrics.aspects.Timed
import no.nav.sosialhjelp.soknad.business.db.repositories.soknadunderarbeid.SoknadUnderArbeidRepository
import no.nav.sosialhjelp.soknad.domain.model.oidc.SubjectHandler
import no.nav.sosialhjelp.soknad.web.sikkerhet.Tilgangskontroll
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
@Path("/soknader/{behandlingsId}/begrunnelse")
@Timed
@Produces(MediaType.APPLICATION_JSON)
open class BegrunnelseRessurs(
    private val tilgangskontroll: Tilgangskontroll,
    private val soknadUnderArbeidRepository: SoknadUnderArbeidRepository
) {
    @GET
    open fun hentBegrunnelse(
        @PathParam("behandlingsId") behandlingsId: String
    ): BegrunnelseFrontend {
        tilgangskontroll.verifiserAtBrukerHarTilgang()
        val eier = SubjectHandler.getUserId()
        val soknad = soknadUnderArbeidRepository.hentSoknad(behandlingsId, eier).jsonInternalSoknad
        val begrunnelse = soknad.soknad.data.begrunnelse
        return BegrunnelseFrontend(begrunnelse.hvaSokesOm, begrunnelse.hvorforSoke)
    }

    @PUT
    open fun updateBegrunnelse(
        @PathParam("behandlingsId") behandlingsId: String,
        begrunnelseFrontend: BegrunnelseFrontend
    ) {
        tilgangskontroll.verifiserAtBrukerKanEndreSoknad(behandlingsId)
        val eier = SubjectHandler.getUserId()
        val soknad = soknadUnderArbeidRepository.hentSoknad(behandlingsId, eier)
        val begrunnelse = soknad.jsonInternalSoknad.soknad.data.begrunnelse
        begrunnelse.kilde = JsonKildeBruker.BRUKER
        begrunnelse.hvaSokesOm = begrunnelseFrontend.hvaSokesOm
        begrunnelse.hvorforSoke = begrunnelseFrontend.hvorforSoke
        soknadUnderArbeidRepository.oppdaterSoknadsdata(soknad, eier)
    }

    data class BegrunnelseFrontend(
        val hvaSokesOm: String?,
        val hvorforSoke: String?
    )
}
