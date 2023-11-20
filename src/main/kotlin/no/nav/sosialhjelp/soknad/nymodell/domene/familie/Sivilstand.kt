package no.nav.sosialhjelp.soknad.nymodell.domene.familie

import no.nav.sosialhjelp.soknad.nymodell.domene.BubbleRepository
import no.nav.sosialhjelp.soknad.nymodell.domene.Kilde
import no.nav.sosialhjelp.soknad.nymodell.domene.Navn
import no.nav.sosialhjelp.soknad.nymodell.domene.SoknadBubble
import no.nav.sosialhjelp.soknad.nymodell.domene.UpsertRepository
import org.springframework.data.annotation.Id
import org.springframework.data.relational.core.mapping.Embedded
import org.springframework.stereotype.Repository
import java.util.*

@Repository
interface SivilstandRepository : UpsertRepository<Sivilstand>, BubbleRepository<Sivilstand>

data class Sivilstand(
    @Id override val soknadId: UUID,
    val kilde: Kilde,
    val sivilstatus: Sivilstatus? = null,
    val ektefelle: Ektefelle? = null,
) : SoknadBubble(soknadId)

data class Ektefelle(
    val personId: String,
    @Embedded(onEmpty = Embedded.OnEmpty.USE_NULL)
    val navn: Navn? = null,
    val fodselsdato: String? = null, // YYYY-MM-DD
    val harDiskresjonskode: Boolean? = null,
    val borSammen: Boolean? = null,
    val folkeregistrertMed: Boolean? = null
)

enum class Sivilstatus {
    GIFT, UGIFT, SAMBOER, ENKE, SKILT, SEPARERT;
}
