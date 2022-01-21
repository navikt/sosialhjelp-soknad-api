package no.nav.sosialhjelp.soknad.inntekt.andreinntekter

import no.nav.sbl.soknadsosialhjelp.json.SoknadJsonTyper.BEKREFTELSE_UTBETALING
import no.nav.sbl.soknadsosialhjelp.json.SoknadJsonTyper.UTBETALING_ANNET
import no.nav.sbl.soknadsosialhjelp.json.SoknadJsonTyper.UTBETALING_FORSIKRING
import no.nav.sbl.soknadsosialhjelp.json.SoknadJsonTyper.UTBETALING_SALG
import no.nav.sbl.soknadsosialhjelp.json.SoknadJsonTyper.UTBETALING_UTBYTTE
import no.nav.sbl.soknadsosialhjelp.soknad.common.JsonKildeBruker
import no.nav.sbl.soknadsosialhjelp.soknad.okonomi.JsonOkonomiopplysninger
import no.nav.sbl.soknadsosialhjelp.soknad.okonomi.opplysning.JsonOkonomibeskrivelserAvAnnet
import no.nav.security.token.support.core.api.ProtectedWithClaims
import no.nav.sosialhjelp.metrics.aspects.Timed
import no.nav.sosialhjelp.soknad.business.db.repositories.soknadunderarbeid.SoknadUnderArbeidRepository
import no.nav.sosialhjelp.soknad.common.Constants
import no.nav.sosialhjelp.soknad.common.mapper.OkonomiMapper.addUtbetalingIfCheckedElseDeleteInOpplysninger
import no.nav.sosialhjelp.soknad.common.mapper.OkonomiMapper.setBekreftelse
import no.nav.sosialhjelp.soknad.common.mapper.TitleKeyMapper.soknadTypeToTitleKey
import no.nav.sosialhjelp.soknad.common.subjecthandler.SubjectHandlerUtils
import no.nav.sosialhjelp.soknad.tekster.TextService
import no.nav.sosialhjelp.soknad.tilgangskontroll.Tilgangskontroll
import org.springframework.stereotype.Controller
import javax.ws.rs.GET
import javax.ws.rs.PUT
import javax.ws.rs.Path
import javax.ws.rs.PathParam
import javax.ws.rs.Produces
import javax.ws.rs.core.MediaType

