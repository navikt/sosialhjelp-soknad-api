package no.nav.sosialhjelp.soknad.okonomiskeopplysninger

import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Import

@Configuration
@Import(
    OkonomiskeOpplysningerRessurs::class
)
open class OkonomiskeOpplysningerConfig
