package no.nav.sosialhjelp.soknad.health

import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Import

@Configuration
@Import(
    InternalRessurs::class
)
open class HealthConfig
