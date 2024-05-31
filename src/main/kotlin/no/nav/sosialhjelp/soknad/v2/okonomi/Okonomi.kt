package no.nav.sosialhjelp.soknad.v2.okonomi

import no.nav.sosialhjelp.soknad.v2.config.repository.DomainRoot
import no.nav.sosialhjelp.soknad.v2.config.repository.UpsertRepository
import no.nav.sosialhjelp.soknad.v2.okonomi.formue.Formue
import no.nav.sosialhjelp.soknad.v2.okonomi.inntekt.Inntekt
import no.nav.sosialhjelp.soknad.v2.okonomi.utgift.Utgift
import org.springframework.data.annotation.Id
import org.springframework.data.relational.core.mapping.Column
import org.springframework.data.relational.core.mapping.Embedded
import org.springframework.data.relational.core.mapping.Table
import org.springframework.data.repository.ListCrudRepository
import org.springframework.stereotype.Repository
import java.util.UUID

@Repository
interface OkonomiRepository : UpsertRepository<Okonomi>, ListCrudRepository<Okonomi, UUID>

@Table
data class Okonomi(
    @Id val soknadId: UUID,
    val inntekter: List<Inntekt> = emptyList(),
    val utgifter: List<Utgift> = emptyList(),
    val formuer: List<Formue> = emptyList(),
    val bekreftelser: Set<Bekreftelse> = emptySet(),
    @Embedded.Empty
    val beskrivelserAnnet: BeskrivelserAnnet = BeskrivelserAnnet(),
) : DomainRoot {
    override fun getDbId() = soknadId
}

@Table
data class Bekreftelse(
    val type: BekreftelseType,
    val tittel: String,
    val verdi: Boolean,
)

@Table
data class BeskrivelserAnnet(
    @Column("beskrivelse_verdi")
    val verdi: String? = null,
    @Column("beskrivelse_sparing")
    val sparing: String? = null,
    @Column("beskrivelse_utbetaling")
    val utbetaling: String? = null,
    @Column("beskrivelse_boutgifter")
    val boutgifter: String? = null,
    @Column("beskrivelse_barneutgifter")
    val barneutgifter: String? = null,
)

interface OkonomiType {
    // denne må hete `name` for å override enum.name
    val name: String
}

enum class BekreftelseType(val tittelKey: String) {
    BEKREFTELSE_BARNEUTGIFTER(tittelKey = "utgifter.barn"),
    BEKREFTELSE_BOUTGIFTER(tittelKey = "utgifter.boutgift"),
    BEKREFTELSE_SPARING(tittelKey = "inntekt.bankinnskudd"),
    BEKREFTELSE_UTBETALING(tittelKey = "inntekt.inntekter"),
    BEKREFTELSE_VERDI(tittelKey = "inntekt.eierandeler"),

    // TODO sjekk bruk av disse
    BOSTOTTE(tittelKey = "inntekt.bostotte"),

    // TODO sjekk bruk av disse
    BOSTOTTE_SAMTYKKE(tittelKey = "inntekt.bostotte.samtykke"),
    STUDIELAN_BEKREFTELSE(tittelKey = "inntekt.student"),
    UTBETALING_SKATTEETATEN_SAMTYKKE(tittelKey = "utbetalinger.skattbar.samtykke"),
}
