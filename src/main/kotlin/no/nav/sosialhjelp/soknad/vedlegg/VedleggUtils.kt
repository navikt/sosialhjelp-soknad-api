package no.nav.sosialhjelp.soknad.vedlegg

import org.bouncycastle.jcajce.provider.digest.SHA512
import org.bouncycastle.util.encoders.Hex

object VedleggUtils {

    fun getSha512FromByteArray(bytes: ByteArray?): String? {
        if (bytes == null) {
            return ""
        }
        val sha512 = SHA512.Digest()
        sha512.update(bytes)
        return Hex.toHexString(sha512.digest())
    }
}
