package no.nav.sosialhjelp.soknad.nymodell.domene.livssituasjon

import no.nav.sosialhjelp.soknad.nymodell.domene.BubbleRepository
import no.nav.sosialhjelp.soknad.nymodell.domene.UpsertRepository
import no.nav.sosialhjelp.soknad.nymodell.domene.SoknadBubble
import org.springframework.data.annotation.Id
import org.springframework.stereotype.Repository
import java.util.*

@Repository
interface BosituasjonRepository : UpsertRepository<Bosituasjon>, BubbleRepository<Bosituasjon>

data class Bosituasjon (
    @Id override val soknadId: UUID,
    var botype: Botype?,
    var antallPersoner: Int
): SoknadBubble(soknadId)

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