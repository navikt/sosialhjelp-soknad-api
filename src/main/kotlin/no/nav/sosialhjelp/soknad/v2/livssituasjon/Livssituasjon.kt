package no.nav.sosialhjelp.soknad.v2.livssituasjon

import no.nav.sosialhjelp.soknad.v2.config.repository.DomainRoot
import no.nav.sosialhjelp.soknad.v2.config.repository.UpsertRepository
import org.springframework.data.annotation.Id
import org.springframework.data.relational.core.mapping.Column
import org.springframework.data.relational.core.mapping.Embedded
import org.springframework.data.relational.core.mapping.MappedCollection
import org.springframework.data.relational.core.mapping.Table
import org.springframework.data.repository.ListCrudRepository
import org.springframework.stereotype.Repository
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.UUID

@Repository
interface LivssituasjonRepository : UpsertRepository<Livssituasjon>, ListCrudRepository<Livssituasjon, UUID>

// TODO Navn?
@Table
data class Livssituasjon(
    @Id val soknadId: UUID,
    @Embedded.Empty
    val arbeid: Arbeid = Arbeid(),
    @Embedded.Nullable
    val utdanning: Utdanning? = null,
    @Embedded.Nullable
    val bosituasjon: Bosituasjon? = null,
) : DomainRoot {
    override fun getDbId() = soknadId
}

data class Bosituasjon(
    val botype: Botype? = null,
    val antallHusstand: Int? = null,
)

data class Utdanning(
    val erStudent: Boolean = false,
    val studentgrad: Studentgrad? = null,
)

data class Arbeid(
    @Column("kommentar_arbeidsforhold")
    val kommentar: String? = null,
    @MappedCollection(idColumn = "soknad_id", keyColumn = "index")
    val arbeidsforhold: List<Arbeidsforhold> = emptyList(),
)

data class Arbeidsforhold(
    val arbeidsgivernavn: String,
    val orgnummer: String?,
    val start: LocalDate?,
    val slutt: LocalDate?,
    val fastStillingsprosent: Int? = 0,
    val harFastStilling: Boolean?,
)

fun LocalDate.toIsoString(): String = this.format(DateTimeFormatter.ISO_LOCAL_DATE)

enum class Studentgrad {
    HELTID,
    DELTID,
}

enum class Botype {
    EIER,
    LEIER,
    KOMMUNAL,
    INGEN,
    INSTITUSJON,
    KRISESENTER,
    FENGSEL,
    VENNER,
    FORELDRE,
    FAMILIE,
    ANNET,
}
