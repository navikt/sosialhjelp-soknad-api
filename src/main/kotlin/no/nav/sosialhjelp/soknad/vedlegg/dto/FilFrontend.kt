package no.nav.sosialhjelp.soknad.vedlegg.dto

data class FilFrontend(
    val filNavn: String?,
    val uuid: String? = null,
)

data class KonvertertFilFrontend(
    val filnavn: String,
    val bytes: ByteArray,
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as KonvertertFilFrontend

        if (filnavn != other.filnavn) return false
        if (!bytes.contentEquals(other.bytes)) return false

        return true
    }

    override fun hashCode(): Int {
        var result = filnavn.hashCode()
        result = 31 * result + bytes.contentHashCode()
        return result
    }
}
