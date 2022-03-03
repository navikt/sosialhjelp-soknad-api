package no.nav.sosialhjelp.soknad.api.dialog

import no.nav.sosialhjelp.soknad.db.repositories.soknadmetadata.SoknadMetadataRepository
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Import

@Configuration
@Import(SistInnsendteSoknadRessurs::class)
open class DialogApiConfig(
    private val soknadMetadataRepository: SoknadMetadataRepository
) {

    @Bean
    open fun sistInnsendteSoknadService(): SistInnsendteSoknadService {
        return SistInnsendteSoknadService(soknadMetadataRepository)
    }
}
