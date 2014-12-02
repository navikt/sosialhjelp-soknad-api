package no.nav.sbl.dialogarena.soknadinnsending.business.db;

import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

@Configuration
@Import({
        SoknadRepositoryJdbc.class,
        VedleggRepositoryJdbc.class,
        SoknadInnsendingDBConfig.class
})
public class DbConfig {

}
