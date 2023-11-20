package no.nav.sosialhjelp.soknad.nymodell.domene.okonomi

import no.nav.sosialhjelp.soknad.nymodell.domene.BubblesRepository
import no.nav.sosialhjelp.soknad.nymodell.domene.SoknadBubbles
import no.nav.sosialhjelp.soknad.nymodell.domene.UpsertRepository
import org.springframework.data.annotation.Id
import org.springframework.stereotype.Repository
import java.time.LocalDate
import java.util.*

@Repository
interface BekreftelseRepository : UpsertRepository<Bekreftelse>, BubblesRepository<Bekreftelse>

data class Bekreftelse(
    @Id override val id: UUID = UUID.randomUUID(),
    override val soknadId: UUID,
    val type: BekreftelseType? = null,
    val tittel: String? = null,
    val bekreftet: Boolean? = null,
    val dato: LocalDate = LocalDate.now()
) : SoknadBubbles(id, soknadId)

enum class BekreftelseType(tittel: String) {
    SPARING("inntekt.bankinnskudd"),
    UTBETALING("inntekt.inntekter"),
    VERDI("inntekt.eierandeler"),
    BARNEUTGIFTER("utgifter.barn"),
    BOUTGIFTER("utgifter.boutgift"),
    BOSTOTTE("inntekt.bostotte"),
    STUDIELAN("inntekt.student"),
}
