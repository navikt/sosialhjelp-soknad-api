package no.nav.sbl.dialogarena.soknadinnsending.business.db;


import no.nav.sbl.dialogarena.soknadinnsending.business.db.config.DatabaseTestContext;
import no.nav.sbl.dialogarena.soknadinnsending.business.db.soknad.SoknadRepository;
import no.nav.sbl.dialogarena.soknadinnsending.business.db.soknad.SoknadRepositoryJdbc;
import no.nav.sbl.dialogarena.soknadinnsending.business.db.vedlegg.VedleggRepository;
import no.nav.sbl.dialogarena.soknadinnsending.business.db.vedlegg.VedleggRepositoryJdbc;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import javax.inject.Inject;
import javax.sql.DataSource;

@Configuration
@Import(value = {DatabaseTestContext.class})
@EnableTransactionManagement()
public class DbTestConfig {
    @Inject
    private DataSource dataSource;

    @Bean
    public SoknadRepository soknadInnsendingRepository() {
        return new SoknadRepositoryJdbc();
    }

    @Bean
    public VedleggRepository vedleggRepository() {
        return new VedleggRepositoryJdbc();
    }

    @Bean
    public RepositoryTestSupport testSupport() {
        return new TestSupport(dataSource);
    }

}
