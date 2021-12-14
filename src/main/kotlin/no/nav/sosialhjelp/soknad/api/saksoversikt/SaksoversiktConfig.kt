package no.nav.sosialhjelp.soknad.api.saksoversikt

import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Import

@Configuration
@Import(
    SaksoversiktMetadataRessurs::class,
    SaksoversiktMetadataOidcRessurs::class
)
open class SaksoversiktConfig
