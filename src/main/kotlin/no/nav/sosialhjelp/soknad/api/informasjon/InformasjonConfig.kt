package no.nav.sosialhjelp.soknad.api.informasjon

import no.nav.sosialhjelp.soknad.db.repositories.soknadmetadata.SoknadMetadataRepository
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
open class InformasjonConfig(
    private val soknadMetadataRepository: SoknadMetadataRepository
) {

    @Bean
    open fun pabegynteSoknaderService(): PabegynteSoknaderService {
        return PabegynteSoknaderService(soknadMetadataRepository)
    }
}
