package no.nav.sosialhjelp.soknad.v2.okonomi

import no.nav.sosialhjelp.soknad.v2.config.repository.DomainRoot
import no.nav.sosialhjelp.soknad.v2.config.repository.UpsertRepository
import org.springframework.data.annotation.Id
import org.springframework.data.jdbc.repository.query.Modifying
import org.springframework.data.jdbc.repository.query.Query
import org.springframework.data.relational.core.mapping.Column
import org.springframework.data.relational.core.mapping.Table
import org.springframework.data.repository.ListCrudRepository
import org.springframework.stereotype.Repository
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.temporal.ChronoUnit
import java.util.UUID

@Repository
interface OkonomiRepository : UpsertRepository<Okonomi>, ListCrudRepository<Okonomi, UUID> {
    @Modifying
    @Transactional
    @Query("INSERT INTO bekreftelse(okonomi, type, tidspunkt, verdi) VALUES(:soknadId, :type, :tidspunkt, :verdi)")
    fun addBekreftelse(
        soknadId: UUID,
        type: BekreftelseType,
        tidspunkt: LocalDateTime,
        verdi: Boolean,
    )

    @Modifying
    @Transactional
    @Query("DELETE FROM bekreftelse WHERE okonomi = :soknadId AND type = :type")
    fun deleteBekreftelse(
        soknadId: UUID,
        type: BekreftelseType,
    )

    @Modifying
    @Transactional
    @Query("INSERT INTO formue(okonomi, type, beskrivelse, detaljer) VALUES(:soknadId, :type, :beskrivelse, :detaljer)")
    fun addFormue(
        soknadId: UUID,
        type: FormueType,
        beskrivelse: String?,
        detaljer: String?,
    )

    @Modifying
    @Transactional
    @Query("DELETE FROM formue WHERE okonomi = :soknadId AND type = :type")
    fun deleteFormue(
        soknadId: UUID,
        type: FormueType,
    )

    @Modifying
    @Transactional
    @Query("INSERT INTO inntekt(okonomi, type, beskrivelse, detaljer) VALUES(:soknadId, :type, :beskrivelse, :detaljer)")
    fun addInntekt(
        soknadId: UUID,
        type: InntektType,
        beskrivelse: String?,
        detaljer: String?,
    )

    @Modifying
    @Transactional
    @Query("DELETE FROM inntekt WHERE okonomi = :soknadId AND type = :type")
    fun deleteInntekt(
        soknadId: UUID,
        type: InntektType,
    )

    @Modifying
    @Transactional
    @Query("INSERT INTO utgift(okonomi, type, beskrivelse, detaljer) VALUES(:soknadId, :type, :beskrivelse, :detaljer)")
    fun addUtgift(
        soknadId: UUID,
        type: UtgiftType,
        beskrivelse: String?,
        detaljer: String?,
    )

    @Modifying
    @Transactional
    @Query("DELETE FROM utgift WHERE okonomi = :soknadId AND type = :type")
    fun deleteUtgift(
        soknadId: UUID,
        type: UtgiftType,
    )
}

@Table
data class Okonomi(
    @Id val soknadId: UUID,
    val inntekter: Set<Inntekt> = emptySet(),
    val utgifter: Set<Utgift> = emptySet(),
    val formuer: Set<Formue> = emptySet(),
    val bekreftelser: Set<Bekreftelse> = emptySet(),
    val bostotteSaker: List<BostotteSak> = emptyList(),
) : DomainRoot {
    override fun getDbId() = soknadId
}

@Table
data class Bekreftelse(
    val type: BekreftelseType,
    val tidspunkt: LocalDateTime = LocalDateTime.now().truncatedTo(ChronoUnit.MILLIS),
    val verdi: Boolean,
)

@Table
data class BostotteSak(
    val dato: LocalDate,
    val status: BostotteStatus,
    val beskrivelse: String?,
    val vedtaksstatus: Vedtaksstatus?,
)

enum class BekreftelseType {
    BEKREFTELSE_BARNEUTGIFTER,
    BEKREFTELSE_BOUTGIFTER,
    BEKREFTELSE_SPARING,
    BEKREFTELSE_UTBETALING,
    BEKREFTELSE_VERDI,
    STUDIELAN_BEKREFTELSE,

    BOSTOTTE,

    BOSTOTTE_SAMTYKKE,
    UTBETALING_SKATTEETATEN_SAMTYKKE,
}

enum class Vedtaksstatus {
    INNVILGET,
    AVSLAG,
    AVVIST,
}

enum class BostotteStatus {
    UNDER_BEHANDLING,
    VEDTATT,
}

// Inntekt, Utgift, Formue
sealed interface OkonomiOpplysning {
    val type: OkonomiOpplysningType
    val beskrivelse: String?
}

@Table
data class Inntekt(
    override val type: InntektType,
    override val beskrivelse: String? = null,
    @Column("detaljer")
    val inntektDetaljer: OkonomiDetaljer<OkonomiDetalj> = OkonomiDetaljer(),
) : OkonomiOpplysning

@Table
data class Formue(
    override val type: FormueType,
    override val beskrivelse: String? = null,
    @Column("detaljer")
    val formueDetaljer: OkonomiDetaljer<Belop> = OkonomiDetaljer(),
) : OkonomiOpplysning

@Table
data class Utgift(
    override val type: UtgiftType,
    override val beskrivelse: String? = null,
    @Column("detaljer")
    val utgiftDetaljer: OkonomiDetaljer<OkonomiDetalj> = OkonomiDetaljer(),
) : OkonomiOpplysning

fun Set<Utgift>.hasType(type: UtgiftType): Boolean = any { it.type == type }
