package no.nav.sosialhjelp.soknad.innsending

import no.finn.unleash.Unleash
import no.nav.security.token.support.core.api.ProtectedWithClaims
import no.nav.sosialhjelp.soknad.api.nedetid.NedetidService
import no.nav.sosialhjelp.soknad.app.Constants
import no.nav.sosialhjelp.soknad.app.exceptions.SendingTilKommuneErIkkeAktivertException
import no.nav.sosialhjelp.soknad.app.exceptions.SendingTilKommuneErMidlertidigUtilgjengeligException
import no.nav.sosialhjelp.soknad.app.exceptions.SendingTilKommuneUtilgjengeligException
import no.nav.sosialhjelp.soknad.app.exceptions.SoknadenHarNedetidException
import no.nav.sosialhjelp.soknad.app.mapper.KommuneTilNavEnhetMapper
import no.nav.sosialhjelp.soknad.app.subjecthandler.SubjectHandlerUtils
import no.nav.sosialhjelp.soknad.db.repositories.soknadmetadata.SoknadMetadataInnsendingStatus.SENDT_MED_DIGISOS_API
import no.nav.sosialhjelp.soknad.db.repositories.soknadmetadata.SoknadMetadataRepository
import no.nav.sosialhjelp.soknad.db.repositories.soknadunderarbeid.SoknadUnderArbeid
import no.nav.sosialhjelp.soknad.db.repositories.soknadunderarbeid.SoknadUnderArbeidRepository
import no.nav.sosialhjelp.soknad.innsending.JsonVedleggUtils.FEATURE_UTVIDE_VEDLEGGJSON
import no.nav.sosialhjelp.soknad.innsending.JsonVedleggUtils.addHendelseTypeAndHendelseReferanse
import no.nav.sosialhjelp.soknad.innsending.SenderUtils.INNSENDING_DIGISOSAPI_ENABLED
import no.nav.sosialhjelp.soknad.innsending.digisosapi.DigisosApiService
import no.nav.sosialhjelp.soknad.innsending.digisosapi.kommuneinfo.KommuneInfoService
import no.nav.sosialhjelp.soknad.innsending.digisosapi.kommuneinfo.KommuneStatus
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
@ProtectedWithClaims(issuer = Constants.SELVBETJENING, claimMap = [Constants.CLAIM_ACR_LEVEL_4])
@RequestMapping("/soknader/{behandlingsId}/actions", produces = [MediaType.APPLICATION_JSON_VALUE])
open class SoknadActions(
    private val soknadService: SoknadService,
    private val kommuneInfoService: KommuneInfoService,
    private val tilgangskontroll: Tilgangskontroll,
    private val soknadUnderArbeidRepository: SoknadUnderArbeidRepository,
    private val soknadMetadataRepository: SoknadMetadataRepository,
    private val digisosApiService: DigisosApiService,
    private val unleash: Unleash,
    private val nedetidService: NedetidService,
) {
    @PostMapping("/send")
    open fun sendSoknad(
        @PathVariable("behandlingsId") behandlingsId: String,
        @RequestHeader(value = HttpHeaders.AUTHORIZATION) token: String?
    ): SendTilUrlFrontend {
        if (nedetidService.isInnenforNedetid) {
            throw SoknadenHarNedetidException("Soknaden har nedetid fram til ${nedetidService.nedetidSluttAsString}")
        }

        tilgangskontroll.verifiserAtBrukerKanEndreSoknad(behandlingsId)
        val eier = SubjectHandlerUtils.getUserIdFromToken()

        val soknadUnderArbeid = soknadUnderArbeidRepository.hentSoknad(behandlingsId, eier)

        updateVedleggJsonWithHendelseTypeAndHendelseReferanse(eier, soknadUnderArbeid)

        if (!unleash.isEnabled(INNSENDING_DIGISOSAPI_ENABLED, true) || isEttersendelsePaSoknadSendtViaSvarUt(
                soknadUnderArbeid
            )
        ) {
            log.info("BehandlingsId $behandlingsId sendes til SvarUt.")
            soknadService.sendSoknad(behandlingsId)
            return SendTilUrlFrontend(SoknadMottakerFrontend.SVARUT, behandlingsId)
        }

        if (soknadUnderArbeid.erEttersendelse) {
            log.error("Ettersendelse $behandlingsId blir forsøkt sendt med soknad-api selv om den tiknyttede søknaden ble sendt til Fiks-Digisos-api. Dette skal ikke skje, disse skal sendes via innsyn-api.")
            throw IllegalStateException("Ettersendelse på søknad sendt via fiks-digisos-api skal sendes via innsyn-api")
        }

        log.info("BehandlingsId $behandlingsId sendes til SvarUt eller fiks-digisos-api avhengig av kommuneinfo.")
        val kommunenummer = soknadUnderArbeid.jsonInternalSoknad?.soknad?.mottaker?.kommunenummer
            ?: throw IllegalStateException("Kommunenummer ikke funnet for JsonInternalSoknad.soknad.mottaker.kommunenummer")
        val kommuneStatus = kommuneInfoService.kommuneInfo(kommunenummer)
        log.info("Kommune: $kommunenummer Status: $kommuneStatus")

        return when (kommuneStatus) {
            KommuneStatus.FIKS_NEDETID_OG_TOM_CACHE -> {
                throw SendingTilKommuneUtilgjengeligException("Sending til kommune $kommunenummer er ikke tilgjengelig fordi fiks har nedetid og kommuneinfo-cache er tom.")
            }

            MANGLER_KONFIGURASJON, HAR_KONFIGURASJON_MEN_SKAL_SENDE_VIA_SVARUT -> {
                if (!KommuneTilNavEnhetMapper.digisoskommuner.contains(kommunenummer)) {
                    throw SendingTilKommuneErIkkeAktivertException("Sending til kommune $kommunenummer er ikke aktivert og kommunen er ikke i listen over svarUt-kommuner")
                }
                log.info("BehandlingsId $behandlingsId sendes til SvarUt (sfa. Fiks-konfigurasjon).")
                soknadService.sendSoknad(behandlingsId)
                SendTilUrlFrontend(SoknadMottakerFrontend.SVARUT, behandlingsId)
            }

            SKAL_SENDE_SOKNADER_OG_ETTERSENDELSER_VIA_FDA -> {
                log.info("BehandlingsId $behandlingsId sendes til Fiks-digisos-api (sfa. Fiks-konfigurasjon).")
                val digisosId = digisosApiService.sendSoknad(soknadUnderArbeid, token, kommunenummer)
                SendTilUrlFrontend(SoknadMottakerFrontend.FIKS_DIGISOS_API, digisosId)
            }

            SKAL_VISE_MIDLERTIDIG_FEILSIDE_FOR_SOKNAD_OG_ETTERSENDELSER -> {
                throw SendingTilKommuneErMidlertidigUtilgjengeligException("Sending til kommune $kommunenummer er midlertidig utilgjengelig.")
            }
        }
    }

    private fun updateVedleggJsonWithHendelseTypeAndHendelseReferanse(
        eier: String,
        soknadUnderArbeid: SoknadUnderArbeid
    ) {
        val jsonVedleggSpesifikasjon = soknadUnderArbeid.jsonInternalSoknad?.vedlegg ?: return

        val isUtvideVedleggJsonFeatureActive = unleash.isEnabled(FEATURE_UTVIDE_VEDLEGGJSON, false)
        addHendelseTypeAndHendelseReferanse(
            jsonVedleggSpesifikasjon,
            !soknadUnderArbeid.erEttersendelse,
            isUtvideVedleggJsonFeatureActive
        )
        soknadUnderArbeidRepository.oppdaterSoknadsdata(soknadUnderArbeid, eier)
    }

    private fun isEttersendelsePaSoknadSendtViaSvarUt(soknadUnderArbeid: SoknadUnderArbeid): Boolean {
        if (!soknadUnderArbeid.erEttersendelse) return false
        val soknadensMetadata = soknadMetadataRepository.hent(soknadUnderArbeid.tilknyttetBehandlingsId)
        return soknadensMetadata != null && soknadensMetadata.status != SENDT_MED_DIGISOS_API
    }

    companion object {
        private val log = LoggerFactory.getLogger(SoknadActions::class.java)
    }
}
