package no.nav.sosialhjelp.soknad.config

import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.jdbc.core.support.JdbcDaoSupport
import javax.sql.DataSource

interface RepositoryTestSupport {
    fun getJdbcTemplate(): JdbcTemplate
}

class TestSupport(dataSource: DataSource) : JdbcDaoSupport(), RepositoryTestSupport {
    init {
        super.setDataSource(dataSource)
    }
}