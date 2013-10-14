package no.nav.sbl.dialogarena.soknadinnsending.db;


import no.nav.sbl.dialogarena.soknadinnsending.db.config.DatabaseTestContext;

import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

@Configuration
@ComponentScan
@Import(value = { DatabaseTestContext.class})
public class DbConfig {

}
