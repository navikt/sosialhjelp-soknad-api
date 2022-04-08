package no.nav.sosialhjelp.soknad.db.repositories.opplastetvedlegg

import no.nav.sosialhjelp.soknad.db.SQLUtils
import no.nav.sosialhjelp.soknad.db.repositories.opplastetvedlegg.OpplastetVedleggRowMapper.opplastetVedleggRowMapper
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.stereotype.Repository

@Suppress("RECEIVER_NULLABILITY_MISMATCH_BASED_ON_JAVA_ANNOTATIONS")
@Repository
open class OpplastetVedleggRepositoryJdbc(
    private val jdbcTemplate: JdbcTemplate
) : OpplastetVedleggRepository {

    override fun hentVedlegg(uuid: String?, eier: String): OpplastetVedlegg? {
        return jdbcTemplate.query(
            "select * from OPPLASTET_VEDLEGG where EIER = ? and UUID = ?",
            opplastetVedleggRowMapper,
            eier,
            uuid
        ).firstOrNull()
    }

    override fun hentVedleggForSoknad(soknadId: Long, eier: String?): List<OpplastetVedlegg> {
        return jdbcTemplate.query(
            "select * from OPPLASTET_VEDLEGG where EIER = ? and SOKNAD_UNDER_ARBEID_ID = ?",
            opplastetVedleggRowMapper,
            eier,
            soknadId
        )
    }

    override fun opprettVedlegg(opplastetVedlegg: OpplastetVedlegg, eier: String): String {
        if (!eier.equals(opplastetVedlegg.eier, ignoreCase = true)) {
            throw RuntimeException("Eier stemmer ikke med vedleggets eier")
        }
        jdbcTemplate.update(
            "insert into OPPLASTET_VEDLEGG (UUID, EIER, TYPE, DATA, SOKNAD_UNDER_ARBEID_ID, FILNAVN, SHA512) values (?,?,?,?,?,?,?)",
            opplastetVedlegg.uuid,
            opplastetVedlegg.eier,
            opplastetVedlegg.vedleggType.sammensattType,
            opplastetVedlegg.data,
            opplastetVedlegg.soknadId,
            opplastetVedlegg.filnavn,
            opplastetVedlegg.sha512
        )
        return opplastetVedlegg.uuid
    }

    override fun slettVedlegg(uuid: String?, eier: String) {
        jdbcTemplate.update("delete from OPPLASTET_VEDLEGG where EIER = ? and UUID = ?", eier, uuid)
    }

    override fun slettAlleVedleggForSoknad(soknadId: Long, eier: String) {
        jdbcTemplate.update(
            "delete from OPPLASTET_VEDLEGG where EIER = ? and SOKNAD_UNDER_ARBEID_ID = ?",
            eier,
            soknadId
        )
    }

    override fun hentSamletVedleggStorrelse(soknadId: Long, eier: String): Int {
        if (jdbcTemplate.queryForObject(
                "select count(*) from OPPLASTET_VEDLEGG where EIER = ? and SOKNAD_UNDER_ARBEID_ID = ?",
                Int::class.java, eier, soknadId
            ) > 0
        ) {
            val blobSize = SQLUtils.blobSizeQuery()
            return jdbcTemplate.queryForObject(
                "select sum($blobSize) from OPPLASTET_VEDLEGG where EIER = ? and SOKNAD_UNDER_ARBEID_ID = ?",
                Int::class.java,
                eier,
                soknadId
            )
        }
        return 0
    }
}
