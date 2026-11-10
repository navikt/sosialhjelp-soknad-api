package no.nav.sosialhjelp.soknad

import no.nav.security.mock.oauth2.MockOAuth2Server
import org.springframework.beans.factory.support.DefaultSingletonBeanRegistry
import org.springframework.boot.test.context.TestConfiguration
import org.springframework.boot.test.util.TestPropertyValues
import org.springframework.context.ApplicationContextInitializer
import org.springframework.context.ConfigurableApplicationContext

@TestConfiguration(proxyBeanMethods = false)
class MockOAuth2Initializer : ApplicationContextInitializer<ConfigurableApplicationContext> {
    override fun initialize(applicationContext: ConfigurableApplicationContext) {
        val beanFactory = applicationContext.beanFactory
        check(beanFactory is DefaultSingletonBeanRegistry) {
            "MockOAuth2Initializer requires DefaultSingletonBeanRegistry"
        }

        val server = MockOAuth2Server().apply { start() }

        beanFactory.registerSingleton("mockOAuth2Server", server)
        beanFactory.registerDisposableBean(
            "mockOAuth2Server",
        ) { server.shutdown() }

        val issuerUrl = server.issuerUrl("idporten")
        val tokenXIssuerUrl = server.issuerUrl("tokenx")

        TestPropertyValues.of(
            "mock-oauth2-server.port=${issuerUrl.port}",
            "spring.security.oauth2.resourceserver.jwt.issuer-uri=$issuerUrl",
            "idporten.issuer=$issuerUrl",
            "IDPORTEN_ISSUER=$issuerUrl",
            "TOKEN_X_ISSUER=$tokenXIssuerUrl",
        ).applyTo(applicationContext)
    }
}
