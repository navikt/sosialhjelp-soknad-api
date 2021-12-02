package no.nav.sosialhjelp.soknad.inntekt

import no.nav.sosialhjelp.soknad.inntekt.husbanken.BostotteRessurs
import no.nav.sosialhjelp.soknad.inntekt.studielan.StudielanRessurs
import no.nav.sosialhjelp.soknad.inntekt.verdi.VerdiRessurs
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Import

@Configuration
@Import(
    StudielanRessurs::class,
    BostotteRessurs::class,
    VerdiRessurs::class
)
open class InntektConfig
