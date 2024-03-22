package no.nav.sosialhjelp.soknad.db.repositories.soknadunderarbeid

import no.nav.sbl.soknadsosialhjelp.soknad.JsonInternalSoknad
import java.time.LocalDateTime

data class SoknadUnderArbeid(
    var soknadId: Long = 0L, // dummy verdi, settes i DB
    var versjon: Long,
    var behandlingsId: String,
    var tilknyttetBehandlingsId: String? = null,
    var eier: String,
    var jsonInternalSoknad: JsonInternalSoknad?,
    var status: SoknadUnderArbeidStatus,
    var opprettetDato: LocalDateTime,
    var sistEndretDato: LocalDateTime
) {
    @Deprecated("SvarUt og denne type ettersendelse støttes ikke lenger")
    val erEttersendelse: Boolean get() = false
}

enum class SoknadUnderArbeidStatus {
    UNDER_ARBEID, LAAST
}
