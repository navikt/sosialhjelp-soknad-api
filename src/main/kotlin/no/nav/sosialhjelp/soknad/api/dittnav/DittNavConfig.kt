package no.nav.sosialhjelp.soknad.api.dittnav

import no.nav.sosialhjelp.soknad.business.db.repositories.soknadmetadata.SoknadMetadataRepository
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Import

@Configuration
@Import(
    DittNavMetadataRessurs::class
)
open class DittNavConfig(
    private val soknadMetadataRepository: SoknadMetadataRepository
) {

    @Bean
    open fun dittNavMetadataService(): DittNavMetadataService {
        return DittNavMetadataService(soknadMetadataRepository)
    }
}
