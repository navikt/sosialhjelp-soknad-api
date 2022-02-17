package no.nav.sosialhjelp.soknad.tilgangskontroll

import no.nav.sosialhjelp.soknad.common.exceptions.AuthorizationException
import no.nav.sosialhjelp.soknad.common.exceptions.SosialhjelpSoknadApiException
import no.nav.sosialhjelp.soknad.common.subjecthandler.SubjectHandlerUtils
import org.apache.commons.codec.binary.Base64
import java.security.InvalidKeyException
import java.security.NoSuchAlgorithmException
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import javax.crypto.Mac
import javax.crypto.spec.SecretKeySpec

object XsrfGenerator {

    private const val SECRET = "9f8c0d81-d9b3-4b70-af03-bb9375336c4f"

    @JvmOverloads
    fun generateXsrfToken(
        behandlingsId: String?,
        date: String = ZonedDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd")),
        token: String? = SubjectHandlerUtils.getToken()
    ): String {
        return try {
            val signKey = token + behandlingsId + date
            val hmac = Mac.getInstance("HmacSHA256")
            val secretKey = SecretKeySpec(SECRET.toByteArray(), "HmacSHA256")
            hmac.init(secretKey)
            Base64.encodeBase64URLSafeString(hmac.doFinal(signKey.toByteArray()))
        } catch (e: InvalidKeyException) {
            throw SosialhjelpSoknadApiException("Kunne ikke generere token: ", e)
        } catch (e: NoSuchAlgorithmException) {
            throw SosialhjelpSoknadApiException("Kunne ikke generere token: ", e)
        }
    }

    fun sjekkXsrfToken(givenToken: String?, behandlingsId: String?, isMockProfil: Boolean) {
        val token = generateXsrfToken(behandlingsId)
        val valid = token == givenToken || generateXsrfToken(behandlingsId, ZonedDateTime.now().minusDays(1).format(DateTimeFormatter.ofPattern("yyyyMMdd"))) == givenToken
        if (!valid && !isMockProfil) {
            throw AuthorizationException("Feil token")
        }
    }
}
