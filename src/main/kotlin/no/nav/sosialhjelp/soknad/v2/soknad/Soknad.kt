package no.nav.sosialhjelp.soknad.v2.soknad

import org.springframework.data.annotation.Id
import org.springframework.data.relational.core.mapping.Embedded
import org.springframework.data.repository.ListCrudRepository
import org.springframework.stereotype.Repository
import java.time.LocalDateTime
import java.util.*

@Repository
interface SoknadRepository : ListCrudRepository<Soknad, UUID>

data class Soknad(
    @Id
    val id: UUID? = null,
    @Embedded(onEmpty = Embedded.OnEmpty.USE_EMPTY)
    val eier: Eier,
    var innsendingstidspunkt: LocalDateTime? = null,
    var navEnhet: NavEnhet? = null,
)

data class Eier(
    val personId: String,
    var statsborgerskap: String? = null,
    var nordiskBorger: Boolean? = null,
    var kontonummer: String? = null,
    var telefonnummer: String? = null,
    @Embedded(onEmpty = Embedded.OnEmpty.USE_NULL)
    val navn: Navn,
)

data class Navn(
    val fornavn: String,
    val mellomnavn: String? = null,
    val etternavn: String,
)

data class NavEnhet(
    val enhetsnummer: String?,
    val enhetsnavn: String,
    val kommunenummer: String?,
    val orgnummer: String?
)
