package no.nav.sosialhjelp.soknad.repository

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import no.nav.sosialhjelp.soknad.model.personalia.AdresseForSoknad
import no.nav.sosialhjelp.soknad.model.personalia.AdresseForSoknadId
import no.nav.sosialhjelp.soknad.model.personalia.AdresseType
import no.nav.sosialhjelp.soknad.model.personalia.AdresseValg
import no.nav.sosialhjelp.soknad.model.personalia.PersonForSoknad
import no.nav.sosialhjelp.soknad.model.personalia.PersonForSoknadId

import org.springframework.data.repository.Repository
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.jdbc.core.RowMapper
import java.sql.ResultSet
import java.util.*

@org.springframework.stereotype.Repository
interface PersonRepository : PersonForSoknadRepository, Repository<PersonForSoknad, PersonForSoknadId>

// Samme signatur som CrudRepository slik at denne overrides når et
// repository-interface implementerer begge
interface PersonForSoknadRepository {
    fun findById(id: PersonForSoknadId): PersonForSoknad?
    fun save(personForSoknad: PersonForSoknad): PersonForSoknad
    fun delete(personForSoknadId: PersonForSoknadId)
    fun existsById(id: PersonForSoknadId): Boolean
}
class PersonForSoknadRepositoryImpl (
    private val jdbcTemplate: JdbcTemplate
): PersonForSoknadRepository {

    override fun findById(personForSoknadId: PersonForSoknadId): PersonForSoknad? {
        return jdbcTemplate.query(
            "SELECT * FROM person_for_soknad WHERE person_id = ? AND soknad_id = ?",
            PersonForSoknadRowMapper(),
            personForSoknadId.personId,
            personForSoknadId.soknadId
        ).firstOrNull()
    }

    override fun save(personForSoknad: PersonForSoknad): PersonForSoknad {
        with(personForSoknad) {
            jdbcTemplate.update(
                "INSERT INTO person_for_soknad " +
                        "(person_id, soknad_id, fornavn, mellomnavn, etternavn, statsborgerskap, nordisk_borger, fodselsdato) " +
                        "VALUES (?, ?, ?, ?, ?, ?, ?, ?)",
                id.personId,
                id.soknadId,
                fornavn,
                mellomnavn,
                etternavn,
                statsborgerskap,
                nordiskBorger,
                fodselsdato
            )
        }
        return findById(personForSoknad.id)
            ?: throw IllegalStateException("Lagring av person for soknad feilet")
    }

    override fun delete(personForSoknadId: PersonForSoknadId) {
        jdbcTemplate.update(
            "DELETE FROM person_for_soknad WHERE soknad_id = ? AND person_id = ?",
            personForSoknadId.personId,
            personForSoknadId.soknadId
        )
    }

    override fun existsById(id: PersonForSoknadId): Boolean {
        val numberOfRows = jdbcTemplate
            .queryForObject(
                "select count(*) from person_for_soknad where soknad_id = ? and person_id = ?",
                Int::class.java,
                id.soknadId,
                id.personId
            ) as Int

        return numberOfRows == 1
    }
}

internal class PersonForSoknadRowMapper: RowMapper<PersonForSoknad> {
    override fun mapRow(rs: ResultSet, rowNum: Int): PersonForSoknad {
        return PersonForSoknad(
            id = PersonForSoknadId(
                personId = rs.getString("person_id"),
                soknadId = UUID.fromString(rs.getString("soknad_id")),
            ),
            fornavn = rs.getString("fornavn"),
            mellomnavn = rs.getString("mellomnavn"),
            etternavn = rs.getString("etternavn"),
            statsborgerskap = rs.getString("statsborgerskap"),
//            nordiskBorger = rs.getBoolean("nordisk_borger"),
            nordiskBorger = decodeBoolean(rs),
            fodselsdato = rs.getString("fodselsdato"),
        )
    }

    private fun decodeBoolean(rs: ResultSet): Boolean? {
        val boolean = rs.getBoolean("nordisk_borger")
        return if (rs.wasNull()) null else boolean
    }
}