package no.nav.sosialhjelp.soknad.vedlegg

import no.nav.security.token.support.core.api.ProtectedWithClaims
import no.nav.sosialhjelp.metrics.aspects.Timed
import no.nav.sosialhjelp.soknad.common.Constants
import no.nav.sosialhjelp.soknad.common.filedetection.FileDetectionUtils.getMimeType
import no.nav.sosialhjelp.soknad.common.filedetection.MimeTypes
import no.nav.sosialhjelp.soknad.common.subjecthandler.SubjectHandlerUtils
import no.nav.sosialhjelp.soknad.db.repositories.opplastetvedlegg.OpplastetVedlegg
import no.nav.sosialhjelp.soknad.db.repositories.opplastetvedlegg.OpplastetVedleggRepository
import no.nav.sosialhjelp.soknad.tilgangskontroll.Tilgangskontroll
import no.nav.sosialhjelp.soknad.vedlegg.dto.FilFrontend
import no.nav.sosialhjelp.soknad.vedlegg.exceptions.OpplastingException
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
    private val tilgangskontroll: Tilgangskontroll
) {
    @GET
    @Path("/{vedleggId}")
    @Produces(MediaType.APPLICATION_JSON)
    open fun getVedlegg(@PathParam("vedleggId") vedleggId: String): OpplastetVedlegg {
        tilgangskontroll.verifiserAtBrukerHarTilgang()
        val eier = SubjectHandlerUtils.getUserIdFromToken()
        return opplastetVedleggRepository.hentVedlegg(vedleggId, eier).orElse(null)
    }

    @GET
    @Path("/{vedleggId}/fil")
    @Produces(MediaType.APPLICATION_JSON)
    open fun getVedleggFil(@PathParam("vedleggId") vedleggId: String, @Context response: HttpServletResponse): Response {
        tilgangskontroll.verifiserAtBrukerHarTilgang()
        val eier = SubjectHandlerUtils.getUserIdFromToken()
        val opplastetVedlegg = opplastetVedleggRepository.hentVedlegg(vedleggId, eier).orElse(null)
        if (opplastetVedlegg != null) {
            response.setHeader("Content-Disposition", "attachment; filename=\"" + opplastetVedlegg.filnavn + "\"")
        } else {
            return Response.noContent().build()
        }
        val detectedMimeType = getMimeType(opplastetVedlegg.data)
        val mimetype = if (detectedMimeType.equals(MimeTypes.TEXT_X_MATLAB, ignoreCase = true)) MimeTypes.APPLICATION_PDF else detectedMimeType
        return Response.ok(opplastetVedlegg.data).type(mimetype).build()
    }

    @POST
    @Path("/{behandlingsId}/{type}")
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    @Produces(MediaType.APPLICATION_JSON)
    open fun saveVedlegg(
        @PathParam("behandlingsId") behandlingsId: String,
        @PathParam("type") vedleggstype: String,
        @FormDataParam("file") fil: FormDataBodyPart
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
        opplastetVedleggService.sjekkOmSoknadUnderArbeidTotalVedleggStorrelseOverskriderMaksgrense(behandlingsId, data)
        val opplastetVedlegg =
            opplastetVedleggService.saveVedleggAndUpdateVedleggstatus(behandlingsId, vedleggstype, data, filnavn)
        return FilFrontend(opplastetVedlegg.filnavn, opplastetVedlegg.uuid)
    }

    @DELETE
    @Path("/{behandlingsId}/{vedleggId}")
    open fun deleteVedlegg(@PathParam("behandlingsId") behandlingsId: String, @PathParam("vedleggId") vedleggId: String) {
        tilgangskontroll.verifiserAtBrukerKanEndreSoknad(behandlingsId)
        opplastetVedleggService.deleteVedleggAndUpdateVedleggstatus(behandlingsId, vedleggId)
    }

    companion object {
        private const val MAKS_TOTAL_FILSTORRELSE = 1024 * 1024 * 10
        private fun getByteArray(file: FormDataBodyPart): ByteArray {
            return try {
                IOUtils.toByteArray(file.getValueAs(InputStream::class.java))
            } catch (e: IOException) {
                throw OpplastingException("Kunne ikke lagre fil", e, "vedlegg.opplasting.feil.generell")
            }
        }
    }
}
