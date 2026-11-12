package no.nav.sosialhjelp.soknad.app.security

import com.ninjasquad.springmockk.MockkBean
import io.mockk.every
import io.mockk.verify
import jakarta.servlet.Filter
import jakarta.servlet.http.HttpServletRequest
import no.nav.sosialhjelp.soknad.app.security.principal.CitizenPrincipal
import no.nav.sosialhjelp.soknad.app.security.texas.IdentityProvider
import no.nav.sosialhjelp.soknad.app.security.texas.TexasClient
import no.nav.sosialhjelp.soknad.app.security.texas.TexasServiceImpl
import no.nav.sosialhjelp.soknad.app.security.texas.TokenResponse
import no.nav.sosialhjelp.soknad.app.subjecthandler.DefaultSubjectHandler
import org.assertj.core.api.Assertions.assertThat
import org.hamcrest.Matchers.containsString
import org.hamcrest.Matchers.not
import org.hamcrest.Matchers.startsWith
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.EnumSource
import org.junit.jupiter.params.provider.MethodSource
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Import
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpMethod
import org.springframework.security.authentication.AuthenticationServiceException
import org.springframework.security.core.Authentication
import org.springframework.security.oauth2.jwt.BadJwtException
import org.springframework.security.oauth2.jwt.Jwt
import org.springframework.security.oauth2.jwt.JwtDecoder
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken
import org.springframework.test.context.junit.jupiter.web.SpringJUnitWebConfig
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.ResultActions
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.request
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.content
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.header
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import org.springframework.test.web.servlet.setup.DefaultMockMvcBuilder
import org.springframework.test.web.servlet.setup.MockMvcBuilders
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.context.WebApplicationContext
import org.springframework.web.servlet.config.annotation.EnableWebMvc
import java.security.Principal
import java.time.Instant

@SpringJUnitWebConfig(SecurityFilterChainTest.TestConfig::class)
class SecurityFilterChainTest {
    @Autowired
    private lateinit var context: WebApplicationContext

    @Autowired
    @Qualifier("springSecurityFilterChain")
    private lateinit var securityFilter: Filter

    @MockkBean
    private lateinit var texasClient: TexasClient

    @MockkBean
    private lateinit var jwtDecoder: JwtDecoder

    private lateinit var mockMvc: MockMvc

    @BeforeEach
    fun setup() {
        mockMvc =
            MockMvcBuilders.webAppContextSetup(context)
                .addFilters<DefaultMockMvcBuilder>(securityFilter)
                .build()
    }

    @ParameterizedTest
    @EnumSource(Endpoint::class)
    fun `missing token gives a Bearer challenge without calling Texas`(endpoint: Endpoint) {
        perform(endpoint)
            .andExpect(status().isUnauthorized)
            .andExpect(header().string(HttpHeaders.WWW_AUTHENTICATE, startsWith("Bearer")))
            .andExpect(header().string(HttpHeaders.WWW_AUTHENTICATE, not(containsString("error="))))

        verify(exactly = 0) { texasClient.validateToken(any(), any()) }
        verify(exactly = 0) { jwtDecoder.decode(any()) }
    }

    @ParameterizedTest
    @EnumSource(Endpoint::class)
    fun `inactive token gives invalid_token`(endpoint: Endpoint) {
        stubValidation(endpoint.provider, TEST_TOKEN, active = false)

        perform(endpoint, TEST_TOKEN)
            .andExpect(status().isUnauthorized)
            .andExpect(header().string(HttpHeaders.WWW_AUTHENTICATE, containsString("error=\"invalid_token\"")))

        verifyValidation(endpoint, TEST_TOKEN)
    }

    @ParameterizedTest
    @MethodSource("wrongProviderCases")
    fun `token for another provider is rejected`(
        endpoint: Endpoint,
        tokenProvider: IdentityProvider,
    ) {
        val token = "${tokenProvider.value}-token"
        stubValidation(tokenProvider, token, active = true, acr = "idporten-loa-high")
        stubValidation(endpoint.provider, token, active = false)

        perform(endpoint, token)
            .andExpect(status().isUnauthorized)
            .andExpect(header().string(HttpHeaders.WWW_AUTHENTICATE, containsString("error=\"invalid_token\"")))

        verifyValidation(endpoint, token)
        verify(exactly = 0) { texasClient.validateToken(tokenProvider.value, any()) }
    }

