package no.nav.sosialhjelp.soknad.innsending

import no.nav.security.token.support.core.api.ProtectedWithClaims
import no.nav.sosialhjelp.soknad.api.nedetid.NedetidService
import no.nav.sosialhjelp.soknad.app.Constants
import no.nav.sosialhjelp.soknad.app.exceptions.SendingTilKommuneErMidlertidigUtilgjengeligException
import no.nav.sosialhjelp.soknad.app.exceptions.SendingTilKommuneUtilgjengeligException
import no.nav.sosialhjelp.soknad.app.exceptions.SoknadenHarNedetidException
import no.nav.sosialhjelp.soknad.app.subjecthandler.SubjectHandlerUtils
import no.nav.sosialhjelp.soknad.db.repositories.soknadmetadata.SoknadMetadataRepository
import no.nav.sosialhjelp.soknad.db.repositories.soknadunderarbeid.SoknadUnderArbeid
import no.nav.sosialhjelp.soknad.db.repositories.soknadunderarbeid.SoknadUnderArbeidRepository
import no.nav.sosialhjelp.soknad.innsending.JsonVedleggUtils.addHendelseTypeAndHendelseReferanse
import no.nav.sosialhjelp.soknad.innsending.digisosapi.DigisosApiService
import no.nav.sosialhjelp.soknad.innsending.digisosapi.kommuneinfo.KommuneInfoService
import no.nav.sosialhjelp.soknad.innsending.digisosapi.kommuneinfo.KommuneStatus.FIKS_NEDETID_OG_TOM_CACHE
import no.nav.sosialhjelp.soknad.innsending.digisosapi.kommuneinfo.KommuneStatus.HAR_KONFIGURASJON_MEN_SKAL_SENDE_VIA_SVARUT
import no.nav.sosialhjelp.soknad.innsending.digisosapi.kommuneinfo.KommuneStatus.MANGLER_KONFIGURASJON
import no.nav.sosialhjelp.soknad.innsending.digisosapi.kommuneinfo.KommuneStatus.SKAL_SENDE_SOKNADER_OG_ETTERSENDELSER_VIA_FDA
import no.nav.sosialhjelp.soknad.innsending.digisosapi.kommuneinfo.KommuneStatus.SKAL_VISE_MIDLERTIDIG_FEILSIDE_FOR_SOKNAD_OG_ETTERSENDELSER
import no.nav.sosialhjelp.soknad.innsending.dto.SendTilUrlFrontend
import no.nav.sosialhjelp.soknad.innsending.dto.SoknadMottakerFrontend
import no.nav.sosialhjelp.soknad.tilgangskontroll.Tilgangskontroll
import org.slf4j.LoggerFactory
import org.springframework.http.HttpHeaders
import org.springframework.http.MediaType
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestHeader
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@ProtectedWithClaims(
    issuer = Constants.SELVBETJENING,
    claimMap = [Constants.CLAIM_ACR_LEVEL_4, Constants.CLAIM_ACR_LOA_HIGH],
    combineWithOr = true,
)
@RequestMapping("/soknader/{behandlingsId}/actions", produces = [MediaType.APPLICATION_JSON_VALUE])
class SoknadActions(
    private val soknadServiceOld: SoknadServiceOld,
    private val kommuneInfoService: KommuneInfoService,
    private val tilgangskontroll: Tilgangskontroll,
    private val soknadUnderArbeidRepository: SoknadUnderArbeidRepository,
    private val soknadMetadataRepository: SoknadMetadataRepository,
    private val digisosApiService: DigisosApiService,
    private val nedetidService: NedetidService,
) {
    @PostMapping("/send")
    fun sendSoknad(
        @PathVariable("behandlingsId") behandlingsId: String,
        @RequestHeader(value = HttpHeaders.AUTHORIZATION) token: String?,
    ): SendTilUrlFrontend {
        if (nedetidService.isInnenforNedetid) {
            throw SoknadenHarNedetidException("Soknaden har nedetid fram til ${nedetidService.nedetidSluttAsString}")
        }

        tilgangskontroll.verifiserAtBrukerKanEndreSoknad(behandlingsId)
        val eier = SubjectHandlerUtils.getUserIdFromToken()

        val soknadUnderArbeid = soknadUnderArbeidRepository.hentSoknad(behandlingsId, eier)

        updateVedleggJsonWithHendelseTypeAndHendelseReferanse(eier, soknadUnderArbeid)

        // TODO Returnerer alltid false
        if (isEttersendelsePaSoknadSendtViaSvarUt(soknadUnderArbeid)) {
            error("Ettersendelse på søknad sendt via SvarUt - skal aldri forekomme")
        }

        // TODO Returnerer alltid false
        if (soknadUnderArbeid.erEttersendelse) {
            error("Ettersendelse - skal aldri forekomme")
        }

        log.info("BehandlingsId $behandlingsId sendes til SvarUt eller fiks-digisos-api avhengig av kommuneinfo.")
        val kommunenummer =
            soknadUnderArbeid.jsonInternalSoknad?.soknad?.mottaker?.kommunenummer
                ?: throw IllegalStateException("Kommunenummer ikke funnet for JsonInternalSoknad.soknad.mottaker.kommunenummer")
        val kommuneStatus = kommuneInfoService.getKommuneStatus(kommunenummer = kommunenummer, withLogging = true)
        log.info("Kommune: $kommunenummer Status: $kommuneStatus")

        return when (kommuneStatus) {
            FIKS_NEDETID_OG_TOM_CACHE ->
                throw SendingTilKommuneUtilgjengeligException(
                    "Sending til kommune $kommunenummer er ikke tilgjengelig fordi fiks har nedetid og kommuneinfo-cache er tom.",
                )
            MANGLER_KONFIGURASJON, HAR_KONFIGURASJON_MEN_SKAL_SENDE_VIA_SVARUT ->
                throw SendingTilKommuneUtilgjengeligException("Manglende eller feil konfigurasjon. (SvarUt)")
            SKAL_VISE_MIDLERTIDIG_FEILSIDE_FOR_SOKNAD_OG_ETTERSENDELSER ->
                throw SendingTilKommuneErMidlertidigUtilgjengeligException(
                    "Sending til kommune $kommunenummer er midlertidig utilgjengelig.",
                )
            SKAL_SENDE_SOKNADER_OG_ETTERSENDELSER_VIA_FDA -> {
                log.info("BehandlingsId $behandlingsId sendes til Fiks-digisos-api (sfa. Fiks-konfigurasjon).")
                val digisosId = digisosApiService.sendSoknad(soknadUnderArbeid, token, kommunenummer)
                SendTilUrlFrontend(SoknadMottakerFrontend.FIKS_DIGISOS_API, digisosId)
            }
        }
    }

    private fun updateVedleggJsonWithHendelseTypeAndHendelseReferanse(
        eier: String,
        soknadUnderArbeid: SoknadUnderArbeid,
    ) {
        val jsonVedleggSpesifikasjon = soknadUnderArbeid.jsonInternalSoknad?.vedlegg ?: return

        addHendelseTypeAndHendelseReferanse(
            jsonVedleggSpesifikasjon = jsonVedleggSpesifikasjon,
            isSoknad = !soknadUnderArbeid.erEttersendelse,
        )
        soknadUnderArbeidRepository.oppdaterSoknadsdata(soknadUnderArbeid, eier)
    }

    @Deprecated("SvarUt støttes ikke")
    private fun isEttersendelsePaSoknadSendtViaSvarUt(soknadUnderArbeid: SoknadUnderArbeid): Boolean {
        return false
    }

    companion object {
        private val log = LoggerFactory.getLogger(SoknadActions::class.java)
    }
}
