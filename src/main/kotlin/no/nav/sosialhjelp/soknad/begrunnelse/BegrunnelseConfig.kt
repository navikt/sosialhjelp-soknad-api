package no.nav.sosialhjelp.soknad.begrunnelse

import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Import

@Configuration
@Import(BegrunnelseRessurs::class)
open class BegrunnelseConfig
