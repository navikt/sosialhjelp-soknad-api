package no.nav.sosialhjelp.soknad.business.sendtsoknad;

import org.springframework.jdbc.core.namedparam.NamedParameterJdbcDaoSupport;
import org.springframework.stereotype.Component;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.TransactionCallbackWithoutResult;
import org.springframework.transaction.support.TransactionTemplate;

import javax.inject.Inject;
import javax.inject.Named;
import javax.sql.DataSource;
import java.util.Optional;

@Named("BatchSendtSoknadRepository")
@Component
public class BatchSendtSoknadRepositoryJdbc extends NamedParameterJdbcDaoSupport implements BatchSendtSoknadRepository {

    @Inject
    private TransactionTemplate transactionTemplate;

    @Inject
    public void setDS(DataSource ds) {
        super.setDataSource(ds);
    }

    @Override
    public Optional<Long> hentSendtSoknad(String behandlingsId) {
        return getJdbcTemplate().query("select * from SENDT_SOKNAD where BEHANDLINGSID = ?",
                (resultSet, i) -> resultSet.getLong("sendt_soknad_id"), behandlingsId).stream().findFirst();
    }

    @Override
    public void slettSendtSoknad(Long sendtSoknadId) {
        transactionTemplate.execute(new TransactionCallbackWithoutResult() {
            @Override
            protected void doInTransactionWithoutResult(TransactionStatus transactionStatus) {
                if (sendtSoknadId == null) {
                    throw new RuntimeException("Kan ikke slette sendt søknad uten søknadsid");
                }
                getJdbcTemplate().update("delete from SENDT_SOKNAD where SENDT_SOKNAD_ID = ?", sendtSoknadId);
            }
        });
    }
}
