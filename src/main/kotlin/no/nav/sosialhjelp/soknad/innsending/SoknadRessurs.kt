package no.nav.sosialhjelp.soknad.innsending

import no.nav.sbl.soknadsosialhjelp.json.SoknadJsonTyper.BOSTOTTE_SAMTYKKE
import no.nav.sbl.soknadsosialhjelp.json.SoknadJsonTyper.UTBETALING_SKATTEETATEN_SAMTYKKE
import no.nav.sbl.soknadsosialhjelp.soknad.okonomi.opplysning.JsonOkonomibekreftelse
import no.nav.security.token.support.core.api.ProtectedWithClaims
import no.nav.sosialhjelp.metrics.aspects.Timed
import no.nav.sosialhjelp.soknad.api.nedetid.NedetidService
import no.nav.sosialhjelp.soknad.business.db.repositories.soknadunderarbeid.SoknadUnderArbeidRepository
import no.nav.sosialhjelp.soknad.common.Constants
import no.nav.sosialhjelp.soknad.common.exceptions.SoknadenHarNedetidException
import no.nav.sosialhjelp.soknad.common.subjecthandler.SubjectHandlerUtils
import no.nav.sosialhjelp.soknad.common.systemdata.SystemdataUpdater
import no.nav.sosialhjelp.soknad.domain.SoknadUnderArbeid
import no.nav.sosialhjelp.soknad.innsending.dto.BekreftelseRessurs
import no.nav.sosialhjelp.soknad.innsending.soknadunderarbeid.SoknadUnderArbeidService
import no.nav.sosialhjelp.soknad.tilgangskontroll.Tilgangskontroll
import no.nav.sosialhjelp.soknad.tilgangskontroll.XsrfGenerator
import org.slf4j.LoggerFactory
import org.springframework.http.HttpHeaders
import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.RequestBody
import javax.servlet.http.Cookie
import javax.servlet.http.HttpServletResponse
import javax.ws.rs.Consumes
import javax.ws.rs.DELETE
import javax.ws.rs.GET
import javax.ws.rs.HeaderParam
import javax.ws.rs.POST
import javax.ws.rs.Path
import javax.ws.rs.PathParam
import javax.ws.rs.Produces
import javax.ws.rs.QueryParam
import javax.ws.rs.core.Context
import javax.ws.rs.core.MediaType

