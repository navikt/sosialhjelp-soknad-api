package no.nav.sosialhjelp.soknad.oppsummering

import no.nav.sosialhjelp.soknad.business.db.repositories.opplastetvedlegg.OpplastetVedleggRepository
import no.nav.sosialhjelp.soknad.business.db.repositories.soknadunderarbeid.SoknadUnderArbeidRepository
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Import

@Configuration
@Import(OppsummeringRessurs::class)
open class OppsummeringConfig(
    private val soknadUnderArbeidRepository: SoknadUnderArbeidRepository,
    private val opplastetVedleggRepository: OpplastetVedleggRepository
) {

    @Bean
    open fun oppsummeringService(): OppsummeringService {
        return OppsummeringService(soknadUnderArbeidRepository, opplastetVedleggRepository)
    }
}
