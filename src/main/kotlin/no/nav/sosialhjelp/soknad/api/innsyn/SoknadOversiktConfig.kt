package no.nav.sosialhjelp.soknad.api.innsyn

import no.nav.sosialhjelp.soknad.business.db.repositories.soknadmetadata.SoknadMetadataRepository
import no.nav.sosialhjelp.soknad.common.ServiceUtils
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Import

@Configuration
@Import(SoknadOversiktRessurs::class)
open class SoknadOversiktConfig(
    private val soknadMetadataRepository: SoknadMetadataRepository,
    private val serviceUtils: ServiceUtils
) {

    @Bean
    open fun soknadOversiktService(): SoknadOversiktService {
        return SoknadOversiktService(soknadMetadataRepository, serviceUtils)
    }
}
