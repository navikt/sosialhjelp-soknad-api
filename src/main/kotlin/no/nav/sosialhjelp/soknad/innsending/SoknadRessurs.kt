package no.nav.sosialhjelp.soknad.innsending

import jakarta.servlet.http.Cookie
import jakarta.servlet.http.HttpServletResponse
import no.nav.sbl.soknadsosialhjelp.json.SoknadJsonTyper.BOSTOTTE_SAMTYKKE
import no.nav.sbl.soknadsosialhjelp.json.SoknadJsonTyper.UTBETALING_SKATTEETATEN_SAMTYKKE
import no.nav.sbl.soknadsosialhjelp.soknad.okonomi.opplysning.JsonOkonomibekreftelse
import no.nav.security.token.support.core.api.ProtectedWithClaims
import no.nav.sosialhjelp.soknad.ControllerToNewDatamodellProxy.nyDatamodellAktiv
import no.nav.sosialhjelp.soknad.api.nedetid.NedetidService
import no.nav.sosialhjelp.soknad.app.Constants
import no.nav.sosialhjelp.soknad.app.LoggingUtils.logger
import no.nav.sosialhjelp.soknad.app.MiljoUtils
import no.nav.sosialhjelp.soknad.app.exceptions.SoknadenHarNedetidException
import no.nav.sosialhjelp.soknad.app.subjecthandler.SubjectHandlerUtils.getUserIdFromToken
import no.nav.sosialhjelp.soknad.app.systemdata.SystemdataUpdater
import no.nav.sosialhjelp.soknad.db.repositories.soknadunderarbeid.SoknadUnderArbeid
import no.nav.sosialhjelp.soknad.db.repositories.soknadunderarbeid.SoknadUnderArbeidRepository
import no.nav.sosialhjelp.soknad.innsending.dto.BekreftelseRessurs
import no.nav.sosialhjelp.soknad.innsending.dto.StartSoknadResponse
import no.nav.sosialhjelp.soknad.innsending.soknadunderarbeid.SoknadUnderArbeidService
import no.nav.sosialhjelp.soknad.metrics.PrometheusMetricsService
import no.nav.sosialhjelp.soknad.tilgangskontroll.Tilgangskontroll
import no.nav.sosialhjelp.soknad.tilgangskontroll.XsrfGenerator
import no.nav.sosialhjelp.soknad.v2.SoknadLifecycleController
import org.springframework.http.HttpHeaders
import org.springframework.http.MediaType
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestHeader
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@RestController
@ProtectedWithClaims(
    issuer = Constants.SELVBETJENING,
    claimMap = [Constants.CLAIM_ACR_LEVEL_4, Constants.CLAIM_ACR_LOA_HIGH],
    combineWithOr = true,
)
@RequestMapping("/soknader", produces = [MediaType.APPLICATION_JSON_VALUE])
class SoknadRessurs(
    private val soknadServiceOld: SoknadServiceOld,
    private val soknadUnderArbeidService: SoknadUnderArbeidService,
    private val soknadUnderArbeidRepository: SoknadUnderArbeidRepository,
    private val systemdata: SystemdataUpdater,
    private val tilgangskontroll: Tilgangskontroll,
    private val nedetidService: NedetidService,
    private val prometheusMetricsService: PrometheusMetricsService,
    private val lifecycleController: SoknadLifecycleController,
) {
    @GetMapping("/{behandlingsId}/xsrfCookie")
    fun hentXsrfCookie(
        @PathVariable("behandlingsId") behandlingsId: String,
        response: HttpServletResponse,
    ): Boolean {
        tilgangskontroll.verifiserBrukerHarTilgangTilSoknad(behandlingsId)
        response.addCookie(xsrfCookie(behandlingsId))
        response.addCookie(xsrfCookieMedBehandlingsid(behandlingsId))
        soknadServiceOld.oppdaterSistEndretDatoPaaMetadata(behandlingsId)
        return true
    }

    @GetMapping("/{behandlingsId}/erSystemdataEndret")
    fun sjekkOmSystemdataErEndret(
        @PathVariable("behandlingsId") behandlingsId: String,
        @RequestHeader(value = HttpHeaders.AUTHORIZATION) token: String?,
    ): Boolean {
        tilgangskontroll.verifiserAtBrukerHarTilgang()
        val eier = getUserIdFromToken()
        val soknadUnderArbeid = soknadUnderArbeidRepository.hentSoknad(behandlingsId, eier)

        logger.info("Kjører ny runde med innhenting av Systemdata")
        systemdata.update(soknadUnderArbeid)

        val updatedJsonInternalSoknad = soknadUnderArbeid.jsonInternalSoknad
        val notUpdatedSoknadUnderArbeid = soknadUnderArbeidRepository.hentSoknad(behandlingsId, eier)
        val notUpdatedJsonInternalSoknad = notUpdatedSoknadUnderArbeid.jsonInternalSoknad

        soknadUnderArbeid.jsonInternalSoknad
            ?.soknad
            ?.data
            ?.let { soknadUnderArbeidService.sortOkonomi(it.okonomi) }
        notUpdatedSoknadUnderArbeid.jsonInternalSoknad
            ?.soknad
            ?.data
            ?.let { soknadUnderArbeidService.sortOkonomi(it.okonomi) }
        soknadUnderArbeid.jsonInternalSoknad
            ?.soknad
            ?.data
            ?.let { soknadUnderArbeidService.sortArbeid(it.arbeid) }
        notUpdatedSoknadUnderArbeid.jsonInternalSoknad
            ?.soknad
            ?.data
            ?.let { soknadUnderArbeidService.sortArbeid(it.arbeid) }

        return if (updatedJsonInternalSoknad == notUpdatedJsonInternalSoknad) {
            false
        } else {
            soknadUnderArbeidRepository.oppdaterSoknadsdata(soknadUnderArbeid, eier)
            true
        }
    }

    @PostMapping("/{behandlingsId}/oppdaterSamtykker")
    fun oppdaterSamtykker(
        @PathVariable("behandlingsId") behandlingsId: String,
        @RequestBody samtykker: List<BekreftelseRessurs>,
        @RequestHeader(value = HttpHeaders.AUTHORIZATION) token: String?,
    ) {
        tilgangskontroll.verifiserAtBrukerHarTilgang()
        val harBostotteSamtykke =
            samtykker
                .any { it.type.equals(BOSTOTTE_SAMTYKKE, ignoreCase = true) && it.verdi == true }
        val harSkatteetatenSamtykke =
            samtykker
                .any { it.type.equals(UTBETALING_SKATTEETATEN_SAMTYKKE, ignoreCase = true) && it.verdi == true }
        soknadServiceOld.oppdaterSamtykker(behandlingsId, harBostotteSamtykke, harSkatteetatenSamtykke, token)
    }

    @GetMapping("/{behandlingsId}/hentSamtykker")
    fun hentSamtykker(
        @PathVariable("behandlingsId") behandlingsId: String,
        @RequestHeader(value = HttpHeaders.AUTHORIZATION) token: String?,
    ): List<BekreftelseRessurs> {
        tilgangskontroll.verifiserAtBrukerHarTilgang()
        val eier = getUserIdFromToken()
        val soknadUnderArbeid = soknadUnderArbeidRepository.hentSoknad(behandlingsId, eier)

        val bekreftelser: MutableList<JsonOkonomibekreftelse> = mutableListOf()
        hentBekreftelse(soknadUnderArbeid, BOSTOTTE_SAMTYKKE)?.let { bekreftelser.add(it) }
        hentBekreftelse(soknadUnderArbeid, UTBETALING_SKATTEETATEN_SAMTYKKE)?.let { bekreftelser.add(it) }
        return bekreftelser
            .filter { it.verdi }
            .map { BekreftelseRessurs(it.type, it.verdi) }
    }

    private fun hentBekreftelse(
        soknadUnderArbeid: SoknadUnderArbeid,
        samtykke: String,
    ): JsonOkonomibekreftelse? {
        val bekreftelser =
            soknadUnderArbeid.jsonInternalSoknad
                ?.soknad
                ?.data
                ?.okonomi
                ?.opplysninger
                ?.bekreftelse
        return bekreftelser
            ?.firstOrNull { it.type.equals(samtykke, ignoreCase = true) }
    }

    @PostMapping("/opprettSoknad")
    fun opprettSoknad(
        @RequestHeader(value = HttpHeaders.AUTHORIZATION) token: String?,
        @RequestParam(value = "soknadstype", required = false) soknadstype: String?,
        response: HttpServletResponse,
    ): StartSoknadResponse {
        if (nedetidService.isInnenforNedetid) {
            throw SoknadenHarNedetidException("Soknaden har nedetid fram til ${nedetidService.nedetidSluttAsString}")
        }
        tilgangskontroll.verifiserAtBrukerHarTilgang()
        // Tillater å overstyre søknadstype i test-miljøene
        val isKort =
            if (MiljoUtils.isNonProduction()) {
                when (soknadstype) {
                    "kort" -> true
                    "standard" -> false
                    else -> false
                }
            } else {
                false
            }

        return if (nyDatamodellAktiv) {
            lifecycleController.createSoknad(soknadstype, response)
                .let { StartSoknadResponse(it.soknadId.toString(), it.useKortSoknad) }
        } else {
            soknadServiceOld
                .startSoknad(token, isKort)
                .also {
                    response.addCookie(xsrfCookie(it.brukerBehandlingId))
                    response.addCookie(xsrfCookieMedBehandlingsid(it.brukerBehandlingId))
                }
        }
    }

    @DeleteMapping("/{behandlingsId}")
    fun slettSoknad(
        @PathVariable("behandlingsId") behandlingsId: String,
        @RequestHeader(value = HttpHeaders.REFERER) referer: String?,
    ) {
        tilgangskontroll.verifiserAtBrukerKanEndreSoknad(behandlingsId)
        soknadServiceOld.avbrytSoknad(behandlingsId, referer)
    }

    @GetMapping("/{behandlingsId}/isKort")
    fun isKortSoknad(
        @PathVariable behandlingsId: String,
    ): Boolean {
        tilgangskontroll.verifiserBrukerHarTilgangTilSoknad(behandlingsId)
        return soknadServiceOld.hentSoknadMetadata(behandlingsId).kortSoknad
    }

    @GetMapping("/{behandlingsId}/isNyDatamodell")
    fun isNyDatamodell(
        @PathVariable behandlingsId: String,
    ): Boolean = soknadServiceOld.hentSoknadMetadataOrNull(behandlingsId) == null

    companion object {
        const val XSRF_TOKEN = "XSRF-TOKEN-SOKNAD-API"

        private val logger by logger()

        private fun xsrfCookie(behandlingId: String): Cookie {
            val xsrfCookie = Cookie(XSRF_TOKEN, XsrfGenerator.generateXsrfToken(behandlingId))
            xsrfCookie.path = "/"
            xsrfCookie.secure = true
            return xsrfCookie
        }

        private fun xsrfCookieMedBehandlingsid(behandlingId: String): Cookie {
            val xsrfCookie = Cookie("$XSRF_TOKEN-$behandlingId", XsrfGenerator.generateXsrfToken(behandlingId))
            xsrfCookie.path = "/"
            xsrfCookie.secure = true
            return xsrfCookie
        }
    }
}
