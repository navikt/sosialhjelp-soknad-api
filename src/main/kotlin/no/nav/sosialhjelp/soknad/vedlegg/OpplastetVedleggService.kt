package no.nav.sosialhjelp.soknad.vedlegg

import no.nav.sbl.soknadsosialhjelp.json.VedleggsforventningMaster
import no.nav.sbl.soknadsosialhjelp.vedlegg.JsonFiler
import no.nav.sbl.soknadsosialhjelp.vedlegg.JsonVedlegg
import no.nav.sbl.soknadsosialhjelp.vedlegg.JsonVedleggSpesifikasjon
import no.nav.sosialhjelp.soknad.common.filedetection.FileDetectionUtils.detectTikaType
import no.nav.sosialhjelp.soknad.common.filedetection.FileDetectionUtils.getMimeType
import no.nav.sosialhjelp.soknad.common.filedetection.TikaFileType
import no.nav.sosialhjelp.soknad.common.subjecthandler.SubjectHandlerUtils
import no.nav.sosialhjelp.soknad.db.repositories.opplastetvedlegg.OpplastetVedlegg
import no.nav.sosialhjelp.soknad.db.repositories.opplastetvedlegg.OpplastetVedleggRepository
import no.nav.sosialhjelp.soknad.db.repositories.opplastetvedlegg.OpplastetVedleggType
import no.nav.sosialhjelp.soknad.db.repositories.soknadmetadata.Vedleggstatus
import no.nav.sosialhjelp.soknad.db.repositories.soknadunderarbeid.SoknadUnderArbeid
import no.nav.sosialhjelp.soknad.db.repositories.soknadunderarbeid.SoknadUnderArbeidRepository
import no.nav.sosialhjelp.soknad.innsending.JsonVedleggUtils
import no.nav.sosialhjelp.soknad.vedlegg.VedleggUtils.getSha512FromByteArray
import no.nav.sosialhjelp.soknad.vedlegg.exceptions.OpplastingException
import no.nav.sosialhjelp.soknad.vedlegg.exceptions.SamletVedleggStorrelseForStorException
import no.nav.sosialhjelp.soknad.vedlegg.exceptions.UgyldigOpplastingTypeException
import no.nav.sosialhjelp.soknad.vedlegg.virusscan.VirusScanner
import org.apache.pdfbox.pdmodel.PDDocument
import org.apache.pdfbox.pdmodel.encryption.InvalidPasswordException
import org.apache.pdfbox.text.PDFTextStripper
import org.slf4j.LoggerFactory
import java.io.ByteArrayInputStream
import java.io.IOException
import java.io.UnsupportedEncodingException
import java.net.URLDecoder
import java.nio.charset.StandardCharsets
import java.util.Locale
import java.util.UUID
import java.util.function.Predicate
import javax.ws.rs.NotFoundException

