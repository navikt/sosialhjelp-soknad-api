package no.nav.sosialhjelp.soknad.common.subjecthandler

import no.nav.security.token.support.core.context.TokenValidationContext
import no.nav.security.token.support.jaxrs.JaxrsTokenValidationContextHolder
import no.nav.sosialhjelp.kotlin.utils.logger
import no.nav.sosialhjelp.soknad.web.utils.Constants.SELVBETJENING
import no.nav.sosialhjelp.soknad.web.utils.Constants.TOKENX
import org.springframework.stereotype.Component

interface SubjectHandler {
    fun getConsumerId(): String
    fun getUserIdFromToken(): String
    fun getToken(): String
}

@Component
class SubjectHandlerImpl : SubjectHandler {

    private val tokenValidationContext: TokenValidationContext
        get() {
            return JaxrsTokenValidationContextHolder.getHolder().tokenValidationContext
                ?: throw RuntimeException("Could not find TokenValidationContext. Possibly no token in request.")
                    .also { log.error("Could not find TokenValidationContext. Possibly no token in request and request was not captured by token-validation filters.") }
        }

    override fun getUserIdFromToken(): String {
        return when {
            tokenValidationContext.hasTokenFor(TOKENX) -> getUserIdFromTokenXToken()
            else -> tokenValidationContext.getClaims(SELVBETJENING).subject
        }
    }

    private fun getUserIdFromTokenXToken(): String {
        val pid: String? = tokenValidationContext.getClaims(TOKENX).getStringClaim(CLAIM_PID)
        val sub: String? = tokenValidationContext.getClaims(TOKENX).subject
        return pid ?: sub ?: throw RuntimeException("Could not find any userId for tokenX in pid or sub claim")
    }

    override fun getToken(): String {
        return tokenValidationContext.getJwtToken(SELVBETJENING).tokenAsString
    }

    override fun getConsumerId(): String {
        return System.getProperty("systemuser.username") ?: "srvsoknadsosialhje"
    }

    companion object {
        private const val CLAIM_PID = "pid"
        private val log by logger()
    }
}

class StaticSubjectHandlerImpl : SubjectHandler {
    private val DEFAULT_USER = "11111111111"
    private val DEFAULT_TOKEN = "token"
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
