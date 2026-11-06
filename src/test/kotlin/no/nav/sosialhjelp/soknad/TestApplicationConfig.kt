package no.nav.sosialhjelp.soknad

import no.nav.security.mock.oauth2.MockOAuth2Server
import org.springframework.boot.test.context.TestConfiguration
import org.springframework.context.annotation.Bean
import org.springframework.test.context.DynamicPropertyRegistrar

@TestConfiguration(proxyBeanMethods = false)
class TestApplicationConfig {
    @Bean(destroyMethod = "shutdown")
    fun mockOAuth2Server(): MockOAuth2Server =
        MockOAuth2Server().apply { start() }

    @Bean
    fun mockOAuth2ServerProperties(
        mockOAuth2Server: MockOAuth2Server,
    ): DynamicPropertyRegistrar =
        DynamicPropertyRegistrar { registry ->
            registry.add("mock-oauth2-server.port") {
                mockOAuth2Server.issuerUrl("selvbetjening").port
            }
        }
}
