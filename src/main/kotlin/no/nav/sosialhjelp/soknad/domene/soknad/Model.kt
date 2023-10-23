package no.nav.sosialhjelp.soknad.domene.soknad

import no.nav.sosialhjelp.soknad.domene.personalia.AdresseValg
import org.springframework.data.annotation.Id
import java.time.LocalDateTime
import java.util.*


interface KeyErSoknadId {
    val id: UUID
}

data class Soknad (
    @Id override val id: UUID = UUID.randomUUID(),
    val eier: String,
    var adresseValg: AdresseValg? = null,
    var innsendingstidspunkt: LocalDateTime? = null,
    var hvorforSoke: String? = null,
    var hvaSokesOm: String? = null,
): KeyErSoknadId

data class Bosituasjon (
    @Id val soknadId: UUID,
    var botype: Botype?,
    var antallPersoner: Int
): KeyErSoknadId {
    override val id: UUID
        get() = soknadId
}

data class Utdanning (
    @Id val soknadId: UUID,
    val erStudent: Boolean,
    val studentGrad: String
): KeyErSoknadId {
    override val id: UUID
        get() = soknadId
}

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
