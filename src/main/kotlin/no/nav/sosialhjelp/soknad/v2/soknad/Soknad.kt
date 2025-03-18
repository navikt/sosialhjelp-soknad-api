package no.nav.sosialhjelp.soknad.v2.soknad

import com.fasterxml.jackson.annotation.JsonSubTypes
import com.fasterxml.jackson.annotation.JsonTypeInfo
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import no.nav.sosialhjelp.soknad.v2.config.repository.DomainRoot
import no.nav.sosialhjelp.soknad.v2.config.repository.UpsertRepository
import org.springframework.core.convert.converter.Converter
import no.nav.sosialhjelp.soknad.v2.metadata.SoknadStatus
import org.springframework.data.annotation.Id
import org.springframework.data.convert.ReadingConverter
import org.springframework.data.convert.WritingConverter
import org.springframework.data.jdbc.repository.query.Query
import org.springframework.data.relational.core.mapping.Column
import org.springframework.data.relational.core.mapping.Embedded
import org.springframework.data.relational.core.mapping.Table
import org.springframework.data.repository.ListCrudRepository
import org.springframework.stereotype.Repository
import java.time.LocalDateTime
import java.util.UUID

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
            "WHERE person_id = :fnr AND status = 'OPPRETTET')",
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
    @Column("is_kort_soknad")
    val kortSoknad: Boolean,
) : DomainRoot {
    override fun getDbId() = id
}

data class Tidspunkt(
    val opprettet: LocalDateTime,
    // TODO Hvordan skal diverse PUT / POSTS / REGISTER-OPPDATERINGER oppdatere denne? Skal den brukes til noe?
    var sistEndret: LocalDateTime? = null,
    var sendtInn: LocalDateTime? = null,
)

data class Begrunnelse(
    val hvorforSoke: String = "",
    val hvaSokesOm: String = "",
    val kategorier: Kategorier = Kategorier(),
)

data class Kategorier(
    val sett: Set<Kategori> = emptySet(),
)

@JsonTypeInfo(
    use = JsonTypeInfo.Id.NAME,
    include = JsonTypeInfo.As.PROPERTY,
    property = "type",
)
@JsonSubTypes(
    JsonSubTypes.Type(value = Kategori.Husleie::class, name = "Husleie"),
    JsonSubTypes.Type(value = Kategori.Livsopphold::class, name = "Livsopphold"),
    JsonSubTypes.Type(value = Kategori.StromOgOppvarming::class, name = "StromOgOppvarming"),
    JsonSubTypes.Type(value = Kategori.Nodhjelp.IkkeMat::class, name = "Nodhjelp.IkkeMat"),
    JsonSubTypes.Type(value = Kategori.Nodhjelp.IkkeStrom::class, name = "Nodhjelp.IkkeBosted"),
    JsonSubTypes.Type(value = Kategori.Nodhjelp.IkkeBosted::class, name = "Nodhjelp.IkkeStrom"),
)
sealed class Kategori(val key: String) {
    data object Livsopphold : Kategori("Livsopphold")

    data object Husleie : Kategori("Husleie")

    data object StromOgOppvarming : Kategori("StromOgOppvarming")

    sealed class Nodhjelp(underKategori: String) : Kategori("Nodhjelp.$underKategori") {
        data object IkkeMat : Nodhjelp("IkkeMat")

        data object IkkeBosted : Nodhjelp("IkkeBosted")

        data object IkkeStrom : Nodhjelp("IkkeStrom")
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
