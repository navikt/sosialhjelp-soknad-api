package no.nav.sosialhjelp.soknad.migration.dto

import no.nav.sosialhjelp.soknad.repository.opplastetvedlegg.OpplastetVedleggType

data class OpplastetVedleggDto(
    val uuid: String,
    val eier: String,
    val vedleggType: OpplastetVedleggType,
    val data: ByteArray,
    val soknadId: Long,
    val filnavn: String,
    val sha512: String
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as OpplastetVedleggDto

        if (uuid != other.uuid) return false
        if (eier != other.eier) return false
        if (vedleggType != other.vedleggType) return false
        if (!data.contentEquals(other.data)) return false
        if (soknadId != other.soknadId) return false
        if (filnavn != other.filnavn) return false
        if (sha512 != other.sha512) return false

        return true
    }

    override fun hashCode(): Int {
        var result = uuid.hashCode()
        result = 31 * result + eier.hashCode()
        result = 31 * result + vedleggType.hashCode()
        result = 31 * result + data.contentHashCode()
        result = 31 * result + soknadId.hashCode()
        result = 31 * result + filnavn.hashCode()
        result = 31 * result + sha512.hashCode()
        return result
    }
}
