package no.nav.sosialhjelp.soknad.innsending.digisosapi

import no.nav.sosialhjelp.soknad.business.db.repositories.soknadunderarbeid.SoknadUnderArbeidRepository
import no.nav.sosialhjelp.soknad.business.pdfmedpdfbox.SosialhjelpPdfGenerator
import no.nav.sosialhjelp.soknad.business.service.HenvendelseService
import no.nav.sosialhjelp.soknad.business.service.soknadservice.SoknadMetricsService
import no.nav.sosialhjelp.soknad.consumer.fiks.DigisosApi
import no.nav.sosialhjelp.soknad.innsending.InnsendingService
import no.nav.sosialhjelp.soknad.innsending.soknadunderarbeid.SoknadUnderArbeidService
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
open class DigisosApiConfig {

    @Bean
    open fun digisosApiService(
        digisosApi: DigisosApi,
        sosialhjelpPdfGenerator: SosialhjelpPdfGenerator,
        innsendingService: InnsendingService,
        henvendelseService: HenvendelseService,
        soknadUnderArbeidService: SoknadUnderArbeidService,
        soknadMetricsService: SoknadMetricsService,
        soknadUnderArbeidRepository: SoknadUnderArbeidRepository
    ): DigisosApiService {
        return DigisosApiService(
            digisosApi,
            sosialhjelpPdfGenerator,
            innsendingService,
            henvendelseService,
            soknadUnderArbeidService,
            soknadMetricsService,
            soknadUnderArbeidRepository
        )
    }
}
