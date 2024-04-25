package no.nav.sosialhjelp.soknad.inntekt.andreinntekter

import no.nav.sbl.soknadsosialhjelp.json.SoknadJsonTyper.BEKREFTELSE_UTBETALING
import no.nav.sbl.soknadsosialhjelp.json.SoknadJsonTyper.UTBETALING_ANNET
import no.nav.sbl.soknadsosialhjelp.json.SoknadJsonTyper.UTBETALING_FORSIKRING
import no.nav.sbl.soknadsosialhjelp.json.SoknadJsonTyper.UTBETALING_SALG
import no.nav.sbl.soknadsosialhjelp.json.SoknadJsonTyper.UTBETALING_UTBYTTE
import no.nav.sbl.soknadsosialhjelp.soknad.common.JsonKildeBruker
import no.nav.sbl.soknadsosialhjelp.soknad.okonomi.JsonOkonomiopplysninger
import no.nav.sbl.soknadsosialhjelp.soknad.okonomi.opplysning.JsonOkonomiOpplysningUtbetaling
import no.nav.sbl.soknadsosialhjelp.soknad.okonomi.opplysning.JsonOkonomibeskrivelserAvAnnet
import no.nav.sosialhjelp.soknad.app.annotation.ProtectionSelvbetjeningHigh
import no.nav.sosialhjelp.soknad.app.mapper.OkonomiMapper.setBekreftelse
import no.nav.sosialhjelp.soknad.app.mapper.OkonomiMapper.setUtbetalingInOpplysninger
import no.nav.sosialhjelp.soknad.app.mapper.TitleKeyMapper.soknadTypeToTitleKey
import no.nav.sosialhjelp.soknad.db.repositories.soknadunderarbeid.SoknadUnderArbeidRepository
import no.nav.sosialhjelp.soknad.tekster.TextService
import no.nav.sosialhjelp.soknad.tilgangskontroll.Tilgangskontroll
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
                bekreftelse = getBekreftelse(opplysninger),
                utbytte = opplysninger.hasUtbetalingType(UTBETALING_UTBYTTE),
                salg = opplysninger.hasUtbetalingType(UTBETALING_SALG),
                forsikring = opplysninger.hasUtbetalingType(UTBETALING_FORSIKRING),
                annet = opplysninger.hasUtbetalingType(UTBETALING_ANNET),
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

        setUtbetalingerBekreftet(opplysninger, utbetalingerFrontend.bekreftelse)
        setUtbetalinger(opplysninger.utbetaling, utbetalingerFrontend)
        opplysninger.setBeskrivelseAvAnnet(utbetalingerFrontend.beskrivelseAvAnnet)

        soknadUnderArbeidRepository.oppdaterSoknadsdata(soknad, eier())
    }

    private fun setUtbetalingerBekreftet(
        opplysninger: JsonOkonomiopplysninger,
        bekreftelse: Boolean?,
    ) = setBekreftelse(opplysninger, BEKREFTELSE_UTBETALING, bekreftelse, textService.getJsonOkonomiTittel("inntekt.inntekter"))

    private fun setUtbetalinger(
        utbetalinger: MutableList<JsonOkonomiOpplysningUtbetaling>,
        utbetalingerFrontend: UtbetalingerFrontend,
    ) = listOf(
        UTBETALING_UTBYTTE to utbetalingerFrontend.utbytte,
        UTBETALING_SALG to utbetalingerFrontend.salg,
        UTBETALING_FORSIKRING to utbetalingerFrontend.forsikring,
        UTBETALING_ANNET to utbetalingerFrontend.annet,
    ).forEach { (utbetalingJsonType, isExpected) ->
        setUtbetalingInOpplysninger(utbetalinger, utbetalingJsonType, isExpected, textService.getJsonOkonomiTittel(soknadTypeToTitleKey[utbetalingJsonType]))
    }

    // TODO Er dette eneste vi fyller ut av denne? Er den nødvendig ref. beskrivelse på Json-objektet?
    private fun JsonOkonomiopplysninger.setBeskrivelseAvAnnet(beskrivelseAvAnnet: String?) = this.beskrivelseAvAnnet?.let { apply { it.utbetaling = beskrivelseAvAnnet } } ?: makeBeskrivelseAvAnnet(beskrivelseAvAnnet)

    private fun JsonOkonomiopplysninger.hasUtbetalingType(type: String): Boolean = this.utbetaling.any { it.type == type }

    private fun getBekreftelse(opplysninger: JsonOkonomiopplysninger): Boolean? = opplysninger.bekreftelse.firstOrNull { it.type == BEKREFTELSE_UTBETALING }?.verdi

    private fun makeBeskrivelseAvAnnet(utbetaling: String? = ""): JsonOkonomibeskrivelserAvAnnet = JsonOkonomibeskrivelserAvAnnet().withKilde(JsonKildeBruker.BRUKER).withVerdi("").withSparing("").withUtbetaling(utbetaling).withBoutgifter("").withBarneutgifter("")

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