@Controller
@ProtectedWithClaims(issuer = Constants.SELVBETJENING, claimMap = [Constants.CLAIM_ACR_LEVEL_4])
@Path("/soknader")
@Timed
@Produces(MediaType.APPLICATION_JSON)
open class SoknadRessurs(
    private val soknadService: SoknadService,
    private val soknadUnderArbeidService: SoknadUnderArbeidService,
    private val soknadUnderArbeidRepository: SoknadUnderArbeidRepository,
    private val systemdata: SystemdataUpdater,
    private val tilgangskontroll: Tilgangskontroll,
    private val henvendelseService: HenvendelseService,
    private val nedetidService: NedetidService
) {
    @GET
    @Path("/{behandlingsId}/xsrfCookie")
    open fun hentXsrfCookie(
        @PathParam("behandlingsId") behandlingsId: String,
        @Context response: HttpServletResponse
    ): Boolean {
        tilgangskontroll.verifiserBrukerHarTilgangTilSoknad(behandlingsId)
        response.addCookie(xsrfCookie(behandlingsId))
        response.addCookie(xsrfCookieMedBehandlingsid(behandlingsId))
        henvendelseService.oppdaterSistEndretDatoPaaMetadata(behandlingsId)
        return true
    }

    @GET
    @Path("/{behandlingsId}/erSystemdataEndret")
    open fun sjekkOmSystemdataErEndret(
        @PathParam("behandlingsId") behandlingsId: String,
        @HeaderParam(value = HttpHeaders.AUTHORIZATION) token: String?
    ): Boolean {
        tilgangskontroll.verifiserAtBrukerHarTilgang()
        val eier = SubjectHandlerUtils.getUserIdFromToken()
        val soknadUnderArbeid = soknadUnderArbeidRepository.hentSoknad(behandlingsId, eier)
        systemdata.update(soknadUnderArbeid)

        val updatedJsonInternalSoknad = soknadUnderArbeid.jsonInternalSoknad
        val notUpdatedSoknadUnderArbeid = soknadUnderArbeidRepository.hentSoknad(behandlingsId, eier)
        val notUpdatedJsonInternalSoknad = notUpdatedSoknadUnderArbeid.jsonInternalSoknad

        soknadUnderArbeidService.sortOkonomi(soknadUnderArbeid.jsonInternalSoknad.soknad.data.okonomi)
        soknadUnderArbeidService.sortOkonomi(notUpdatedSoknadUnderArbeid.jsonInternalSoknad.soknad.data.okonomi)
        soknadUnderArbeidService.sortArbeid(soknadUnderArbeid.jsonInternalSoknad.soknad.data.arbeid)
        soknadUnderArbeidService.sortArbeid(notUpdatedSoknadUnderArbeid.jsonInternalSoknad.soknad.data.arbeid)

        return if (updatedJsonInternalSoknad == notUpdatedJsonInternalSoknad) {
            false
        } else {
            soknadUnderArbeidRepository.oppdaterSoknadsdata(soknadUnderArbeid, eier)
            true
        }
    }

    @POST
    @Path("/{behandlingsId}/oppdaterSamtykker")
    open fun oppdaterSamtykker(
        @PathParam("behandlingsId") behandlingsId: String,
        @RequestBody samtykker: List<BekreftelseRessurs>,
        @HeaderParam(value = HttpHeaders.AUTHORIZATION) token: String?
    ) {
        tilgangskontroll.verifiserAtBrukerHarTilgang()
        val harBostotteSamtykke = samtykker
            .any { it.type.equals(BOSTOTTE_SAMTYKKE, ignoreCase = true) && it.verdi == true }
        val harSkatteetatenSamtykke = samtykker
            .any { it.type.equals(UTBETALING_SKATTEETATEN_SAMTYKKE, ignoreCase = true) && it.verdi == true }
        soknadService.oppdaterSamtykker(behandlingsId, harBostotteSamtykke, harSkatteetatenSamtykke, token)
    }

    @GET
    @Path("/{behandlingsId}/hentSamtykker")
    open fun hentSamtykker(
        @PathParam("behandlingsId") behandlingsId: String,
        @HeaderParam(value = HttpHeaders.AUTHORIZATION) token: String?
    ): List<BekreftelseRessurs> {
        tilgangskontroll.verifiserAtBrukerHarTilgang()
        val eier = SubjectHandlerUtils.getUserIdFromToken()
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
        val bekreftelser = soknadUnderArbeid.jsonInternalSoknad.soknad.data.okonomi.opplysninger.bekreftelse
        return bekreftelser
            .firstOrNull { it.type.equals(samtykke, ignoreCase = true) }
    }

    @POST
    @Path("/opprettSoknad")
    @Consumes(MediaType.APPLICATION_JSON)
    open fun opprettSoknad(
        @QueryParam("ettersendTil") behandlingsId: String?,
        @Context response: HttpServletResponse,
        @HeaderParam(value = HttpHeaders.AUTHORIZATION) token: String?
    ): Map<String, String> {
        if (nedetidService.isInnenforNedetid) {
            throw SoknadenHarNedetidException(
                "Soknaden har nedetid fram til ${nedetidService.nedetidSluttAsString}"
            )
        }
        if (behandlingsId == null) {
            tilgangskontroll.verifiserAtBrukerHarTilgang()
        } else {
            tilgangskontroll.verifiserAtBrukerKanEndreSoknad(behandlingsId)
        }
        val result: MutableMap<String, String> = HashMap()
        val opprettetBehandlingsId: String = if (behandlingsId == null) {
            soknadService.startSoknad(token)
        } else {
            val eier = SubjectHandlerUtils.getUserIdFromToken()
            val soknadUnderArbeid = soknadUnderArbeidRepository.hentEttersendingMedTilknyttetBehandlingsId(behandlingsId, eier)
            if (soknadUnderArbeid.isPresent) {
                soknadUnderArbeid.get().behandlingsId
            } else {
                soknadService.startEttersending(behandlingsId)
            }
        }
        result["brukerBehandlingId"] = opprettetBehandlingsId
        response.addCookie(xsrfCookie(opprettetBehandlingsId))
        response.addCookie(xsrfCookieMedBehandlingsid(opprettetBehandlingsId))
        return result
    }

    @DELETE
    @Path("/{behandlingsId}")
    open fun slettSoknad(@PathParam("behandlingsId") behandlingsId: String) {
        tilgangskontroll.verifiserAtBrukerKanEndreSoknad(behandlingsId)
        soknadService.avbrytSoknad(behandlingsId)
    }

    companion object {
        const val XSRF_TOKEN = "XSRF-TOKEN-SOKNAD-API"
        private val log = LoggerFactory.getLogger(SoknadRessurs::class.java)
        private fun xsrfCookie(behandlingId: String): Cookie {
            val xsrfCookie = Cookie(XSRF_TOKEN, XsrfGenerator.generateXsrfToken(behandlingId))
            xsrfCookie.path = "/"
            xsrfCookie.secure = true
            return xsrfCookie
        }

        private fun xsrfCookieMedBehandlingsid(behandlingId: String): Cookie {
            val xsrfCookie = Cookie(XSRF_TOKEN + "-" + behandlingId, XsrfGenerator.generateXsrfToken(behandlingId))
            xsrfCookie.path = "/"
            xsrfCookie.secure = true
            return xsrfCookie
        }
    }
}
