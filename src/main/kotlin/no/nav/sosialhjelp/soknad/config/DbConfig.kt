package no.nav.sosialhjelp.soknad.config

import no.nav.sosialhjelp.soknad.business.db.repositories.soknadmetadata.SoknadMetadataRepositoryJdbc
import no.nav.sosialhjelp.soknad.business.db.repositories.soknadunderarbeid.BatchSoknadUnderArbeidRepositoryJdbc
import no.nav.sosialhjelp.soknad.db.repositories.oppgave.OppgaveRepositoryJdbc
import no.nav.sosialhjelp.soknad.db.repositories.opplastetvedlegg.BatchOpplastetVedleggRepositoryJdbc
import no.nav.sosialhjelp.soknad.db.repositories.opplastetvedlegg.OpplastetVedleggRepositoryJdbc
import no.nav.sosialhjelp.soknad.db.repositories.sendtsoknad.BatchSendtSoknadRepositoryJdbc
import no.nav.sosialhjelp.soknad.db.repositories.sendtsoknad.SendtSoknadRepositoryJdbc
import no.nav.sosialhjelp.soknad.db.repositories.soknadmetadata.BatchSoknadMetadataRepositoryJdbc
import no.nav.sosialhjelp.soknad.db.repositories.soknadunderarbeid.SoknadUnderArbeidRepositoryJdbc
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Import
import java.time.Clock

@Configuration
@Import(
    SendtSoknadRepositoryJdbc::class,
    SoknadUnderArbeidRepositoryJdbc::class,
    OpplastetVedleggRepositoryJdbc::class,
    SoknadMetadataRepositoryJdbc::class,
    BatchOpplastetVedleggRepositoryJdbc::class,
    BatchSoknadUnderArbeidRepositoryJdbc::class,
    BatchSoknadMetadataRepositoryJdbc::class,
    BatchSendtSoknadRepositoryJdbc::class,
    OppgaveRepositoryJdbc::class,
    SoknadInnsendingDBConfig::class
)
open class DbConfig {

    @Bean
    open fun clock(): Clock {
        return Clock.systemDefaultZone()
    }
}
