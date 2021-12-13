package no.nav.sosialhjelp.soknad.vedlegg

import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Import

@Configuration
@Import(
    OpplastetVedleggRessurs::class
)
open class VedleggConfig
