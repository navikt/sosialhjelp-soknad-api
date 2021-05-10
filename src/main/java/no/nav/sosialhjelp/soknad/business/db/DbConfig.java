package no.nav.sosialhjelp.soknad.business.db;

import no.nav.sosialhjelp.soknad.business.db.oppgave.OppgaveRepositoryJdbc;
import no.nav.sosialhjelp.soknad.business.db.soknad.SoknadInnsendingDBConfig;
import no.nav.sosialhjelp.soknad.business.db.soknadmetadata.SoknadMetadataRepositoryJdbc;
import no.nav.sosialhjelp.soknad.business.sendtsoknad.SendtSoknadRepositoryJdbc;
import no.nav.sosialhjelp.soknad.business.soknadunderbehandling.BatchOpplastetVedleggRepositoryJdbc;
import no.nav.sosialhjelp.soknad.business.soknadunderbehandling.BatchSoknadUnderArbeidRepositoryJdbc;
import no.nav.sosialhjelp.soknad.business.soknadunderbehandling.OpplastetVedleggRepositoryJdbc;
import no.nav.sosialhjelp.soknad.business.soknadunderbehandling.SoknadUnderArbeidRepositoryJdbc;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

import java.time.Clock;

@Configuration
@Import({
        SendtSoknadRepositoryJdbc.class,
        SoknadUnderArbeidRepositoryJdbc.class,
        OpplastetVedleggRepositoryJdbc.class,
        SoknadMetadataRepositoryJdbc.class,
        BatchOpplastetVedleggRepositoryJdbc.class,
        BatchSoknadUnderArbeidRepositoryJdbc.class,
        OppgaveRepositoryJdbc.class,
        SoknadInnsendingDBConfig.class,
})
public class DbConfig {

    @Bean
    public Clock clock(){
        return Clock.systemDefaultZone();
    }

}
