package no.nav.sosialhjelp.soknad.innsending.digisosapi

import com.nimbusds.jose.JOSEObjectType
import com.nimbusds.jose.JWSAlgorithm
import com.nimbusds.jose.JWSHeader
import com.nimbusds.jose.crypto.RSASSASigner
import com.nimbusds.jose.jwk.RSAKey
import com.nimbusds.jwt.JWTClaimsSet
import com.nimbusds.jwt.SignedJWT
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.MediaType
import org.springframework.stereotype.Service
import org.springframework.web.reactive.function.client.WebClient
import java.time.Instant
import java.util.Date
import java.util.UUID

@Service
class MaskinportenV2Service(
    private val maskinportenTokenCache: MaskinportenTokenCache,
    @Value("maskinporten.v2") private val config: MaskinportenConfig,
) {
    private val client: WebClient =
        WebClient.builder()
            .baseUrl(config.endpoint)
            .build()

    fun getMaskinportenToken(
        scope: String,
        pid: String,
    ): String =
        maskinportenTokenCache.get(pid, scope) ?: obtainMaskinportenToken(scope, pid)
            .also { token -> maskinportenTokenCache.put(pid, scope, token) }
            .access_token

    private fun obtainMaskinportenToken(
        scope: String,
        pid: String,
    ): MaskinportenToken =
        client.post()
            .contentType(MediaType.APPLICATION_FORM_URLENCODED)
            .bodyValue(jwtBearerGrant(getMaskinportenScope(scope, pid)))
            .retrieve()
            .bodyToMono(MaskinportenToken::class.java)
            .blockOptional().orElseThrow { error("Failed to obtain Maskinporten token") }

    private fun jwtBearerGrant(assertion: String) = "grant_type=urn:ietf:params:oauth:grant-type:jwt-bearer&assertion=$assertion"

    private fun getMaskinportenScope(
        scope: String,
        pid: String,
    ): String {
        val now = Instant.now()
        val rsaKey = RSAKey.parse(config.clientJwk)
        val signer = RSASSASigner(rsaKey.toPrivateKey())

        val header =
            JWSHeader.Builder(JWSAlgorithm.RS256)
                .keyID(rsaKey.keyID)
                .type(JOSEObjectType.JWT)
                .build()

        val claims: JWTClaimsSet =
            JWTClaimsSet.Builder()
                .issuer(config.clientId)
                .audience(config.issuer)
                .issueTime(Date.from(now))
                .subject(pid) // not sure if this is needed
                .claim("pid", pid)
                .claim("scope", scope)
                .expirationTime(Date.from(now.plusSeconds(60)))
                .jwtID(UUID.randomUUID().toString())
                .build()

        return SignedJWT(header, claims).apply { sign(signer) }.serialize()
    }
}
