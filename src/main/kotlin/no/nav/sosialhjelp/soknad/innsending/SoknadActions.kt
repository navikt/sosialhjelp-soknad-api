package no.nav.sosialhjelp.soknad.innsending

import no.finn.unleash.Unleash
import no.nav.security.token.support.core.api.ProtectedWithClaims
import no.nav.sosialhjelp.metrics.aspects.Timed
import no.nav.sosialhjelp.soknad.api.nedetid.NedetidUtils
import no.nav.sosialhjelp.soknad.api.nedetid.NedetidUtils.NEDETID_SLUTT
import no.nav.sosialhjelp.soknad.api.nedetid.NedetidUtils.getNedetidAsStringOrNull
import no.nav.sosialhjelp.soknad.business.db.repositories.soknadmetadata.SoknadMetadataRepository
import no.nav.sosialhjelp.soknad.business.db.repositories.soknadunderarbeid.SoknadUnderArbeidRepository
import no.nav.sosialhjelp.soknad.business.exceptions.SendingTilKommuneErIkkeAktivertException
import no.nav.sosialhjelp.soknad.business.exceptions.SendingTilKommuneErMidlertidigUtilgjengeligException
import no.nav.sosialhjelp.soknad.business.exceptions.SendingTilKommuneUtilgjengeligException
import no.nav.sosialhjelp.soknad.business.exceptions.SoknadenHarNedetidException
import no.nav.sosialhjelp.soknad.client.fiks.kommuneinfo.KommuneInfoService
import no.nav.sosialhjelp.soknad.client.fiks.kommuneinfo.KommuneStatus
import no.nav.sosialhjelp.soknad.client.fiks.kommuneinfo.KommuneStatus.HAR_KONFIGURASJON_MEN_SKAL_SENDE_VIA_SVARUT
import no.nav.sosialhjelp.soknad.client.fiks.kommuneinfo.KommuneStatus.MANGLER_KONFIGURASJON
import no.nav.sosialhjelp.soknad.client.fiks.kommuneinfo.KommuneStatus.SKAL_SENDE_SOKNADER_OG_ETTERSENDELSER_VIA_FDA
import no.nav.sosialhjelp.soknad.client.fiks.kommuneinfo.KommuneStatus.SKAL_VISE_MIDLERTIDIG_FEILSIDE_FOR_SOKNAD_OG_ETTERSENDELSER
import no.nav.sosialhjelp.soknad.common.subjecthandler.SubjectHandlerUtils
import no.nav.sosialhjelp.soknad.domain.SoknadMetadataInnsendingStatus.SENDT_MED_DIGISOS_API
import no.nav.sosialhjelp.soknad.domain.SoknadUnderArbeid
import no.nav.sosialhjelp.soknad.domain.model.mock.MockUtils.isAlltidSendTilNavTestkommune
import no.nav.sosialhjelp.soknad.domain.model.util.KommuneTilNavEnhetMapper
import no.nav.sosialhjelp.soknad.domain.model.util.ServiceUtils.isNonProduction
import no.nav.sosialhjelp.soknad.domain.model.util.ServiceUtils.isSendingTilFiksEnabled
import no.nav.sosialhjelp.soknad.innsending.JsonVedleggUtils.FEATURE_UTVIDE_VEDLEGGJSON
import no.nav.sosialhjelp.soknad.innsending.JsonVedleggUtils.addHendelseTypeAndHendelseReferanse
import no.nav.sosialhjelp.soknad.innsending.digisosapi.DigisosApiService
import no.nav.sosialhjelp.soknad.innsending.dto.SendTilUrlFrontend
import no.nav.sosialhjelp.soknad.tilgangskontroll.Tilgangskontroll
import no.nav.sosialhjelp.soknad.web.utils.Constants
import org.slf4j.LoggerFactory
import org.springframework.http.HttpHeaders
import org.springframework.stereotype.Controller
import javax.servlet.ServletContext
import javax.ws.rs.HeaderParam
import javax.ws.rs.POST
import javax.ws.rs.Path
import javax.ws.rs.PathParam
import javax.ws.rs.Produces
import javax.ws.rs.core.Context
import javax.ws.rs.core.MediaType

