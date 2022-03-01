package no.nav.sosialhjelp.soknad.business.db.repositories.soknadunderarbeid;

import no.nav.sosialhjelp.soknad.db.repositories.opplastetvedlegg.BatchOpplastetVedleggRepository;
import no.nav.sosialhjelp.soknad.domain.SoknadUnderArbeid;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcDaoSupport;
import org.springframework.stereotype.Component;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.TransactionCallbackWithoutResult;
import org.springframework.transaction.support.TransactionTemplate;

import javax.inject.Inject;
import javax.inject.Named;
import javax.sql.DataSource;
import java.util.List;
import java.util.Optional;

import static no.nav.sosialhjelp.soknad.domain.SoknadUnderArbeidStatus.UNDER_ARBEID;


/**
 * Repository for SoknadUnderArbeid.
 * Operasjoner som kun er tiltenkt batch/schedulerte jobber.
 */
@Named("BatchSoknadUnderArbeidRepository")
@Component
public class BatchSoknadUnderArbeidRepositoryJdbc extends NamedParameterJdbcDaoSupport implements BatchSoknadUnderArbeidRepository {

    @Inject
    private TransactionTemplate transactionTemplate;

    @Inject
    private BatchOpplastetVedleggRepository batchOpplastetVedleggRepository;

    @Inject
    public void setDS(DataSource ds) {
        super.setDataSource(ds);
    }

    @Override
    public Optional<Long> hentSoknadUnderArbeidIdFromBehandlingsIdOptional(String behandlingsId) {
        return getJdbcTemplate().query("select * from SOKNAD_UNDER_ARBEID where BEHANDLINGSID = ?",
                (resultSet, i) -> resultSet.getLong("soknad_under_arbeid_id"), behandlingsId).stream().findFirst();
    }

    @Override
    public List<Long> hentGamleSoknadUnderArbeidForBatch() {
        return getJdbcTemplate().query("select SOKNAD_UNDER_ARBEID_ID from SOKNAD_UNDER_ARBEID where SISTENDRETDATO < CURRENT_TIMESTAMP - (INTERVAL '14' DAY) and STATUS = ?",
                (resultSet, i) -> resultSet.getLong("soknad_under_arbeid_id"), UNDER_ARBEID.toString());
    }

    @Override
    public void slettSoknad(Long soknadUnderArbeidId) {
        transactionTemplate.execute(new TransactionCallbackWithoutResult() {
            @Override
            protected void doInTransactionWithoutResult(TransactionStatus transactionStatus) {
                if (soknadUnderArbeidId == null) {
                    throw new RuntimeException("Kan ikke slette sendt søknad uten søknadsid");
                }
                batchOpplastetVedleggRepository.slettAlleVedleggForSoknad(soknadUnderArbeidId);
                getJdbcTemplate().update("delete from SOKNAD_UNDER_ARBEID where SOKNAD_UNDER_ARBEID_ID = ?", soknadUnderArbeidId);
            }
        });
    }

    @Override
    public List<SoknadUnderArbeid> hentForeldedeEttersendelser() {
        return getJdbcTemplate().query("select * from SOKNAD_UNDER_ARBEID where SISTENDRETDATO < CURRENT_TIMESTAMP - (INTERVAL '1' HOUR) " +
                        "and TILKNYTTETBEHANDLINGSID IS NOT NULL and STATUS = ?",
                new SoknadUnderArbeidRowMapper(), UNDER_ARBEID.toString());
    }
}
