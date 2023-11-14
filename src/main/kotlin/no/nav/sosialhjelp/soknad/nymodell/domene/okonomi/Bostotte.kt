package no.nav.sosialhjelp.soknad.nymodell.domene.okonomi

import no.nav.sosialhjelp.soknad.nymodell.domene.common.BubblesRepository
import no.nav.sosialhjelp.soknad.nymodell.domene.common.SoknadBubbles
import no.nav.sosialhjelp.soknad.nymodell.domene.common.UpsertRepository
import org.springframework.data.annotation.Id
import org.springframework.stereotype.Repository
import java.time.LocalDate
import java.util.*

@Repository
interface BostotteRepository: UpsertRepository<Bostotte>, BubblesRepository<Bostotte>

data class Bostotte (
    @Id override val id: UUID = UUID.randomUUID(),
    override val soknadId: UUID,
    val type: String? = null, // TODO For sterkere typing, sjekk API-dokumentasjon
    val dato: LocalDate? = null,
    val status: BostotteStatus? = null,
    val beskrivelse: String? = null,
    val vedtaksstatus: Vedtaksstatus? = null,
): SoknadBubbles(id, soknadId)

enum class BostotteStatus {
    UNDER_BEHANDLING, VEDTATT
}

enum class Vedtaksstatus {
    INNVILGET, AVSLAG, AVVIST;
}