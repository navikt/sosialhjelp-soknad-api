package no.nav.sosialhjelp.soknad.api.dittnav

import no.nav.sosialhjelp.soknad.db.repositories.soknadmetadata.SoknadMetadataRepository
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
open class DittNavConfig(
    private val soknadMetadataRepository: SoknadMetadataRepository
) {

    @Bean
    open fun dittNavMetadataService(): DittNavMetadataService {
        return DittNavMetadataService(soknadMetadataRepository)
    }
}
