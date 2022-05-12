package no.nav.sosialhjelp.soknad.inntekt.husbanken

import no.nav.sbl.soknadsosialhjelp.json.SoknadJsonTyper.BOSTOTTE
import no.nav.sbl.soknadsosialhjelp.json.SoknadJsonTyper.BOSTOTTE_SAMTYKKE
import no.nav.sbl.soknadsosialhjelp.json.SoknadJsonTyper.UTBETALING_HUSBANKEN
import no.nav.sbl.soknadsosialhjelp.soknad.JsonInternalSoknad
import no.nav.sbl.soknadsosialhjelp.soknad.bostotte.JsonBostotteSak
import no.nav.sbl.soknadsosialhjelp.soknad.okonomi.JsonOkonomiopplysninger
import no.nav.sbl.soknadsosialhjelp.soknad.okonomi.opplysning.JsonOkonomiOpplysningUtbetaling
import no.nav.security.token.support.core.api.ProtectedWithClaims
import no.nav.sosialhjelp.metrics.aspects.Timed
import no.nav.sosialhjelp.soknad.common.Constants
import no.nav.sosialhjelp.soknad.common.mapper.OkonomiMapper
import no.nav.sosialhjelp.soknad.common.mapper.TitleKeyMapper
import no.nav.sosialhjelp.soknad.common.subjecthandler.SubjectHandlerUtils
import no.nav.sosialhjelp.soknad.db.repositories.soknadunderarbeid.SoknadUnderArbeidRepository
import no.nav.sosialhjelp.soknad.tekster.TextService
import no.nav.sosialhjelp.soknad.tilgangskontroll.Tilgangskontroll
import org.springframework.http.HttpHeaders
import org.springframework.stereotype.Controller
import javax.ws.rs.GET
import javax.ws.rs.HeaderParam
import javax.ws.rs.POST
import javax.ws.rs.PUT
import javax.ws.rs.Path
import javax.ws.rs.PathParam
import javax.ws.rs.Produces
import javax.ws.rs.core.MediaType

