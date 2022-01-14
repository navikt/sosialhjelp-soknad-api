package no.nav.sosialhjelp.soknad.api.nedetid

import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Import

@Configuration
@Import(
    NedetidRessurs::class
)
open class NedetidConfig(
    @Value("\${nedetid.start}") private val nedetidStart: String?,
    @Value("\${nedetid.slutt}") private val nedetidSlutt: String?
) {

    @Bean
    open fun nedetidService(): NedetidService {
        return NedetidService(nedetidStart, nedetidSlutt)
    }
}
