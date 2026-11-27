package no.nav.sosialhjelp.soknad.app.security.principal

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.oauth2.core.OAuth2AuthenticationException

class PrincipalTest {
    @Test
    fun `user principal exposes token and prefers pid over sub`() {
        val attributes = mapOf("pid" to "test-person", "sub" to "test-subject")
        val principal = CitizenPrincipal(TEST_TOKEN, attributes, listOf(AUTHORITY))

        assertThat(principal.userId).isEqualTo("test-person")
        assertThat(principal.name).isEqualTo(principal.userId)
        assertThat(principal.token).isEqualTo(TEST_TOKEN)
        assertThat(principal.attributes).isEqualTo(attributes)
        assertThat(principal.authorities).containsExactly(AUTHORITY)
        assertThat(principal.getAttribute<String>("pid")).isEqualTo("test-person")
    }

    @Test
    fun `user principal falls back to sub when pid is missing blank or not a string`() {
        listOf(
            mapOf("sub" to "test-subject"),
            mapOf("pid" to " ", "sub" to "test-subject"),
            mapOf("pid" to 123, "sub" to "test-subject"),
        ).forEach { attributes ->
            assertThat(CitizenPrincipal(TEST_TOKEN, attributes, emptyList()).userId).isEqualTo("test-subject")
        }
    }

    @Test
    fun `user principal rejects missing blank or non-string identity`() {
        listOf(
            emptyMap(),
            mapOf("pid" to "", "sub" to " "),
            mapOf("pid" to 123, "sub" to 456),
        ).forEach { attributes ->
            val exception =
                assertThrows<OAuth2AuthenticationException> {
                    CitizenPrincipal(TEST_TOKEN, attributes, emptyList())
                }

            assertThat(exception.error.errorCode).isEqualTo("invalid_token")
        }
    }

    @Test
    fun `user principal rejects blank token`() {
        listOf("", " ").forEach { token ->
            val exception =
                assertThrows<OAuth2AuthenticationException> {
                    CitizenPrincipal(token, mapOf("sub" to "test-subject"), emptyList())
                }

            assertThat(exception.error.errorCode).isEqualTo("invalid_token")
        }
    }

    @Test
    fun `entra principal exposes token and delegates attributes without requiring person identity`() {
        val attributes = mapOf("sub" to "test-application")
        val principal = EntraIdPrincipal(TEST_TOKEN, attributes, listOf(AUTHORITY))

        assertThat(principal.token).isEqualTo(TEST_TOKEN)
        assertThat(principal.name).isEqualTo("test-application")
        assertThat(principal.attributes).isEqualTo(attributes)
        assertThat(principal.authorities).containsExactly(AUTHORITY)
        assertThat(principal.getAttribute<String>("sub")).isEqualTo("test-application")
    }

    @Test
    fun `entra principal rejects blank token`() {
        listOf("", " ").forEach { token ->
            val exception =
                assertThrows<OAuth2AuthenticationException> {
                    EntraIdPrincipal(token, mapOf("sub" to "test-application"), emptyList())
                }

            assertThat(exception.error.errorCode).isEqualTo("invalid_token")
        }
    }

    companion object {
        private const val TEST_TOKEN = "test-token"
        private val AUTHORITY = SimpleGrantedAuthority("test-authority")
    }
}
