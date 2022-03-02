package no.nav.sosialhjelp.soknad.innsending.soknadunderarbeid

import no.nav.sosialhjelp.soknad.db.repositories.soknadunderarbeid.SoknadUnderArbeidRepository
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
open class SoknadUnderArbeidConfig {

    @Bean
    open fun soknadUnderArbeidService(soknadUnderArbeidRepository: SoknadUnderArbeidRepository): SoknadUnderArbeidService {
        return SoknadUnderArbeidService(soknadUnderArbeidRepository)
    }
}
