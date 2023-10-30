package no.nav.sosialhjelp.soknad.domene.personalia.repository

import no.nav.sosialhjelp.soknad.domene.personalia.SoknadKilde
import no.nav.sosialhjelp.soknad.domene.personalia.Telefonnummer
import org.springframework.data.repository.Repository
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.jdbc.core.RowMapper
import java.sql.ResultSet
import java.util.*

@org.springframework.stereotype.Repository
interface TelefonnummerRepository: TelefonFragmentRepository, Repository<Telefonnummer, UUID>

interface TelefonFragmentRepository {
    fun findById(soknadId: UUID, kilde: SoknadKilde): Telefonnummer?
    fun findAllBySoknadId(soknadId: UUID): List<Telefonnummer>
    fun save(telefonnummer: Telefonnummer): Telefonnummer
    fun delete(telefonnummer: Telefonnummer): Boolean
}

class TelefonFragmentRepositoryImpl (
    private val jdbcTemplate: JdbcTemplate
): TelefonFragmentRepository {
    override fun findById(soknadId: UUID, kilde: SoknadKilde): Telefonnummer? {
        return jdbcTemplate.query(
            "SELECT * FROM telefonnummer WHERE soknad_id = ? AND kilde = ?",
            TelefonnummerRowMapper(),
            soknadId,
            kilde.name
        ).firstOrNull()
    }

    override fun findAllBySoknadId(soknadId: UUID): List<Telefonnummer> {
        return jdbcTemplate.query(
            "SELECT * FROM telefonnummer WHERE soknad_id = ?",
            TelefonnummerRowMapper(),
            soknadId
        )
    }

    override fun save(telefonnummer: Telefonnummer): Telefonnummer {
        jdbcTemplate.update(
            "INSERT INTO telefonnummer (soknad_id, kilde, nummer) VALUES (?, ?, ?)",
            telefonnummer.soknadId,
            telefonnummer.kilde.name,
            telefonnummer.nummer
        )
        return findById(telefonnummer.soknadId, telefonnummer.kilde)
            ?: throw IllegalStateException("Lagring av telefonnummer feiled")
    }

    override fun delete(telefonnummer: Telefonnummer): Boolean {
        jdbcTemplate.update(
            "DELETE FROM telefonnummer WHERE soknad_id = ? AND kilde = ?",
            telefonnummer.soknadId,
            telefonnummer.kilde.name
        )
        return findById(telefonnummer.soknadId, telefonnummer.kilde) == null
    }
}

internal class TelefonnummerRowMapper: RowMapper<Telefonnummer> {
    override fun mapRow(rs: ResultSet, rowNum: Int): Telefonnummer {
        return Telefonnummer(
            soknadId = UUID.fromString(rs.getString("soknad_id")),
            kilde = SoknadKilde.valueOf(rs.getString("kilde")),
            nummer = rs.getString("nummer")
        )
    }
}