    @ParameterizedTest
    @MethodSource("tokenXAcrCases")
    fun `TokenX endpoints enforce their ACR requirements`(
        endpoint: Endpoint,
        acr: String?,
        expectedStatus: Int,
    ) {
        stubValidation(IdentityProvider.TOKENX, TEST_TOKEN, active = true, acr = acr)

        val result =
            perform(endpoint, TEST_TOKEN)
                .andExpect(status().`is`(expectedStatus))

        if (expectedStatus == 200) {
            result.andExpect(content().string(TEST_SUBJECT))
        } else {
            result.andExpect(header().string(HttpHeaders.WWW_AUTHENTICATE, containsString("error=\"insufficient_scope\"")))
        }

        verifyValidation(endpoint, TEST_TOKEN)
    }

    @ParameterizedTest
    @EnumSource(value = Endpoint::class, names = ["UNRESOLVED", "RESOLVE"])
    fun `Entra ID endpoints accept active tokens without ACR`(endpoint: Endpoint) {
        stubValidation(IdentityProvider.ENTRA_ID, TEST_TOKEN, active = true)

        perform(endpoint, TEST_TOKEN)
            .andExpect(status().isOk)
            .andExpect(content().string(TEST_SUBJECT))

        verifyValidation(endpoint, TEST_TOKEN)
    }

    @Test
    fun `tokenx request provides citizen principal to subject handler`() {
        every { texasClient.validateToken(IdentityProvider.TOKENX.value, TEST_TOKEN) } returns
            TokenResponse.ValidationResponse(active = true).apply {
                claims["iss"] = "https://tokenx.example"
                claims["pid"] = "test-person"
                claims["sub"] = TEST_SUBJECT
                claims["acr"] = "idporten-loa-high"
            }
        mockMvc.perform(
            request(HttpMethod.GET, Endpoint.INNSENDTE.uri)
                .param("checkSubjectHandler", "true")
                .header(HttpHeaders.AUTHORIZATION, "Bearer $TEST_TOKEN"),
        )
            .andExpect(status().isOk)
            .andExpect(content().string("test-person"))
        verifyValidation(Endpoint.INNSENDTE, TEST_TOKEN)
    }

    @Test
    fun `tokenx token without user identity is rejected as invalid token`() {
        every { texasClient.validateToken(IdentityProvider.TOKENX.value, TEST_TOKEN) } returns
            TokenResponse.ValidationResponse(active = true).apply {
                claims["acr"] = "idporten-loa-high"
            }

        perform(Endpoint.INNSENDTE, TEST_TOKEN)
            .andExpect(status().isUnauthorized)
            .andExpect(header().string(HttpHeaders.WWW_AUTHENTICATE, containsString("error=\"invalid_token\"")))
        verifyValidation(Endpoint.INNSENDTE, TEST_TOKEN)
    }

    @ParameterizedTest
    @EnumSource(Endpoint::class)
    fun `unexpected validation failure propagates as an authentication service failure`(endpoint: Endpoint) {
        every { texasClient.validateToken(endpoint.provider.value, TEST_TOKEN) } throws
            IllegalStateException("Unexpected validation failure")

        val exception = assertThrows<AuthenticationServiceException> { perform(endpoint, TEST_TOKEN) }
        assertThat(exception).hasRootCauseInstanceOf(IllegalStateException::class.java)

        verifyValidation(endpoint, TEST_TOKEN)
    }

    @Test
    fun `validated idporten jwt provides citizen principal and preserves jwt and scopes`() {
        val jwt =
            Jwt.withTokenValue(TEST_TOKEN)
                .header("alg", "RS256")
                .claim("iss", "https://idporten.example")
                .claim("sub", TEST_SUBJECT)
                .claim("pid", "test-person")
                .claim("scope", "read write")
                .issuedAt(Instant.parse("2026-01-01T00:00:00Z"))
                .expiresAt(Instant.parse("2027-01-01T00:00:00Z"))
                .build()
        every { jwtDecoder.decode(TEST_TOKEN) } returns jwt

        mockMvc.perform(
            request(HttpMethod.GET, "/test/idporten")
                .header(HttpHeaders.AUTHORIZATION, "Bearer $TEST_TOKEN"),
        )
            .andExpect(status().isOk)
            .andExpect(content().string("authenticated"))

        verify(exactly = 1) { jwtDecoder.decode(TEST_TOKEN) }
        verify(exactly = 0) { texasClient.validateToken(any(), any()) }
    }

