package no.nav.sosialhjelp.soknad.api.innsyn

import no.nav.sosialhjelp.soknad.db.repositories.soknadmetadata.SoknadMetadataRepository
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
open class SoknadOversiktConfig(
    private val soknadMetadataRepository: SoknadMetadataRepository
) {

    @Bean
    open fun soknadOversiktService(): SoknadOversiktService {
        return SoknadOversiktService(soknadMetadataRepository)
    }
}
