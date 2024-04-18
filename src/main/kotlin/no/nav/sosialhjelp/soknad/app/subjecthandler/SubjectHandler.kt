package no.nav.sosialhjelp.soknad.app.subjecthandler

import no.nav.security.token.support.core.context.TokenValidationContext
import no.nav.security.token.support.core.context.TokenValidationContextHolder
import no.nav.sosialhjelp.soknad.app.Constants.SELVBETJENING
import no.nav.sosialhjelp.soknad.app.Constants.TOKENX
import no.nav.sosialhjelp.soknad.app.LoggingUtils.logger
import org.springframework.stereotype.Component

interface SubjectHandler {
    fun getConsumerId(): String

    fun getUserIdFromToken(): String

    fun getToken(): String
}

@Component
class SubjectHandlerImpl(
    private val tokenValidationContextHolder: TokenValidationContextHolder,
) : SubjectHandler {
    private val tokenValidationContext: TokenValidationContext
        get() {
            return tokenValidationContextHolder.getTokenValidationContext()
                .also {
                    if (!it.hasValidToken()) {
                        log.error(
                            "Could not find TokenValidationContext. Possibly no token in request and request was not captured by token-validation filters.",
                        )
                        throw RuntimeException("Could not find TokenValidationContext. Possibly no token in request.")
                    }
                }
        }

    override fun getUserIdFromToken(): String =
        when {
            tokenValidationContext.hasTokenFor(TOKENX) -> getUserIdFromTokenWithIssuer(TOKENX)
            else -> getUserIdFromTokenWithIssuer(SELVBETJENING)
        }

    private fun getUserIdFromTokenWithIssuer(issuer: String): String {
        val pid: String? = tokenValidationContext.getClaims(issuer).getStringClaim(CLAIM_PID)
        val sub: String? = tokenValidationContext.getClaims(issuer).subject
        return pid ?: sub ?: throw RuntimeException("Could not find any userId for token in pid or sub claim")
    }

    override fun getToken(): String {
        return tokenValidationContext.getJwtToken(TOKENX)?.encodedToken
            ?: tokenValidationContext.getJwtToken(SELVBETJENING)?.encodedToken
            ?: throw IllegalStateException("Finnes ingen tokens (TOKENX/SELVBETJENING")
    }

    override fun getConsumerId(): String {
        return "srvsoknadsosialhje"
    }

    companion object {
        private const val CLAIM_PID = "pid"
        private val log by logger()
    }
}

class StaticSubjectHandlerImpl : SubjectHandler {
    companion object {
        private const val DEFAULT_USER = "11111111111"
        private const val DEFAULT_TOKEN = "token"
    }

    private var user = DEFAULT_USER
    private var token = DEFAULT_TOKEN

    override fun getUserIdFromToken(): String {
        return this.user
    }

    override fun getToken(): String {
        return this.token
    }

    override fun getConsumerId(): String {
        return "StaticConsumerId"
    }

    fun setUser(user: String) {
        this.user = user
    }

    fun setFakeToken(fakeToken: String) {
        this.token = fakeToken
    }

    fun reset() {
        this.user = DEFAULT_USER
        this.token = DEFAULT_TOKEN
    }
}
