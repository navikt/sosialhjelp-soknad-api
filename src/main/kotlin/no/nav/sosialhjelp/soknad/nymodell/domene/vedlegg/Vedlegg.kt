package no.nav.sosialhjelp.soknad.nymodell.domene.vedlegg

import no.nav.sosialhjelp.soknad.nymodell.domene.BubblesRepository
import no.nav.sosialhjelp.soknad.nymodell.domene.okonomi.OkonomiType
import no.nav.sosialhjelp.soknad.nymodell.domene.HasUuidAsId
import no.nav.sosialhjelp.soknad.nymodell.domene.SoknadBubbles
import org.springframework.data.annotation.Id
import org.springframework.data.repository.ListCrudRepository
import org.springframework.stereotype.Repository
import java.util.*

@Repository
interface VedleggRepository : BubblesRepository<Vedlegg>

/**
 * Vedlegg er såpass sterkt knyttet til okonomi-objekter, at det kunne vært interessant å bake det inn
 * som en del av del-domene okonomi. Dog har Vedlegg en posisjon i verdi-kjeden som kanskje forsvarer å
 * håndtere det isolert.
 */
data class Vedlegg (
    @Id override val id: UUID = UUID.randomUUID(),
    override val soknadId: UUID,
    val vedleggType: OkonomiType,
    val status: VedleggStatus,
    val hendelseType: HendelseType,
    val hendelseReferanse: String,
): SoknadBubbles(id, soknadId)

enum class VedleggStatus {
    KREVES, LASTET_OPP, LEVERT
}

enum class HendelseType(value: String?) {
    DOKUMENTASJON_ETTERSPURT("dokumentasjonEtterspurt"),
    DOKUMENTASJONKRAV("dokumentasjonkrav"),
    SOKNAD("soknad"),
    BRUKER("bruker");
}

@Repository
interface FilMetaRepository : ListCrudRepository<FilMeta, UUID> {
    fun findAllByVedleggId(vedleggId: UUID): List<FilMeta>
}

data class FilMeta (
    @Id override val id: UUID = UUID.randomUUID(),
    val vedleggId: UUID,
    val filnavn: String? = null,
    val sha512: String? = null
): HasUuidAsId