package no.nav.sosialhjelp.soknad.api.nedetid

import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Import

@Configuration
@Import(
    NedetidRessurs::class
)
open class NedetidConfig
