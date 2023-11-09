package no.nav.sosialhjelp.soknad.nymodell.domene.soknad

import no.nav.sosialhjelp.soknad.nymodell.domene.adresse.AdresseObject
import org.springframework.data.annotation.Id
import org.springframework.data.relational.core.mapping.Embedded
import org.springframework.data.relational.core.mapping.Embedded.OnEmpty.USE_NULL
import java.time.LocalDateTime
import java.util.*

// TODO Denne kan fjernes hvis man ønsker å overlate ID-generering til databasen
interface UuidAsIdObject {
    val id: UUID
}

abstract class IdIsSoknadIdObject (
    open val soknadId: UUID,
): UuidAsIdObject { override val id: UUID get() = soknadId }

data class Soknad(
    @Id override val id: UUID,
    val eier: Eier,
    var innsendingstidspunkt: LocalDateTime? = null,
): UuidAsIdObject

data class Eier(
    val personId: String,
    val statsborgerskap: String? = null,
    val nordiskBorger: Boolean? = null,
    val kontonummer: String? = null,
    @Embedded(onEmpty = USE_NULL) val navn: Navn? = null,
    @Embedded(onEmpty = USE_NULL) val kontaktInfo: KontaktInfo? = null
)

data class Navn(
    val fornavn: String? = null,
    val mellomnavn: String? = null,
    val etternavn: String? = null,
)

data class KontaktInfo(
    val telefonnummer: String? = null,
    val folkeregistrertAdresse: AdresseObject? = null
)
