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

    @Embedded.Empty
    val eier: Eier,

    @Embedded.Empty
    val tidspunkt: Tidspunkt = Tidspunkt(),

    var navEnhet: NavEnhet? = null,
    var arbeidsForhold: List<Arbeidsforhold> = emptyList()
): SoknadBubble { override val soknadId: UUID get() = id }

data class Eier(
    val personId: String,
    var statsborgerskap: String? = null,
    var nordiskBorger: Boolean? = null,
    var kontonummer: String? = null,
    var telefonnummer: String? = null,
    @Embedded(onEmpty = Embedded.OnEmpty.USE_NULL)
    val navn: Navn,
)

data class Tidspunkt(
    val opprettet: LocalDateTime = LocalDateTime.now().truncatedTo(ChronoUnit.SECONDS),
    var sistEndret: LocalDateTime? = null,
    var sendtInn: LocalDateTime? = null,
)

data class Navn(
    val fornavn: String,
    val mellomnavn: String? = null,
    val etternavn: String,
)

data class NavEnhet(
    val enhetsnavn: String,
    val enhetsnummer: String? = null,
    val kommunenummer: String? = null,
    val orgnummer: String? = null,
    val kommunenavn: String? = null,
)

// TODO Denne hører ikke hjemme her..... Men hvor? Stand-alone?
data class Arbeidsforhold(
    val arbeidsgivernavn: String,
    val orgnummer: String?,
    val start: String?,
    val slutt: String?,
    val fastStillingsprosent: Int? = 0,
    val harFastStilling: Boolean?
)
