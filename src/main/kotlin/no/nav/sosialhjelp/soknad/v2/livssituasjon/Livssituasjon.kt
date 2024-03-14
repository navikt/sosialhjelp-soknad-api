package no.nav.sosialhjelp.soknad.v2.livssituasjon

import no.nav.sosialhjelp.soknad.v2.config.repository.AggregateRoot
import no.nav.sosialhjelp.soknad.v2.config.repository.UpsertRepository
import org.springframework.data.annotation.Id
import org.springframework.data.relational.core.mapping.Column
import org.springframework.data.relational.core.mapping.Embedded
import org.springframework.data.relational.core.mapping.MappedCollection
import org.springframework.data.repository.ListCrudRepository
import org.springframework.stereotype.Repository
import java.util.*

@Repository
interface LivssituasjonRepository : UpsertRepository<Livssituasjon>, ListCrudRepository<Livssituasjon, UUID>

// TODO Navn?
data class Livssituasjon(
    @Id
    override val soknadId: UUID,
    @Embedded.Nullable
    val arbeid: Arbeid? = null,
    @Embedded.Nullable
    val utdanning: Utdanning? = null,
    @Embedded.Nullable
    val bosituasjon: Bosituasjon? = null,
) : AggregateRoot

data class Bosituasjon(
    val botype: Botype? = null,
    val antallHusstand: Int? = null
)

data class Utdanning(
    val erStudent: Boolean? = null,
    val studentgrad: Studentgrad? = null
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
    val start: String?,
    val slutt: String?,
    val fastStillingsprosent: Int? = 0,
    val harFastStilling: Boolean?
)

enum class Studentgrad {
    HELTID, DELTID
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
    ANNET;
}
