package no.nav.sosialhjelp.soknad.v2.dokumentasjon

import no.nav.sosialhjelp.api.fiks.exceptions.FiksException
import no.nav.sosialhjelp.soknad.app.LoggingUtils.logger
import no.nav.sosialhjelp.soknad.app.exceptions.IkkeFunnetException
import no.nav.sosialhjelp.soknad.innsending.digisosapi.dto.FilMetadata
import no.nav.sosialhjelp.soknad.innsending.digisosapi.dto.FilOpplasting
import no.nav.sosialhjelp.soknad.vedlegg.VedleggUtils
import no.nav.sosialhjelp.soknad.vedlegg.fiks.MellomlagretVedlegg
import no.nav.sosialhjelp.soknad.vedlegg.fiks.MellomlagringClient
import no.nav.sosialhjelp.soknad.vedlegg.fiks.MellomlagringDokumentInfo
import no.nav.sosialhjelp.soknad.vedlegg.fiks.MellomlagringDto
import no.nav.sosialhjelp.soknad.vedlegg.filedetection.FileDetectionUtils.detectMimeType
import no.nav.sosialhjelp.soknad.vedlegg.virusscan.VirusScanner
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Propagation
import org.springframework.transaction.annotation.Transactional
import java.io.ByteArrayInputStream
import java.util.UUID

interface DokumentlagerService {
    fun getAllDokumenterMetadata(soknadId: UUID): List<MellomlagretDokument>

    fun getDokumentMetadata(
        soknadId: UUID,
        dokumentId: UUID,
    ): MellomlagretDokument?

    fun getDokument(
        soknadId: UUID,
        dokumentId: UUID,
    ): MellomlagretDokument

    fun uploadDokument(
        soknadId: UUID,
        data: ByteArray,
        orginaltFilnavn: String,
    ): MellomlagretDokument

    fun deleteDokument(
        soknadId: UUID,
        dokumentId: UUID,
    )

    fun deleteAllDokumenterForSoknad(soknadId: UUID)
}

// Eksterne kall skal aldri inngå i en transaksjon
@Transactional(propagation = Propagation.NEVER)
@Service
class FiksDokumentService(
    private val mellomlagringClient: MellomlagringClient,
    private val virusScanner: VirusScanner,
) : DokumentlagerService {
    override fun getAllDokumenterMetadata(soknadId: UUID): List<MellomlagretDokument> =
        mellomlagringClient.hentDokumenterMetadata(soknadId.toString())?.toMellomlagretDokumentList() ?: emptyList()

    override fun getDokumentMetadata(
        soknadId: UUID,
        dokumentId: UUID,
    ): MellomlagretDokument? =
        getAllDokumenterMetadata(soknadId).find { it.filId == dokumentId.toString() }

    override fun getDokument(
        soknadId: UUID,
        dokumentId: UUID,
    ): MellomlagretDokument =
        kotlin.runCatching {
            getDokumentMetadata(soknadId, dokumentId).let { mellomlagretDokument ->
                mellomlagretDokument?.copy(
                    data = mellomlagringClient.hentDokument(soknadId.toString(), dokumentId.toString()),
                )
            }
                ?: error("Fant ikke fil $dokumentId hos mellomlager")
        }.getOrElse {
            throw IkkeFunnetException("Klarte ikke å hente dokument $dokumentId for søknad $soknadId", it as Exception)
        }

    override fun uploadDokument(
        soknadId: UUID,
        data: ByteArray,
        orginaltFilnavn: String,
    ): MellomlagretDokument {
        virusScanner.scan(filnavn = orginaltFilnavn, data = data, soknadId = soknadId)

        val nyttFilnavn = VedleggUtils.validerFilOgReturnerNyttFilnavn(orginaltFilnavn, data)

        return mellomlagringClient.lastOppDokument(
            navEksternId = soknadId.toString(),
            filOpplasting = createFilOpplasting(nyttFilnavn, data),
        )
            .let { dto -> dto.mellomlagringMetadataList?.firstOrNull() }?.toMellomlagretDokument(data)
            ?.also { logger.info("Dokument lastet opp til mellomlager: ${it.filId}") }
            ?: throw FiksException("Klarte ikke å laste opp dokument", null)
    }

    override fun deleteDokument(
        soknadId: UUID,
        dokumentId: UUID,
    ) {
        mellomlagringClient.slettDokument(soknadId.toString(), dokumentId.toString())
    }

    override fun deleteAllDokumenterForSoknad(soknadId: UUID) {
        mellomlagringClient.slettAlleDokumenter(soknadId.toString())
    }

    companion object {
        private val logger by logger()
    }
}

private fun MellomlagringDto.toMellomlagretDokumentList(): List<MellomlagretDokument> =
    mellomlagringMetadataList?.map { it.toMellomlagretDokument() } ?: emptyList()

private fun MellomlagringDokumentInfo.toMellomlagretDokument(data: ByteArray? = null) =
    MellomlagretDokument(
        filnavn = filnavn,
        filId = filId,
        data = data,
    )

private fun createFilOpplasting(
    filnavn: String,
    data: ByteArray,
): FilOpplasting {
    return FilOpplasting(
        data = ByteArrayInputStream(data),
        metadata =
            FilMetadata(
                filnavn = filnavn,
                mimetype = detectMimeType(data),
                storrelse = data.size.toLong(),
            ),
    )
}

data class MellomlagretDokument(
    val filnavn: String,
    val filId: String,
    val data: ByteArray?,
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
