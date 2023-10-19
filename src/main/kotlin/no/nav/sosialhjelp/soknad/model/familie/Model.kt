package no.nav.sosialhjelp.soknad.model.familie

import no.nav.sosialhjelp.soknad.model.soknad.KeyErSoknadId
import org.springframework.data.annotation.Id
import org.springframework.data.relational.core.mapping.Column
import org.springframework.data.relational.core.mapping.MappedCollection
import java.util.*

data class Familie (
    @Id val soknadId: UUID,
    val harForsorgerplikt: Boolean? = null, // fra JsonForsorgerplikt
    val barnebidrag: Barnebidrag? = null, // fra JsonForsorgerplikt
    val sivilstatus: Sivilstatus? = null,
    @MappedCollection(idColumn = "SOKNAD_ID")
    val ansvar: Set<Barn> = emptySet(),        // jsonForsorgerplikt
    @Column(value = "SOKNAD_ID" )
    val ektefelle: Ektefelle? = null // jsonSivilstatus
): KeyErSoknadId { override val id: UUID get() = soknadId }

data class Barn ( // JsonAnsvar
    val soknadId: UUID,
    val personId: String,
    val borSammen: Boolean? = null,
    val folkeregistrertSammen: Boolean? = null,
    val deltBosted: Boolean? = null,
    val samvarsgrad: Int? = null
)

data class Ektefelle (
    val soknadId: UUID,
    val personId: String?,
    val harDiskresjonskode: Boolean? = null,
    val folkeregistrertMedEktefelle: Boolean? = null,
    val borSammen: Boolean? = null
)

enum class Barnebidrag {
    BETALER, MOTTAR, BEGGE, INGEN
}

enum class Sivilstatus {
    GIFT, UGIFT, SAMBOER, ENKE, SKILT, SEPARERT;
}
