package no.nav.sosialhjelp.soknad.nymodell.domene.vedlegg

import no.nav.sosialhjelp.soknad.nymodell.domene.okonomi.type.OkonomiType
import no.nav.sosialhjelp.soknad.nymodell.domene.soknad.UuidAsIdObject
import no.nav.sosialhjelp.soknad.nymodell.domene.vedlegg.typer.VedleggHendelseType
import no.nav.sosialhjelp.soknad.nymodell.domene.vedlegg.typer.VedleggStatus
import org.springframework.data.annotation.Id
import java.util.*

data class Vedlegg (
    @Id override val id: UUID = UUID.randomUUID(),
    val soknadId: UUID,
    val vedleggType: OkonomiType,
    val status: VedleggStatus,
    val hendelseType: VedleggHendelseType,
    val hendelseReferanse: String,
): UuidAsIdObject

data class FilMeta (
    @Id override val id: UUID = UUID.randomUUID(),
    val vedleggId: UUID,
    val filnavn: String? = null,
    val sha512: String? = null
): UuidAsIdObject