package no.nav.sosialhjelp.soknad.db.repositories.opplastetvedlegg

import org.springframework.jdbc.core.namedparam.NamedParameterJdbcDaoSupport
import org.springframework.stereotype.Component
import javax.inject.Inject
import javax.sql.DataSource

/**
 * Repository for OpplastetVedlegg.
 * Operasjoner som kun er tiltenkt batch/schedulerte jobber.
 */
@Component
class BatchOpplastetVedleggRepositoryJdbc : NamedParameterJdbcDaoSupport(), BatchOpplastetVedleggRepository {

    @Inject
    fun setDS(ds: DataSource) {
        super.setDataSource(ds)
    }

    override fun slettAlleVedleggForSoknad(soknadId: Long) {
        jdbcTemplate.update("delete from OPPLASTET_VEDLEGG where SOKNAD_UNDER_ARBEID_ID = ?", soknadId)
    }
}
