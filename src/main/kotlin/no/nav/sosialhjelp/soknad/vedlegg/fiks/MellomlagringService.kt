package no.nav.sosialhjelp.soknad.vedlegg.fiks

import no.nav.sbl.soknadsosialhjelp.vedlegg.JsonFiler
import no.nav.sosialhjelp.soknad.common.filedetection.FileDetectionUtils
import no.nav.sosialhjelp.soknad.common.filedetection.MimeTypes
import no.nav.sosialhjelp.soknad.common.subjecthandler.SubjectHandlerUtils
import no.nav.sosialhjelp.soknad.db.repositories.soknadmetadata.Vedleggstatus
import no.nav.sosialhjelp.soknad.db.repositories.soknadunderarbeid.SoknadUnderArbeidRepository
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

@Component
class MellomlagringService(
    private val mellomlagringClient: MellomlagringClient,
    private val soknadUnderArbeidRepository: SoknadUnderArbeidRepository,
    private val virusScanner: VirusScanner
) {

    fun getVedlegg(vedleggId: String, token: String): MellomlagretVedlegg? {
        // todo hvordan gå fra vedleggId til behandlingsId? legge inn behandlingsId som path-param til GET-kallet?
        val vedlegg = mellomlagringClient.getVedlegg("behandlingsId", vedleggId, token)
        return null
    }

    fun uploadVedlegg(
        behandlingsId: String,
        vedleggstype: String,
        data: ByteArray,
        originalfilnavn: String,
        token: String
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

        mellomlagringClient.postVedlegg(
            navEksternId = behandlingsId,
            filOpplasting = filOpplasting,
            token = token
        )
        return MellomlagretVedleggMetadata(filnavn = "filnavn", filId = "uuid")
    }

    fun deleteVedleggAndUpdateVedleggstatus(behandlingsId: String, vedleggId: String, token: String) {
        val eier = SubjectHandlerUtils.getUserIdFromToken()

        // hent alle mellomlagrede vedlegg
        val mellomlagredeVedlegg = mellomlagringClient.getMellomlagredeVedlegg(navEksternId = behandlingsId, token = token)
        mellomlagredeVedlegg.mellomlagringMetadataList

        // oppdater vedleggstatus
        val soknadUnderArbeid = soknadUnderArbeidRepository.hentSoknad(behandlingsId, eier)
        // ...
        soknadUnderArbeidRepository.oppdaterSoknadsdata(soknadUnderArbeid, eier)

        // slett mellomlagret vedlegg
        mellomlagringClient.deleteVedlegg(navEksternId = behandlingsId, digisosDokumentId = vedleggId, token = token)
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
