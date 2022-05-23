package no.nav.sosialhjelp.soknad.migration.repo

import no.nav.sosialhjelp.soknad.db.repositories.opplastetvedlegg.OpplastetVedlegg
import no.nav.sosialhjelp.soknad.db.repositories.opplastetvedlegg.OpplastetVedleggRowMapper.opplastetVedleggRowMapper
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.stereotype.Repository

@Repository
open class OpplastetVedleggMigrationRepository(
    private val jdbcTemplate: JdbcTemplate,
) {
    open fun getOpplastetVedlegg(soknadUnderArbeidId: Long): List<OpplastetVedlegg> {
        return jdbcTemplate.query(
            "select * from opplastet_vedlegg where soknad_under_arbeid_id = ?",
            opplastetVedleggRowMapper,
            soknadUnderArbeidId
        )
    }

    open fun count(): Int {
        return jdbcTemplate.queryForObject(
            "select count(*) from opplastet_vedlegg",
            Int::class.java
        ) ?: 0
    }
}
