package no.nav.sosialhjelp.soknad.api.minesaker

import no.nav.sosialhjelp.soknad.db.repositories.soknadmetadata.SoknadMetadataRepository
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Import

@Configuration
@Import(
    MineSakerMetadataRessurs::class
)
open class MineSakerConfig(
    private val soknadMetadataRepository: SoknadMetadataRepository
) {
    @Bean
    open fun mineSakerMetadataService(): MineSakerMetadataService {
        return MineSakerMetadataService(soknadMetadataRepository)
    }
}
