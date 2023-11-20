package no.nav.sosialhjelp.soknad.nymodell.domene.familie

import no.nav.sosialhjelp.soknad.nymodell.domene.BubbleRepository
import no.nav.sosialhjelp.soknad.nymodell.domene.Navn
import no.nav.sosialhjelp.soknad.nymodell.domene.SoknadBubble
import no.nav.sosialhjelp.soknad.nymodell.domene.UpsertRepository
import org.springframework.data.annotation.Id
import org.springframework.data.relational.core.mapping.Embedded
import org.springframework.stereotype.Repository
import java.time.LocalDate
import java.util.*

@Repository
interface ForsorgerRepository : UpsertRepository<Forsorger>, BubbleRepository<Forsorger>

data class Forsorger(
    @Id override val soknadId: UUID,
    val harForsorgerplikt: Boolean? = null,
    val barnebidrag: Barnebidrag? = null,
    val barn: Set<Barn> = emptySet()
) : SoknadBubble(soknadId)

data class Barn( // JsonAnsvar
    val personId: String,
    @Embedded(onEmpty = Embedded.OnEmpty.USE_NULL)
    val navn: Navn? = null,
    val fodselsdato: LocalDate? = null, // YYYY-MM-DD
    val borSammen: Boolean? = null,
    val folkeregistrertMed: Boolean? = null,
    val deltBosted: Boolean? = null,
    val samvarsgrad: Int? = null
)

enum class Barnebidrag {
    BETALER, MOTTAR, BEGGE, INGEN
}
