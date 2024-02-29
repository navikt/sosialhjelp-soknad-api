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
    val tidspunkt: Tidspunkt = Tidspunkt(),
    @Embedded.Empty
    val begrunnelse: Begrunnelse = Begrunnelse(),
    @Embedded.Empty
    val driftsinformasjon: Driftsinformasjon = Driftsinformasjon(),
) : SoknadBubble { override val soknadId: UUID get() = id }

data class Tidspunkt(
    val opprettet: LocalDateTime = LocalDateTime.now().truncatedTo(ChronoUnit.SECONDS),
    var sistEndret: LocalDateTime? = null,
    var sendtInn: LocalDateTime? = null,
)

data class Begrunnelse(
    val hvorforSoke: String? = null,
    val hvaSokesOm: String? = null,
)

data class Driftsinformasjon(
    val utbetalingerFraNav: Boolean? = null,
    val inntektFraSkatt: Boolean? = null,
    val stotteFraHusbanken: Boolean? = null,
)
