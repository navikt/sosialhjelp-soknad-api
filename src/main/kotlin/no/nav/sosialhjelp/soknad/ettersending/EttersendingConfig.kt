package no.nav.sosialhjelp.soknad.ettersending

import no.finn.unleash.Unleash
import no.nav.sosialhjelp.soknad.business.db.repositories.soknadunderarbeid.SoknadUnderArbeidRepository
import no.nav.sosialhjelp.soknad.business.service.HenvendelseService
import no.nav.sosialhjelp.soknad.ettersending.innsendtsoknad.InnsendtSoknadService
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Import
import java.time.Clock

@Configuration
@Import(EttersendingRessurs::class)
open class EttersendingConfig(
    private val henvendelseService: HenvendelseService,
    private val soknadUnderArbeidRepository: SoknadUnderArbeidRepository,
    private val unleash: Unleash,
    private val clock: Clock
) {

    @Bean
    open fun innsendtSoknadService(): InnsendtSoknadService {
        return InnsendtSoknadService(henvendelseService)
    }

    @Bean
    open fun ettersendingService(): EttersendingService {
        return EttersendingService(henvendelseService, soknadUnderArbeidRepository, unleash, clock)
    }
}
