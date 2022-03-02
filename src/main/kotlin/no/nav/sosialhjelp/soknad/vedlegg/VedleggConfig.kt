package no.nav.sosialhjelp.soknad.vedlegg

import no.nav.sosialhjelp.soknad.business.db.repositories.soknadunderarbeid.SoknadUnderArbeidRepository
import no.nav.sosialhjelp.soknad.db.repositories.opplastetvedlegg.OpplastetVedleggRepository
import no.nav.sosialhjelp.soknad.vedlegg.virusscan.VirusScanner
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Import

@Configuration
@Import(
    OpplastetVedleggRessurs::class
)
open class VedleggConfig {

    @Bean
    open fun opplastetVedleggService(
        opplastetVedleggRepository: OpplastetVedleggRepository,
        soknadUnderArbeidRepository: SoknadUnderArbeidRepository,
        virusScanner: VirusScanner
    ): OpplastetVedleggService {
        return OpplastetVedleggService(opplastetVedleggRepository, soknadUnderArbeidRepository, virusScanner)
    }
}
