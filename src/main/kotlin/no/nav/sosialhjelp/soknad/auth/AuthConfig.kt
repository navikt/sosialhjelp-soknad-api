package no.nav.sosialhjelp.soknad.auth

import no.nav.sosialhjelp.soknad.app.LoggingUtils.logger
import no.nav.sosialhjelp.soknad.auth.texas.TexasClient
import no.nav.sosialhjelp.soknad.auth.texas.TexasService
import no.nav.sosialhjelp.soknad.auth.texas.TexasServiceImpl
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Profile

@Configuration
class AuthConfig {
    @Bean
    @Profile("!prodfss")
    fun texasService(texasClient: TexasClient): TexasService {
        logger.info("Using TexasService for GCP")
        return TexasServiceImpl(texasClient)
    }

    companion object {
        private val logger by logger()
    }
}
