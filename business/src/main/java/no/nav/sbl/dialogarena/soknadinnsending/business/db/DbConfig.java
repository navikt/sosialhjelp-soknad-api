package no.nav.sbl.dialogarena.soknadinnsending.business.db;

import no.nav.sbl.dialogarena.soknadinnsending.business.db.fillager.FillagerRepositoryJdbc;
import no.nav.sbl.dialogarena.soknadinnsending.business.db.oppgave.OppgaveRepositoryJdbc;
import no.nav.sbl.dialogarena.soknadinnsending.business.db.soknad.HendelseRepositoryJdbc;
import no.nav.sbl.dialogarena.soknadinnsending.business.db.soknad.SoknadInnsendingDBConfig;
import no.nav.sbl.dialogarena.soknadinnsending.business.db.soknad.SoknadRepositoryJdbc;
import no.nav.sbl.dialogarena.soknadinnsending.business.db.soknadmetadata.SoknadMetadataRepositoryJdbc;
import no.nav.sbl.dialogarena.soknadinnsending.business.db.vedlegg.VedleggRepositoryJdbc;
import no.nav.sbl.sosialhjelp.soknad.SendtSoknadRepositoryJdbc;
import no.nav.sbl.sosialhjelp.vedlegg.VedleggstatusRepositoryJdbc;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

import java.time.Clock;

@Configuration
@Import({
        HendelseRepositoryJdbc.class,
        SoknadRepositoryJdbc.class,
        SendtSoknadRepositoryJdbc.class,
        VedleggstatusRepositoryJdbc.class,
        VedleggRepositoryJdbc.class,
        FillagerRepositoryJdbc.class,
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
