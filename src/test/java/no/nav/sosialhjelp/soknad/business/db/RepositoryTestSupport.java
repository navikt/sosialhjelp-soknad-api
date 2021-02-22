package no.nav.sosialhjelp.soknad.business.db;


import org.springframework.jdbc.core.JdbcTemplate;

public interface RepositoryTestSupport {

    JdbcTemplate getJdbcTemplate();
}
