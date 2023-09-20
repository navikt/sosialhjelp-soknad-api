package no.nav.sosialhjelp.soknad.innsending

import jakarta.servlet.http.Cookie
import jakarta.servlet.http.HttpServletResponse
import no.nav.sbl.soknadsosialhjelp.json.SoknadJsonTyper.BOSTOTTE_SAMTYKKE
import no.nav.sbl.soknadsosialhjelp.json.SoknadJsonTyper.UTBETALING_SKATTEETATEN_SAMTYKKE
import no.nav.sbl.soknadsosialhjelp.soknad.okonomi.opplysning.JsonOkonomibekreftelse
import no.nav.security.token.support.core.api.ProtectedWithClaims
import no.nav.sosialhjelp.soknad.api.nedetid.NedetidService
import no.nav.sosialhjelp.soknad.app.Constants
import no.nav.sosialhjelp.soknad.app.exceptions.SoknadenHarNedetidException
import no.nav.sosialhjelp.soknad.app.subjecthandler.SubjectHandlerUtils.getUserIdFromToken
import no.nav.sosialhjelp.soknad.app.systemdata.SystemdataUpdater
import no.nav.sosialhjelp.soknad.db.repositories.soknadunderarbeid.SoknadUnderArbeid
import no.nav.sosialhjelp.soknad.db.repositories.soknadunderarbeid.SoknadUnderArbeidRepository
import no.nav.sosialhjelp.soknad.ettersending.EttersendingService
import no.nav.sosialhjelp.soknad.innsending.dto.BekreftelseRessurs
import no.nav.sosialhjelp.soknad.innsending.soknadunderarbeid.SoknadUnderArbeidService
import no.nav.sosialhjelp.soknad.tilgangskontroll.Tilgangskontroll
import no.nav.sosialhjelp.soknad.tilgangskontroll.XsrfGenerator
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
@ProtectedWithClaims(issuer = Constants.SELVBETJENING, claimMap = [Constants.CLAIM_ACR_LEVEL_4, Constants.CLAIM_ACR_LOA_HIGH], combineWithOr = true)
@RequestMapping("/soknader", produces = [MediaType.APPLICATION_JSON_VALUE])
class SoknadRessurs(
    private val soknadService: SoknadService,
    private val ettersendingService: EttersendingService,
    private val soknadUnderArbeidService: SoknadUnderArbeidService,
    private val soknadUnderArbeidRepository: SoknadUnderArbeidRepository,
    private val systemdata: SystemdataUpdater,
    private val tilgangskontroll: Tilgangskontroll,
    private val nedetidService: NedetidService
) {
    @GetMapping("/{behandlingsId}/xsrfCookie")
    fun hentXsrfCookie(
        @PathVariable("behandlingsId") behandlingsId: String,
        response: HttpServletResponse
    ): Boolean {
        tilgangskontroll.verifiserBrukerForSoknad(behandlingsId)
        response.addCookie(xsrfCookie(behandlingsId))
        response.addCookie(xsrfCookieMedBehandlingsid(behandlingsId))
        soknadService.oppdaterSistEndretDatoPaaMetadata(behandlingsId)
        return true
    }

    @GetMapping("/{behandlingsId}/erSystemdataEndret")
    fun sjekkOmSystemdataErEndret(
        @PathVariable("behandlingsId") behandlingsId: String,
        @RequestHeader(value = HttpHeaders.AUTHORIZATION) token: String?
    ): Boolean {
        val eier = tilgangskontroll.verifiserBrukerForSoknad(behandlingsId)
        val soknadUnderArbeid = soknadUnderArbeidRepository.hentSoknad(behandlingsId, eier)
        systemdata.update(soknadUnderArbeid)

        val updatedJsonInternalSoknad = soknadUnderArbeid.jsonInternalSoknad
        val notUpdatedSoknadUnderArbeid = soknadUnderArbeidRepository.hentSoknad(behandlingsId, eier)
        val notUpdatedJsonInternalSoknad = notUpdatedSoknadUnderArbeid.jsonInternalSoknad

        soknadUnderArbeid.jsonInternalSoknad?.soknad?.data
            ?.let { soknadUnderArbeidService.sortOkonomi(it.okonomi) }
        notUpdatedSoknadUnderArbeid.jsonInternalSoknad?.soknad?.data
            ?.let { soknadUnderArbeidService.sortOkonomi(it.okonomi) }
        soknadUnderArbeid.jsonInternalSoknad?.soknad?.data
            ?.let { soknadUnderArbeidService.sortArbeid(it.arbeid) }
        notUpdatedSoknadUnderArbeid.jsonInternalSoknad?.soknad?.data
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
        @RequestHeader(value = HttpHeaders.AUTHORIZATION) token: String?
    ) {
        tilgangskontroll.verifiserAtBrukerHarTilgang()
        val harBostotteSamtykke = samtykker
            .any { it.type.equals(BOSTOTTE_SAMTYKKE, ignoreCase = true) && it.verdi == true }
        val harSkatteetatenSamtykke = samtykker
            .any { it.type.equals(UTBETALING_SKATTEETATEN_SAMTYKKE, ignoreCase = true) && it.verdi == true }
        soknadService.oppdaterSamtykker(behandlingsId, harBostotteSamtykke, harSkatteetatenSamtykke, token)
    }

    @GetMapping("/{behandlingsId}/hentSamtykker")
    fun hentSamtykker(
        @PathVariable("behandlingsId") behandlingsId: String,
        @RequestHeader(value = HttpHeaders.AUTHORIZATION) token: String?
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
        samtykke: String
    ): JsonOkonomibekreftelse? {
        val bekreftelser = soknadUnderArbeid.jsonInternalSoknad?.soknad?.data?.okonomi?.opplysninger?.bekreftelse
        return bekreftelser
            ?.firstOrNull { it.type.equals(samtykke, ignoreCase = true) }
    }

    @PostMapping("/opprettSoknad")
    fun opprettSoknad(
        @RequestParam("ettersendTil") tilknyttetBehandlingsId: String?,
        response: HttpServletResponse,
        @RequestHeader(value = HttpHeaders.AUTHORIZATION) token: String?
    ): Map<String, String> {
        if (nedetidService.isInnenforNedetid) {
            throw SoknadenHarNedetidException(
                "Soknaden har nedetid fram til ${nedetidService.nedetidSluttAsString}"
            )
        }
        if (tilknyttetBehandlingsId == null) {
            tilgangskontroll.verifiserAtBrukerHarTilgang()
        } else {
            tilgangskontroll.verifiserBrukerHarTilgangTilMetadata(tilknyttetBehandlingsId)
        }
        val result: MutableMap<String, String> = HashMap()
        val opprettetBehandlingsId: String = if (tilknyttetBehandlingsId == null) {
            soknadService.startSoknad(token)
        } else {
            val eier = getUserIdFromToken()
            soknadUnderArbeidRepository.hentEttersendingMedTilknyttetBehandlingsId(tilknyttetBehandlingsId, eier)
                ?.behandlingsId
                ?: ettersendingService.startEttersendelse(tilknyttetBehandlingsId)
        }
        result["brukerBehandlingId"] = opprettetBehandlingsId
        response.addCookie(xsrfCookie(opprettetBehandlingsId))
        response.addCookie(xsrfCookieMedBehandlingsid(opprettetBehandlingsId))
        return result
    }

    @DeleteMapping("/{behandlingsId}")
    fun slettSoknad(
        @PathVariable("behandlingsId") behandlingsId: String,
        @RequestHeader(value = HttpHeaders.REFERER) referer: String?
    ) {
        tilgangskontroll.verifiserBrukerForSoknad(behandlingsId)
        val steg: String = referer?.substringAfterLast(delimiter = "/", missingDelimiterValue = "ukjent") ?: "ukjent"
        soknadService.avbrytSoknad(behandlingsId, steg)
    }

    companion object {
        const val XSRF_TOKEN = "XSRF-TOKEN-SOKNAD-API"

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
