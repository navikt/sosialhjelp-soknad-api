package no.nav.sosialhjelp.soknad.innsending

import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Import

@Configuration
@Import(
    SoknadRessurs::class
)
open class InnsendingConfig
