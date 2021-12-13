package no.nav.sosialhjelp.soknad.bosituasjon

import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Import

@Configuration
@Import(BosituasjonRessurs::class)
open class BosituasjonConfig
