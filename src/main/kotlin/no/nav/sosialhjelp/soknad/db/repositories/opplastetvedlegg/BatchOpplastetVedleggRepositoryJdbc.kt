package no.nav.sosialhjelp.soknad.db.repositories.opplastetvedlegg

import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.stereotype.Repository

/**
 * Repository for OpplastetVedlegg.
 * Operasjoner som kun er tiltenkt batch/schedulerte jobber.
 */
@Suppress("RECEIVER_NULLABILITY_MISMATCH_BASED_ON_JAVA_ANNOTATIONS")
@Repository
class BatchOpplastetVedleggRepositoryJdbc(
    private val jdbcTemplate: JdbcTemplate,
) : BatchOpplastetVedleggRepository {

    override fun slettAlleVedleggForSoknad(soknadId: Long) {
        jdbcTemplate.update("delete from OPPLASTET_VEDLEGG where SOKNAD_UNDER_ARBEID_ID = ?", soknadId)
    }
}
