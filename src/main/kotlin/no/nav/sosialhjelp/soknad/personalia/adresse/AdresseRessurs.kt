package no.nav.sosialhjelp.soknad.personalia.adresse

import no.nav.sbl.soknadsosialhjelp.soknad.adresse.JsonAdresse
import no.nav.sbl.soknadsosialhjelp.soknad.adresse.JsonAdresseValg
import no.nav.security.token.support.core.api.ProtectedWithClaims
import no.nav.sosialhjelp.metrics.aspects.Timed
import no.nav.sosialhjelp.soknad.business.db.repositories.soknadunderarbeid.SoknadUnderArbeidRepository
import no.nav.sosialhjelp.soknad.domain.model.oidc.SubjectHandler
import no.nav.sosialhjelp.soknad.navenhet.NavEnhetRessurs
import no.nav.sosialhjelp.soknad.navenhet.dto.NavEnhetFrontend
import no.nav.sosialhjelp.soknad.personalia.adresse.dto.AdresserFrontend
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
@Path("/soknader/{behandlingsId}/personalia/adresser")
@Timed
@Produces(MediaType.APPLICATION_JSON)
open class AdresseRessurs(
    private val tilgangskontroll: Tilgangskontroll,
    private val adresseSystemdata: AdresseSystemdata,
    private val soknadUnderArbeidRepository: SoknadUnderArbeidRepository,
    private val navEnhetRessurs: NavEnhetRessurs
) {
    @GET
    open fun hentAdresser(@PathParam("behandlingsId") behandlingsId: String): AdresserFrontend {
        tilgangskontroll.verifiserAtBrukerHarTilgang()
        val eier = SubjectHandler.getUserId()
        val soknad = soknadUnderArbeidRepository.hentSoknad(behandlingsId, eier)
        val personIdentifikator = soknad.jsonInternalSoknad.soknad.data.personalia.personIdentifikator.verdi
        val jsonOppholdsadresse = soknad.jsonInternalSoknad.soknad.data.personalia.oppholdsadresse
        val sysFolkeregistrertAdresse = soknad.jsonInternalSoknad.soknad.data.personalia.folkeregistrertAdresse
        val sysMidlertidigAdresse = adresseSystemdata.innhentMidlertidigAdresse(personIdentifikator)
        soknad.jsonInternalSoknad.midlertidigAdresse = sysMidlertidigAdresse
        soknadUnderArbeidRepository.oppdaterSoknadsdata(soknad, eier)
        return AdresseMapper.mapToAdresserFrontend(
            sysFolkeregistrertAdresse,
            sysMidlertidigAdresse,
            jsonOppholdsadresse
        )
    }

    @PUT
    open fun updateAdresse(
        @PathParam("behandlingsId") behandlingsId: String,
        adresserFrontend: AdresserFrontend
    ): List<NavEnhetFrontend>? {
        tilgangskontroll.verifiserAtBrukerKanEndreSoknad(behandlingsId)
        val eier = SubjectHandler.getUserId()
        val soknad = soknadUnderArbeidRepository.hentSoknad(behandlingsId, eier)
        val personalia = soknad.jsonInternalSoknad.soknad.data.personalia
        when (adresserFrontend.valg) {
            JsonAdresseValg.FOLKEREGISTRERT ->
                personalia.oppholdsadresse =
                    adresseSystemdata.createDeepCopyOfJsonAdresse(personalia.folkeregistrertAdresse)
            JsonAdresseValg.MIDLERTIDIG ->
                personalia.oppholdsadresse =
                    adresseSystemdata.innhentMidlertidigAdresse(eier)
            JsonAdresseValg.SOKNAD ->
                personalia.oppholdsadresse =
                    adresserFrontend.soknad?.let { AdresseMapper.mapToJsonAdresse(it) }
        }
        personalia.oppholdsadresse.adresseValg = adresserFrontend.valg
        personalia.postadresse = midlertidigLosningForPostadresse(personalia.oppholdsadresse)
        soknadUnderArbeidRepository.oppdaterSoknadsdata(soknad, eier)
        return navEnhetRessurs.findSoknadsmottaker(
            eier,
            soknad.jsonInternalSoknad.soknad,
            adresserFrontend.valg.toString(),
            null
        )
    }

    private fun midlertidigLosningForPostadresse(oppholdsadresse: JsonAdresse?): JsonAdresse? {
        if (oppholdsadresse == null) {
            return null
        }
        return if (oppholdsadresse.type == JsonAdresse.Type.MATRIKKELADRESSE) {
            null
        } else adresseSystemdata.createDeepCopyOfJsonAdresse(oppholdsadresse)!!.withAdresseValg(null)
    }
}
