package no.nav.sbl.dialogarena.soknadinnsending.business.db;


import org.springframework.jdbc.core.JdbcTemplate;

public interface RepositoryTestSupport {

    JdbcTemplate getJdbcTemplate();
}
