package no.nav.sosialhjelp.soknad.vedlegg

import no.finn.unleash.Unleash
import no.nav.security.token.support.core.api.ProtectedWithClaims
import no.nav.sosialhjelp.kotlin.utils.logger
import no.nav.sosialhjelp.metrics.aspects.Timed
import no.nav.sosialhjelp.soknad.common.Constants
import no.nav.sosialhjelp.soknad.common.filedetection.FileDetectionUtils.getMimeType
import no.nav.sosialhjelp.soknad.common.subjecthandler.SubjectHandlerUtils
import no.nav.sosialhjelp.soknad.db.repositories.opplastetvedlegg.OpplastetVedleggRepository
import no.nav.sosialhjelp.soknad.db.repositories.soknadunderarbeid.SoknadUnderArbeidRepository
import no.nav.sosialhjelp.soknad.tilgangskontroll.Tilgangskontroll
import no.nav.sosialhjelp.soknad.vedlegg.dto.FilFrontend
import no.nav.sosialhjelp.soknad.vedlegg.exceptions.OpplastingException
import no.nav.sosialhjelp.soknad.vedlegg.fiks.MellomlagringService
import org.apache.commons.io.IOUtils
import org.glassfish.jersey.media.multipart.FormDataBodyPart
import org.glassfish.jersey.media.multipart.FormDataParam
import org.springframework.stereotype.Controller
import java.io.File
import java.io.IOException
import java.io.InputStream
import javax.servlet.http.HttpServletResponse
import javax.ws.rs.Consumes
import javax.ws.rs.DELETE
import javax.ws.rs.GET
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
    private val mellomlagringService: MellomlagringService,
    private val unleash: Unleash
) {

    @GET
    @Path("/{vedleggId}/fil")
    @Produces(MediaType.APPLICATION_JSON)
    open fun getVedleggFil(
        @PathParam("vedleggId") vedleggId: String,
        @Context response: HttpServletResponse,
    ): Response {
        tilgangskontroll.verifiserAtBrukerHarTilgang()
        val eier = SubjectHandlerUtils.getUserIdFromToken()

        return opplastetVedleggRepository.hentVedlegg(vedleggId, eier)
            ?.let {
                response.setHeader("Content-Disposition", "attachment; filename=\"${it.filnavn}\"")
                val mimeType = getMimeType(it.data)
                Response.ok(it.data).type(mimeType).build()
            }
            ?: Response.noContent().build()
    }

    @GET
    @Path("/{behandlingsId}/{vedleggId}/fil")
    @Produces(MediaType.APPLICATION_JSON)
    open fun getVedleggFil(
        @PathParam("behandlingsId") behandlingsId: String,
        @PathParam("vedleggId") vedleggId: String,
        @Context response: HttpServletResponse
    ): Response {
        tilgangskontroll.verifiserAtBrukerHarTilgang()
        val eier = SubjectHandlerUtils.getUserIdFromToken()

        opplastetVedleggRepository.hentVedlegg(vedleggId, eier)?.let {
            response.setHeader("Content-Disposition", "attachment; filename=\"${it.filnavn}\"")
            val mimeType = getMimeType(it.data)
            return Response.ok(it.data).type(mimeType).build()
        }

        if (mellomlagringEnabled) {
            log.info("Forsøker å hente vedlegg $vedleggId fra mellomlagring hos KS")
            mellomlagringService.getVedlegg(behandlingsId, vedleggId)?.let {
                response.setHeader("Content-Disposition", "attachment; filename=\"${it.filnavn}\"")
                val mimeType = getMimeType(it.data)
                return Response.ok(it.data).type(mimeType).build()
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

        val soknadUnderArbeid = soknadUnderArbeidRepository.hentSoknad(behandlingsId, eier)

        // bruk KS mellomlagringstjeneste hvis featuren er enablet og søknad skal sendes med DigisosApi
        return if (mellomlagringService.erMellomlagringEnabledOgSoknadSkalSendesMedDigisosApi(soknadUnderArbeid)) {
            log.info("Forsøker å laste opp vedlegg til mellomlagring hos KS")
            val mellomlagretVedlegg = mellomlagringService.uploadVedlegg(behandlingsId, vedleggstype, data, filnavn)
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
    ) {
        tilgangskontroll.verifiserAtBrukerKanEndreSoknad(behandlingsId)
        if (!mellomlagringEnabled) {
            opplastetVedleggService.deleteVedleggAndUpdateVedleggstatus(behandlingsId, vedleggId)
        } else {
            // forsøk sletting via KS mellomlagringstjeneste hvis feature er enablet og sletting fra DB ikke ble utført
            log.info("Sletter vedlegg $vedleggId fra KS mellomlagring")
            mellomlagringService.deleteVedleggAndUpdateVedleggstatus(behandlingsId, vedleggId)
        }
    }

    private val mellomlagringEnabled get() = unleash.isEnabled(KS_MELLOMLAGRING_ENABLED, false)

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
