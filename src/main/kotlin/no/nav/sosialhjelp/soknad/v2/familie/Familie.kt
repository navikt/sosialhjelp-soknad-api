package no.nav.sosialhjelp.soknad.v2.familie

import no.nav.sosialhjelp.soknad.v2.config.repository.SoknadBubble
import no.nav.sosialhjelp.soknad.v2.navn.Navn
import org.springframework.data.annotation.Id
import org.springframework.data.relational.core.mapping.Embedded
import java.util.UUID

data class Familie(
    @Id
    override val soknadId: UUID,
    val harForsorgerplikt: Boolean? = null, // fra JsonForsorgerplikt
    val barnebidrag: Barnebidrag? = null, // fra JsonForsorgerplikt
    val sivilstatus: Sivilstatus? = null,
    val ansvar: Map<UUID, Barn> = emptyMap(), // jsonForsorgerplikt
    val ektefelle: Ektefelle? = null, // jsonSivilstatus
) : SoknadBubble

data class Barn(
    // JsonAnsvar
    val familieKey: UUID,
    val personId: String? = null,
    @Embedded.Nullable
    val navn: Navn? = null,
    val fodselsdato: String? = null,
    val borSammen: Boolean? = null,
    val folkeregistrertSammen: Boolean? = null,
    val deltBosted: Boolean? = null,
    val samvarsgrad: Int? = null,
)

data class Ektefelle(
    @Embedded.Nullable
    val navn: Navn?,
    val fodselsdato: String?,
    val personId: String?,
    val folkeregistrertMedEktefelle: Boolean? = null,
    val borSammen: Boolean? = null,
    val kildeErSystem: Boolean = true,
)

enum class Barnebidrag {
    BETALER, MOTTAR, BEGGE, INGEN,
}

enum class Sivilstatus {
    GIFT, UGIFT, SAMBOER, ENKE, SKILT, SEPARERT,
}
