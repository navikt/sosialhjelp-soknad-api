package no.nav.sosialhjelp.soknad.v2.soknad

import no.nav.sosialhjelp.soknad.v2.config.repository.SoknadBubble
import no.nav.sosialhjelp.soknad.v2.config.repository.UpsertRepository
import org.springframework.data.annotation.Id
import org.springframework.data.relational.core.mapping.Embedded
import org.springframework.data.repository.ListCrudRepository
import org.springframework.stereotype.Repository
import java.time.LocalDateTime
import java.time.temporal.ChronoUnit
import java.util.*

@Repository
interface SoknadRepository : UpsertRepository<Soknad>, ListCrudRepository<Soknad, UUID>

data class Soknad(
    @Id
    val id: UUID = UUID.randomUUID(),
    val eierPersonId: String,
    @Embedded.Empty
    val tidspunkt: Tidspunkt = Tidspunkt(opprettet = LocalDateTime.now().truncatedTo(ChronoUnit.SECONDS)),
    @Embedded.Nullable
    val begrunnelse: Begrunnelse? = null,
    @Embedded.Nullable
    val driftsinformasjon: Driftsinformasjon? = null,
) : SoknadBubble { override val soknadId: UUID get() = id }

data class Tidspunkt(
    val opprettet: LocalDateTime,
    var sistEndret: LocalDateTime? = null,
    var sendtInn: LocalDateTime? = null,
)

data class Begrunnelse(
    val hvorforSoke: String = "",
    val hvaSokesOm: String = "",
)

data class Driftsinformasjon(
    val utbetalingerFraNav: Boolean? = null,
    val inntektFraSkatt: Boolean? = null,
    val stotteFraHusbanken: Boolean? = null,
)
