package no.nav.sbl.dialogarena.soknadinnsending.business.db;

import no.nav.sbl.dialogarena.soknadinnsending.business.db.oppgave.OppgaveRepositoryJdbc;
import no.nav.sbl.dialogarena.soknadinnsending.business.db.soknad.HendelseRepositoryJdbc;
import no.nav.sbl.dialogarena.soknadinnsending.business.db.soknad.SoknadInnsendingDBConfig;
import no.nav.sbl.dialogarena.soknadinnsending.business.db.soknadmetadata.SoknadMetadataRepositoryJdbc;
import no.nav.sbl.sosialhjelp.sendtsoknad.SendtSoknadRepositoryJdbc;
import no.nav.sbl.sosialhjelp.soknadunderbehandling.OpplastetVedleggRepositoryJdbc;
import no.nav.sbl.sosialhjelp.soknadunderbehandling.SoknadUnderArbeidRepositoryJdbc;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

import java.time.Clock;

@Configuration
@Import({
        HendelseRepositoryJdbc.class,
        SendtSoknadRepositoryJdbc.class,
        SoknadUnderArbeidRepositoryJdbc.class,
        OpplastetVedleggRepositoryJdbc.class,
        SoknadMetadataRepositoryJdbc.class,
        OppgaveRepositoryJdbc.class,
        SoknadInnsendingDBConfig.class,
})
public class DbConfig {

    @Bean
    public Clock clock(){
        return Clock.systemDefaultZone();
    }

}
