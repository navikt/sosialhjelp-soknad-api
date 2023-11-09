package no.nav.sosialhjelp.soknad.nymodell.domene.soknad.repository

import no.nav.sosialhjelp.soknad.nymodell.domene.UpsertRepository
import no.nav.sosialhjelp.soknad.nymodell.domene.soknad.brukerdata.Brukerdata
import org.springframework.data.repository.ListCrudRepository
import java.util.*


@org.springframework.stereotype.Repository
interface BrukerdataRepository: UpsertRepository<Brukerdata>, ListCrudRepository<Brukerdata, UUID>
//
//interface BrukerdataFragmentRepository {
//    fun findById(soknadId: UUID, brukerdataKey: BrukerdataKey): Brukerdata?
//    fun save(brukerdata: Brukerdata): Brukerdata
//}
//
//class BrukerdataFragmentRepositoryImpl(
//    private val jdbcTemplate: JdbcTemplate,
//): BrukerdataFragmentRepository {
//
//    override fun save(brukerdata: Brukerdata): Brukerdata {
//        with(brukerdata) {
//            if (repo.findById(soknadId, key) != null) doUpdate(this)
//            else doInsert(this)
//
//            return repo.findById(soknadId, key)
//                ?: throw IllegalStateException("Lagring av Brukerdata feilet")
//        }
//    }
//
//    private fun doInsert(data: Brukerdata) {
//        val insertSql = "INSERT INTO brukerdata (soknad_id, key, value) VALUES (?, ?, ?)"
//        jdbcTemplate.update(insertSql,data.soknadId, data.key.key, data.value)
//    }
//
//    private fun doUpdate(data: Brukerdata) {
//        val updateSql = "UPDATE brukerdata SET value = ? WHERE soknad_id = ? AND key = ?"
//        jdbcTemplate.update(updateSql, data.value, data.soknadId, data.key.key)
//    }
//}
