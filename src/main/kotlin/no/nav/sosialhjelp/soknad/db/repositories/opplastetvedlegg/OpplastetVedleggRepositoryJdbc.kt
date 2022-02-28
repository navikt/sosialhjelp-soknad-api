package no.nav.sosialhjelp.soknad.db.repositories.opplastetvedlegg

import no.nav.sosialhjelp.soknad.domain.OpplastetVedlegg
import no.nav.sosialhjelp.soknad.domain.VedleggType
import org.springframework.jdbc.core.RowMapper
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcDaoSupport
import org.springframework.stereotype.Component
import java.sql.ResultSet
import java.util.Optional
import javax.inject.Inject
import javax.sql.DataSource

@Component
open class OpplastetVedleggRepositoryJdbc : NamedParameterJdbcDaoSupport(), OpplastetVedleggRepository {

    @Inject
    fun setDS(ds: DataSource) {
        super.setDataSource(ds)
    }

    override fun hentVedlegg(uuid: String?, eier: String): Optional<OpplastetVedlegg> {
        return jdbcTemplate.query(
            "select * from OPPLASTET_VEDLEGG where EIER = ? and UUID = ?",
            OpplastetVedleggRowMapper(), eier, uuid
        ).stream().findFirst()
    }

    override fun hentVedleggForSoknad(soknadId: Long, eier: String?): List<OpplastetVedlegg> {
        return jdbcTemplate.query(
            "select * from OPPLASTET_VEDLEGG where EIER = ? and SOKNAD_UNDER_ARBEID_ID = ?",
            OpplastetVedleggRowMapper(), eier, soknadId
        )
    }

    override fun opprettVedlegg(opplastetVedlegg: OpplastetVedlegg, eier: String): String {
        if (eier == null || !eier.equals(opplastetVedlegg.eier, ignoreCase = true)) {
            throw RuntimeException("Eier stemmer ikke med vedleggets eier")
        }
        jdbcTemplate
            .update(
                "insert into OPPLASTET_VEDLEGG (UUID, EIER, TYPE, DATA, SOKNAD_UNDER_ARBEID_ID, FILNAVN, SHA512)" +
                    " values (?,?,?,?,?,?,?)",
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
        jdbcTemplate
            .update(
                "delete from OPPLASTET_VEDLEGG where EIER = ? and UUID = ?",
                eier,
                uuid
            )
    }

    override fun slettAlleVedleggForSoknad(soknadId: Long, eier: String) {
        jdbcTemplate
            .update(
                "delete from OPPLASTET_VEDLEGG where EIER = ? and SOKNAD_UNDER_ARBEID_ID = ?",
                eier,
                soknadId
            )
    }

    override fun hentSamletVedleggStorrelse(soknadId: Long, eier: String): Int {
        if (jdbcTemplate
            .queryForObject(
                    "select count(*) from OPPLASTET_VEDLEGG where EIER = ? and SOKNAD_UNDER_ARBEID_ID = ?",
                    Int::class.java, eier, soknadId
                ) > 0
        ) {
            val sql =
                "select sum(dbms_lob.getLength(DATA)) from OPPLASTET_VEDLEGG where EIER = ? and SOKNAD_UNDER_ARBEID_ID = ?"
            return jdbcTemplate
                .queryForObject(sql, Int::class.java, eier, soknadId)
        }
        return 0
    }

    inner class OpplastetVedleggRowMapper : RowMapper<OpplastetVedlegg> {
        override fun mapRow(rs: ResultSet, rowNum: Int): OpplastetVedlegg {
            return OpplastetVedlegg()
                .withUuid(rs.getString("uuid"))
                .withEier(rs.getString("eier"))
                .withVedleggType(VedleggType(rs.getString("type")))
                .withData(rs.getBytes("data"))
                .withSoknadId(rs.getLong("soknad_under_arbeid_id"))
                .withFilnavn(rs.getString("filnavn"))
                .withSha512(rs.getString("sha512"))
        }
    }
}
