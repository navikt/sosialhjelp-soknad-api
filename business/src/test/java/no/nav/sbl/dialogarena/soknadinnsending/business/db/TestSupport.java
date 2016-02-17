package no.nav.sbl.dialogarena.soknadinnsending.business.db;

import org.springframework.jdbc.core.support.JdbcDaoSupport;

import javax.inject.Inject;
import javax.sql.DataSource;


public class TestSupport extends JdbcDaoSupport implements RepositoryTestSupport {

    @Inject
    public TestSupport(DataSource dataSource) {
        super.setDataSource(dataSource);
    }
}
