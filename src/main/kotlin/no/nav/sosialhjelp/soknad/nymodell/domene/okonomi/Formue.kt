package no.nav.sosialhjelp.soknad.nymodell.domene.okonomi

import no.nav.sosialhjelp.soknad.nymodell.domene.BubblesRepository
import no.nav.sosialhjelp.soknad.nymodell.domene.SoknadBubbles
import no.nav.sosialhjelp.soknad.nymodell.domene.UpsertRepository
import org.springframework.data.annotation.Id
import org.springframework.stereotype.Repository
import java.util.*

@Repository
interface FormueRepository : UpsertRepository<Formue>, BubblesRepository<Formue>

data class Formue(
    @Id override val id: UUID = UUID.randomUUID(),
    override val soknadId: UUID,
    val type: FormueType,
    val tittel: String? = null,
    val belop: Int? = null
) : SoknadBubbles(id, soknadId)

enum class FormueType(
    tittel: String = ""
) : OkonomiType {
    AKSJER("opplysninger.inntekt.bankinnskudd.aksjer"),
    ANNET("opplysninger.inntekt.bankinnskudd.annet"),
    BRUKSKONTO("opplysninger.inntekt.bankinnskudd.brukskonto"),
    BSU("opplysninger.inntekt.bankinnskudd.bsu"),
    LIVSFORSIKRING("opplysninger.inntekt.bankinnskudd.livsforsikring"),
    SPAREKONTO("opplysninger.inntekt.bankinnskudd.sparekonto");
}
