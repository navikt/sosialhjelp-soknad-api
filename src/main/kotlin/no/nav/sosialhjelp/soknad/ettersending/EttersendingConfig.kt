package no.nav.sosialhjelp.soknad.ettersending

import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Import

@Configuration
@Import(EttersendingRessurs::class)
open class EttersendingConfig
