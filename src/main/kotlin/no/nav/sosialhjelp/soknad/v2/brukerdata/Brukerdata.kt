package no.nav.sosialhjelp.soknad.v2.brukerdata

import no.nav.sosialhjelp.soknad.v2.config.repository.SoknadBubble
import no.nav.sosialhjelp.soknad.v2.config.repository.UpsertRepository
import org.springframework.data.annotation.Id
import org.springframework.data.relational.core.mapping.Embedded
import org.springframework.data.repository.ListCrudRepository
import org.springframework.stereotype.Repository
import java.time.LocalDate
import java.util.*

@Repository
interface BrukerdataRepository : UpsertRepository<Brukerdata>, ListCrudRepository<Brukerdata, UUID>

data class Brukerdata(
    @Id override val soknadId: UUID,
    @Embedded(onEmpty = Embedded.OnEmpty.USE_NULL)
    var begrunnelse: Begrunnelse? = null,
    @Embedded(onEmpty = Embedded.OnEmpty.USE_NULL)
    var kontoInformasjon: KontoInformasjonBruker? = null,
    var telefonnummer: String? = null,
    var kommentarArbeidsforhold: String? = null,
    val samtykker: Set<Samtykke> = initSamtykker(),
    var beskrivelseAvAnnet: BeskrivelseAvAnnet? = null,
) : SoknadBubble

data class KontoInformasjonBruker(
    val kontonummer: String? = null,
    val harIkkeKonto: Boolean? = null,
)

enum class AdresseValg {
    FOLKEREGISTRERT,
    MIDLERTIDIG,
    SOKNAD;
}

data class Begrunnelse(
    val hvorforSoke: String? = null,
    val hvaSokesOm: String? = null,
)

data class BeskrivelseAvAnnet(
    val barneutgifter: String? = null,
    val verdier: String? = null,
    val sparing: String? = null,
    val utbetalinger: String? = null,
    val boutgifter: String? = null
)

data class Samtykke(
    val type: SamtykkeType,
    var verdi: Boolean?,
    var dato: LocalDate?
)
enum class SamtykkeType(val tittel: String) {
    BOSTOTTE("inntekt.bostotte.samtykke"),
    UTBETALING_SKATTEETATEN("utbetalinger.skattbar.samtykke");
}

private fun initSamtykker(): Set<Samtykke> {
    return setOf(
        Samtykke(SamtykkeType.BOSTOTTE, null, null),
        Samtykke(SamtykkeType.UTBETALING_SKATTEETATEN, null, null)
    )
}
