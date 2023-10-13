package no.nav.sosialhjelp.soknad.model

import no.nav.sosialhjelp.soknad.repository.PartOfSoknad
import org.springframework.data.annotation.Id
import java.time.LocalDateTime
import java.util.*

//interface SoknadEntitet {
//    val id: UUID
//}
data class Soknad (
//    @Id val id: Long = 0,
//    val soknadId: UUID,
    @Id override val id: UUID = UUID.randomUUID(),
    var innsendingstidspunkt: LocalDateTime? = null,
    var hvorforSoke: String? = null,
    var hvaSokesOm: String? = null,
    var kommentarArbeid: String? = null
): PartOfSoknad

data class SoknadEier (
    @Id val id: UUID = UUID.randomUUID(),
    val personIdentifikator: String
)

data class Bosituasjon (
//    @Id val id: Long = 0,
    @Id val soknadId: UUID,
    var botype: Botype? = null,
    var antallPersoner: Int? = null
): PartOfSoknad {
    override val id: UUID
        get() = soknadId
}

data class Vedlegg (
    @Id val id: Long = 0,
    val soknadId: UUID,
    val vedleggstype: String? = null,
    val tilleggsinfo: String? = null,
    val status: String? = null,
    val hendelseType: String? = null,
    val hendelseReferanse: String? = null,
)

data class Fil (
    @Id val id: Long = 0,
    val vedleggId: Long,
    val filnavn: String? = null,
    val sha512: String? = null
)