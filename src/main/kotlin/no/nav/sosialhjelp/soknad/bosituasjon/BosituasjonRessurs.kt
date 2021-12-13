package no.nav.sosialhjelp.soknad.bosituasjon

import no.nav.sbl.soknadsosialhjelp.soknad.bosituasjon.JsonBosituasjon.Botype
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
@Path("/soknader/{behandlingsId}/bosituasjon")
@Timed
@Produces(MediaType.APPLICATION_JSON)
open class BosituasjonRessurs(
    private val tilgangskontroll: Tilgangskontroll,
    private val soknadUnderArbeidRepository: SoknadUnderArbeidRepository
) {
    @GET
    open fun hentBosituasjon(@PathParam("behandlingsId") behandlingsId: String?): BosituasjonFrontend {
        tilgangskontroll.verifiserAtBrukerHarTilgang()
        val eier = SubjectHandler.getUserId()
        val soknad = soknadUnderArbeidRepository.hentSoknad(behandlingsId, eier).jsonInternalSoknad
        val bosituasjon = soknad.soknad.data.bosituasjon
        return BosituasjonFrontend(bosituasjon.botype, bosituasjon.antallPersoner)
    }

    @PUT
    open fun updateBosituasjon(
        @PathParam("behandlingsId") behandlingsId: String?,
        bosituasjonFrontend: BosituasjonFrontend
    ) {
        tilgangskontroll.verifiserAtBrukerKanEndreSoknad(behandlingsId)
        val eier = SubjectHandler.getUserId()
        val soknad = soknadUnderArbeidRepository.hentSoknad(behandlingsId, eier)
        val bosituasjon = soknad.jsonInternalSoknad.soknad.data.bosituasjon
        bosituasjon.kilde = JsonKildeBruker.BRUKER
        if (bosituasjonFrontend.botype != null) {
            bosituasjon.botype = bosituasjonFrontend.botype
        }
        bosituasjon.antallPersoner = bosituasjonFrontend.antallPersoner
        soknadUnderArbeidRepository.oppdaterSoknadsdata(soknad, eier)
    }

    data class BosituasjonFrontend(
        var botype: Botype?,
        var antallPersoner: Int?
    )
}
