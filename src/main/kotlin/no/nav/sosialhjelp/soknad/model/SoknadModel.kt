package no.nav.sosialhjelp.soknad.model

import no.nav.sosialhjelp.soknad.repository.DelAvSoknad
import org.springframework.data.annotation.Id
import org.springframework.data.relational.core.mapping.Column
import org.springframework.data.relational.core.mapping.MappedCollection
import java.time.LocalDateTime
import java.util.*

data class Soknad (
    @Id val id: UUID = UUID.randomUUID(),
    val eier: String,
    var innsendingstidspunkt: LocalDateTime? = null,
    var hvorforSoke: String? = null,
    var hvaSokesOm: String? = null,
): DelAvSoknad {
    override val soknadId: UUID get() = id
}

data class Bosituasjon (
    @Id override val soknadId: UUID,
    var botype: Botype?,
    var antallPersoner: Int
): DelAvSoknad

data class Arbeid (
    @Id override val soknadId: UUID,
    var kommentarArbeid: String? = null,
    @MappedCollection(idColumn = "SOKNAD_ID")
    val arbeidsforhold: Set<Arbeidsforhold> = emptySet()
): DelAvSoknad

data class Arbeidsforhold (
//    @Column(value = "SOKNAD_ID")
    val soknadId: UUID,
    val orgnummer: String? = null,
    val arbeidsgivernavn: String,
    val fraOgMed: String? = null,
    val tilOgMed: String? = null,
    val stillingsprosent: Int? = null,
    val stillingstype: Stillingstype
)

data class Vedlegg (
    @Id val id: Long = 0,
    val soknadId: UUID,
    val vedleggType: VedleggType,
    val status: String,
    val hendelseType: VedleggHendelseType,
    val hendelseReferanse: String,
)

data class Fil (
    @Id val id: Long = 0,
    val vedleggId: Long,
    val filnavn: String? = null,
    val sha512: String? = null
)
