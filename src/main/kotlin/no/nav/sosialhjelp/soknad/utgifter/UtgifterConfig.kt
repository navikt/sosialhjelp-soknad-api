package no.nav.sosialhjelp.soknad.utgifter

import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Import

@Configuration
@Import(
    BarneutgiftRessurs::class,
    BoutgiftRessurs::class
)
open class UtgifterConfig
