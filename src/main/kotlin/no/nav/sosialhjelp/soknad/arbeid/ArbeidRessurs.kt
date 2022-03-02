package no.nav.sosialhjelp.soknad.arbeid

import no.nav.sbl.soknadsosialhjelp.soknad.arbeid.JsonArbeidsforhold
import no.nav.sbl.soknadsosialhjelp.soknad.arbeid.JsonArbeidsforhold.Stillingstype
import no.nav.sbl.soknadsosialhjelp.soknad.arbeid.JsonKommentarTilArbeidsforhold
import no.nav.sbl.soknadsosialhjelp.soknad.common.JsonKildeBruker
import no.nav.security.token.support.core.api.ProtectedWithClaims
import no.nav.sosialhjelp.metrics.aspects.Timed
import no.nav.sosialhjelp.soknad.common.Constants
import no.nav.sosialhjelp.soknad.common.subjecthandler.SubjectHandlerUtils
import no.nav.sosialhjelp.soknad.db.repositories.soknadunderarbeid.SoknadUnderArbeidRepository
import no.nav.sosialhjelp.soknad.tilgangskontroll.Tilgangskontroll
import org.apache.commons.lang3.StringUtils
import org.springframework.stereotype.Controller
import javax.ws.rs.GET
import javax.ws.rs.PUT
import javax.ws.rs.Path
import javax.ws.rs.PathParam
import javax.ws.rs.Produces
import javax.ws.rs.core.MediaType

@Controller
@ProtectedWithClaims(issuer = Constants.SELVBETJENING, claimMap = [Constants.CLAIM_ACR_LEVEL_4])
@Path("/soknader/{behandlingsId}/arbeid")
@Timed
@Produces(MediaType.APPLICATION_JSON)
open class ArbeidRessurs(
    private val soknadUnderArbeidRepository: SoknadUnderArbeidRepository,
    private val tilgangskontroll: Tilgangskontroll
) {
    @GET
    open fun hentArbeid(@PathParam("behandlingsId") behandlingsId: String): ArbeidFrontend {
        tilgangskontroll.verifiserAtBrukerHarTilgang()
        val eier = SubjectHandlerUtils.getUserIdFromToken()
        val soknad = soknadUnderArbeidRepository.hentSoknad(behandlingsId, eier).jsonInternalSoknad
        val arbeid = soknad.soknad.data.arbeid
        val kommentarTilArbeidsforhold = soknad.soknad.data.arbeid.kommentarTilArbeidsforhold
        val forhold = arbeid.forhold?.map { mapToArbeidsforholdFrontend(it) }

        return ArbeidFrontend(forhold, kommentarTilArbeidsforhold?.verdi)
    }

    @PUT
    open fun updateArbeid(@PathParam("behandlingsId") behandlingsId: String, arbeidFrontend: ArbeidFrontend) {
        tilgangskontroll.verifiserAtBrukerKanEndreSoknad(behandlingsId)
        val eier = SubjectHandlerUtils.getUserIdFromToken()
        val soknad = soknadUnderArbeidRepository.hentSoknad(behandlingsId, eier)
        val arbeid = soknad.jsonInternalSoknad.soknad.data.arbeid
        if (!StringUtils.isBlank(arbeidFrontend.kommentarTilArbeidsforhold)) {
            arbeid.kommentarTilArbeidsforhold = JsonKommentarTilArbeidsforhold()
                .withKilde(JsonKildeBruker.BRUKER)
                .withVerdi(arbeidFrontend.kommentarTilArbeidsforhold)
        } else {
            arbeid.kommentarTilArbeidsforhold = null
        }
        soknadUnderArbeidRepository.oppdaterSoknadsdata(soknad, eier)
    }

    private fun mapToArbeidsforholdFrontend(arbeidsforhold: JsonArbeidsforhold): ArbeidsforholdFrontend {
        return ArbeidsforholdFrontend(
            arbeidsforhold.arbeidsgivernavn,
            arbeidsforhold.fom,
            arbeidsforhold.tom,
            isStillingstypeErHeltid(arbeidsforhold.stillingstype),
            arbeidsforhold.stillingsprosent,
            java.lang.Boolean.FALSE
        )
    }

    data class ArbeidFrontend(
        val arbeidsforhold: List<ArbeidsforholdFrontend>?,
        val kommentarTilArbeidsforhold: String?
    )

    data class ArbeidsforholdFrontend(
        var arbeidsgivernavn: String?,
        var fom: String?,
        var tom: String?,
        var stillingstypeErHeltid: Boolean?,
        var stillingsprosent: Int?,
        var overstyrtAvBruker: Boolean?
    )

    companion object {
        private fun isStillingstypeErHeltid(stillingstype: Stillingstype?): Boolean? {
            if (stillingstype == null) {
                return null
            }
            return if (stillingstype == Stillingstype.FAST) java.lang.Boolean.TRUE else java.lang.Boolean.FALSE
        }
    }
}
