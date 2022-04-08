package no.nav.sosialhjelp.soknad.db.repositories.opplastetvedlegg

import no.nav.sosialhjelp.soknad.db.repositories.opplastetvedlegg.OpplastetVedleggRowMapper.opplastetVedleggRowMapper
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.stereotype.Repository

@Repository
class OpplastetVedleggMigrationRepository(
    private val jdbcTemplate: JdbcTemplate,
) {
    fun getOpplastetVedlegg(soknadUnderArbeidId: Long): List<OpplastetVedlegg> {
        return jdbcTemplate.query(
            "select * from opplastet_vedlegg where soknad_under_arbeid_id = ?",
            opplastetVedleggRowMapper,
            soknadUnderArbeidId
        )
    }
}