@Controller
@ProtectedWithClaims(issuer = Constants.SELVBETJENING, claimMap = [Constants.CLAIM_ACR_LEVEL_4])
@Path("/soknader/{behandlingsId}/inntekt/utbetalinger")
@Timed
@Produces(MediaType.APPLICATION_JSON)
open class UtbetalingRessurs(
    private val tilgangskontroll: Tilgangskontroll,
    private val soknadUnderArbeidRepository: SoknadUnderArbeidRepository,
    private val textService: TextService
) {
    @GET
    open fun hentUtbetalinger(@PathParam("behandlingsId") behandlingsId: String): UtbetalingerFrontend {
        tilgangskontroll.verifiserAtBrukerHarTilgang()
        val eier = SubjectHandlerUtils.getUserIdFromToken()
        val soknad = soknadUnderArbeidRepository.hentSoknad(behandlingsId, eier).jsonInternalSoknad
        val opplysninger = soknad.soknad.data.okonomi.opplysninger

        if (opplysninger.bekreftelse == null) {
            return UtbetalingerFrontend(utbetalingerFraNavFeilet = soknad.soknad.driftsinformasjon.utbetalingerFraNavFeilet)
        }
        return UtbetalingerFrontend(
            bekreftelse = getBekreftelse(opplysninger),
            utbytte = hasUtbetalingType(opplysninger, UTBETALING_UTBYTTE),
            salg = hasUtbetalingType(opplysninger, UTBETALING_SALG),
            forsikring = hasUtbetalingType(opplysninger, UTBETALING_FORSIKRING),
            annet = hasUtbetalingType(opplysninger, UTBETALING_ANNET),
            beskrivelseAvAnnet = opplysninger.beskrivelseAvAnnet?.utbetaling,
            utbetalingerFraNavFeilet = soknad.soknad.driftsinformasjon.utbetalingerFraNavFeilet
        )
    }

    @PUT
    open fun updateUtbetalinger(
        @PathParam("behandlingsId") behandlingsId: String,
        utbetalingerFrontend: UtbetalingerFrontend
    ) {
        tilgangskontroll.verifiserAtBrukerKanEndreSoknad(behandlingsId)
        val eier = SubjectHandlerUtils.getUserIdFromToken()
        val soknad = soknadUnderArbeidRepository.hentSoknad(behandlingsId, eier)
        val opplysninger = soknad.jsonInternalSoknad.soknad.data.okonomi.opplysninger
        if (opplysninger.bekreftelse == null) {
            opplysninger.bekreftelse = ArrayList()
        }
        setBekreftelse(
            opplysninger,
            BEKREFTELSE_UTBETALING,
            utbetalingerFrontend.bekreftelse,
            textService.getJsonOkonomiTittel("inntekt.inntekter")
        )
        setUtbetalinger(opplysninger, utbetalingerFrontend)
        setBeskrivelseAvAnnet(opplysninger, utbetalingerFrontend)
        soknadUnderArbeidRepository.oppdaterSoknadsdata(soknad, eier)
    }

    private fun setUtbetalinger(opplysninger: JsonOkonomiopplysninger, utbetalingerFrontend: UtbetalingerFrontend) {
        val utbetalinger = opplysninger.utbetaling
        var tittel =
            textService.getJsonOkonomiTittel(soknadTypeToTitleKey[UTBETALING_UTBYTTE])
        addUtbetalingIfCheckedElseDeleteInOpplysninger(
            utbetalinger,
            UTBETALING_UTBYTTE,
            tittel,
            utbetalingerFrontend.utbytte
        )
        tittel = textService.getJsonOkonomiTittel(soknadTypeToTitleKey[UTBETALING_SALG])
        addUtbetalingIfCheckedElseDeleteInOpplysninger(
            utbetalinger,
            UTBETALING_SALG,
            tittel,
            utbetalingerFrontend.salg
        )
        tittel =
            textService.getJsonOkonomiTittel(soknadTypeToTitleKey[UTBETALING_FORSIKRING])
        addUtbetalingIfCheckedElseDeleteInOpplysninger(
            utbetalinger,
            UTBETALING_FORSIKRING,
            tittel,
            utbetalingerFrontend.forsikring
        )
        tittel = textService.getJsonOkonomiTittel("opplysninger.inntekt.inntekter.annet")
        addUtbetalingIfCheckedElseDeleteInOpplysninger(
            utbetalinger,
            UTBETALING_ANNET,
            tittel,
            utbetalingerFrontend.annet
        )
    }

    private fun setBeskrivelseAvAnnet(opplysninger: JsonOkonomiopplysninger, utbetalingerFrontend: UtbetalingerFrontend) {
        if (opplysninger.beskrivelseAvAnnet == null) {
            opplysninger.withBeskrivelseAvAnnet(
                JsonOkonomibeskrivelserAvAnnet()
                    .withKilde(JsonKildeBruker.BRUKER)
                    .withVerdi("")
                    .withSparing("")
                    .withUtbetaling("")
                    .withBoutgifter("")
                    .withBarneutgifter("")
            )
        }
        opplysninger.beskrivelseAvAnnet.utbetaling = utbetalingerFrontend.beskrivelseAvAnnet ?: ""
    }

    private fun hasUtbetalingType(opplysninger: JsonOkonomiopplysninger, type: String): Boolean {
        return opplysninger.utbetaling.any { it.type == type }
    }

    private fun getBekreftelse(opplysninger: JsonOkonomiopplysninger): Boolean? {
        return opplysninger.bekreftelse.firstOrNull() { it.type == BEKREFTELSE_UTBETALING }?.verdi
    }

    data class UtbetalingerFrontend(
        var bekreftelse: Boolean? = null,
        var utbytte: Boolean = false,
        var salg: Boolean = false,
        var forsikring: Boolean = false,
        var annet: Boolean = false,
        var beskrivelseAvAnnet: String? = null,
        var utbetalingerFraNavFeilet: Boolean? = null
    )
}
