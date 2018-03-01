package no.nav.sbl.dialogarena.soknadinnsending.business.db.soknadmetadata;

import no.nav.sbl.dialogarena.soknadinnsending.business.db.SQLUtils;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcDaoSupport;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import javax.inject.Inject;
import javax.sql.DataSource;

@Component
@Transactional
public class SoknadMetadataRepositoryJdbc extends NamedParameterJdbcDaoSupport implements SoknadMetadataRepository {

    @Inject
    public void setDS(DataSource ds) {
        super.setDataSource(ds);
    }

    @Override
    public Long hentNesteId() {
        return getJdbcTemplate().queryForObject(SQLUtils.selectNextSequenceValue("METADATA_ID_DEQ"), Long.class);
    }

    @Override
    public void opprett(SoknadMetadata metadata) {

    }

    @Override
    public void oppdater(SoknadMetadata metadata) {

    }

    @Override
    public SoknadMetadata hent(String behandlingsId) {
        return null;
    }
}
