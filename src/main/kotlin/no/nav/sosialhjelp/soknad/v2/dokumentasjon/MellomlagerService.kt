package no.nav.sosialhjelp.soknad.v2.dokumentasjon

import no.nav.sosialhjelp.soknad.app.exceptions.IkkeFunnetException
import no.nav.sosialhjelp.soknad.vedlegg.fiks.MellomlagringClient
import no.nav.sosialhjelp.soknad.vedlegg.fiks.MellomlagringDokumentInfo
import no.nav.sosialhjelp.soknad.vedlegg.fiks.MellomlagringDto
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Propagation
import org.springframework.transaction.annotation.Transactional
import java.util.UUID

interface MellomlagerService {
    fun getAllDokumenterMetadata(soknadId: UUID): List<MellomlagretDokument>

    fun getDokumentMetadata(
        soknadId: UUID,
        dokumentId: UUID,
    ): MellomlagretDokument?

    fun getDokument(
        soknadId: UUID,
        dokumentId: UUID,
    ): MellomlagretDokument
}

// Eksterne kall skal aldri inngå i en transaksjon
@Transactional(propagation = Propagation.NEVER)
@Service
class FiksDokumentService(
    private val mellomlagringClient: MellomlagringClient,
) : MellomlagerService {
    override fun getAllDokumenterMetadata(soknadId: UUID): List<MellomlagretDokument> =
        mellomlagringClient.hentDokumenterMetadata(soknadId.toString())
            ?.toMellomlagretDokumentList()
            ?: emptyList()

    override fun getDokumentMetadata(
        soknadId: UUID,
        dokumentId: UUID,
    ): MellomlagretDokument? =
        getAllDokumenterMetadata(soknadId).find { it.filId == dokumentId.toString() }

    override fun getDokument(
        soknadId: UUID,
        dokumentId: UUID,
    ): MellomlagretDokument {
        val metadata =
            getDokumentMetadata(soknadId, dokumentId)
                ?: throw IkkeFunnetException("Fant ikke metadata for dokument $dokumentId")

        return metadata.copy(
            data =
                mellomlagringClient.hentDokument(
                    soknadId.toString(),
                    dokumentId.toString(),
                ),
        )
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

data class MellomlagretDokument(
    val filnavn: String,
    val filId: String,
    val data: ByteArray?,
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as MellomlagretDokument

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
