package no.nav.sosialhjelp.soknad.vedlegg.fiks

import no.nav.sbl.soknadsosialhjelp.vedlegg.JsonFiler
import no.nav.sbl.soknadsosialhjelp.vedlegg.JsonVedlegg
import no.nav.sosialhjelp.kotlin.utils.logger
import no.nav.sosialhjelp.soknad.common.filedetection.FileDetectionUtils
import no.nav.sosialhjelp.soknad.common.filedetection.MimeTypes
import no.nav.sosialhjelp.soknad.common.subjecthandler.SubjectHandlerUtils
import no.nav.sosialhjelp.soknad.db.repositories.soknadmetadata.Vedleggstatus
import no.nav.sosialhjelp.soknad.db.repositories.soknadunderarbeid.SoknadUnderArbeidRepository
import no.nav.sosialhjelp.soknad.innsending.JsonVedleggUtils
import no.nav.sosialhjelp.soknad.innsending.digisosapi.dto.FilMetadata
import no.nav.sosialhjelp.soknad.innsending.digisosapi.dto.FilOpplasting
import no.nav.sosialhjelp.soknad.vedlegg.VedleggUtils.finnVedleggEllerKastException
import no.nav.sosialhjelp.soknad.vedlegg.VedleggUtils.getSha512FromByteArray
import no.nav.sosialhjelp.soknad.vedlegg.VedleggUtils.lagFilnavn
import no.nav.sosialhjelp.soknad.vedlegg.VedleggUtils.validerFil
import no.nav.sosialhjelp.soknad.vedlegg.virusscan.VirusScanner
import org.springframework.stereotype.Component
import java.io.ByteArrayInputStream
import java.util.UUID
import javax.ws.rs.NotFoundException

