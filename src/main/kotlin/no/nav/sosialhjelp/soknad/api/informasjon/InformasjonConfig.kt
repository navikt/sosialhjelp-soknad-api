package no.nav.sosialhjelp.soknad.api.informasjon

import no.nav.sosialhjelp.soknad.db.repositories.soknadmetadata.SoknadMetadataRepository
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Import

@Configuration
@Import(InformasjonRessurs::class)
open class InformasjonConfig(
    private val soknadMetadataRepository: SoknadMetadataRepository
) {

    @Bean
    open fun pabegynteSoknaderService(): PabegynteSoknaderService {
        return PabegynteSoknaderService(soknadMetadataRepository)
    }
}
