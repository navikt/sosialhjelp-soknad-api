package no.nav.sosialhjelp.soknad.api.dialog

import no.nav.sosialhjelp.soknad.db.repositories.soknadmetadata.SoknadMetadataRepository
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
open class DialogApiConfig(
    private val soknadMetadataRepository: SoknadMetadataRepository
) {

    @Bean
    open fun sistInnsendteSoknadService(): SistInnsendteSoknadService {
        return SistInnsendteSoknadService(soknadMetadataRepository)
    }
}