@Controller
@ProtectedWithClaims(issuer = Constants.SELVBETJENING, claimMap = [Constants.CLAIM_ACR_LEVEL_4])
@Path("/soknader/{behandlingsId}/actions")
@Produces(MediaType.APPLICATION_JSON)
@Timed(name = "SoknadActionsRessurs")
open class SoknadActions(
    private val soknadService: SoknadService,
    private val kommuneInfoService: KommuneInfoService,
    private val tilgangskontroll: Tilgangskontroll,
    private val soknadUnderArbeidRepository: SoknadUnderArbeidRepository,
    private val soknadMetadataRepository: SoknadMetadataRepository,
    private val digisosApiService: DigisosApiService,
    private val unleash: Unleash
) {
    @POST
    @Path("/send")
    open fun sendSoknad(
        @PathParam("behandlingsId") behandlingsId: String,
        @Context servletContext: ServletContext?,
        @HeaderParam(value = HttpHeaders.AUTHORIZATION) token: String?
    ): SendTilUrlFrontend {
        if (NedetidUtils.isInnenforNedetid) {
            throw SoknadenHarNedetidException("Soknaden har nedetid fram til ${getNedetidAsStringOrNull(NEDETID_SLUTT)}")
        }

        tilgangskontroll.verifiserAtBrukerKanEndreSoknad(behandlingsId)
        val eier = SubjectHandlerUtils.getUserIdFromToken()

        val soknadUnderArbeid = soknadUnderArbeidRepository.hentSoknad(behandlingsId, eier)
            ?: throw IllegalStateException("SoknadUnderArbeid er null ved sending for behandlingsId $behandlingsId")

        updateVedleggJsonWithHendelseTypeAndHendelseReferanse(eier, soknadUnderArbeid)

        if (!isSendingTilFiksEnabled() || isEttersendelsePaSoknadSendtViaSvarUt(soknadUnderArbeid)) {
            log.info("BehandlingsId $behandlingsId sendes til SvarUt.")
            soknadService.sendSoknad(behandlingsId)
            return SendTilUrlFrontend(SVARUT, behandlingsId)
        }

        if (soknadUnderArbeid.erEttersendelse()) {
            log.error("Ettersendelse $behandlingsId blir forsøkt sendt med soknad-api selv om den tiknyttede søknaden ble sendt til Fiks-Digisos-api. Dette skal ikke skje, disse skal sendes via innsyn-api.")
            throw IllegalStateException("Ettersendelse på søknad sendt via fiks-digisos-api skal sendes via innsyn-api")
        }

        log.info("BehandlingsId {} sendes til SvarUt eller fiks-digisos-api avhengig av kommuneinfo.", behandlingsId)
        val kommunenummer = getKommunenummerOrMock(soknadUnderArbeid)
        val kommuneStatus = kommuneInfoService.kommuneInfo(kommunenummer)
        log.info("Kommune: $kommunenummer Status: $kommuneStatus")

        return when (kommuneStatus) {
            KommuneStatus.FIKS_NEDETID_OG_TOM_CACHE -> {
                throw SendingTilKommuneUtilgjengeligException("Sending til kommune $kommunenummer er ikke tilgjengelig fordi fiks har nedetid og kommuneinfo-cache er tom.")
            }
            MANGLER_KONFIGURASJON, HAR_KONFIGURASJON_MEN_SKAL_SENDE_VIA_SVARUT -> {
                if (!KommuneTilNavEnhetMapper.getDigisoskommuner().contains(kommunenummer)) {
                    throw SendingTilKommuneErIkkeAktivertException("Sending til kommune $kommunenummer er ikke aktivert og kommunen er ikke i listen over svarUt-kommuner")
                }
                log.info("BehandlingsId $behandlingsId sendes til SvarUt (sfa. Fiks-konfigurasjon).")
                soknadService.sendSoknad(behandlingsId)
                SendTilUrlFrontend(SVARUT, behandlingsId)
            }
            SKAL_SENDE_SOKNADER_OG_ETTERSENDELSER_VIA_FDA -> {
                log.info("BehandlingsId $behandlingsId sendes til Fiks-digisos-api (sfa. Fiks-konfigurasjon).")
                val digisosId = digisosApiService.sendSoknad(soknadUnderArbeid, token, kommunenummer)
                SendTilUrlFrontend(FIKS_DIGISOS_API, digisosId)
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
        val jsonVedleggSpesifikasjon = soknadUnderArbeid.jsonInternalSoknad.vedlegg
        val isUtvideVedleggJsonFeatureActive = unleash.isEnabled(FEATURE_UTVIDE_VEDLEGGJSON, false)
        addHendelseTypeAndHendelseReferanse(
            jsonVedleggSpesifikasjon,
            !soknadUnderArbeid.erEttersendelse(),
            isUtvideVedleggJsonFeatureActive
        )
        soknadUnderArbeidRepository.oppdaterSoknadsdata(soknadUnderArbeid, eier)
    }

    fun getKommunenummerOrMock(soknadUnderArbeid: SoknadUnderArbeid): String {
        return if (isNonProduction() && isAlltidSendTilNavTestkommune()) {
            log.error("Sender til Nav-testkommune (3002). Du skal aldri se denne meldingen i PROD")
            "3002"
        } else {
            soknadUnderArbeid.jsonInternalSoknad.soknad.mottaker.kommunenummer
        }
    }

    private fun isEttersendelsePaSoknadSendtViaSvarUt(soknadUnderArbeid: SoknadUnderArbeid): Boolean {
        if (!soknadUnderArbeid.erEttersendelse()) return false
        val soknadensMetadata = soknadMetadataRepository.hent(soknadUnderArbeid.tilknyttetBehandlingsId)
        return soknadensMetadata != null && soknadensMetadata.status != SENDT_MED_DIGISOS_API
    }

    companion object {
        private val log = LoggerFactory.getLogger(SoknadActions::class.java)
        private const val SVARUT = "SVARUT"
        private const val FIKS_DIGISOS_API = "FIKS_DIGISOS_API"
    }
}
