package no.nav.sosialhjelp.soknad.inntekt.husbanken

import no.nav.sbl.soknadsosialhjelp.json.SoknadJsonTyper.BOSTOTTE
import no.nav.sbl.soknadsosialhjelp.json.SoknadJsonTyper.BOSTOTTE_SAMTYKKE
import no.nav.sbl.soknadsosialhjelp.json.SoknadJsonTyper.UTBETALING_HUSBANKEN
import no.nav.sbl.soknadsosialhjelp.soknad.JsonInternalSoknad
import no.nav.sbl.soknadsosialhjelp.soknad.bostotte.JsonBostotteSak
import no.nav.sbl.soknadsosialhjelp.soknad.okonomi.JsonOkonomiopplysninger
import no.nav.sbl.soknadsosialhjelp.soknad.okonomi.opplysning.JsonOkonomiOpplysningUtbetaling
import no.nav.security.token.support.core.api.ProtectedWithClaims
import no.nav.sosialhjelp.soknad.app.Constants
import no.nav.sosialhjelp.soknad.app.mapper.OkonomiMapper
import no.nav.sosialhjelp.soknad.app.mapper.TitleKeyMapper
import no.nav.sosialhjelp.soknad.app.subjecthandler.SubjectHandlerUtils
import no.nav.sosialhjelp.soknad.db.repositories.soknadunderarbeid.SoknadUnderArbeidRepository
import no.nav.sosialhjelp.soknad.tekster.TextService
import no.nav.sosialhjelp.soknad.tilgangskontroll.Tilgangskontroll
import org.springframework.http.HttpHeaders
import org.springframework.http.MediaType
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestHeader
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@ProtectedWithClaims(issuer = Constants.SELVBETJENING, claimMap = [Constants.CLAIM_ACR_LEVEL_4, Constants.CLAIM_ACR_LOA_HIGH], combineWithOr = true)
@RequestMapping("/soknader/{behandlingsId}/inntekt/bostotte", produces = [MediaType.APPLICATION_JSON_VALUE])
class BostotteRessurs(
    private val tilgangskontroll: Tilgangskontroll,
    private val soknadUnderArbeidRepository: SoknadUnderArbeidRepository,
    private val bostotteSystemdata: BostotteSystemdata,
    private val textService: TextService
) {
    @GetMapping
    fun hentBostotte(@PathVariable("behandlingsId") behandlingsId: String): BostotteFrontend {
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

    @PutMapping
    fun updateBostotte(
        @PathVariable("behandlingsId") behandlingsId: String,
        @RequestBody bostotteFrontend: BostotteFrontend,
        @RequestHeader(value = HttpHeaders.AUTHORIZATION) token: String?
    ) {
        tilgangskontroll.verifiserAtBrukerKanEndreSoknad(behandlingsId)
        val eier = SubjectHandlerUtils.getUserIdFromToken()
        val soknad = soknadUnderArbeidRepository.hentSoknad(behandlingsId, eier)
        val jsonInternalSoknad = soknad.jsonInternalSoknad
            ?: throw IllegalStateException("Kan ikke oppdatere søknaddata hvis SoknadUnderArbeid.jsonInternalSoknad er null")
        val opplysninger = jsonInternalSoknad.soknad.data.okonomi.opplysninger
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

    @PostMapping("/samtykke")
    fun updateSamtykke(
        @PathVariable("behandlingsId") behandlingsId: String,
        @RequestBody samtykke: Boolean,
        @RequestHeader(value = HttpHeaders.AUTHORIZATION) token: String?
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
            .firstOrNull { it.verdi }
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
