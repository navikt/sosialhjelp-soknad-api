package no.nav.sbl.dialogarena.soknadinnsending.db;


import no.nav.sbl.dialogarena.soknadinnsending.db.config.DatabaseTestContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

@Configuration
@Import(value = { DatabaseTestContext.class})
public class DbConfig {
    @Bean
    public SoknadRepository soknadRepository(){
        return new SoknadRepositoryJdbc();
    }
}
