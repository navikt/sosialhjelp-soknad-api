package no.nav.sbl.dialogarena.soknadinnsending.business.db;


import no.nav.sbl.dialogarena.soknadinnsending.business.db.config.DatabaseTestContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.transaction.annotation.EnableTransactionManagement;

@Configuration
@Import(value = { DatabaseTestContext.class})
@EnableTransactionManagement()
public class DbConfig {
    @Bean
    public SoknadRepository soknadRepository(){
        return new SoknadRepositoryJdbc();
    }
}
