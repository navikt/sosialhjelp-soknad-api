package no.nav.sosialhjelp.soknad.nymodell.domene.vedlegg

import no.nav.sosialhjelp.soknad.nymodell.domene.common.BubblesRepository
import no.nav.sosialhjelp.soknad.nymodell.domene.okonomi.OkonomiType
import no.nav.sosialhjelp.soknad.nymodell.domene.common.HasUuidAsId
import no.nav.sosialhjelp.soknad.nymodell.domene.common.SoknadBubbles
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
    val hendelseType: VedleggHendelseType,
    val hendelseReferanse: String,
): SoknadBubbles(id, soknadId)

enum class VedleggStatus {
    KREVES, LASTET_OPP, LEVERT
}

enum class VedleggHendelseType(value: String?) {
    DOKUMENTASJON_ETTERSPURT("dokumentasjonEtterspurt"),
    DOKUMENTASJONKRAV("dokumentasjonkrav"),
    SOKNAD("soknad"),
    BRUKER("bruker");
}

/**
 * Slik vi har håndtert filopplasting, er det fristende å håndtere filer som en semantisk kobling til Vedlegg
 * på samme måte som SoknadBubble(s) er koblet til Soknad. Dog bør det være andre måter å håndtere filopplasting
 * på som gjør at Filmeta kan være en del av aggregatet til Vedlegg, og ikke stand-alone.
 */
abstract class VedleggBubbles (
    override val id: UUID = UUID.randomUUID(),
    open val vedleggId: UUID
): HasUuidAsId

@Repository
interface FilMetaRepository : ListCrudRepository<FilMeta, UUID> {
    fun findAllByVedleggId(vedleggId: UUID): List<FilMeta>
}

data class FilMeta (
    @Id override val id: UUID = UUID.randomUUID(),
    override val vedleggId: UUID,
    val filnavn: String? = null,
    val sha512: String? = null
): VedleggBubbles(id, vedleggId)