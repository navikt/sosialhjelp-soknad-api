package no.nav.sosialhjelp.soknad.nymodell.domene.okonomi

import no.nav.sosialhjelp.soknad.nymodell.domene.okonomi.type.BekreftelseType
import no.nav.sosialhjelp.soknad.nymodell.domene.okonomi.type.BostotteStatus
import no.nav.sosialhjelp.soknad.nymodell.domene.okonomi.type.Vedtaksstatus
import no.nav.sosialhjelp.soknad.nymodell.domene.soknad.IdIsSoknadIdObject
import no.nav.sosialhjelp.soknad.nymodell.domene.soknad.UuidAsIdObject
import org.springframework.data.annotation.Id
import java.time.LocalDate
import java.util.*

data class Bekreftelse (
    @Id override val id: UUID = UUID.randomUUID(),
    val soknadId: UUID,
    val type: BekreftelseType? = null,
    val tittel: String? = null,
    val bekreftet: Boolean? = null,
    val bekreftelsesDato: LocalDate = LocalDate.now()
): OneOfManyObject

data class Bostotte ( // systemdata
    @Id override val id: UUID = UUID.randomUUID(),
    val soknadId: UUID,
    val type: String? = null, // TODO For sterkere typing, sjekk API-dokumentasjon
    val dato: LocalDate? = null,
    val status: BostotteStatus? = null,
    val beskrivelse: String? = null,
    val vedtaksstatus: Vedtaksstatus? = null,
): OneOfManyObject