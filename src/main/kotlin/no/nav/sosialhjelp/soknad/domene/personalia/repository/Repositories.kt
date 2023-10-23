package no.nav.sosialhjelp.soknad.domene.personalia.repository

import no.nav.sosialhjelp.soknad.domene.personalia.Kontonummer
import no.nav.sosialhjelp.soknad.domene.personalia.Nummer
import no.nav.sosialhjelp.soknad.domene.personalia.NummerObject
import no.nav.sosialhjelp.soknad.domene.personalia.Telefonnummer
import no.nav.sosialhjelp.soknad.domene.personalia.TelefonnummerBruker
import no.nav.sosialhjelp.soknad.domene.personalia.TelefonnummerSystem
import org.springframework.data.relational.core.mapping.DefaultNamingStrategy
import org.springframework.data.repository.Repository
import org.springframework.jdbc.core.JdbcTemplate
import java.util.*
import kotlin.reflect.KClass

@org.springframework.stereotype.Repository
interface TelefonnummerRepository<T: Telefonnummer>: CustomNummerRepository<T>, Repository<T, UUID>
@org.springframework.stereotype.Repository
interface KontonummerRepository<T: Kontonummer>: CustomNummerRepository<T>, Repository<T, UUID>

interface CustomNummerRepository<T: Nummer> {
    fun<S: T> save(s: S): S
    fun<S: T> findById(soknadId: UUID): S?
    fun deleteTelefonnummerBrukerById(soknadId: UUID)
}

class CustomNummerRepositoryImpl<T: NummerObject> (
    private val jdbcTemplate: JdbcTemplate
): CustomNummerRepository<T> {

    override fun <S : T> save(s: S): S {
        val tableName = getTableName(s::class.java)

        jdbcTemplate.update(
            "INSERT INTO $tableName (soknad_id, nummer) VALUES (?, ?)",
            s.soknadId,
            s.nummer
        )
        return
    }

    override fun <S : T> findById(soknadId: UUID): S? {

    }

    override fun deleteTelefonnummerBrukerById(soknadId: UUID) {
        TODO("Not yet implemented")
    }

    private fun<S: T> getTableName(clazz: Class<S>): String = DefaultNamingStrategy().getTableName(clazz)
}
