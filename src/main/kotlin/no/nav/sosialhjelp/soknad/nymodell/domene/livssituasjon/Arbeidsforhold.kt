package no.nav.sosialhjelp.soknad.nymodell.domene.livssituasjon

import no.nav.sosialhjelp.soknad.nymodell.domene.common.BubblesRepository
import no.nav.sosialhjelp.soknad.nymodell.domene.common.UpsertRepository
import no.nav.sosialhjelp.soknad.nymodell.domene.common.SoknadBubbles
import org.springframework.data.annotation.Id
import org.springframework.data.repository.ListCrudRepository
import org.springframework.stereotype.Repository
import java.util.*

@Repository
interface ArbeidsforholdRepository : UpsertRepository<Arbeidsforhold>, BubblesRepository<Arbeidsforhold>

data class Arbeidsforhold (
    @Id override val id: UUID = UUID.randomUUID(),
    override val soknadId: UUID,
    val arbeidsgivernavn: String? = null,
    val fraOgMed: String? = null,
    val tilOgMed: String? = null,
    val stillingsprosent: Int? = null,
    val stillingstype: Stillingstype? = null
): SoknadBubbles(id, soknadId)

enum class Stillingstype {
    FAST, VARIABEL
}