package no.nav.sosialhjelp.soknad.v2.soknad

import org.springframework.data.annotation.Id
import org.springframework.data.relational.core.mapping.Embedded
import org.springframework.data.repository.ListCrudRepository
import org.springframework.stereotype.Repository
import java.time.LocalDateTime
import java.util.UUID

@Repository
interface SoknadRepository : ListCrudRepository<Soknad, UUID>

data class Soknad(
    @Id val id: UUID? = null,
    val eier: Eier,
    var innsendingstidspunkt: LocalDateTime? = null,
)

data class Eier(
    val personId: String,
    val statsborgerskap: String? = null,
    val nordiskBorger: Boolean? = null,
    val kontonummer: String? = null,
    @Embedded(onEmpty = Embedded.OnEmpty.USE_NULL)
    val navn: Navn,
    @Embedded(onEmpty = Embedded.OnEmpty.USE_NULL)
    val kontaktInfo: KontaktInfo? = null
)

data class KontaktInfo(
    val telefonnummer: String? = null,
    val folkeregistrertAdresse: String? = null,
    val midlertidigAdresse: String? = null
)

data class Navn(
    val fornavn: String,
    val mellomnavn: String? = null,
    val etternavn: String,
)
