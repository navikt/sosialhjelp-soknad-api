package no.nav.sosialhjelp.soknad.nymodell.domene.soknad

import no.nav.sosialhjelp.soknad.nymodell.domene.HasUuidAsId
import no.nav.sosialhjelp.soknad.nymodell.domene.UpsertRepository
import no.nav.sosialhjelp.soknad.nymodell.domene.adresse.AdresseObject
import no.nav.sosialhjelp.soknad.nymodell.domene.Navn
import org.springframework.data.annotation.Id
import org.springframework.data.relational.core.mapping.Embedded
import org.springframework.data.relational.core.mapping.Embedded.OnEmpty.USE_NULL
import org.springframework.data.repository.ListCrudRepository
import java.time.LocalDateTime
import java.util.*

@org.springframework.stereotype.Repository
interface SoknadRepository : UpsertRepository<Soknad>, ListCrudRepository<Soknad, UUID>

data class Soknad(
    @Id override val id: UUID,
    val eier: Eier,
    var innsendingstidspunkt: LocalDateTime? = null,
): HasUuidAsId

data class Eier(
    val personId: String,
    val statsborgerskap: String? = null,
    val nordiskBorger: Boolean? = null,
    val kontonummer: String? = null,
    @Embedded(onEmpty = USE_NULL)
    val navn: Navn? = null,
    @Embedded(onEmpty = USE_NULL)
    val kontaktInfo: KontaktInfo? = null
)

data class KontaktInfo(
    val telefonnummer: String? = null,
    val folkeregistrertAdresse: AdresseObject? = null,
    val midlertidigAdresse: AdresseObject? = null
)

