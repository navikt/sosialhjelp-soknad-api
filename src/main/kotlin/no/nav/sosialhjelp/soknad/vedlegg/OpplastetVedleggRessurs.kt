package no.nav.sosialhjelp.soknad.vedlegg

import jakarta.servlet.http.HttpServletResponse
import no.nav.security.token.support.core.api.ProtectedWithClaims
import no.nav.sosialhjelp.soknad.app.Constants
import no.nav.sosialhjelp.soknad.app.LoggingUtils.logger
import no.nav.sosialhjelp.soknad.db.repositories.opplastetvedlegg.OpplastetVedleggRepository
import no.nav.sosialhjelp.soknad.db.repositories.soknadunderarbeid.SoknadUnderArbeidRepository
import no.nav.sosialhjelp.soknad.innsending.soknadunderarbeid.SoknadUnderArbeidService
import no.nav.sosialhjelp.soknad.tilgangskontroll.Tilgangskontroll
import no.nav.sosialhjelp.soknad.vedlegg.dto.FilFrontend
import no.nav.sosialhjelp.soknad.vedlegg.exceptions.OpplastingException
import no.nav.sosialhjelp.soknad.vedlegg.fiks.MellomlagringService
import no.nav.sosialhjelp.soknad.vedlegg.filedetection.FileDetectionUtils.detectMimeType
import org.apache.commons.io.IOUtils
import org.docx4j.fonts.fop.fonts.FontCache
import org.springframework.http.HttpHeaders
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.multipart.MultipartFile
import java.io.IOException
import no.nav.sosialhjelp.soknad.app.subjecthandler.SubjectHandlerUtils.getUserIdFromToken as eier

@RestController
@ProtectedWithClaims(issuer = Constants.SELVBETJENING, claimMap = [Constants.CLAIM_ACR_LEVEL_4])
@RequestMapping("/opplastetVedlegg", produces = [MediaType.APPLICATION_JSON_VALUE])
class OpplastetVedleggRessurs(
    private val opplastetVedleggRepository: OpplastetVedleggRepository,
    private val opplastetVedleggService: OpplastetVedleggService,
    private val tilgangskontroll: Tilgangskontroll,
    private val soknadUnderArbeidRepository: SoknadUnderArbeidRepository,
    private val mellomlagringService: MellomlagringService,
    private val soknadUnderArbeidService: SoknadUnderArbeidService
) {

    @GetMapping("/{vedleggId}/fil")
    fun getVedleggFil(
        @PathVariable("vedleggId") vedleggId: String,
        response: HttpServletResponse,
    ): ResponseEntity<ByteArray> {
        tilgangskontroll.verifiserAtBrukerHarTilgang()

        return opplastetVedleggRepository.hentVedlegg(vedleggId, eier())
            ?.let {
                response.setHeader(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"${it.filnavn}\"")
                val mimeType = detectMimeType(it.data)
                ResponseEntity.ok().contentType(MediaType.parseMediaType(mimeType)).body(it.data)
            }
            ?: ResponseEntity.noContent().build()
    }

    @GetMapping("/{behandlingsId}/{vedleggId}/fil")
    fun getVedleggFil(
        @PathVariable("behandlingsId") behandlingsId: String,
        @PathVariable("vedleggId") vedleggId: String,
        response: HttpServletResponse
    ): ResponseEntity<ByteArray> {
        tilgangskontroll.verifiserAtBrukerHarTilgang()

        opplastetVedleggRepository.hentVedlegg(vedleggId, eier())?.let {
            response.setHeader(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"${it.filnavn}\"")
            val mimeType = detectMimeType(it.data)
            return ResponseEntity.ok().contentType(MediaType.parseMediaType(mimeType)).body(it.data)
        }

        val erMellomlagret = soknadUnderArbeidRepository.hentSoknad(behandlingsId, eier()).let {
            soknadUnderArbeidService.skalSoknadSendesMedDigisosApi(it)
        }
        if (erMellomlagret) {
            log.info("Forsøker å hente vedlegg $vedleggId fra mellomlagring hos KS")

            mellomlagringService.getVedlegg(behandlingsId, vedleggId)?.let {
                response.setHeader(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"${it.filnavn}\"")
                val mimeType = detectMimeType(it.data)
                return ResponseEntity.ok().contentType(MediaType.parseMediaType(mimeType)).body(it.data)
            }
        }
        // hvis vedleggId ikke finnes i DB eller KS mellomlagring
        return ResponseEntity.noContent().build()
    }

    @PostMapping("/{behandlingsId}/{type}", consumes = [MediaType.MULTIPART_FORM_DATA_VALUE])
    fun saveVedlegg(
        @PathVariable("behandlingsId") behandlingsId: String,
        @PathVariable("type") vedleggstype: String,
        @RequestParam("file") fil: MultipartFile,
    ): FilFrontend {
        tilgangskontroll.verifiserAtBrukerKanEndreSoknad(behandlingsId)

        val filnavn = fil.originalFilename ?: throw IllegalStateException("Opplastet fil mangler filnavn?")
        val data = getByteArray(fil)

        val skalMellomlagres = soknadUnderArbeidRepository.hentSoknad(behandlingsId, eier()).let {
            soknadUnderArbeidService.skalSoknadSendesMedDigisosApi(it)
        }

        val defaultFontCachePath = FontCache.getDefaultCacheFile(true).path
        log.info("DefaultFontCache: $defaultFontCachePath")

        return if (skalMellomlagres) {
            val (sha512, vedleggMetadata) = mellomlagringService.uploadVedlegg(behandlingsId, vedleggstype, data, filnavn)

            mellomlagringService.oppdaterSoknadUnderArbeid(sha512, behandlingsId, vedleggstype, vedleggMetadata.filnavn)
            FilFrontend(vedleggMetadata.filnavn, vedleggMetadata.filId)
        } else {
            opplastetVedleggService.sjekkOmSoknadUnderArbeidTotalVedleggStorrelseOverskriderMaksgrense(behandlingsId, data)

            val opplastetVedlegg = opplastetVedleggService.lastOppVedlegg(behandlingsId, vedleggstype, data, filnavn)
            opplastetVedleggService.oppdaterVedleggStatus(opplastetVedlegg, behandlingsId, vedleggstype)
            FilFrontend(opplastetVedlegg.filnavn, opplastetVedlegg.uuid)
        }
    }

    @DeleteMapping("/{behandlingsId}/{vedleggId}")
    fun deleteVedlegg(
        @PathVariable("behandlingsId") behandlingsId: String,
        @PathVariable("vedleggId") vedleggId: String,
    ) {
        tilgangskontroll.verifiserAtBrukerKanEndreSoknad(behandlingsId)
        val erMellomlagret = soknadUnderArbeidRepository.hentSoknad(behandlingsId, eier()).let {
            soknadUnderArbeidService.skalSoknadSendesMedDigisosApi(it)
        }

        if (erMellomlagret) {
            log.info("Sletter vedlegg $vedleggId fra KS mellomlagring")
            mellomlagringService.deleteVedleggAndUpdateVedleggstatus(behandlingsId, vedleggId)
        } else {
            opplastetVedleggService.deleteVedleggAndUpdateVedleggstatus(behandlingsId, vedleggId)
        }
    }

    companion object {
        private val log by logger()

        private fun getByteArray(file: MultipartFile): ByteArray {
            return try {
                file.inputStream.use {
                    IOUtils.toByteArray(it)
                }
            } catch (e: IOException) {
                throw OpplastingException("Kunne ikke lagre fil", e, "vedlegg.opplasting.feil.generell")
            }
        }
    }
}