@Controller
@ProtectedWithClaims(issuer = Constants.SELVBETJENING, claimMap = [Constants.CLAIM_ACR_LEVEL_4])
@Path("/soknader/{behandlingsId}/inntekt/bostotte")
@Timed
@Produces(MediaType.APPLICATION_JSON)
open class BostotteRessurs(
    private val tilgangskontroll: Tilgangskontroll,
    private val soknadUnderArbeidRepository: SoknadUnderArbeidRepository,
    private val bostotteSystemdata: BostotteSystemdata,
    private val textService: TextService
) {
    @GET
    open fun hentBostotte(@PathParam("behandlingsId") behandlingsId: String): BostotteFrontend {
        tilgangskontroll.verifiserAtBrukerHarTilgang()
        val eier = SubjectHandlerUtils.getUserIdFromToken()
        val soknad = soknadUnderArbeidRepository.hentSoknad(behandlingsId, eier).jsonInternalSoknad
            ?: throw IllegalStateException("Kan ikke hente søknaddata hvis SoknadUnderArbeid.jsonInternalSoknad er null")
        val opplysninger = soknad.soknad.data.okonomi.opplysninger
        val bekreftelse = opplysninger.bekreftelse?.run { getBekreftelse(opplysninger) }
        return BostotteFrontend(
            bekreftelse = bekreftelse,
            samtykke = bekreftelse?.run { hentSamtykkeFraSoknad(opplysninger) },
            utbetalinger = mapToUtbetalinger(soknad),
            saker = mapToUtSaksStatuser(soknad),
            stotteFraHusbankenFeilet = soknad.soknad.driftsinformasjon.stotteFraHusbankenFeilet,
            samtykkeTidspunkt = bekreftelse?.run { hentSamtykkeDatoFraSoknad(opplysninger) }
        )
    }

    @PUT
    open fun updateBostotte(
        @PathParam("behandlingsId") behandlingsId: String,
        bostotteFrontend: BostotteFrontend,
        @HeaderParam(value = HttpHeaders.AUTHORIZATION) token: String?
    ) {
        tilgangskontroll.verifiserAtBrukerKanEndreSoknad(behandlingsId)
        val eier = SubjectHandlerUtils.getUserIdFromToken()
        val soknad = soknadUnderArbeidRepository.hentSoknad(behandlingsId, eier)
        val jsonInternalSoknad = soknad.jsonInternalSoknad
            ?: throw IllegalStateException("Kan ikke oppdatere søknaddata hvis SoknadUnderArbeid.jsonInternalSoknad er null")
        val opplysninger = jsonInternalSoknad.soknad.data.okonomi.opplysninger
        if (opplysninger.bekreftelse == null) {
            opplysninger.bekreftelse = ArrayList()
        }
        OkonomiMapper.setBekreftelse(
            opplysninger,
            BOSTOTTE,
            bostotteFrontend.bekreftelse,
            textService.getJsonOkonomiTittel("inntekt.bostotte")
        )

        bostotteFrontend.bekreftelse?.let {
            if (java.lang.Boolean.TRUE == it) {
                val tittel = textService.getJsonOkonomiTittel(TitleKeyMapper.soknadTypeToTitleKey[BOSTOTTE])
                OkonomiMapper.addUtbetalingIfNotPresentInOpplysninger(
                    opplysninger.utbetaling,
                    UTBETALING_HUSBANKEN,
                    tittel
                )
            } else {
                OkonomiMapper.removeUtbetalingIfPresentInOpplysninger(
                    opplysninger.utbetaling,
                    UTBETALING_HUSBANKEN
                )
            }
        }
        soknadUnderArbeidRepository.oppdaterSoknadsdata(soknad, eier)
    }

    @POST
    @Path(value = "/samtykke")
    open fun updateSamtykke(
        @PathParam("behandlingsId") behandlingsId: String,
        samtykke: Boolean,
        @HeaderParam(value = HttpHeaders.AUTHORIZATION) token: String?
    ) {
        tilgangskontroll.verifiserAtBrukerKanEndreSoknad(behandlingsId)
        val eier = SubjectHandlerUtils.getUserIdFromToken()
        val soknad = soknadUnderArbeidRepository.hentSoknad(behandlingsId, eier)
        val jsonInternalSoknad = soknad.jsonInternalSoknad
            ?: throw IllegalStateException("Kan ikke oppdatere samtykke hvis SoknadUnderArbeid.jsonInternalSoknad er null")
        val opplysninger = jsonInternalSoknad.soknad.data.okonomi.opplysninger
        val lagretSamtykke = hentSamtykkeFraSoknad(opplysninger)
        var skalLagre = samtykke
        if (lagretSamtykke != samtykke) {
            skalLagre = true
            OkonomiMapper.removeBekreftelserIfPresent(opplysninger, BOSTOTTE_SAMTYKKE)
            OkonomiMapper.setBekreftelse(
                opplysninger,
                BOSTOTTE_SAMTYKKE,
                samtykke,
                textService.getJsonOkonomiTittel("inntekt.bostotte.samtykke")
            )
        }
        if (skalLagre) {
            bostotteSystemdata.updateSystemdataIn(soknad, token)
            soknadUnderArbeidRepository.oppdaterSoknadsdata(soknad, eier)
        }
    }

    private fun hentSamtykkeFraSoknad(opplysninger: JsonOkonomiopplysninger): Boolean {
        return opplysninger.bekreftelse
            .filter { it.type == BOSTOTTE_SAMTYKKE }
            .any { it.verdi }
    }

    private fun hentSamtykkeDatoFraSoknad(opplysninger: JsonOkonomiopplysninger): String? {
        return opplysninger.bekreftelse
            .filter { it.type == BOSTOTTE_SAMTYKKE }
            .filter { it.verdi }
            .firstOrNull()
            ?.bekreftelsesDato
    }

    private fun getBekreftelse(opplysninger: JsonOkonomiopplysninger): Boolean? {
        return opplysninger.bekreftelse
            .firstOrNull { it.type == BOSTOTTE }
            ?.verdi
    }

    private fun mapToUtbetalinger(soknad: JsonInternalSoknad): List<JsonOkonomiOpplysningUtbetaling> {
        return soknad.soknad.data.okonomi.opplysninger.utbetaling
            .filter { it.type == UTBETALING_HUSBANKEN }
    }

    private fun mapToUtSaksStatuser(soknad: JsonInternalSoknad): List<JsonBostotteSak> {
        return soknad.soknad.data.okonomi.opplysninger.bostotte.saker
            .filter { it.type == UTBETALING_HUSBANKEN }
    }

    data class BostotteFrontend(
        val bekreftelse: Boolean?,
        val samtykke: Boolean?,
        val utbetalinger: List<JsonOkonomiOpplysningUtbetaling>?,
        val saker: List<JsonBostotteSak>?,
        val stotteFraHusbankenFeilet: Boolean?,
        val samtykkeTidspunkt: String?
    )
}
