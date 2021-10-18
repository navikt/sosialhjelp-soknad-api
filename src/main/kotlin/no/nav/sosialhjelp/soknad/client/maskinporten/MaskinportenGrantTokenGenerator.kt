package no.nav.sosialhjelp.soknad.client.maskinporten

import com.nimbusds.jose.JOSEObjectType
import com.nimbusds.jose.JWSAlgorithm
import com.nimbusds.jose.JWSHeader
import com.nimbusds.jose.crypto.RSASSASigner
import com.nimbusds.jose.jwk.KeyUse
import com.nimbusds.jose.jwk.RSAKey
import com.nimbusds.jose.jwk.gen.RSAKeyGenerator
import com.nimbusds.jwt.JWTClaimsSet
import com.nimbusds.jwt.SignedJWT
import no.nav.sosialhjelp.soknad.domain.model.util.ServiceUtils
import java.time.Instant
import java.util.Date
import java.util.UUID

class MaskinportenGrantTokenGenerator(
    private val maskinportenProperties: MaskinportenProperties,
    private val issuer: String
) {

    /**
     * Generer privateRsaKey hvis maskinportenConfig.jwkPrivate == generateRSA, og appen ikke kjører i prod.
     * Dvs at rsaKey genereres i ved lokal kjøring, i test eller mot mock-alt.
     */
    private val privateRsaKey = if (maskinportenProperties.jwkPrivate == "generateRSA") {
        if (!ServiceUtils.isNonProduction()) throw IllegalStateException("Generation of RSA keys is not allowed in prod")
        RSAKeyGenerator(2048).keyUse(KeyUse.SIGNATURE).keyID(UUID.randomUUID().toString()).generate()
    } else {
        RSAKey.parse(maskinportenProperties.jwkPrivate)
    }

    fun getJwt(): String {
        return SignedJWT(signatureHeader, createJwtClaimSet(issuer))
            .apply { sign(RSASSASigner(privateRsaKey.toPrivateKey())) }
            .serialize()
    }

    private fun createJwtClaimSet(audience: String): JWTClaimsSet {
        val now: Instant = Instant.now()
        return JWTClaimsSet.Builder()
            .audience(audience)
            .issuer(maskinportenProperties.clientId)
            .claim(SCOPE_CLAIM, maskinportenProperties.scope)
            .issueTime(Date.from(now))
            .expirationTime(Date.from(now.plusSeconds(120))) // 120s eller noe annet?
            .build()
    }

    private val signatureHeader
        get() = JWSHeader.Builder(JWSAlgorithm.RS256)
            .keyID(privateRsaKey.keyID)
            .type(JOSEObjectType.JWT)
            .build()

    companion object {
        private const val SCOPE_CLAIM = "scope"
    }
}
