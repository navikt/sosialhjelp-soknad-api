package no.nav.sosialhjelp.soknad.domene.personalia.repository

import no.nav.sosialhjelp.soknad.domene.personalia.Kontonummer
import no.nav.sosialhjelp.soknad.domene.personalia.SoknadKilde
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.jdbc.core.RowMapper
import java.sql.ResultSet
import java.util.*

interface KontoFragmentRepository {
    fun findById(soknadId: UUID, kilde: SoknadKilde): Kontonummer?
    fun findAllBySoknadId(soknadId: UUID): List<Kontonummer>
    fun save(kontonummer: Kontonummer): Kontonummer
    fun delete(kontonummer: Kontonummer): Boolean
}

class KontoFragmentRepositoryImpl (
    private val jdbcTemplate: JdbcTemplate
): KontoFragmentRepository {
    override fun findById(soknadId: UUID, kilde: SoknadKilde): Kontonummer? {
        return jdbcTemplate.query(
            "SELECT * FROM kontonummer WHERE soknad_id = ? AND kilde = ?",
            KontonummerRowMapper(),
            soknadId,
            kilde.name
        ).firstOrNull()
    }

    override fun findAllBySoknadId(soknadId: UUID): List<Kontonummer> {
        return jdbcTemplate.query(
            "SELECT * FROM kontonummer WHERE soknad_id = ?",
            KontonummerRowMapper(),
            soknadId
        )
    }

    override fun save(kontonummer: Kontonummer): Kontonummer {
        jdbcTemplate.update(
            "INSERT INTO kontonummer (soknad_id, kilde, nummer) VALUES (?, ?, ?)",
            kontonummer.soknadId,
            kontonummer.kilde.name,
            kontonummer.nummer
        )
        return findById(kontonummer.soknadId, kontonummer.kilde)
            ?: throw IllegalStateException("Lagring av kontonummer feiled")
    }

    override fun delete(kontonummer: Kontonummer): Boolean {
        jdbcTemplate.update(
            "DELETE FROM kontonummer WHERE soknad_id = ? AND kilde = ?",
            kontonummer.soknadId,
            kontonummer.kilde.name
        )
        return findById(kontonummer.soknadId, kontonummer.kilde) == null
    }
}

internal class KontonummerRowMapper: RowMapper<Kontonummer> {
    override fun mapRow(rs: ResultSet, rowNum: Int): Kontonummer {
        return Kontonummer(
            soknadId = UUID.fromString(rs.getString("soknad_id")),
            kilde = SoknadKilde.valueOf(rs.getString("kilde")),
            nummer = rs.getString("nummer")
        )
    }
}
