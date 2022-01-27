package no.nav.sosialhjelp.soknad.integrationtest.oidc

import com.nimbusds.jose.jwk.JWKSet
import com.nimbusds.jose.jwk.RSAKey
import com.nimbusds.jose.util.IOUtils
import java.io.IOException
import java.nio.charset.Charset
import java.text.ParseException

object JwkGenerator {

    private const val DEFAULT_KEYID = "localhost-signer"
    private const val DEFAULT_JWKSET_FILE = "/jwkset.json"

    val defaultRSAKey: RSAKey
        get() = jwkSet.getKeyByKeyId(DEFAULT_KEYID) as RSAKey

    private val jwkSet: JWKSet
        get() = try {
            JWKSet.parse(
                IOUtils.readInputStreamToString(
                    JwkGenerator::class.java.getResourceAsStream(DEFAULT_JWKSET_FILE),
                    Charset.forName("UTF-8")
                )
            )
        } catch (io: IOException) {
            throw RuntimeException(io)
        } catch (io: ParseException) {
            throw RuntimeException(io)
        }
}
