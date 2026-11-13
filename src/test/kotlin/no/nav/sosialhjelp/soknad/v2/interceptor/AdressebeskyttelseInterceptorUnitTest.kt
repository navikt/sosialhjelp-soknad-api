package no.nav.sosialhjelp.soknad.v2.interceptor

import io.mockk.Called
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import no.nav.sosialhjelp.soknad.app.exceptions.AuthorizationException
import no.nav.sosialhjelp.soknad.app.security.principal.CitizenPrincipal
import no.nav.sosialhjelp.soknad.app.security.principal.EntraIdPrincipal
import no.nav.sosialhjelp.soknad.personalia.person.PersonService
import no.nav.sosialhjelp.soknad.v2.metadata.SoknadMetadataServiceImpl
import no.nav.sosialhjelp.soknad.v2.soknad.SoknadService
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.ValueSource
import org.springframework.mock.web.MockHttpServletRequest
import org.springframework.mock.web.MockHttpServletResponse
import org.springframework.security.authentication.AnonymousAuthenticationToken
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.oauth2.core.OAuth2AccessToken
import org.springframework.security.oauth2.jwt.Jwt
import org.springframework.security.oauth2.server.resource.authentication.BearerTokenAuthentication
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken
import java.util.UUID

class AdressebeskyttelseInterceptorUnitTest {
    private val personService = mockk<PersonService>()
    private val metadataService = mockk<SoknadMetadataServiceImpl>()
    private val soknadService = mockk<SoknadService>()
    private val interceptor = AdressebeskyttelseInterceptor(personService, metadataService, soknadService)

    @AfterEach
    fun cleanup() {
        SecurityContextHolder.clearContext()
    }

    @ParameterizedTest
    @ValueSource(strings = ["/informasjon/session", "/internal/isAlive", "/internal/prometheus", "/feature-toggle", "/v3/api-docs"])
    fun `missing authentication skips check regardless of path`(path: String) {
        assertThat(preHandle(path)).isTrue()
        verifyNoServiceCalls()
    }

    @Test
    fun `anonymous authentication skips check`() {
        SecurityContextHolder.getContext().authentication =
            AnonymousAuthenticationToken("test-key", "anonymousUser", listOf(SimpleGrantedAuthority("ROLE_ANONYMOUS")))

        assertThat(preHandle()).isTrue()
        verifyNoServiceCalls()
    }

    @Test
    fun `entra authentication skips check`() {
        val principal = EntraIdPrincipal(TOKEN, mapOf("sub" to "test-application"), emptyList())
        val token = OAuth2AccessToken(OAuth2AccessToken.TokenType.BEARER, TOKEN, null, null)
        SecurityContextHolder.getContext().authentication = BearerTokenAuthentication(principal, token, emptyList())

        assertThat(preHandle()).isTrue()
        verifyNoServiceCalls()
    }

    @Test
    fun `unauthenticated citizen skips check`() {
        authenticateCitizen()
        checkNotNull(SecurityContextHolder.getContext().authentication).isAuthenticated = false

        assertThat(preHandle()).isTrue()
        verifyNoServiceCalls()
    }

    @ParameterizedTest
    @ValueSource(booleans = [true, false])
    fun `authenticated citizen is checked for both authentication variants`(useJwt: Boolean) {
        authenticateCitizen(useJwt)
        every { personService.hasAdressebeskyttelse(USER_ID) } returns false

        assertThat(preHandle()).isTrue()
        verify(exactly = 1) { personService.hasAdressebeskyttelse(USER_ID) }
        verify(exactly = 0) { personService.onSendSoknadHasAdressebeskyttelse(any()) }
        verify { listOf(metadataService, soknadService) wasNot Called }
    }

    @ParameterizedTest
    @ValueSource(
        strings = [
            "/internal/isAlive",
            "/internal/prometheus",
            "/feature-toggle",
            "/internal/health",
            "/v3/api-docs",
            "/dittnav/pabegynte/aktive",
            "/minesaker/innsendte",
        ],
    )
    fun `citizen without address protection is allowed regardless of path`(path: String) {
        authenticateCitizen(useJwt = false)
        every { personService.hasAdressebeskyttelse(USER_ID) } returns false

        assertThat(preHandle(path)).isTrue()
        verify(exactly = 1) { personService.hasAdressebeskyttelse(USER_ID) }
        verify { listOf(metadataService, soknadService) wasNot Called }
    }

    @ParameterizedTest
    @ValueSource(
        strings = [
            "/informasjon/session",
            "/internal/isAlive",
            "/internal/prometheus",
            "/feature-toggle",
            "/dittnav/pabegynte/aktive",
            "/minesaker/innsendte",
        ],
    )
    fun `protected citizen is denied and open applications deleted regardless of path`(path: String) {
        authenticateCitizen(useJwt = false)
        val ids = listOf(UUID.randomUUID())
        every { personService.hasAdressebeskyttelse(USER_ID) } returns true
        every { soknadService.findOpenSoknadIds(USER_ID) } returns ids
        every { metadataService.deleteAll(ids) } returns Unit

        assertThrows<AuthorizationException> { preHandle(path) }
        verify(exactly = 1) { metadataService.deleteAll(ids) }
    }

    @Test
    fun `protected citizen without open applications is denied without deletion`() {
        authenticateCitizen()
        every { personService.hasAdressebeskyttelse(USER_ID) } returns true
        every { soknadService.findOpenSoknadIds(USER_ID) } returns emptyList()

        assertThrows<AuthorizationException> { preHandle() }
        verify { metadataService wasNot Called }
    }

    @Test
    fun `sending uses fresh lookup`() {
        authenticateCitizen()
        every { personService.onSendSoknadHasAdressebeskyttelse(USER_ID) } returns false

        assertThat(preHandle("/soknad/00000000-0000-4000-8000-000000000001/send")).isTrue()
        verify(exactly = 1) { personService.onSendSoknadHasAdressebeskyttelse(USER_ID) }
        verify(exactly = 0) { personService.hasAdressebeskyttelse(any()) }
    }

    @Test
    fun `unexpected lookup error propagates`() {
        authenticateCitizen()
        val failure = IllegalStateException("Lookup failed")
        every { personService.hasAdressebeskyttelse(USER_ID) } throws failure

        assertThat(assertThrows<IllegalStateException> { preHandle() }).isSameAs(failure)
        verify { listOf(metadataService, soknadService) wasNot Called }
    }

    private fun authenticateCitizen(useJwt: Boolean = true) {
        val claims = mapOf<String, Any>("sub" to USER_ID)
        val principal = CitizenPrincipal(TOKEN, claims, emptyList())
        SecurityContextHolder.getContext().authentication =
            if (useJwt) {
                val jwt = Jwt.withTokenValue(TOKEN).header("alg", "RS256").claims { it.putAll(claims) }.build()
                JwtAuthenticationToken(jwt, principal, emptyList())
            } else {
                val token = OAuth2AccessToken(OAuth2AccessToken.TokenType.BEARER, TOKEN, null, null)
                BearerTokenAuthentication(principal, token, emptyList())
            }
    }

    private fun preHandle(path: String = "/informasjon/session"): Boolean =
        interceptor.preHandle(
            MockHttpServletRequest("GET", "/sosialhjelp/soknad-api$path"),
            MockHttpServletResponse(),
            Any(),
        )

    private fun verifyNoServiceCalls() {
        verify { listOf(personService, metadataService, soknadService) wasNot Called }
    }

    companion object {
        private const val USER_ID = "test-person"
        private const val TOKEN = "test-token"
    }
}