class OpplastetVedleggService(
    private val opplastetVedleggRepository: OpplastetVedleggRepository,
    private val soknadUnderArbeidRepository: SoknadUnderArbeidRepository,
    private val virusScanner: VirusScanner
) {
    fun saveVedleggAndUpdateVedleggstatus(
        behandlingsId: String,
        vedleggstype: String,
        data: ByteArray,
        originalfilnavn: String
    ): OpplastetVedlegg {
        var filnavn = originalfilnavn

        val eier = SubjectHandlerUtils.getUserIdFromToken()
        val sha512 = getSha512FromByteArray(data)

        val fileType = validerFil(data, filnavn)
        virusScanner.scan(filnavn, data, behandlingsId, fileType.name)

        val soknadUnderArbeid = soknadUnderArbeidRepository.hentSoknad(behandlingsId, eier)
        val soknadId = soknadUnderArbeid.soknadId

        val uuid = UUID.randomUUID().toString()
        filnavn = lagFilnavn(filnavn, fileType, uuid)

        val opplastetVedlegg = OpplastetVedlegg(
            uuid = uuid,
            eier = eier,
            vedleggType = OpplastetVedleggType(vedleggstype),
            data = data,
            soknadId = soknadId,
            filnavn = filnavn,
            sha512 = sha512
        )
        opplastetVedleggRepository.opprettVedlegg(opplastetVedlegg, eier)

        val jsonVedlegg = finnVedleggEllerKastException(vedleggstype, soknadUnderArbeid)
        if (jsonVedlegg.filer == null) {
            jsonVedlegg.filer = ArrayList()
        }
        jsonVedlegg.withStatus(Vedleggstatus.LastetOpp.toString()).filer.add(
            JsonFiler().withFilnavn(filnavn).withSha512(sha512)
        )

        soknadUnderArbeidRepository.oppdaterSoknadsdata(soknadUnderArbeid, eier)

        return opplastetVedlegg
    }

    fun oppdaterVedleggsforventninger(soknadUnderArbeid: SoknadUnderArbeid, eier: String) {
        val jsonVedleggs = JsonVedleggUtils.getVedleggFromInternalSoknad(soknadUnderArbeid)
        val paakrevdeVedlegg = VedleggsforventningMaster.finnPaakrevdeVedlegg(soknadUnderArbeid.jsonInternalSoknad)
        val opplastedeVedlegg = opplastetVedleggRepository.hentVedleggForSoknad(soknadUnderArbeid.soknadId, eier)

        fjernIkkePaakrevdeVedlegg(jsonVedleggs, paakrevdeVedlegg, opplastedeVedlegg)

        jsonVedleggs.addAll(
            paakrevdeVedlegg
                .filter { isNotInList(jsonVedleggs).test(it) }
                .map { it.withStatus(Vedleggstatus.VedleggKreves.toString()) }
        )

        soknadUnderArbeid.jsonInternalSoknad?.vedlegg = JsonVedleggSpesifikasjon().withVedlegg(jsonVedleggs)
        soknadUnderArbeidRepository.oppdaterSoknadsdata(soknadUnderArbeid, eier)
    }

    fun sjekkOmSoknadUnderArbeidTotalVedleggStorrelseOverskriderMaksgrense(behandlingsId: String?, data: ByteArray) {
        val eier = SubjectHandlerUtils.getUserIdFromToken()
        val soknadUnderArbeid = soknadUnderArbeidRepository.hentSoknad(behandlingsId, eier)
        val soknadId = soknadUnderArbeid.soknadId

        val samletVedleggStorrelse = opplastetVedleggRepository.hentSamletVedleggStorrelse(soknadId, eier)
        val newStorrelse = samletVedleggStorrelse + data.size
        if (newStorrelse > MAKS_SAMLET_VEDLEGG_STORRELSE) {
            val feilmeldingId = if (soknadUnderArbeid.erEttersendelse) {
                "ettersending.vedlegg.feil.samletStorrelseForStor"
            } else {
                "vedlegg.opplasting.feil.samletStorrelseForStor"
            }
            throw SamletVedleggStorrelseForStorException(
                "Kunne ikke lagre fil fordi samlet størrelse på alle vedlegg er for stor",
                null,
                feilmeldingId
            )
        }
    }

    fun deleteVedleggAndUpdateVedleggstatus(behandlingsId: String?, vedleggId: String?) {
        val eier = SubjectHandlerUtils.getUserIdFromToken()
        val opplastetVedlegg = opplastetVedleggRepository.hentVedlegg(vedleggId, eier).orElse(null) ?: return

        val vedleggstype = opplastetVedlegg.vedleggType.sammensattType

        val soknadUnderArbeid = soknadUnderArbeidRepository.hentSoknad(behandlingsId, eier)

        val jsonVedlegg = finnVedleggEllerKastException(vedleggstype, soknadUnderArbeid)
        jsonVedlegg.filer.removeIf { it.sha512 == opplastetVedlegg.sha512 && it.filnavn == opplastetVedlegg.filnavn }

        if (jsonVedlegg.filer.isEmpty()) {
            jsonVedlegg.status = Vedleggstatus.VedleggKreves.toString()
        }

        soknadUnderArbeidRepository.oppdaterSoknadsdata(soknadUnderArbeid, eier)
        opplastetVedleggRepository.slettVedlegg(vedleggId, eier)
    }

    private fun fjernIkkePaakrevdeVedlegg(
        jsonVedleggs: MutableList<JsonVedlegg>,
        paakrevdeVedlegg: List<JsonVedlegg>,
        opplastedeVedlegg: List<OpplastetVedlegg>
    ) {
        val ikkeLengerPaakrevdeVedlegg = jsonVedleggs.filter { isNotInList(paakrevdeVedlegg).test(it) }.toMutableList()

        excludeTypeAnnetAnnetFromList(ikkeLengerPaakrevdeVedlegg)
        jsonVedleggs.removeAll(ikkeLengerPaakrevdeVedlegg)
        for (ikkePaakrevdVedlegg in ikkeLengerPaakrevdeVedlegg) {
            for (oVedlegg in opplastedeVedlegg) {
                if (isSameType(ikkePaakrevdVedlegg, oVedlegg)) {
                    opplastetVedleggRepository.slettVedlegg(oVedlegg.uuid, oVedlegg.eier)
                }
            }
        }
    }

    private fun isNotInList(jsonVedleggs: List<JsonVedlegg>): Predicate<JsonVedlegg> {
        return Predicate<JsonVedlegg> { v: JsonVedlegg ->
            jsonVedleggs.none { it.type == v.type && it.tilleggsinfo == v.tilleggsinfo }
        }
    }

    private fun excludeTypeAnnetAnnetFromList(jsonVedleggs: MutableList<JsonVedlegg>) {
        jsonVedleggs.removeAll(
            jsonVedleggs.filter { it.type == "annet" && it.tilleggsinfo == "annet" }
        )
    }

    private fun isSameType(jsonVedlegg: JsonVedlegg, opplastetVedlegg: OpplastetVedlegg): Boolean {
        return opplastetVedlegg.vedleggType.sammensattType == jsonVedlegg.type + "|" + jsonVedlegg.tilleggsinfo
    }

    private fun finnVedleggEllerKastException(vedleggstype: String, soknadUnderArbeid: SoknadUnderArbeid): JsonVedlegg {
        return JsonVedleggUtils.getVedleggFromInternalSoknad(soknadUnderArbeid)
            .firstOrNull { vedleggstype == it.type + "|" + it.tilleggsinfo }
            ?: throw NotFoundException("Dette vedlegget tilhører $vedleggstype utgift som har blitt tatt bort fra søknaden. Er det flere tabber oppe samtidig?")
    }

    fun lagFilnavn(opplastetNavn: String, fileType: TikaFileType, uuid: String): String {
        var filnavn = opplastetNavn
        val fileExtension = findFileExtension(opplastetNavn)

        if (fileExtension != null) {
            val separatorPosition = opplastetNavn.lastIndexOf(".")
            if (separatorPosition != -1) {
                filnavn = opplastetNavn.substring(0, separatorPosition)
            }
        }
        try {
            filnavn = URLDecoder.decode(filnavn, StandardCharsets.UTF_8.toString())
        } catch (e: UnsupportedEncodingException) {
            logger.warn("Klarte ikke å URIdecode fil med navn {}", filnavn, e)
        }

        filnavn = filnavn
            .replace("æ", "e")
            .replace("ø", "o")
            .replace("å", "a")
            .replace("Æ", "E")
            .replace("Ø", "O")
            .replace("Å", "A")

        filnavn = filnavn.replace("[^a-zA-Z0-9_-]".toRegex(), "")

        if (filnavn.length > 50) {
            filnavn = filnavn.substring(0, 50)
        }

        filnavn += "-" + uuid.split("-").toTypedArray()[0]
        filnavn += if (fileExtension != null && fileExtension.isNotEmpty() && erTikaOgFileExtensionEnige(fileExtension, fileType)) {
            fileExtension
        } else {
            logger.info("Opplastet vedlegg mangler fil extension -> setter fil extension lik validert filtype = ${fileType.extension}")
            fileType.extension
        }

        return filnavn
    }

    private fun erTikaOgFileExtensionEnige(fileExtension: String, fileType: TikaFileType): Boolean {
        if (TikaFileType.JPEG == fileType) {
            return ".jpg".equals(fileExtension, ignoreCase = true) || ".jpeg".equals(fileExtension, ignoreCase = true)
        }
        if (TikaFileType.PNG == fileType) {
            return ".png".equals(fileExtension, ignoreCase = true)
        }
        return if (TikaFileType.PDF == fileType) {
            ".pdf".equals(fileExtension, ignoreCase = true)
        } else false
    }

    private fun validerFil(data: ByteArray, filnavn: String): TikaFileType {
        val fileType = detectTikaType(data)

        if (fileType == TikaFileType.UNKNOWN) {
            val mimeType = getMimeType(data)
            val filType = findFileExtension(filnavn)
            throw UgyldigOpplastingTypeException(
                "Ugyldig filtype for opplasting. Mimetype var $mimeType, filtype var $filType",
                null,
                "opplasting.feilmelding.feiltype"
            )
        }
        if (fileType == TikaFileType.JPEG || fileType == TikaFileType.PNG) {
            validerFiltypeForBilde(filnavn)
        }
        if (fileType == TikaFileType.PDF) {
            sjekkOmPdfErGyldig(data)
        }
        return fileType
    }

    private fun findFileExtension(filnavn: String): String? {
        val sisteIndexForPunktum = filnavn.lastIndexOf(".")
        if (sisteIndexForPunktum < 0) {
            return null
        }
        val fileExtension = filnavn.substring(sisteIndexForPunktum)
        return if (!isValidFileExtension(fileExtension)) {
            null
        } else fileExtension
    }

    private fun isValidFileExtension(fileExtension: String): Boolean {
        val validFileExtensions = listOf(".pdf", ".jpeg", ".jpg", ".png")
        return validFileExtensions.contains(fileExtension.lowercase(Locale.getDefault()))
    }

    private fun validerFiltypeForBilde(filnavn: String) {
        val fileExtension = findFileExtension(filnavn)
        if (fileExtension == null) {
            logger.info("Opplastet bilde validerer OK, men mangler filtype for fil")
        }
        val lowercaseFilenavn = filnavn.lowercase(Locale.getDefault())
        if (lowercaseFilenavn.endsWith(".jfif") || lowercaseFilenavn.endsWith(".pjpeg") || lowercaseFilenavn.endsWith(".pjp")) {
            throw UgyldigOpplastingTypeException(
                "Ugyldig filtype for opplasting. Filtype var $fileExtension",
                null,
                "opplasting.feilmelding.feiltype"
            )
        }
    }

    private fun sjekkOmPdfErGyldig(data: ByteArray) {
        try {
            PDDocument.load(ByteArrayInputStream(data))
                .use { document ->
                    val text = PDFTextStripper().getText(document)
                    if (text == null || text.isEmpty()) {
                        logger.warn("PDF er tom") // En PDF med ett helt blankt ark generert av word gir text = "\r\n"
                    }
                    if (document.isEncrypted) {
                        throw UgyldigOpplastingTypeException(
                            "PDF kan ikke være kryptert.",
                            null,
                            "opplasting.feilmelding.pdf.kryptert"
                        )
                    }
                }
        } catch (e: InvalidPasswordException) {
            throw UgyldigOpplastingTypeException(
                "PDF kan ikke være krypert.",
                null,
                "opplasting.feilmelding.pdf.kryptert"
            )
        } catch (e: IOException) {
            throw OpplastingException("Kunne ikke lagre fil", e, "vedlegg.opplasting.feil.generell")
        }
    }

    companion object {
        const val MAKS_SAMLET_VEDLEGG_STORRELSE_I_MB = 150
        const val MAKS_SAMLET_VEDLEGG_STORRELSE = MAKS_SAMLET_VEDLEGG_STORRELSE_I_MB * 1024 * 1024 // 150 MB

        private val logger = LoggerFactory.getLogger(OpplastetVedleggService::class.java)
    }
}
