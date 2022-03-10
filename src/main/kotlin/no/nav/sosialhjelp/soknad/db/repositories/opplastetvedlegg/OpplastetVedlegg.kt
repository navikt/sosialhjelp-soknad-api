package no.nav.sosialhjelp.soknad.db.repositories.opplastetvedlegg

import java.util.UUID

data class OpplastetVedlegg(
    var uuid: String = UUID.randomUUID().toString(),
    var eier: String,
    var vedleggType: OpplastetVedleggType,
    var data: ByteArray,
    var soknadId: Long,
    var filnavn: String,
    var sha512: String
) {

    // generated
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as OpplastetVedlegg

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

data class OpplastetVedleggType(
    var sammensattType: String
) {
    val type get() = sammensattType.substringBefore('|')
    val tilleggsinfo get() = sammensattType.substringAfter('|')
}
