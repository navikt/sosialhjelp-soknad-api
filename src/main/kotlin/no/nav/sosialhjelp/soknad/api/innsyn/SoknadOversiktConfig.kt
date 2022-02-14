package no.nav.sosialhjelp.soknad.api.innsyn

import no.nav.sosialhjelp.soknad.business.db.repositories.soknadmetadata.SoknadMetadataRepository
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Import

@Configuration
@Import(SoknadOversiktRessurs::class)
open class SoknadOversiktConfig(
    private val soknadMetadataRepository: SoknadMetadataRepository
) {

    @Bean
    open fun soknadOversiktService(): SoknadOversiktService {
        return SoknadOversiktService(soknadMetadataRepository)
    }
}
