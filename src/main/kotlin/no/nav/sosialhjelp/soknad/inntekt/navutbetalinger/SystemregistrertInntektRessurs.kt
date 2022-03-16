package no.nav.sosialhjelp.soknad.inntekt.navutbetalinger

import no.nav.sbl.soknadsosialhjelp.json.SoknadJsonTyper
import no.nav.sbl.soknadsosialhjelp.soknad.okonomi.opplysning.JsonOkonomiOpplysningUtbetaling
import no.nav.security.token.support.core.api.ProtectedWithClaims
import no.nav.sosialhjelp.metrics.aspects.Timed
import no.nav.sosialhjelp.soknad.common.Constants
import no.nav.sosialhjelp.soknad.common.subjecthandler.SubjectHandlerUtils
import no.nav.sosialhjelp.soknad.db.repositories.soknadunderarbeid.SoknadUnderArbeidRepository
import no.nav.sosialhjelp.soknad.tilgangskontroll.Tilgangskontroll
import org.springframework.stereotype.Controller
import javax.ws.rs.GET
import javax.ws.rs.Path
import javax.ws.rs.PathParam
import javax.ws.rs.Produces
import javax.ws.rs.core.MediaType

@Controller
@ProtectedWithClaims(issuer = Constants.SELVBETJENING, claimMap = [Constants.CLAIM_ACR_LEVEL_4])
@Path("/soknader/{behandlingsId}/inntekt/systemdata")
@Timed
@Produces(MediaType.APPLICATION_JSON)
open class SystemregistrertInntektRessurs(
    private val soknadUnderArbeidRepository: SoknadUnderArbeidRepository,
    private val tilgangskontroll: Tilgangskontroll
) {
    @GET
    open fun hentSystemregistrerteInntekter(@PathParam("behandlingsId") behandlingsId: String): SysteminntekterFrontend {
        tilgangskontroll.verifiserAtBrukerHarTilgang()
        val eier = SubjectHandlerUtils.getUserIdFromToken()
        val soknad = soknadUnderArbeidRepository.hentSoknad(behandlingsId, eier).jsonInternalSoknad
            ?: throw IllegalStateException("Kan ikke hente søknaddata hvis SoknadUnderArbeid.jsonInternalSoknad er null")
        val utbetalinger = soknad.soknad.data.okonomi.opplysninger.utbetaling

        return SysteminntekterFrontend(
            systeminntekter = utbetalinger
                ?.filter { it.type == SoknadJsonTyper.UTBETALING_NAVYTELSE }
                ?.map { mapToUtbetalingFrontend(it) },
            utbetalingerFraNavFeilet = soknad.soknad.driftsinformasjon.utbetalingerFraNavFeilet
        )
    }

    private fun mapToUtbetalingFrontend(utbetaling: JsonOkonomiOpplysningUtbetaling): SysteminntektFrontend {
        return SysteminntektFrontend(
            inntektType = utbetaling.tittel,
            utbetalingsdato = utbetaling.utbetalingsdato,
            belop = utbetaling.netto
        )
    }

    data class SysteminntekterFrontend(
        val systeminntekter: List<SysteminntektFrontend>? = null,
        val utbetalingerFraNavFeilet: Boolean? = null,
    )

    data class SysteminntektFrontend(
        val inntektType: String? = null,
        val utbetalingsdato: String? = null,
        val belop: Double? = null
    )
}
