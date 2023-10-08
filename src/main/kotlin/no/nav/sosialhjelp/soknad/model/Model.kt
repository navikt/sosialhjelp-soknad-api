package no.nav.sosialhjelp.soknad.model

import no.nav.sbl.soknadsosialhjelp.soknad.bosituasjon.JsonBosituasjon.Botype
import org.springframework.data.annotation.Id
import org.springframework.data.relational.core.mapping.MappedCollection
import java.time.LocalDateTime
import java.util.UUID

//interface SoknadEntitet {
//    val id: UUID
//}
data class Soknad (
    @Id val id: Long = 0,
    val soknadId: UUID,
    val innsendingstidspunkt: LocalDateTime? = null,
    val hvorforSoke: String? = null,
    val hvaSokesOm: String? = null,
    val kommentarArbeid: String? = null
)

data class Bosituasjon (
    @Id val id: Long = 0,
    val soknadId: UUID,
    val botype: Botype? = null,
    val antallPersoner: Int? = null
)

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