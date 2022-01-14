package no.nav.sosialhjelp.soknad.business.db.config;

import no.nav.sosialhjelp.soknad.business.db.RepositoryTestSupport;
import no.nav.sosialhjelp.soknad.business.db.TestSupport;
import no.nav.sosialhjelp.soknad.business.db.repositories.opplastetvedlegg.BatchOpplastetVedleggRepository;
import no.nav.sosialhjelp.soknad.business.db.repositories.opplastetvedlegg.BatchOpplastetVedleggRepositoryJdbc;
import no.nav.sosialhjelp.soknad.business.db.repositories.opplastetvedlegg.OpplastetVedleggRepository;
import no.nav.sosialhjelp.soknad.business.db.repositories.opplastetvedlegg.OpplastetVedleggRepositoryJdbc;
import no.nav.sosialhjelp.soknad.business.db.repositories.sendtsoknad.BatchSendtSoknadRepository;
import no.nav.sosialhjelp.soknad.business.db.repositories.sendtsoknad.BatchSendtSoknadRepositoryJdbc;
import no.nav.sosialhjelp.soknad.business.db.repositories.sendtsoknad.SendtSoknadRepository;
import no.nav.sosialhjelp.soknad.business.db.repositories.sendtsoknad.SendtSoknadRepositoryJdbc;
import no.nav.sosialhjelp.soknad.business.db.repositories.soknadmetadata.BatchSoknadMetadataRepository;
import no.nav.sosialhjelp.soknad.business.db.repositories.soknadmetadata.BatchSoknadMetadataRepositoryJdbc;
import no.nav.sosialhjelp.soknad.business.db.repositories.soknadmetadata.SoknadMetadataRepository;
import no.nav.sosialhjelp.soknad.business.db.repositories.soknadmetadata.SoknadMetadataRepositoryJdbc;
import no.nav.sosialhjelp.soknad.business.db.repositories.soknadunderarbeid.BatchSoknadUnderArbeidRepository;
import no.nav.sosialhjelp.soknad.business.db.repositories.soknadunderarbeid.BatchSoknadUnderArbeidRepositoryJdbc;
import no.nav.sosialhjelp.soknad.business.db.repositories.soknadunderarbeid.SoknadUnderArbeidRepository;
import no.nav.sosialhjelp.soknad.business.db.repositories.soknadunderarbeid.SoknadUnderArbeidRepositoryJdbc;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import org.springframework.transaction.support.TransactionTemplate;

import javax.sql.DataSource;
import java.io.IOException;

import static no.nav.sosialhjelp.soknad.business.db.config.DatabaseTestContext.buildDataSource;

@Configuration
@Import(value = {DatabaseTestContext.class})
@EnableTransactionManagement()
public class DbTestConfig {

    @Bean
    public DataSource dataSource() throws IOException {
        return buildDataSource();
    }

    @Bean
    public DataSourceTransactionManager transactionManager(DataSource dataSource) {
        return new DataSourceTransactionManager(dataSource);
    }

    @Bean
    public TransactionTemplate transactionTemplate(DataSourceTransactionManager transactionManager) {
        return new TransactionTemplate(transactionManager);
    }

    @Bean
    public SoknadMetadataRepository soknadMetadataRepository() {
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
    public BatchSoknadMetadataRepository batchSoknadMetadataRepository() {
        return new BatchSoknadMetadataRepositoryJdbc();
    }

    @Bean
    public BatchSendtSoknadRepository batchSendtSoknadRepository() {
        return new BatchSendtSoknadRepositoryJdbc();
    }

    @Bean
    public RepositoryTestSupport testSupport(DataSource dataSource) {
        return new TestSupport(dataSource);
    }
}
