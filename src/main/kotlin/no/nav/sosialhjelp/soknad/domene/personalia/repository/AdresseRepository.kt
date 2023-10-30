package no.nav.sosialhjelp.soknad.domene.personalia.repository

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import no.nav.sosialhjelp.soknad.domene.personalia.AdresseForSoknad
import no.nav.sosialhjelp.soknad.domene.personalia.AdresseForSoknadId
import no.nav.sosialhjelp.soknad.domene.personalia.AdresseType
import no.nav.sosialhjelp.soknad.domene.personalia.AdresseValg
import org.springframework.data.repository.Repository
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.jdbc.core.RowMapper
import java.sql.ResultSet
import java.util.*

@org.springframework.stereotype.Repository
interface AdresseRepository: AdresseFragmentRepository, Repository<AdresseForSoknad, AdresseForSoknadId>

interface AdresseFragmentRepository {
    fun findById(id: AdresseForSoknadId): AdresseForSoknad?
    fun findAllBySoknadId(soknadId: UUID): List<AdresseForSoknad>
    fun save(adresseForSoknad: AdresseForSoknad): AdresseForSoknad
    fun delete(adresseForSoknadId: AdresseForSoknadId)
    fun delete(adresseForSoknad: AdresseForSoknad)
    fun deleteBySoknadId(soknadId: UUID)
    fun existsById(id: AdresseForSoknadId): Boolean
}

class AdresseFragmentRepositoryImpl (
    val jdbcTemplate: JdbcTemplate,
): AdresseFragmentRepository {

    private val mapper = jacksonObjectMapper()

    override fun findById(id: AdresseForSoknadId): AdresseForSoknad? {
        return jdbcTemplate.query (
            "SELECT * FROM adresse_for_soknad WHERE soknad_id = ? AND type_adressevalg = ?",
            AdresseForSoknadRowMapper(),
            id.soknadId,
            id.typeAdressevalg.name
        ).firstOrNull()
    }

    override fun findAllBySoknadId(soknadId: UUID): List<AdresseForSoknad> {
        return jdbcTemplate.query (
            "SELECT * FROM adresse_for_soknad WHERE soknad_id = ?",
            AdresseForSoknadRowMapper(),
            soknadId
        )
    }

    override fun save(adresseForSoknad: AdresseForSoknad): AdresseForSoknad {
        return adresseForSoknad.let {

            if (existsById(it.id)) doUpdate(it) else doInsert(it)

            findById(id = it.id)
                ?: throw IllegalStateException("Lagring av adresse feilet")
        }
    }

    override fun delete(adresseForSoknadId: AdresseForSoknadId) {
        jdbcTemplate.update(
            "DELETE FROM adresse_for_soknad WHERE soknad_id = ? AND type_adressevalg = ?",
            adresseForSoknadId.soknadId,
            adresseForSoknadId.typeAdressevalg.name
        )
    }

    override fun delete(adresseForSoknad: AdresseForSoknad) {
        delete(adresseForSoknad.id)
    }

    override fun deleteBySoknadId(soknadId: UUID) {
        jdbcTemplate.update(
            "DELETE FROM adresse_for_soknad WHERE soknad_id = ?",
            soknadId
        )
    }

    override fun existsById(id: AdresseForSoknadId): Boolean {
        val numberOfRows = jdbcTemplate
            .queryForObject(
                "select count(*) from adresse_for_soknad where soknad_id = ? and type_adressevalg = ?",
                Int::class.java,
                id.soknadId, id.typeAdressevalg.name
            ) as Int

        return numberOfRows == 1
    }

    private fun doInsert(adresseForSoknad: AdresseForSoknad) {

        with(adresseForSoknad) {
            val adresse_json = mapper.writeValueAsString(adresse)

            jdbcTemplate.update(
                "INSERT INTO adresse_for_soknad (soknad_id, type_adressevalg, adresse_type, adresse_json) " +
                        "VALUES (?, ?, ?, ?)",
                id.soknadId,
                id.typeAdressevalg.name,
                adresseType.name,
                adresse_json
            )
        }
    }

    private fun doUpdate(adresseForSoknad: AdresseForSoknad) {

        with (adresseForSoknad) {
            val adresse_json = mapper.writeValueAsString(adresse)

            jdbcTemplate.update(
                "UPDATE adresse_for_soknad SET adresse_type = ?, adresse_json = ? " +
                        "WHERE soknad_id = ? AND type_adressevalg = ?",
                adresseType.name,
                adresse_json,
                id.soknadId,
                id.typeAdressevalg.name
            )
        }
    }
}

internal class AdresseForSoknadRowMapper: RowMapper<AdresseForSoknad> {
    override fun mapRow(rs: ResultSet, rowNum: Int): AdresseForSoknad {
        val adresseType = AdresseType.valueOf(rs.getString("adresse_type"))
        return AdresseForSoknad(
            id = AdresseForSoknadId(
                soknadId = UUID.fromString(rs.getString("soknad_id")),
                typeAdressevalg = AdresseValg.valueOf(rs.getString("type_adressevalg")),
            ),
            adresseType = adresseType,
            adresse = adresseType.mapJsonToAdresse(rs.getString("adresse_json")),
        )
    }
}