    @Test
    fun `idporten request provides citizen principal to subject handler`() {
        val jwt =
            Jwt.withTokenValue(TEST_TOKEN)
                .header("alg", "RS256")
                .claim("iss", "https://idporten.example")
                .claim("sub", TEST_SUBJECT)
                .claim("pid", "test-person")
                .claim("scope", "read write")
                .build()
        every { jwtDecoder.decode(TEST_TOKEN) } returns jwt
        mockMvc.perform(
            request(HttpMethod.GET, "/test/idporten")
                .param("checkSubjectHandler", "true")
                .header(HttpHeaders.AUTHORIZATION, "Bearer $TEST_TOKEN"),
        )
            .andExpect(status().isOk)
            .andExpect(content().string("authenticated"))
        verify(exactly = 1) { jwtDecoder.decode(TEST_TOKEN) }
        verify(exactly = 0) { texasClient.validateToken(any(), any()) }
    }

    @Test
    fun `idporten jwt without user identity is rejected`() {
        val jwt =
            Jwt.withTokenValue(TEST_TOKEN)
                .header("alg", "RS256")
                .claim("iss", "https://idporten.example")
                .build()
        every { jwtDecoder.decode(TEST_TOKEN) } returns jwt

        mockMvc.perform(
            request(HttpMethod.GET, "/test/idporten")
                .header(HttpHeaders.AUTHORIZATION, "Bearer $TEST_TOKEN"),
        )
            .andExpect(status().isUnauthorized)
            .andExpect(header().string(HttpHeaders.WWW_AUTHENTICATE, containsString("error=\"invalid_token\"")))
    }

    @Test
    fun `rejected idporten jwt does not reach principal converter`() {
        every { jwtDecoder.decode(TEST_TOKEN) } throws BadJwtException("Invalid JWT")

        mockMvc.perform(
            request(HttpMethod.GET, "/test/idporten")
                .header(HttpHeaders.AUTHORIZATION, "Bearer $TEST_TOKEN"),
        )
            .andExpect(status().isUnauthorized)
            .andExpect(header().string(HttpHeaders.WWW_AUTHENTICATE, containsString("error=\"invalid_token\"")))
    }

    @Test
    fun `default chain permits public endpoints without authentication`() {
        mockMvc.perform(request(HttpMethod.GET, "/internal/test"))
            .andExpect(status().isOk)
            .andExpect(content().string("public"))

        verify(exactly = 0) { jwtDecoder.decode(any()) }
        verify(exactly = 0) { texasClient.validateToken(any(), any()) }
    }

    @Test
    fun `default chain rejects requests without authentication`() {
        mockMvc.perform(request(HttpMethod.GET, "/test/idporten"))
            .andExpect(status().isUnauthorized)

        verify(exactly = 0) { jwtDecoder.decode(any()) }
        verify(exactly = 0) { texasClient.validateToken(any(), any()) }
    }

    private fun perform(
        endpoint: Endpoint,
        token: String? = null,
    ): ResultActions {
        val request = request(endpoint.method, endpoint.uri)
        if (token != null) {
            request.header(HttpHeaders.AUTHORIZATION, "Bearer $token")
        }
        return mockMvc.perform(request)
    }

    private fun stubValidation(
        provider: IdentityProvider,
        token: String,
        active: Boolean,
        acr: String? = null,
    ) {
        every { texasClient.validateToken(provider.value, token) } returns
            TokenResponse.ValidationResponse(active = active).apply {
                if (active) {
                    claims["sub"] = TEST_SUBJECT
                    if (acr != null) {
                        claims["acr"] = acr
                    }
                }
            }
    }

    private fun verifyValidation(
        endpoint: Endpoint,
        token: String,
    ) {
        verify(exactly = 1) { texasClient.validateToken(endpoint.provider.value, token) }
        verify(exactly = 1) { texasClient.validateToken(any(), any()) }
        verify(exactly = 0) { jwtDecoder.decode(any()) }
    }

