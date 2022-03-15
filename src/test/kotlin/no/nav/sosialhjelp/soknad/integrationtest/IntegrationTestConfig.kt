package no.nav.sosialhjelp.soknad.integrationtest

import no.nav.security.token.support.spring.test.MockOAuth2ServerAutoConfiguration
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Import

@Import(MockOAuth2ServerAutoConfiguration::class)
@Configuration
open class IntegrationTestConfig
