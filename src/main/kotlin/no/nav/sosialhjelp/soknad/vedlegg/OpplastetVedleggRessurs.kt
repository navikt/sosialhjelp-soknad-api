package no.nav.sosialhjelp.soknad.vedlegg

import no.finn.unleash.Unleash
import no.nav.security.token.support.core.api.ProtectedWithClaims
import no.nav.sosialhjelp.kotlin.utils.logger
import no.nav.sosialhjelp.metrics.aspects.Timed
import no.nav.sosialhjelp.soknad.common.Constants
import no.nav.sosialhjelp.soknad.common.exceptions.SendingTilKommuneErMidlertidigUtilgjengeligException
import no.nav.sosialhjelp.soknad.common.exceptions.SendingTilKommuneUtilgjengeligException
import no.nav.sosialhjelp.soknad.common.filedetection.FileDetectionUtils.getMimeType
import no.nav.sosialhjelp.soknad.common.filedetection.MimeTypes
import no.nav.sosialhjelp.soknad.common.subjecthandler.SubjectHandlerUtils
import no.nav.sosialhjelp.soknad.db.repositories.opplastetvedlegg.OpplastetVedleggRepository
import no.nav.sosialhjelp.soknad.db.repositories.soknadunderarbeid.SoknadUnderArbeidRepository
import no.nav.sosialhjelp.soknad.innsending.digisosapi.kommuneinfo.KommuneInfoService
import no.nav.sosialhjelp.soknad.innsending.digisosapi.kommuneinfo.KommuneStatus.FIKS_NEDETID_OG_TOM_CACHE
import no.nav.sosialhjelp.soknad.innsending.digisosapi.kommuneinfo.KommuneStatus.HAR_KONFIGURASJON_MEN_SKAL_SENDE_VIA_SVARUT
import no.nav.sosialhjelp.soknad.innsending.digisosapi.kommuneinfo.KommuneStatus.MANGLER_KONFIGURASJON
import no.nav.sosialhjelp.soknad.innsending.digisosapi.kommuneinfo.KommuneStatus.SKAL_SENDE_SOKNADER_OG_ETTERSENDELSER_VIA_FDA
import no.nav.sosialhjelp.soknad.innsending.digisosapi.kommuneinfo.KommuneStatus.SKAL_VISE_MIDLERTIDIG_FEILSIDE_FOR_SOKNAD_OG_ETTERSENDELSER
import no.nav.sosialhjelp.soknad.tilgangskontroll.Tilgangskontroll
import no.nav.sosialhjelp.soknad.vedlegg.dto.FilFrontend
import no.nav.sosialhjelp.soknad.vedlegg.exceptions.OpplastingException
import no.nav.sosialhjelp.soknad.vedlegg.fiks.MellomlagringService
import org.apache.commons.io.IOUtils
import org.glassfish.jersey.media.multipart.FormDataBodyPart
import org.glassfish.jersey.media.multipart.FormDataParam
import org.springframework.http.HttpHeaders
import org.springframework.stereotype.Controller
import java.io.File
import java.io.IOException
import java.io.InputStream
import javax.servlet.http.HttpServletResponse
import javax.ws.rs.Consumes
import javax.ws.rs.DELETE
import javax.ws.rs.GET
import javax.ws.rs.HeaderParam
import javax.ws.rs.POST
import javax.ws.rs.Path
import javax.ws.rs.PathParam
import javax.ws.rs.Produces
import javax.ws.rs.core.Context
import javax.ws.rs.core.MediaType
import javax.ws.rs.core.Response

