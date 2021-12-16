package no.nav.sosialhjelp.soknad.ettersending

import no.nav.sosialhjelp.soknad.business.service.HenvendelseService
import no.nav.sosialhjelp.soknad.ettersending.innsendtsoknad.InnsendtSoknadService
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Import

@Configuration
@Import(EttersendingRessurs::class)
open class EttersendingConfig(
    private val henvendelseService: HenvendelseService
) {

    @Bean
    open fun innsendtSoknadService(): InnsendtSoknadService {
        return InnsendtSoknadService(henvendelseService)
    }
}
