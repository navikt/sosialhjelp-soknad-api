package no.nav.sbl.dialogarena.soknadinnsending.business.db;


import java.time.Clock;

import javax.inject.Inject;
import javax.sql.DataSource;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import no.digipost.time.ControllableClock;
import no.nav.sbl.dialogarena.soknadinnsending.business.db.config.DatabaseTestContext;
import no.nav.sbl.dialogarena.soknadinnsending.business.db.soknad.HendelseRepository;
import no.nav.sbl.dialogarena.soknadinnsending.business.db.soknad.HendelseRepositoryJdbc;
import no.nav.sbl.dialogarena.soknadinnsending.business.db.soknad.SoknadRepository;
import no.nav.sbl.dialogarena.soknadinnsending.business.db.soknad.SoknadRepositoryJdbc;
import no.nav.sbl.dialogarena.soknadinnsending.business.db.soknadmetadata.SoknadMetadataRepository;
import no.nav.sbl.dialogarena.soknadinnsending.business.db.soknadmetadata.SoknadMetadataRepositoryJdbc;
import no.nav.sbl.dialogarena.soknadinnsending.business.db.vedlegg.VedleggRepository;
import no.nav.sbl.dialogarena.soknadinnsending.business.db.vedlegg.VedleggRepositoryJdbc;

@Configuration
@Import(value = {DatabaseTestContext.class})
@EnableTransactionManagement()
public class DbTestConfig {

    public static final ControllableClock clock = ControllableClock.control(Clock.systemDefaultZone());

    @Inject
    private DataSource dataSource;

    @Bean
    public SoknadRepository soknadInnsendingRepository() {
        return new SoknadRepositoryJdbc();
    }

    @Bean
    public HendelseRepository hendelseRepository() {
        return new HendelseRepositoryJdbc();
    }

    @Bean
    public VedleggRepository vedleggRepository() {
        return new VedleggRepositoryJdbc();
    }
    
    @Bean SoknadMetadataRepository soknadMetadataRepository() {
        return new SoknadMetadataRepositoryJdbc();
    }

    @Bean
    public RepositoryTestSupport testSupport() {
        return new TestSupport(dataSource);
    }

    @Bean
    public Clock clock(){
        return clock;
    }

}
