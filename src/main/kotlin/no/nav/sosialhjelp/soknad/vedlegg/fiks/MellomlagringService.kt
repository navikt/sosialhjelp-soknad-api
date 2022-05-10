package no.nav.sosialhjelp.soknad.vedlegg.fiks

import org.springframework.stereotype.Component

@Component
class MellomlagringService {

    fun getVedlegg(vedleggId: String, token: String): MellomlagretVedlegg? {
        // todo implement
        return null
    }

    fun uploadVedlegg(
        behandlingsId: String,
        vedleggstype: String,
        data: ByteArray,
        originalfilnavn: String,
        token: String
    ) : MellomlagretVedleggMetadata {
        // todo implement
        return MellomlagretVedleggMetadata(filnavn = "filnavn", filId = "uuid")
    }

    fun deleteVedleggAndUpdateVedleggstatus(behandlingsId: String, vedleggId: String, token: String) {
        // todo implement
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