    enum class Endpoint(val uri: String, val provider: IdentityProvider, val method: HttpMethod = HttpMethod.GET) {
        INNSENDTE("/minesaker/innsendte", IdentityProvider.TOKENX),
        AKTIVE("/dittnav/pabegynte/aktive", IdentityProvider.TOKENX),
        UNRESOLVED("/admin/unresolved", IdentityProvider.ENTRA_ID),
        RESOLVE("/admin/soknad/00000000-0000-0000-0000-000000000001/resolve", IdentityProvider.ENTRA_ID, HttpMethod.POST),
    }

    @Configuration(proxyBeanMethods = false)
    @EnableWebMvc
    @Import(SecurityConfig::class, TexasServiceImpl::class, TestController::class)
    class TestConfig

    @RestController
    class TestController {
        @RequestMapping(
            "/minesaker/innsendte",
            "/dittnav/pabegynte/aktive",
            "/admin/unresolved",
            "/admin/soknad/{soknadId}/resolve",
        )
        fun authenticated(
            principal: Principal,
            request: HttpServletRequest,
        ): String {
            if (request.getParameter("checkSubjectHandler") != null) {
                val authentication = principal as Authentication
                assertThat(authentication.principal).isInstanceOf(CitizenPrincipal::class.java)
                assertThat(DefaultSubjectHandler.getUserIdFromToken()).isEqualTo("test-person")
                assertThat(DefaultSubjectHandler.getToken()).isEqualTo(TEST_TOKEN)
            }
            return principal.name
        }

        @RequestMapping("/test/idporten")
        fun idporten(
            authentication: Authentication,
            request: HttpServletRequest,
        ): String {
            assertThat(authentication).isInstanceOf(JwtAuthenticationToken::class.java)
            val jwtAuthentication = authentication as JwtAuthenticationToken
            assertThat(jwtAuthentication.principal).isInstanceOf(CitizenPrincipal::class.java)
            val principal = jwtAuthentication.principal as CitizenPrincipal
            assertThat(principal.userId).isEqualTo("test-person")
            assertThat(principal.token).isEqualTo(TEST_TOKEN)
            assertThat(principal.attributes).isEqualTo(jwtAuthentication.token.claims)
            assertThat(jwtAuthentication.tokenAttributes).isEqualTo(jwtAuthentication.token.claims)
            assertThat(jwtAuthentication.isAuthenticated).isTrue()
            assertThat(jwtAuthentication.token.tokenValue).isEqualTo(TEST_TOKEN)
            assertThat(jwtAuthentication.name).isEqualTo(principal.userId)
            assertThat(principal.authorities.map { it.authority }).containsExactlyInAnyOrder("SCOPE_read", "SCOPE_write")
            assertThat(jwtAuthentication.authorities.map { it.authority }).contains("SCOPE_read", "SCOPE_write")
            if (request.getParameter("checkSubjectHandler") != null) {
                assertThat(DefaultSubjectHandler.getUserIdFromToken()).isEqualTo("test-person")
                assertThat(DefaultSubjectHandler.getToken()).isEqualTo(TEST_TOKEN)
            }
            return "authenticated"
        }

        @RequestMapping("/internal/test")
        fun publicEndpoint(): String = "public"
    }

    companion object {
        private const val TEST_TOKEN = "test-token"
        private const val TEST_SUBJECT = "test-user"

        @JvmStatic
        fun wrongProviderCases(): List<Arguments> =
            Endpoint.entries.flatMap { endpoint ->
                listOf(IdentityProvider.TOKENX, IdentityProvider.ENTRA_ID, IdentityProvider.IDPORTEN)
                    .filter { it != endpoint.provider }
                    .map { Arguments.of(endpoint, it) }
            }

        @JvmStatic
        fun tokenXAcrCases(): List<Arguments> =
            Endpoint.entries.filter { it.provider == IdentityProvider.TOKENX }.flatMap { endpoint ->
                listOf(null, "unknown-acr", "Level2", "Level3", "idporten-loa-substantial", "Level4", "idporten-loa-high")
                    .map { acr ->
                        val allowed =
                            when (acr) {
                                "Level3", "idporten-loa-substantial", "Level4", "idporten-loa-high" -> true
                                else -> false
                            }
                        Arguments.of(endpoint, acr, if (allowed) 200 else 403)
                    }
            }
    }
}
