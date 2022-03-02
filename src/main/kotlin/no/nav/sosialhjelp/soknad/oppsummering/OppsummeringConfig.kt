package no.nav.sosialhjelp.soknad.oppsummering

import no.nav.sosialhjelp.soknad.db.repositories.opplastetvedlegg.OpplastetVedleggRepository
import no.nav.sosialhjelp.soknad.db.repositories.soknadunderarbeid.SoknadUnderArbeidRepository
import no.nav.sosialhjelp.soknad.vedlegg.OpplastetVedleggService
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Import

@Configuration
@Import(OppsummeringRessurs::class)
open class OppsummeringConfig(
    private val soknadUnderArbeidRepository: SoknadUnderArbeidRepository,
    private val opplastetVedleggRepository: OpplastetVedleggRepository,
    private val opplastetVedleggService: OpplastetVedleggService
) {

    @Bean
    open fun oppsummeringService(): OppsummeringService {
        return OppsummeringService(soknadUnderArbeidRepository, opplastetVedleggRepository, opplastetVedleggService)
    }
}
