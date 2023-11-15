package no.nav.sosialhjelp.soknad.nymodell.domene.livssituasjon

import no.nav.sosialhjelp.soknad.nymodell.domene.BubbleRepository
import no.nav.sosialhjelp.soknad.nymodell.domene.SoknadBubble
import no.nav.sosialhjelp.soknad.nymodell.domene.UpsertRepository
import org.springframework.data.annotation.Id
import org.springframework.stereotype.Repository
import java.util.*

@Repository
interface UtdanningRepository: UpsertRepository<Utdanning>, BubbleRepository<Utdanning>

data class Utdanning (
    @Id override val soknadId: UUID,
    val erStudent: Boolean,
    val studentGrad: Studentgrad? = null
): SoknadBubble(soknadId)

enum class Studentgrad {
    HELTID, DELTID
}