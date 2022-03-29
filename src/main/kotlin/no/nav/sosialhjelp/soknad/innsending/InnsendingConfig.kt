package no.nav.sosialhjelp.soknad.innsending

import no.nav.sosialhjelp.soknad.common.systemdata.SystemdataUpdater
import no.nav.sosialhjelp.soknad.db.repositories.opplastetvedlegg.OpplastetVedleggRepository
import no.nav.sosialhjelp.soknad.db.repositories.sendtsoknad.SendtSoknadRepository
import no.nav.sosialhjelp.soknad.db.repositories.soknadmetadata.SoknadMetadataRepository
import no.nav.sosialhjelp.soknad.db.repositories.soknadunderarbeid.SoknadUnderArbeidRepository
import no.nav.sosialhjelp.soknad.ettersending.EttersendingService
import no.nav.sosialhjelp.soknad.innsending.soknadunderarbeid.SoknadUnderArbeidService
import no.nav.sosialhjelp.soknad.innsending.svarut.OppgaveHandterer
import no.nav.sosialhjelp.soknad.inntekt.husbanken.BostotteSystemdata
import no.nav.sosialhjelp.soknad.inntekt.skattbarinntekt.SkatteetatenSystemdata
import no.nav.sosialhjelp.soknad.metrics.SoknadMetricsService
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.transaction.support.TransactionTemplate
import java.time.Clock

@Configuration
open class InnsendingConfig {

    @Bean
    open fun innsendingService(
        transactionTemplate: TransactionTemplate,
        sendtSoknadRepository: SendtSoknadRepository,
        soknadUnderArbeidRepository: SoknadUnderArbeidRepository,
        opplastetVedleggRepository: OpplastetVedleggRepository,
        soknadUnderArbeidService: SoknadUnderArbeidService,
        soknadMetadataRepository: SoknadMetadataRepository
    ): InnsendingService {
        return InnsendingService(
            transactionTemplate,
            sendtSoknadRepository,
            soknadUnderArbeidRepository,
            opplastetVedleggRepository,
            soknadUnderArbeidService,
            soknadMetadataRepository
        )
    }

    @Bean
    open fun soknadService(
        henvendelseService: HenvendelseService,
        oppgaveHandterer: OppgaveHandterer,
        soknadMetricsService: SoknadMetricsService,
        innsendingService: InnsendingService,
        ettersendingService: EttersendingService,
        soknadUnderArbeidRepository: SoknadUnderArbeidRepository,
        systemdataUpdater: SystemdataUpdater,
        bostotteSystemdata: BostotteSystemdata,
        skatteetatenSystemdata: SkatteetatenSystemdata
    ): SoknadService {
        return SoknadService(
            henvendelseService,
            oppgaveHandterer,
            soknadMetricsService,
            innsendingService,
            ettersendingService,
            soknadUnderArbeidRepository,
            systemdataUpdater,
            bostotteSystemdata,
            skatteetatenSystemdata
        )
    }

    @Bean
    open fun henvendelseService(
        soknadMetadataRepository: SoknadMetadataRepository,
        clock: Clock
    ): HenvendelseService {
        return HenvendelseService(soknadMetadataRepository, clock)
    }
}
