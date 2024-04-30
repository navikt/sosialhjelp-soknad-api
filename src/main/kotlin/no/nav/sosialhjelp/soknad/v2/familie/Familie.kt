package no.nav.sosialhjelp.soknad.v2.familie

import no.nav.sosialhjelp.soknad.v2.config.repository.DomainRoot
import no.nav.sosialhjelp.soknad.v2.navn.Navn
import org.springframework.data.annotation.Id
import org.springframework.data.relational.core.mapping.Embedded
import org.springframework.data.relational.core.mapping.Table
import java.util.UUID
import no.nav.sosialhjelp.soknad.v2.config.repository.UpsertRepository
import org.springframework.data.repository.ListCrudRepository
import org.springframework.stereotype.Repository

@Repository
interface FamilieRepository : UpsertRepository<Familie>, ListCrudRepository<Familie, UUID>

@Table
data class Familie(
    @Id
    override val soknadId: UUID,
    @Embedded.Empty
    val forsorger: Forsorger = Forsorger(),
    @Embedded.Empty
    val sivilstand: Sivilstand = Sivilstand(),
) : DomainRoot

data class Forsorger(
    val harForsorgerplikt: Boolean? = null, // fra JsonForsorgerplikt
    val barnebidrag: Barnebidrag? = null, // fra JsonForsorgerplikt
    val ansvar: Map<UUID, Barn> = emptyMap(), // jsonForsorgerplikt
)

data class Barn(
    // JsonAnsvar
    val familieKey: UUID, // syntetisk id for å ikke eksponere personId
    val personId: String? = null,
    @Embedded.Nullable
    val navn: Navn? = null,
    val fodselsdato: String? = null,
    val borSammen: Boolean? = null,
    val folkeregistrertSammen: Boolean? = null,
    val deltBosted: Boolean? = null,
    val samvarsgrad: Int? = null,
)

data class Sivilstand(
    val sivilstatus: Sivilstatus? = null,
    val ektefelle: Ektefelle? = null, // jsonSivilstatus
)

data class Ektefelle(
    @Embedded.Nullable
    val navn: Navn?,
    val fodselsdato: String?,
    val personId: String?,
    val harDiskresjonskode: Boolean? = null,
    val folkeregistrertMedEktefelle: Boolean? = null,
    val borSammen: Boolean? = null,
    val kildeErSystem: Boolean = true,
) {
    companion object
}

enum class Barnebidrag {
    BETALER,
    MOTTAR,
    BEGGE,
    INGEN,
}

enum class Sivilstatus {
    GIFT,
    UGIFT,
    SAMBOER,
    ENKE,
    SKILT,
    SEPARERT,
    TOM;
}
