package no.nav.sosialhjelp.soknad.nymodell.domene.okonomi

import no.nav.sosialhjelp.soknad.nymodell.domene.common.BubblesRepository
import no.nav.sosialhjelp.soknad.nymodell.domene.common.SoknadBubbles
import no.nav.sosialhjelp.soknad.nymodell.domene.common.UpsertRepository
import org.springframework.data.annotation.Id
import org.springframework.stereotype.Repository
import java.util.*

@Repository
interface FormueRepository: UpsertRepository<Formue>, BubblesRepository<Formue>

data class Formue (
    @Id override val id: UUID = UUID.randomUUID(),
    override val soknadId: UUID,
    val type: FormueType,
    val tittel: String? = null,
    val belop: Int? = null
): SoknadBubbles(id, soknadId)

enum class FormueType(
    tittel: String = ""
): OkonomiType {
    KONTOOVERSIKT_AKSJER("opplysninger.inntekt.bankinnskudd.aksjer"),
    KONTOOVERSIKT_ANNET("opplysninger.inntekt.bankinnskudd.annet"),
    KONTOOVERSIKT_BRUKSKONTO("opplysninger.inntekt.bankinnskudd.brukskonto"),
    KONTOOVERSIKT_BSU("opplysninger.inntekt.bankinnskudd.bsu"),
    KONTOOVERSIKT_LIVSFORSIKRING("opplysninger.inntekt.bankinnskudd.livsforsikring"),
    KONTOOVERSIKT_SPAREKONTO("opplysninger.inntekt.bankinnskudd.sparekonto");
}