package no.nav.sosialhjelp.soknad.inntekt.navutbetalinger

import no.nav.sbl.soknadsosialhjelp.json.SoknadJsonTyper
import no.nav.sbl.soknadsosialhjelp.soknad.okonomi.opplysning.JsonOkonomiOpplysningUtbetaling
import no.nav.security.token.support.core.api.ProtectedWithClaims
import no.nav.sosialhjelp.soknad.app.Constants
import no.nav.sosialhjelp.soknad.app.subjecthandler.SubjectHandlerUtils
import no.nav.sosialhjelp.soknad.db.repositories.soknadunderarbeid.SoknadUnderArbeidRepository
import no.nav.sosialhjelp.soknad.tilgangskontroll.Tilgangskontroll
import org.springframework.http.MediaType
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@ProtectedWithClaims(issuer = Constants.SELVBETJENING, claimMap = [Constants.CLAIM_ACR_LEVEL_4, Constants.CLAIM_ACR_LOA_HIGH], combineWithOr = true)
@RequestMapping("/soknader/{behandlingsId}/inntekt/systemdata", produces = [MediaType.APPLICATION_JSON_VALUE])
class SystemregistrertInntektRessurs(
    private val soknadUnderArbeidRepository: SoknadUnderArbeidRepository,
    private val tilgangskontroll: Tilgangskontroll,
) {
    @GetMapping
    fun hentSystemregistrerteInntekter(@PathVariable("behandlingsId") behandlingsId: String): SysteminntekterFrontend {
        tilgangskontroll.verifiserAtBrukerHarTilgang()
        val eier = SubjectHandlerUtils.getUserIdFromToken()
        val soknad = soknadUnderArbeidRepository.hentSoknad(behandlingsId, eier).jsonInternalSoknad
            ?: throw IllegalStateException("Kan ikke hente søknaddata hvis SoknadUnderArbeid.jsonInternalSoknad er null")
        val utbetalinger = soknad.soknad.data.okonomi.opplysninger.utbetaling

        return SysteminntekterFrontend(
            systeminntekter = utbetalinger
                ?.filter { it.type == SoknadJsonTyper.UTBETALING_NAVYTELSE }
                ?.map { mapToUtbetalingFrontend(it) },
            utbetalingerFraNavFeilet = soknad.soknad.driftsinformasjon.utbetalingerFraNavFeilet,
        )
    }

    private fun mapToUtbetalingFrontend(utbetaling: JsonOkonomiOpplysningUtbetaling): SysteminntektFrontend {
        return SysteminntektFrontend(
            inntektType = utbetaling.tittel,
            utbetalingsdato = utbetaling.utbetalingsdato,
            belop = utbetaling.netto,
        )
    }

    data class SysteminntekterFrontend(
        val systeminntekter: List<SysteminntektFrontend>? = null,
        val utbetalingerFraNavFeilet: Boolean? = null,
    )

    data class SysteminntektFrontend(
        val inntektType: String? = null,
        val utbetalingsdato: String? = null,
        val belop: Double? = null,
    )
}
