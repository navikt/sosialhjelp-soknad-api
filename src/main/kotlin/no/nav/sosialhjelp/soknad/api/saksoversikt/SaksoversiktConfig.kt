package no.nav.sosialhjelp.soknad.api.saksoversikt

import no.nav.sosialhjelp.soknad.business.db.repositories.soknadmetadata.SoknadMetadataRepository
import no.nav.sosialhjelp.soknad.ettersending.EttersendingService
import no.nav.sosialhjelp.soknad.tekster.NavMessageSource
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Import
import java.time.Clock

@Configuration
@Import(
    SaksoversiktMetadataOidcRessurs::class
)
open class SaksoversiktConfig {

    @Bean
    open fun saksoversiktMetadataService(
        soknadMetadataRepository: SoknadMetadataRepository,
        ettersendingService: EttersendingService,
        navMessageSource: NavMessageSource,
        clock: Clock
    ): SaksoversiktMetadataService {
        return SaksoversiktMetadataService(soknadMetadataRepository, ettersendingService, navMessageSource, clock)
    }
}
