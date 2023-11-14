package no.nav.sosialhjelp.soknad.nymodell.domene.okonomi

import no.nav.sosialhjelp.soknad.nymodell.domene.common.BubblesRepository
import no.nav.sosialhjelp.soknad.nymodell.domene.common.SoknadBubbles
import no.nav.sosialhjelp.soknad.nymodell.domene.common.UpsertRepository
import org.springframework.data.annotation.Id
import org.springframework.stereotype.Repository
import java.time.LocalDate
import java.util.*

@Repository
interface BekreftelseRepository: UpsertRepository<Bekreftelse>, BubblesRepository<Bekreftelse>

data class Bekreftelse (
    @Id override val id: UUID = UUID.randomUUID(),
    override val soknadId: UUID,
    val type: BekreftelseType? = null,
    val tittel: String? = null,
    val bekreftet: Boolean? = null,
    val bekreftelsesDato: LocalDate = LocalDate.now()
): SoknadBubbles(id, soknadId)

enum class BekreftelseType(tittel: String) {
    BEKREFTELSE_SPARING("inntekt.bankinnskudd"),
    BEKREFTELSE_UTBETALING("inntekt.inntekter"),
    BEKREFTELSE_VERDI("inntekt.eierandeler"),
    BEKREFTELSE_BARNEUTGIFTER("utgifter.barn"),
    BEKREFTELSE_BOUTGIFTER("utgifter.boutgift"),
    BOSTOTTE("inntekt.bostotte"),
    STUDIELAN("inntekt.student"),
}