@Controller
@ProtectedWithClaims(issuer = Constants.SELVBETJENING, claimMap = [Constants.CLAIM_ACR_LEVEL_4])
@Path("/opplastetVedlegg")
@Produces(MediaType.APPLICATION_JSON)
@Timed
open class OpplastetVedleggRessurs(
    private val opplastetVedleggRepository: OpplastetVedleggRepository,
    private val opplastetVedleggService: OpplastetVedleggService,
    private val tilgangskontroll: Tilgangskontroll,
    private val soknadUnderArbeidRepository: SoknadUnderArbeidRepository,
    private val kommuneInfoService: KommuneInfoService,
    private val mellomlagringService: MellomlagringService,
    private val unleash: Unleash
) {

    @GET
    @Path("/{vedleggId}/fil")
    @Produces(MediaType.APPLICATION_JSON)
    open fun getVedleggFil(
        @PathParam("vedleggId") vedleggId: String,
        @HeaderParam(value = HttpHeaders.AUTHORIZATION) token: String?,
        @Context response: HttpServletResponse,
    ): Response {
        tilgangskontroll.verifiserAtBrukerHarTilgang()
        val eier = SubjectHandlerUtils.getUserIdFromToken()

        opplastetVedleggRepository.hentVedlegg(vedleggId, eier)?.let {
            response.setHeader("Content-Disposition", "attachment; filename=\"" + it.filnavn + "\"")
            val detectedMimeType = getMimeType(it.data)
            val mimetype = if (detectedMimeType.equals(MimeTypes.TEXT_X_MATLAB, ignoreCase = true)) MimeTypes.APPLICATION_PDF else detectedMimeType
            return Response.ok(it.data).type(mimetype).build()
        }

        if (mellomlagringEnabled && token != null) {
            log.info("Forsøker å hente vedlegg $vedleggId fra mellomlagring hos KS")
            mellomlagringService.getVedlegg(vedleggId, token)?.let {
                response.setHeader("Content-Disposition", "attachment; filename=\"" + it.filnavn + "\"")
                val detectedMimeType = getMimeType(it.data)
                val mimetype = if (detectedMimeType.equals(MimeTypes.TEXT_X_MATLAB, ignoreCase = true)) MimeTypes.APPLICATION_PDF else detectedMimeType
                return Response.ok(it.data).type(mimetype).build()
            }
        }
        // hvis vedleggId ikke finnes i DB eller KS mellomlagring
        return Response.noContent().build()
    }

    @POST
    @Path("/{behandlingsId}/{type}")
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    @Produces(MediaType.APPLICATION_JSON)
    open fun saveVedlegg(
        @PathParam("behandlingsId") behandlingsId: String,
        @PathParam("type") vedleggstype: String,
        @FormDataParam("file") fil: FormDataBodyPart,
        @HeaderParam(value = HttpHeaders.AUTHORIZATION) token: String?,
    ): FilFrontend {
        tilgangskontroll.verifiserAtBrukerKanEndreSoknad(behandlingsId)
        if (fil.getValueAs(File::class.java).length() > MAKS_TOTAL_FILSTORRELSE) {
            throw OpplastingException(
                "Kunne ikke lagre fil fordi total filstørrelse er for stor",
                null,
                "vedlegg.opplasting.feil.forStor"
            )
        }
        val filnavn = fil.contentDisposition.fileName
        val data = getByteArray(fil)
        val eier = SubjectHandlerUtils.getUserIdFromToken()

        // bruk KS mellomlagringstjeneste hvis featuren er enablet og søknad skal sendes med DigisosApi
        return if (soknadSkalSendesMedDigisosApi(behandlingsId, eier) && mellomlagringEnabled && token != null) {
            log.info("Forsøker å laste opp vedlegg til mellomlagring hos KS")
            val mellomlagretVedlegg = mellomlagringService.uploadVedlegg(behandlingsId, vedleggstype, data, filnavn, token)
            FilFrontend(mellomlagretVedlegg.filnavn, mellomlagretVedlegg.filId)
        } else {
            opplastetVedleggService.sjekkOmSoknadUnderArbeidTotalVedleggStorrelseOverskriderMaksgrense(behandlingsId, data)
            val opplastetVedlegg = opplastetVedleggService.saveVedleggAndUpdateVedleggstatus(behandlingsId, vedleggstype, data, filnavn)
            FilFrontend(opplastetVedlegg.filnavn, opplastetVedlegg.uuid)
        }
    }

    @DELETE
    @Path("/{behandlingsId}/{vedleggId}")
    open fun deleteVedlegg(
        @PathParam("behandlingsId") behandlingsId: String,
        @PathParam("vedleggId") vedleggId: String,
        @HeaderParam(value = HttpHeaders.AUTHORIZATION) token: String?,
    ) {
        tilgangskontroll.verifiserAtBrukerKanEndreSoknad(behandlingsId)
        val eier = SubjectHandlerUtils.getUserIdFromToken()
        opplastetVedleggRepository.hentVedlegg(vedleggId, eier)?.let {
            opplastetVedleggService.deleteVedleggAndUpdateVedleggstatus(behandlingsId, vedleggId)
            return
        }
        // forsøk sletting via KS mellomlagringstjeneste hvis feature er enablet og sletting fra DB ikke ble utført
        if (mellomlagringEnabled && token != null) {
            log.info("Sletter vedlegg $vedleggId fra KS mellomlagring")
            mellomlagringService.deleteVedleggAndUpdateVedleggstatus(behandlingsId, vedleggId, token)
        }
    }

    private val mellomlagringEnabled get() = unleash.isEnabled(KS_MELLOMLAGRING_ENABLED, false)

    private fun soknadSkalSendesMedDigisosApi(behandlingsId: String, eier: String): Boolean {
        val soknadUnderArbeid = soknadUnderArbeidRepository.hentSoknad(behandlingsId, eier)
        val kommunenummer = soknadUnderArbeid.jsonInternalSoknad?.soknad?.mottaker?.kommunenummer
            ?: throw IllegalStateException("Kommunenummer ikke funnet for JsonInternalSoknad.soknad.mottaker.kommunenummer")

        return when (kommuneInfoService.kommuneInfo(kommunenummer)) {
            FIKS_NEDETID_OG_TOM_CACHE -> {
                throw SendingTilKommuneUtilgjengeligException("Mellomlagring av vedlegg er ikke tilgjengelig fordi fiks har nedetid og kommuneinfo-cache er tom.")
            }
            MANGLER_KONFIGURASJON, HAR_KONFIGURASJON_MEN_SKAL_SENDE_VIA_SVARUT -> false
            SKAL_SENDE_SOKNADER_OG_ETTERSENDELSER_VIA_FDA -> true
            SKAL_VISE_MIDLERTIDIG_FEILSIDE_FOR_SOKNAD_OG_ETTERSENDELSER -> {
                throw SendingTilKommuneErMidlertidigUtilgjengeligException("Sending til kommune $kommunenummer er midlertidig utilgjengelig.")
            }
        }
    }

    companion object {
        const val KS_MELLOMLAGRING_ENABLED = "sosialhjelp.soknad.ks-mellomlagring-enabled"

        private const val MAKS_TOTAL_FILSTORRELSE = 1024 * 1024 * 10

        private val log by logger()

        private fun getByteArray(file: FormDataBodyPart): ByteArray {
            return try {
                file.getValueAs(InputStream::class.java).use {
                    IOUtils.toByteArray(it)
                }
            } catch (e: IOException) {
                throw OpplastingException("Kunne ikke lagre fil", e, "vedlegg.opplasting.feil.generell")
            }
        }
    }
}
