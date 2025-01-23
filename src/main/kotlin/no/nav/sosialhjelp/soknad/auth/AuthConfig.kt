package no.nav.sosialhjelp.soknad.auth

import no.nav.sosialhjelp.soknad.app.LoggingUtils.logger
import no.nav.sosialhjelp.soknad.auth.azure.AzureadService
import no.nav.sosialhjelp.soknad.auth.maskinporten.MaskinportenClient
import no.nav.sosialhjelp.soknad.auth.texas.ProdFssAuthService
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

    @Bean
    @Profile("prodfss")
    fun prodFssAuthService(
        azureadService: AzureadService,
        maskinportenClient: MaskinportenClient,
    ): TexasService {
        logger.info("Using ProdFssAuthService for FSS")
        return ProdFssAuthService(azureadService, maskinportenClient)
    }

    companion object {
        private val logger by logger()
    }
}