@Component
class MellomlagringService(
    private val mellomlagringClient: MellomlagringClient,
    private val soknadUnderArbeidRepository: SoknadUnderArbeidRepository,
    private val virusScanner: VirusScanner
) {

    fun getAlleVedlegg(behandlingsId: String): List<MellomlagretVedleggMetadata> {
        val mellomlagredeVedlegg = mellomlagringClient.getMellomlagredeVedlegg(navEksternId = behandlingsId)
        return mellomlagredeVedlegg?.mellomlagringDokumentInfoDTOList?.map {
            MellomlagretVedleggMetadata(
                filnavn = it.filnavn,
                filId = it.filId
            )
        } ?: emptyList()
    }

    fun getVedlegg(vedleggId: String): MellomlagretVedlegg? {
        // todo hvordan gå fra vedleggId til behandlingsId? legge inn behandlingsId som path-param til GET-kallet?
        val vedlegg = mellomlagringClient.getVedlegg("behandlingsId", vedleggId)
        return null
    }

    fun uploadVedlegg(
        behandlingsId: String,
        vedleggstype: String,
        data: ByteArray,
        originalfilnavn: String,
    ): MellomlagretVedleggMetadata {
        var filnavn = originalfilnavn

        val eier = SubjectHandlerUtils.getUserIdFromToken()
        val sha512 = getSha512FromByteArray(data)

        val fileType = validerFil(data, filnavn)
        virusScanner.scan(filnavn, data, behandlingsId, fileType.name)

        val soknadUnderArbeid = soknadUnderArbeidRepository.hentSoknad(behandlingsId, eier)

        val uuid = UUID.randomUUID().toString()
        filnavn = lagFilnavn(filnavn, fileType, uuid)

        val jsonVedlegg = finnVedleggEllerKastException(vedleggstype, soknadUnderArbeid)
        if (jsonVedlegg.filer == null) {
            jsonVedlegg.filer = ArrayList()
        }
        jsonVedlegg.withStatus(Vedleggstatus.LastetOpp.toString()).filer.add(
            JsonFiler().withFilnavn(filnavn).withSha512(sha512)
        )

        soknadUnderArbeidRepository.oppdaterSoknadsdata(soknadUnderArbeid, eier)

        val detectedMimeType = FileDetectionUtils.getMimeType(data)
        val mimetype = if (detectedMimeType.equals(MimeTypes.TEXT_X_MATLAB, ignoreCase = true)) MimeTypes.APPLICATION_PDF else detectedMimeType

        val filOpplasting = FilOpplasting(
            metadata = FilMetadata(
                filnavn = filnavn,
                mimetype = mimetype,
                storrelse = data.size.toLong()
            ),
            data = ByteArrayInputStream(data)
        )
        log.info("filmetadata: ${filOpplasting.metadata}")

        log.info("kaller mellomlagringClient.postVedlegg")
        mellomlagringClient.postVedlegg(navEksternId = behandlingsId, filOpplasting = filOpplasting)
        log.info("suksessfull mellomlagring")
        val mellomlagredeVedlegg = mellomlagringClient.getMellomlagredeVedlegg(navEksternId = behandlingsId)
        val filId = mellomlagredeVedlegg?.mellomlagringDokumentInfoDTOList?.firstOrNull { it.filnavn == filnavn }?.filId ?: "dummy"
        log.info("Mellomlagrede vedlegg: ${mellomlagredeVedlegg?.mellomlagringDokumentInfoDTOList}")
        return MellomlagretVedleggMetadata(filnavn = filnavn, filId = filId)
    }

    fun deleteVedleggAndUpdateVedleggstatus(behandlingsId: String, vedleggId: String) {
        val eier = SubjectHandlerUtils.getUserIdFromToken()

        // hent alle mellomlagrede vedlegg
        val mellomlagredeVedlegg = mellomlagringClient.getMellomlagredeVedlegg(navEksternId = behandlingsId)?.mellomlagringDokumentInfoDTOList ?: return

        log.info("Mellomlagrede vedlegg: $mellomlagredeVedlegg")
        val aktueltVedlegg = mellomlagredeVedlegg.firstOrNull { it.filId == vedleggId } ?: return

        // oppdater soknadUnderArbeid
        val soknadUnderArbeid = soknadUnderArbeidRepository.hentSoknad(behandlingsId, eier)

        val jsonVedlegg: JsonVedlegg = JsonVedleggUtils.getVedleggFromInternalSoknad(soknadUnderArbeid)
            .firstOrNull {
                it.filer.any { jsonFil -> jsonFil.filnavn == aktueltVedlegg.filnavn }
            } ?: throw NotFoundException("Dette vedlegget tilhører en utgift som har blitt tatt bort fra søknaden. Er det flere tabber oppe samtidig?")

        jsonVedlegg.filer.removeIf { it.filnavn == aktueltVedlegg.filnavn }

        if (jsonVedlegg.filer.isEmpty()) {
            jsonVedlegg.status = Vedleggstatus.VedleggKreves.toString()
        }

        soknadUnderArbeidRepository.oppdaterSoknadsdata(soknadUnderArbeid, eier)

        // slett mellomlagret vedlegg
        mellomlagringClient.deleteVedlegg(navEksternId = behandlingsId, digisosDokumentId = vedleggId)
    }

    fun deleteVedlegg(behandlingsId: String, vedleggId: String) {
        mellomlagringClient.deleteVedlegg(navEksternId = behandlingsId, digisosDokumentId = vedleggId)
    }

    fun deleteAllVedleggFor(behandlingsId: String) {
        mellomlagringClient.deleteAllVedleggFor(navEksternId = behandlingsId)
    }

    companion object {
        private val log by logger()
    }
}

data class MellomlagretVedleggMetadata(
    val filnavn: String,
    val filId: String
)

data class MellomlagretVedlegg(
    val filnavn: String,
    val data: ByteArray
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as MellomlagretVedlegg

        if (filnavn != other.filnavn) return false
        if (!data.contentEquals(other.data)) return false

        return true
    }

    override fun hashCode(): Int {
        var result = filnavn.hashCode()
        result = 31 * result + data.contentHashCode()
        return result
    }
}
