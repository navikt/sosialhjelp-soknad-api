package no.nav.sosialhjelp.soknad.app.annotation

import no.nav.security.token.support.core.api.ProtectedWithClaims
import no.nav.sosialhjelp.soknad.app.Constants

/**
 * Beskyttet ressurs som krever LoA High (tidl. "level 4") fra TokenX.
 *
 * Eventuelle exceptions blir håndtert av no.nav.sosialhjelp.soknad.app.exceptions.ExceptionMapper
 * Se også dokumentasjon av sikkerhetsnivåer https://doc.nais.io/security/auth/idporten/#security-levels
 *
 * @throws no.nav.security.token.support.spring.validation.interceptor.JwtTokenUnauthorizedException Ved autentiseringsfeil
 * @see no.nav.sosialhjelp.soknad.app.exceptions.ExceptionMapper Håndtering av JwtTokenUnauthorizedException
 * @see no.nav.security.token.support.spring.validation.interceptor.JwtTokenHandlerInterceptor.preHandle Interceptor
 * @see no.nav.security.token.support.core.validation.JwtTokenAnnotationHandler.handleProtectedWithClaims Token-validering
 */
@ProtectedWithClaims(
    issuer = Constants.TOKENX, claimMap = [Constants.CLAIM_ACR_LEVEL_4, Constants.CLAIM_ACR_LOA_HIGH], combineWithOr = true
)
annotation class ProtectionTokenXHigh
