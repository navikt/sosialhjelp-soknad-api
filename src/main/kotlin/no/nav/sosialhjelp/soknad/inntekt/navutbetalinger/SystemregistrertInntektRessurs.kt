package no.nav.sosialhjelp.soknad.inntekt.navutbetalinger

import no.nav.sbl.soknadsosialhjelp.json.SoknadJsonTyper
import no.nav.sbl.soknadsosialhjelp.soknad.okonomi.opplysning.JsonOkonomiOpplysningUtbetaling
import no.nav.sosialhjelp.soknad.app.annotation.ProtectionSelvbetjeningHigh
import no.nav.sosialhjelp.soknad.db.repositories.soknadunderarbeid.SoknadUnderArbeidRepository
import no.nav.sosialhjelp.soknad.tilgangskontroll.Tilgangskontroll
import org.springframework.http.MediaType
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import no.nav.sosialhjelp.soknad.app.subjecthandler.SubjectHandlerUtils.getUserIdFromToken as eier

@RestController
@ProtectionSelvbetjeningHigh
@RequestMapping("/soknader/{behandlingsId}/inntekt/systemdata", produces = [MediaType.APPLICATION_JSON_VALUE])
class SystemregistrertInntektRessurs(
    private val soknadUnderArbeidRepository: SoknadUnderArbeidRepository,
    private val tilgangskontroll: Tilgangskontroll,
) {
    @GetMapping
    fun hentSystemregistrerteInntekter(
        @PathVariable("behandlingsId") behandlingsId: String,
    ): SysteminntekterFrontend {
        tilgangskontroll.verifiserAtBrukerHarTilgang()
        val soknad = soknadUnderArbeidRepository.hentSoknad(behandlingsId, eier())
        val jsonInternalSoknad = soknad.jsonInternalSoknad ?: error("jsonInternalSoknad == null")

        val utbetalingerFraNavFeilet = jsonInternalSoknad.soknad.driftsinformasjon.utbetalingerFraNavFeilet
        val opplysninger = jsonInternalSoknad.soknad.data.okonomi.opplysninger

        val systeminntekter = opplysninger.utbetaling.orEmpty().filter { it.type == SoknadJsonTyper.UTBETALING_NAVYTELSE }.map { SysteminntektFrontend.fromJson(it) }

        return SysteminntekterFrontend(systeminntekter, utbetalingerFraNavFeilet)
    }

    data class SysteminntekterFrontend(val systeminntekter: List<SysteminntektFrontend>, val utbetalingerFraNavFeilet: Boolean)

    data class SysteminntektFrontend(val inntektType: String, val utbetalingsdato: String, val belop: Double) {
        companion object {
            fun fromJson(utbetaling: JsonOkonomiOpplysningUtbetaling) =
                SysteminntektFrontend(
                    inntektType = utbetaling.tittel,
                    utbetalingsdato = utbetaling.utbetalingsdato,
                    belop = utbetaling.netto,
                )
        }
    }
}
