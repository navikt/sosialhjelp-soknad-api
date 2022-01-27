package no.nav.sosialhjelp.soknad.integrationtest.oidc

import com.nimbusds.jose.JOSEException
import com.nimbusds.jose.JOSEObjectType
import com.nimbusds.jose.JWSAlgorithm
import com.nimbusds.jose.JWSHeader
import com.nimbusds.jose.JWSSigner
import com.nimbusds.jose.crypto.RSASSASigner
import com.nimbusds.jose.jwk.RSAKey
import com.nimbusds.jwt.JWTClaimsSet
import com.nimbusds.jwt.SignedJWT
import java.util.Date
import java.util.UUID

object JwtTokenGenerator {
    var ISS = "iss-localhost"
    var ISS_TOKENX = "iss-localhost-tokenx"
    var AUD = "aud-localhost"
    var ACR = "Level4"
    var EXPIRY = 60 * 60 * 3600

    fun createSignedJWT(subject: String?): SignedJWT {
        val claimsSet = buildClaimSet(subject, ISS, AUD, ACR, EXPIRY, null)
        return createSignedJWT(JwkGenerator.defaultRSAKey, claimsSet)
    }

    fun createSignedJwtForTokenx(subject: String?, uniqueName: String?): SignedJWT {
        val claimsSet = buildClaimSet(subject, ISS_TOKENX, AUD, ACR, EXPIRY, uniqueName)
        return createSignedJWT(JwkGenerator.defaultRSAKey, claimsSet)
    }

    fun createSignedJWT(claimsSet: JWTClaimsSet?): SignedJWT {
        return createSignedJWT(JwkGenerator.defaultRSAKey, claimsSet)
    }

    private fun buildClaimSet(
        subject: String?, issuer: String?, audience: String?, authLevel: String?,
        expiry: Int, uniqueName: String?
    ): JWTClaimsSet {
        val now = Date()
        return JWTClaimsSet.Builder()
            .subject(subject)
            .issuer(issuer)
            .audience(audience)
            .jwtID(UUID.randomUUID().toString())
            .claim("acr", authLevel)
            .claim("ver", "1.0")
            .claim("nonce", "myNonce")
            .claim("auth_time", now)
            .claim("unique_name", uniqueName)
            .notBeforeTime(now)
            .issueTime(now)
            .expirationTime(Date(now.time + expiry)).build()
    }

    private fun createSignedJWT(rsaJwk: RSAKey, claimsSet: JWTClaimsSet?): SignedJWT {
        return try {
            val header = JWSHeader.Builder(JWSAlgorithm.RS256)
                .keyID(rsaJwk.keyID)
                .type(JOSEObjectType.JWT)
            val signedJWT = SignedJWT(header.build(), claimsSet)
            val signer: JWSSigner = RSASSASigner(rsaJwk.toPrivateKey())
            signedJWT.sign(signer)
            signedJWT
        } catch (e: JOSEException) {
            throw RuntimeException(e)
        }
    }
}
