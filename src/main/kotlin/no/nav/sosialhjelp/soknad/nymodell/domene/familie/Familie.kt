package no.nav.sosialhjelp.soknad.nymodell.domene.familie

import no.nav.sosialhjelp.soknad.nymodell.domene.Kilde
import no.nav.sosialhjelp.soknad.nymodell.domene.familie.type.Barnebidrag
import no.nav.sosialhjelp.soknad.nymodell.domene.familie.type.Sivilstatus
import no.nav.sosialhjelp.soknad.nymodell.domene.soknad.IdIsSoknadIdObject
import no.nav.sosialhjelp.soknad.nymodell.domene.soknad.Navn
import org.springframework.data.annotation.Id
import org.springframework.data.relational.core.mapping.Embedded
import org.springframework.data.relational.core.mapping.Embedded.OnEmpty.USE_NULL
import java.time.LocalDate
import java.util.*

data class Sivilstand (
    @Id override val soknadId: UUID,
    val kilde: Kilde,
    val sivilstatus: Sivilstatus? = null,
    val ektefelle: Ektefelle? = null,
): IdIsSoknadIdObject(soknadId)

data class Ektefelle (
    val personId: String,
    @Embedded(onEmpty = USE_NULL)
    val navn: Navn? = null,
    val fodselsdato: String? = null, // YYYY-MM-DD
    val harDiskresjonskode: Boolean? = null,
    val borSammen: Boolean? = null,
    val folkeregistrertMed: Boolean? = null
)

data class Forsorger(
    @Id override val soknadId: UUID,
    val harForsorgerplikt: Boolean? = null,
    val barnebidrag: Barnebidrag? = null,
    val barn: Set<Barn> = emptySet()
): IdIsSoknadIdObject(soknadId)

data class Barn ( // JsonAnsvar
    val personId: String,
    @Embedded(onEmpty = USE_NULL)
    val navn: Navn? = null,
    val fodselsdato: LocalDate? = null, // YYYY-MM-DD
    val borSammen: Boolean? = null,
    val folkeregistrertMed: Boolean? = null,
    val deltBosted: Boolean? = null,
    val samvarsgrad: Int? = null
)
