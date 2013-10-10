package no.nav.sbl.dialogarena.soknadinnsending.db.config;

import javax.sql.DataSource;

import no.nav.sbl.dialogarena.soknadinnsending.db.SoknadRepository;
import no.nav.sbl.dialogarena.soknadinnsending.db.SoknadRepositoryJdbc;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SoknadInnsendingDBConfig {

	@Bean
    public SoknadRepository soknadRepository(DataSource ds) {
        return new SoknadRepositoryJdbc(ds);
    }
    
}
