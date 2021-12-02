package no.nav.sosialhjelp.soknad.inntekt

import no.nav.sosialhjelp.soknad.inntekt.andreinntekter.UtbetalingRessurs
import no.nav.sosialhjelp.soknad.inntekt.formue.FormueRessurs
import no.nav.sosialhjelp.soknad.inntekt.husbanken.BostotteRessurs
import no.nav.sosialhjelp.soknad.inntekt.navutbetalinger.SystemregistrertInntektRessurs
import no.nav.sosialhjelp.soknad.inntekt.studielan.StudielanRessurs
import no.nav.sosialhjelp.soknad.inntekt.verdi.VerdiRessurs
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Import

@Configuration
@Import(
    StudielanRessurs::class,
    BostotteRessurs::class,
    FormueRessurs::class,
    VerdiRessurs::class,
    SystemregistrertInntektRessurs::class,
    UtbetalingRessurs::class,
)
open class InntektConfig
