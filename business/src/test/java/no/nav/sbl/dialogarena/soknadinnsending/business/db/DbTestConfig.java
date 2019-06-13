package no.nav.sbl.dialogarena.soknadinnsending.business.db;


import no.digipost.time.ControllableClock;
import no.nav.sbl.dialogarena.soknadinnsending.business.db.config.DatabaseTestContext;
import no.nav.sbl.dialogarena.soknadinnsending.business.db.soknadmetadata.SoknadMetadataRepository;
import no.nav.sbl.dialogarena.soknadinnsending.business.db.soknadmetadata.SoknadMetadataRepositoryJdbc;
import no.nav.sbl.sosialhjelp.sendtsoknad.SendtSoknadRepository;
import no.nav.sbl.sosialhjelp.sendtsoknad.SendtSoknadRepositoryJdbc;
import no.nav.sbl.sosialhjelp.soknadunderbehandling.OpplastetVedleggRepository;
import no.nav.sbl.sosialhjelp.soknadunderbehandling.OpplastetVedleggRepositoryJdbc;
import no.nav.sbl.sosialhjelp.soknadunderbehandling.SoknadUnderArbeidRepository;
import no.nav.sbl.sosialhjelp.soknadunderbehandling.SoknadUnderArbeidRepositoryJdbc;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import javax.inject.Inject;
import javax.sql.DataSource;
import java.time.Clock;

@Configuration
@Import(value = {DatabaseTestContext.class})
@EnableTransactionManagement()
public class DbTestConfig {

    public static final ControllableClock clock = ControllableClock.control(Clock.systemDefaultZone());

    @Inject
    private DataSource dataSource;
    
    @Bean SoknadMetadataRepository soknadMetadataRepository() {
        return new SoknadMetadataRepositoryJdbc();
    }

    @Bean
    public SendtSoknadRepository sendtSoknadRepository() {
        return new SendtSoknadRepositoryJdbc();
    }

    @Bean
    public SoknadUnderArbeidRepository soknadUnderArbeidRepository() {
        return new SoknadUnderArbeidRepositoryJdbc();
    }

    @Bean
    public OpplastetVedleggRepository opplastetVedleggRepository() {
        return new OpplastetVedleggRepositoryJdbc();
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
