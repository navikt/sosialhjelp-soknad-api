package no.nav.sosialhjelp.soknad.api.dittnav

import no.nav.sosialhjelp.soknad.business.db.repositories.soknadmetadata.SoknadMetadataRepository
import no.nav.sosialhjelp.soknad.common.ServiceUtils
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Import

@Configuration
@Import(
    DittNavMetadataRessurs::class
)
open class DittNavConfig(
    private val soknadMetadataRepository: SoknadMetadataRepository,
    private val serviceUtils: ServiceUtils
) {

    @Bean
    open fun dittNavMetadataService(): DittNavMetadataService {
        return DittNavMetadataService(soknadMetadataRepository, serviceUtils)
    }
}
