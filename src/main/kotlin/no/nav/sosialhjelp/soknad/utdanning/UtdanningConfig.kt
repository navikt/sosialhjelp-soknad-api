package no.nav.sosialhjelp.soknad.utdanning

import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Import

@Configuration
@Import(
    UtdanningRessurs::class
)
open class UtdanningConfig
