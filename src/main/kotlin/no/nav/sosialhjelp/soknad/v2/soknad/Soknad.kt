package no.nav.sosialhjelp.soknad.v2.soknad

import java.time.LocalDateTime
import java.util.UUID
import no.nav.sosialhjelp.soknad.v2.config.repository.DomainRoot
import no.nav.sosialhjelp.soknad.v2.config.repository.UpsertRepository
import no.nav.sosialhjelp.soknad.v2.metadata.SoknadStatus
import org.springframework.core.convert.converter.Converter
import org.springframework.data.annotation.Id
import org.springframework.data.convert.ReadingConverter
import org.springframework.data.convert.WritingConverter
import org.springframework.data.jdbc.repository.query.Query
import org.springframework.data.relational.core.mapping.Embedded
import org.springframework.data.relational.core.mapping.Table
import org.springframework.data.repository.ListCrudRepository
import org.springframework.stereotype.Repository
import tools.jackson.module.kotlin.jacksonObjectMapper
import tools.jackson.module.kotlin.readValue

@Repository
interface SoknadRepository : UpsertRepository<Soknad>, ListCrudRepository<Soknad, UUID> {
    @Query(
        "SELECT id FROM soknad WHERE id IN " +
            "(SELECT soknad_id FROM soknad_metadata WHERE opprettet < :timestamp AND status = :status)",
    )
    fun findSoknadIdsOlderThanWithStatus(
        timestamp: LocalDateTime,
        status: SoknadStatus,
    ): List<UUID>

    @Query(
        "SELECT id FROM soknad WHERE id IN " +
            "(SELECT soknad_id FROM soknad_metadata " +
            "WHERE person_id = :fnr AND (status = 'OPPRETTET' OR status= 'INNSENDING_FEILET'))",
    )
    fun findOpenSoknadIds(fnr: String): List<UUID>

    @Query("SELECT id FROM soknad WHERE id IN (SELECT soknad_id FROM soknad_metadata WHERE status = :status)")
    fun findIdsWithStatus(status: SoknadStatus): List<UUID>
}

@Table
data class Soknad(
    @Id
    val id: UUID = UUID.randomUUID(),
    val eierPersonId: String,
    @Embedded.Empty
    val begrunnelse: Begrunnelse = Begrunnelse(),
) : DomainRoot {
    override fun getDbId() = id
}

data class Begrunnelse(
    val hvorforSoke: String? = "",
    val hvaSokesOm: String = "",
    val kategorier: Kategorier = Kategorier(),
)

data class Kategorier(
    val definerte: Set<Kategori> = emptySet(),
    val annet: String = "",
)

enum class Kategori {
    LIVSOPPHOLD,
    HUSLEIE,
    STROM_OPPVARMING,
    NODHJELP_IKKE_MAT,
    NODHJELP_IKKE_BOSTED,
    NODHJELP_IKKE_STROM,
    ;

    fun isNodhjelp(): Boolean = nodhjelpList.contains(this)

    companion object {
        private val nodhjelpList get() = setOf(NODHJELP_IKKE_BOSTED, NODHJELP_IKKE_MAT, NODHJELP_IKKE_STROM)
    }
}

@WritingConverter
object KategorierToJsonConverter : Converter<Kategorier, String> {
    override fun convert(source: Kategorier): String = jacksonObjectMapper().writeValueAsString(source)
}

@ReadingConverter
object JsonToKategorierConverter : Converter<String, Kategorier> {
    override fun convert(source: String): Kategorier = jacksonObjectMapper().readValue(source)
}
