package no.nav.sbl.dialogarena.soknadinnsending.business.db;


import no.nav.sbl.dialogarena.soknadinnsending.business.db.config.DatabaseTestContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import javax.inject.Inject;
import javax.sql.DataSource;

@Configuration
@Import(value = {DatabaseTestContext.class})
@EnableTransactionManagement()
public class DbConfig {
    @Inject
    private DataSource dataSource;

    @Bean
    public SoknadRepository soknadRepository() {
        return new SoknadRepositoryJdbc();
    }

    @Bean
    public RepositoryTestSupport testSupport() {
        return new TestSupport(dataSource);
    }

}
