package no.nav.sosialhjelp.soknad.inntekt.andreinntekter

import no.nav.sbl.soknadsosialhjelp.json.SoknadJsonTyper
import no.nav.sbl.soknadsosialhjelp.soknad.okonomi.JsonOkonomiopplysninger
import no.nav.sosialhjelp.soknad.app.annotation.ProtectionSelvbetjeningHigh
import no.nav.sosialhjelp.soknad.app.mapper.OkonomiMapper.setUtbetalingInOpplysninger
import no.nav.sosialhjelp.soknad.app.mapper.TitleKeyMapper.soknadTypeToTitleKey
import no.nav.sosialhjelp.soknad.db.repositories.soknadunderarbeid.SoknadUnderArbeidRepository
import no.nav.sosialhjelp.soknad.tekster.TextService
import no.nav.sosialhjelp.soknad.tilgangskontroll.Tilgangskontroll
import no.nav.sosialhjelp.soknad.v2.okonomi.MigrationToolkit.getBekreftelseVerdi
import no.nav.sosialhjelp.soknad.v2.okonomi.MigrationToolkit.hasUtbetaling
import no.nav.sosialhjelp.soknad.v2.okonomi.MigrationToolkit.updateBekreftelse
import no.nav.sosialhjelp.soknad.v2.okonomi.MigrationToolkit.updateOrCreateBeskrivelseAvAnnet
import org.springframework.http.MediaType
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import no.nav.sosialhjelp.soknad.app.subjecthandler.SubjectHandlerUtils.getUserIdFromToken as eier

@RestController
@ProtectionSelvbetjeningHigh
@RequestMapping("/soknader/{behandlingsId}/inntekt/utbetalinger", produces = [MediaType.APPLICATION_JSON_VALUE])
class UtbetalingRessurs(
    private val tilgangskontroll: Tilgangskontroll,
    private val soknadUnderArbeidRepository: SoknadUnderArbeidRepository,
    private val textService: TextService,
) {
    @GetMapping
    fun hentUtbetalinger(
        @PathVariable("behandlingsId") behandlingsId: String,
    ): UtbetalingerFrontend {
        tilgangskontroll.verifiserAtBrukerHarTilgang()
        val soknad = soknadUnderArbeidRepository.hentSoknad(behandlingsId, eier())
        val jsonInternalSoknad = soknad.jsonInternalSoknad ?: error("jsonInternalSoknad == null")

        val opplysninger = jsonInternalSoknad.soknad.data.okonomi.opplysninger
        val utbetalingerFraNavFeilet = jsonInternalSoknad.soknad.driftsinformasjon.utbetalingerFraNavFeilet

        return opplysninger.bekreftelse?.let {
            UtbetalingerFrontend(
                bekreftelse = opplysninger.getBekreftelseVerdi(SoknadJsonTyper.BEKREFTELSE_UTBETALING),
                utbytte = opplysninger.hasUtbetaling(SoknadJsonTyper.UTBETALING_UTBYTTE),
                salg = opplysninger.hasUtbetaling(SoknadJsonTyper.UTBETALING_SALG),
                forsikring = opplysninger.hasUtbetaling(SoknadJsonTyper.UTBETALING_FORSIKRING),
                annet = opplysninger.hasUtbetaling(SoknadJsonTyper.UTBETALING_ANNET),
                beskrivelseAvAnnet = opplysninger.beskrivelseAvAnnet?.utbetaling,
                utbetalingerFraNavFeilet = utbetalingerFraNavFeilet,
            )
        } ?: UtbetalingerFrontend(utbetalingerFraNavFeilet = utbetalingerFraNavFeilet)
    }

    @PutMapping
    fun updateUtbetalinger(
        @PathVariable("behandlingsId") behandlingsId: String,
        @RequestBody utbetalingerFrontend: UtbetalingerFrontend,
    ) {
        tilgangskontroll.verifiserAtBrukerKanEndreSoknad(behandlingsId)
        val soknad = soknadUnderArbeidRepository.hentSoknad(behandlingsId, eier())
        val jsonInternalSoknad = soknad.jsonInternalSoknad ?: error("jsonInternalSoknad == null")

        val opplysninger = jsonInternalSoknad.soknad.data.okonomi.opplysninger

        opplysninger.updateBekreftelse(SoknadJsonTyper.BEKREFTELSE_UTBETALING, utbetalingerFrontend.bekreftelse, textService.getJsonOkonomiTittel("inntekt.inntekter"))
        opplysninger.setUtbetalinger(utbetalingerFrontend)
        opplysninger.updateOrCreateBeskrivelseAvAnnet(utbetaling = utbetalingerFrontend.beskrivelseAvAnnet)

        soknadUnderArbeidRepository.oppdaterSoknadsdata(soknad, eier())
    }

    private fun JsonOkonomiopplysninger.setUtbetalinger(
        utbetalingerFrontend: UtbetalingerFrontend,
    ) = listOf(
        SoknadJsonTyper.UTBETALING_UTBYTTE to utbetalingerFrontend.utbytte,
        SoknadJsonTyper.UTBETALING_SALG to utbetalingerFrontend.salg,
        SoknadJsonTyper.UTBETALING_FORSIKRING to utbetalingerFrontend.forsikring,
        SoknadJsonTyper.UTBETALING_ANNET to utbetalingerFrontend.annet,
    ).forEach { (utbetalingJsonType, isExpected) ->
        setUtbetalingInOpplysninger(this.utbetaling, utbetalingJsonType, isExpected, textService.getJsonOkonomiTittel(soknadTypeToTitleKey[utbetalingJsonType]))
    }

    data class UtbetalingerFrontend(
        var bekreftelse: Boolean? = null,
        var utbytte: Boolean = false,
        var salg: Boolean = false,
        var forsikring: Boolean = false,
        var annet: Boolean = false,
        var beskrivelseAvAnnet: String? = null,
        var utbetalingerFraNavFeilet: Boolean? = null,
    )
}
