package no.nav.sosialhjelp.soknad.business.db;

import no.nav.sosialhjelp.soknad.business.db.config.DatabaseTestContext;
import no.nav.sosialhjelp.soknad.business.db.soknadmetadata.SoknadMetadataRepository;
import no.nav.sosialhjelp.soknad.business.db.soknadmetadata.SoknadMetadataRepositoryJdbc;
import no.nav.sosialhjelp.soknad.business.sendtsoknad.SendtSoknadRepository;
import no.nav.sosialhjelp.soknad.business.sendtsoknad.SendtSoknadRepositoryJdbc;
import no.nav.sosialhjelp.soknad.business.soknadunderbehandling.BatchOpplastetVedleggRepository;
import no.nav.sosialhjelp.soknad.business.soknadunderbehandling.BatchOpplastetVedleggRepositoryJdbc;
import no.nav.sosialhjelp.soknad.business.soknadunderbehandling.BatchSoknadUnderArbeidRepository;
import no.nav.sosialhjelp.soknad.business.soknadunderbehandling.BatchSoknadUnderArbeidRepositoryJdbc;
import no.nav.sosialhjelp.soknad.business.soknadunderbehandling.OpplastetVedleggRepository;
import no.nav.sosialhjelp.soknad.business.soknadunderbehandling.OpplastetVedleggRepositoryJdbc;
import no.nav.sosialhjelp.soknad.business.soknadunderbehandling.SoknadUnderArbeidRepository;
import no.nav.sosialhjelp.soknad.business.soknadunderbehandling.SoknadUnderArbeidRepositoryJdbc;
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

    public static final Clock clock = Clock.systemDefaultZone();

    @Inject
    private DataSource dataSource;

    @Bean
    SoknadMetadataRepository soknadMetadataRepository() {
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
    public BatchSoknadUnderArbeidRepository batchSoknadUnderArbeidRepository() {
        return new BatchSoknadUnderArbeidRepositoryJdbc();
    }

    @Bean
    public BatchOpplastetVedleggRepository batchOpplastetVedleggRepository() {
        return new BatchOpplastetVedleggRepositoryJdbc();
    }

    @Bean
    public RepositoryTestSupport testSupport() {
        return new TestSupport(dataSource);
    }

    @Bean
    public Clock clock() {
        return clock;
    }

}
